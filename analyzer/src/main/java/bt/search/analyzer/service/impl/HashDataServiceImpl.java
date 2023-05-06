package bt.search.analyzer.service.impl;

import bt.search.analyzer.TextAnalysis.TextAnalyzer;
import bt.search.analyzer.common.*;
import bt.search.analyzer.dao.HashDataDao;
import bt.search.analyzer.pojo.HashData;
import bt.search.analyzer.pojo.HashDataDescriptionMsg;
import bt.search.analyzer.service.EntityDictionaryService;
import bt.search.analyzer.service.HashDataDescriptionMsgService;
import bt.search.analyzer.service.HashDataService;
import bt.search.fegin.dto.DMHYSearchData;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import bt.search.fegin.spider.*;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class HashDataServiceImpl extends ServiceImpl<HashDataDao, HashData> implements HashDataService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private HashDataDescriptionMsgService hashDataDescriptionMsgService;

    @Autowired
    private JsoupSpiderClient jsoupSpiderClient;

    @Autowired
    private TextAnalyzer textAnalyzer;

    private static ExecutorService EXECUTORSERVICE;

    // 只会向该容器添加或移除，不会修改因此不需要线程安全
    private static Map<Long, List<HashData>> SEARCH_DATA_CACHE = null;

    @PostConstruct
    public void init() {
        HashDataServiceImpl.EXECUTORSERVICE = ServiceThreadPool.createExecutorService();
        HashDataServiceImpl.SEARCH_DATA_CACHE = new HashMap<>();
    }

    @PreDestroy
    public void destroy() {
        if (HashDataServiceImpl.EXECUTORSERVICE != null) {
            ServiceThreadPool.shutdown(HashDataServiceImpl.EXECUTORSERVICE);
        }
    }

    @Override
    @Transactional
    public List<HashData> getSearch(String key) throws InterruptedException {
        Long invocationId = System.currentTimeMillis();
        rabbitTemplate.convertAndSend(MqName.SINGLE_SUBJECT, invocationId + ":" + key);  // 异步调用爬虫服务

        // 同步查找数据库中是否存在已经处理过的数据
        LambdaQueryWrapper<HashDataDescriptionMsg> lqw = new LambdaQueryWrapper<>();
        lqw.eq(HashDataDescriptionMsg::getName, key);
        HashDataDescriptionMsg descriptionMsgServiceOne = hashDataDescriptionMsgService.getOne(lqw);

        List<HashData> hashData = null;
        if (descriptionMsgServiceOne == null) {  // 数据库中没有key相关数据
            hashData = this.getAsyncInvocationSpiderIndexToHashData(invocationId);

            // 进行实体匹配
            List<HashData> finalHashData = this.entityMatching(key, hashData);

            EXECUTORSERVICE.execute(() -> this.saveHashData(key, finalHashData));  // 将处理后数据存储（异步）
            hashData = finalHashData;
        } else {
            List<Long> indexs = descriptionMsgServiceOne.getIndexs();
            hashData = this.listByIds(indexs);
        }

        return hashData;
    }

    @Override
    public Long getSearchIndex(String key) throws InterruptedException {
        Long invocationId = System.currentTimeMillis();  // 服务ID
        rabbitTemplate.convertAndSend(MqName.SINGLE_SUBJECT, invocationId + ":" + key);

        LambdaQueryWrapper<HashDataDescriptionMsg> lqw = new LambdaQueryWrapper<>();
        lqw.eq(HashDataDescriptionMsg::getName, key).select(HashDataDescriptionMsg::getId);
        HashDataDescriptionMsg descriptionMsgServiceOne = hashDataDescriptionMsgService.getOne(lqw);

        Long hashDataId = null;
        if (descriptionMsgServiceOne == null) {  // 数据库中没有key相关数据
            List<HashData> hashData = this.getAsyncInvocationSpiderIndexToHashData(invocationId);

            // 进行实体匹配
            List<HashData> finalHashData = this.entityMatching(key, hashData);

            hashDataId = System.currentTimeMillis();  // 获取ID

            Long finalHashDataId = hashDataId;
            if (finalHashData.size() != 0) {  // 实体匹配没有结果
                EXECUTORSERVICE.execute(() -> this.saveHashDataRemoveCache(finalHashDataId, key, finalHashData));  // 将处理后数据存储
            }

            SEARCH_DATA_CACHE.put(finalHashDataId, finalHashData);
        } else {
            hashDataId = descriptionMsgServiceOne.getId();
            this.removeAsyncFlag(invocationId);
        }

        return hashDataId;
    }

    @Override
    public List<HashData> getSearchById(Long id) {
        List<HashData> hashData = SEARCH_DATA_CACHE.get(id);
        if (hashData == null) {  // 缓存中没有数据，查询数据库
            LambdaQueryWrapper<HashDataDescriptionMsg> lqw = new LambdaQueryWrapper<>();
            lqw.eq(HashDataDescriptionMsg::getId, id).select(HashDataDescriptionMsg::getIndexs);
            HashDataDescriptionMsg descriptionMsgServiceOne = hashDataDescriptionMsgService.getOne(lqw);
            List<Long> indexs = descriptionMsgServiceOne.getIndexs();
            hashData = this.listByIds(indexs);
        }

        return hashData;
    }

    private List<HashData> getAsyncInvocationSpiderIndexToHashData(Long invocationId) throws InterruptedException {
        Long spiderIndex = (Long) redisTemplate.opsForHash()
                .get(AsyncDataBaseName.SpiderResultIndex, invocationId.toString());
        while (spiderIndex == null) {
            AsyncLock.await(invocationId);  // 若没有数据，则让线程进行等待
            spiderIndex = (Long) redisTemplate.opsForHash()
                    .get(AsyncDataBaseName.SpiderResultIndex, invocationId.toString());
        }
        this.removeAsyncFlag(invocationId);

        List<DMHYSearchData> searchData = jsoupSpiderClient.findById(spiderIndex);  // 通过索引获取数据
        List<HashData> hashData = searchData.stream().map(item -> new HashData(item)).collect(Collectors.toList());

        return hashData;
    }

    private void removeAsyncFlag(Long id) {
        AsyncLock.remove(id);  // 用完释放
        redisTemplate.opsForHash().delete(AsyncDataBaseName.SpiderResultIndex, id.toString());
    }

    private List<HashData> entityMatching(String key, List<HashData> hashData) {
        /*Set<String> entityList = entityDictionaryService.getEntityList();
        List<HashData> afterMatchingHashData = hashData.stream()
                .map(item -> {
                    item.getName().replaceAll(" ", "");
                    return item;
                })  // 去除空格
                .filter(item -> item.getName().contains(key))  // 标题名中包含关键字，才进行后续处理
                .map(itme -> {
                    String after = itme.getName().toLowerCase(Locale.ROOT);
                    List<String> entitys = entityList.stream()
                            .filter(after::contains).collect(Collectors.toList());
                    itme.setName(key);
                    itme.setEntityWords(entitys);
                    return itme;
                }).filter(item -> item.getEntityWords().size() != 0)  // 没匹配出实体词的抛弃
                .map(item -> {  // 建立子串森林
                    List<String> entity = subStrForest(item.getEntityWords());
                    item.setEntityWords(entity);
                    return item;
                })
                .collect(Collectors.toList());

        return afterMatchingHashData;*/

        Counter counter = new Counter(1);
        Counter index = new Counter(0);
        List<Tuple<Long, String>> tuples;
        Map<Long, Integer> idToIndex = new HashMap<>();   // 添加对象ID到列表下标映射
        if (hashData.get(0).getId() == null) {
            tuples = hashData.stream()
                        .peek(item -> item.setId((long) counter.laterAdd()))  // 手动添加ID
                        .peek(item -> idToIndex.put(item.getId(), index.laterAdd()))  // list流必定按顺序
                        .map(item -> new Tuple<>(item.getId(), item.getName()))
                        .collect(Collectors.toList());
        } else {
            tuples = hashData.stream()
                        .peek(item -> idToIndex.put(item.getId(), index.laterAdd()))  // list流必定按顺序
                        .map(item -> new Tuple<>(item.getId(), item.getName()))
                        .collect(Collectors.toList());
        }

        List<Tuple<Long, List<String>>> entity = textAnalyzer.entityMatching(key, tuples);

        List<HashData> result;
        if (counter.getCount() != 1) {  // ID是在本方法生成的
            result = entity.stream()
                    .map(item -> {
                        HashData data = hashData.get(idToIndex.get(item.getKey()));
                        data.setEntityWords(item.getValue());
                        return data;
                    })
                    .peek(item -> item.setId(null))
                    .peek(item -> item.setName(key))
                    .collect(Collectors.toList());
        } else {
            result = entity.stream()
                    .map(item -> {
                        HashData data = hashData.get(idToIndex.get(item.getKey()));
                        data.setEntityWords(item.getValue());
                        return data;
                    })
                    .peek(item -> item.setName(key))
                    .collect(Collectors.toList());
        }

        return result;
    }

    @Transactional
    void saveHashData(String key, List<HashData> hashData) {
        this.saveHashData(null, key, hashData);
    }

    @Transactional
    boolean saveHashData(Long id, String key, List<HashData> hashData) {
        Long invocationId = System.currentTimeMillis();
        AsyncLock.createLock(invocationId);
        redisTemplate.opsForHash().put(AsyncDataBaseName.HASH_DATA_UPDATE_LOCK, key, invocationId);

        Random random = new Random(invocationId);
        List<Long> indexs = new Vector<>();
        for (HashData hd : hashData) {
            Long index = random.nextLong();
            hd.setId(index);
            indexs.add(index);
        }

        if (id == null) {
            id = System.currentTimeMillis();
        }
        HashDataDescriptionMsg hashDataDescriptionMsg = new HashDataDescriptionMsg(id, key, indexs);

        this.saveBatch(hashData);
        hashDataDescriptionMsgService.save(hashDataDescriptionMsg);  // 保证能获取该表索引列表是，索引映射的数据存在

        redisTemplate.opsForHash().put(AsyncDataBaseName.HASH_DATA_INDEX, invocationId.toString(), id);
        AsyncLock.signalAll(invocationId);
        if (redisTemplate.opsForHash().get(AsyncDataBaseName.HASH_DATA_UPDATE_LOCK, key) == null) {
            // 表明有线程在等待更新操作完成
            redisTemplate.opsForHash().delete(AsyncDataBaseName.HASH_DATA_UPDATE_LOCK, key);
        } else {
            redisTemplate.opsForHash().delete(AsyncDataBaseName.HASH_DATA_UPDATE_LOCK, key);
            redisTemplate.opsForHash().delete(AsyncDataBaseName.HASH_DATA_INDEX, invocationId.toString());
        }

        return true;
    }

    @Transactional
    void saveHashDataRemoveCache(Long id, String key, List<HashData> hashData) {
        if (this.saveHashData(id, key, hashData)) {
            SEARCH_DATA_CACHE.remove(id);
        }
    }
}


