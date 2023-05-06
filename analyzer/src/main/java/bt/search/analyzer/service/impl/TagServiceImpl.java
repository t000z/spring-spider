package bt.search.analyzer.service.impl;

import bt.search.analyzer.common.AsyncDataBaseName;
import bt.search.analyzer.common.ServiceThreadPool;
import bt.search.analyzer.common.TypeConvert;
import bt.search.analyzer.common.pair;
import bt.search.analyzer.dao.TagDao;
import bt.search.analyzer.pojo.HashDataDescriptionMsg;
import bt.search.analyzer.pojo.Tag;
import bt.search.analyzer.service.HashDataDescriptionMsgService;
import bt.search.analyzer.service.TagService;
import bt.search.fegin.dto.SpiderDescriptionInto;
import bt.search.fegin.spider.JsoupSpiderClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagDao, Tag> implements TagService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private HashDataDescriptionMsgService hashDataDescriptionMsgService;

    @Autowired
    private JsoupSpiderClient jsoupSpiderClient;

    private static ExecutorService EXECUTORSERVICE;

    @Override
    @Transactional
    public void updateTag(String key, HashDataDescriptionMsg one, SpiderDescriptionInto spiderDescriptionInto) {
        // 在缓存中的标签
        Set<String> cacheTagList = spiderDescriptionInto.getTag().stream()
                .filter(item -> redisTemplate.opsForHash().hasKey(AsyncDataBaseName.TAG_TABLE_ID, item))
                .collect(Collectors.toSet());
        // 不在缓存中的标签
        Set<String> notCacheTagList = spiderDescriptionInto.getTag().stream()
                .filter(item -> !cacheTagList.contains(item))
                .collect(Collectors.toSet());

        if (cacheTagList.size() != 0) {  // 有标签存在于缓存中
            if (notCacheTagList.size() != 0) {  // 部分标签不存在于缓存中
                List<Tag> tags = this.dataBaseTagContains(notCacheTagList);

                if (tags.size() == notCacheTagList.size()) {  // 不存在于缓存中的标签，全部在数据库中
                    this.dataBaseTagCacheWrite(tags, one.getId());
                } else if (tags.size() != 0) {  // 不存在于缓存中的标签，部分在数据库中
                    this.dataBaseTagCacheWrite(tags, one.getId());

                    // 将不存在的标签保存在数据库中，并且将当前key对应的索引与标签映射
                    this.tagDifferenceSetDataBaseWrite(tags, notCacheTagList, one.getId());
                } else {  // 不存在于缓存中的标签，都不在数据库中
                    // 将不存在的标签保存在数据库中，并且将当前key对应的索引与标签映射
                    this.tagDataBaseWrite(notCacheTagList, one.getId());
                }

                // 更新存在于缓存中的标签的索引列表
                this.tagCacheUpdate(cacheTagList, one.getId());
            } else {  // 所有标签都在缓存中
                // 更新存在于缓存中的标签的索引列表
                this.tagCacheUpdate(cacheTagList, one.getId());
            }
        } else {  // 没有标签在缓存中
            List<Tag> tags = this.dataBaseTagContains(notCacheTagList);

            if (tags.size() == notCacheTagList.size()) {  // 全部标签在数据库中
                this.dataBaseTagCacheWrite(tags, one.getId());
            } else if (tags.size() != 0) {  // 部分标签在数据库中
                // 将不存在的标签保存在数据库中，并且将当前key对应的索引与标签映射
                this.tagDifferenceSetDataBaseWrite(tags, notCacheTagList, one.getId());
                this.dataBaseTagCacheWrite(tags, one.getId());
            } else {  // 都不在数据库中
                // 将不存在的标签保存在数据库中，并且将当前key对应的索引与标签映射
                this.tagDataBaseWrite(notCacheTagList, one.getId());
            }
        }
    }

    @Override
    public pair<List<Long>> getTagAndHashDataIndex(String tag) {
        Set<Long> index = null;  // 保证每个索引唯一

        LambdaQueryWrapper<Tag> lqw = new LambdaQueryWrapper<>();
        lqw.select(Tag::getId, Tag::getIndexs).eq(Tag::getTag, tag);
        Tag one = this.getOne(lqw);

        if (redisTemplate.opsForHash().hasKey(AsyncDataBaseName.TAG_TABLE_ID, tag)) {
            List<Long> temp = TypeConvert.stringToLongList((String)
                        redisTemplate.opsForHash().get(AsyncDataBaseName.TAG_INDEX_LIST, tag));

            index = new HashSet<>(one.getIndexs());
            index.addAll(temp);

            // 更新与移除
            List<Long> finalTemp = index.stream().collect(Collectors.toList());
            EXECUTORSERVICE.execute(() -> this.delCacheUpdateDataBase(one.getId(), tag, finalTemp));
        } else {
            index = new HashSet<>(one.getIndexs());
        }

        // 获得其哈希数据索引
        List<Long> hashDataIndex = new Vector<>();
        List<HashDataDescriptionMsg> msgs = hashDataDescriptionMsgService.listByIds(
                index.stream().collect(Collectors.toList()));
        msgs.forEach(item -> hashDataIndex.addAll(item.getIndexs()));

        return new pair<>(tag, hashDataIndex);
    }

    @Override
    @Transactional
    public void insertAddTag(String key) {
        LambdaQueryWrapper<HashDataDescriptionMsg> lqw = new LambdaQueryWrapper<>();
        lqw.eq(HashDataDescriptionMsg::getName, key)
                .select(HashDataDescriptionMsg::getId,
                        HashDataDescriptionMsg::getDescription);
        HashDataDescriptionMsg one = hashDataDescriptionMsgService.getOne(lqw);

        SpiderDescriptionInto spiderDescriptionInto = jsoupSpiderClient.descSearch(key);

        this.updateTag(key, one, spiderDescriptionInto);
    }

    @PostConstruct
    public void init() {
        TagServiceImpl.EXECUTORSERVICE = ServiceThreadPool.createExecutorService();
    }

    @PreDestroy
    public void destroy() {
        if (TagServiceImpl.EXECUTORSERVICE != null) {
            ServiceThreadPool.shutdown(TagServiceImpl.EXECUTORSERVICE);
        }
    }

    private void tagCacheUpdate(Collection<String> tagList, Long addId) {
        // 更新存在于缓存中的标签的索引列表
        tagList.stream()
                .map(item ->
                        new pair<String>(item,
                                (String) redisTemplate.opsForHash().get(AsyncDataBaseName.TAG_INDEX_LIST, item)))
                .map(item -> new pair<List<Long>>(item.getKey(), TypeConvert.stringToLongList(item.getValue())))
                .peek(item -> item.getValue().add(addId))
                .map(item -> new pair<String>(item.getKey(), TypeConvert.longListToString(item.getValue())))
                .forEach(item ->
                        redisTemplate.opsForHash().put(AsyncDataBaseName.TAG_INDEX_LIST,
                                item.getKey(), item.getValue()));
    }

    private void dataBaseTagCacheWrite(List<Tag> tagList, Long addId) {
        tagList.stream().forEach(item -> {
            // 将标签写入缓存，并且将当前key对应的索引与标签映射
            redisTemplate.opsForHash().put(AsyncDataBaseName.TAG_TABLE_ID, item.getTag(), item.getId());
            redisTemplate.opsForHash()
                    .put(AsyncDataBaseName.TAG_INDEX_LIST, item.getTag(), addId.toString());
        });
    }

    @Transactional
    List<Tag> dataBaseTagContains(Collection<String> tagList) {
        LambdaQueryWrapper<Tag> lqw = new LambdaQueryWrapper();
        lqw.select(Tag::getId, Tag::getTag);
        tagList.stream().forEach(item -> lqw.eq(Tag::getTag, item));
        return this.list(lqw);
    }

    @Transactional
    void tagDataBaseWrite(Collection<String> tagList, Long addId) {
        List<Tag> tags = tagList.stream()
                .map(item -> new Tag(item, Arrays.asList(addId)))
                .collect(Collectors.toList());
        this.saveBatch(tags);
    }

    @Transactional
    void tagDifferenceSetDataBaseWrite(Collection<Tag> tags, Collection<String> tags1, Long addId) {
        Set<String> set = tags.stream().map(item -> item.getTag()).collect(Collectors.toSet());
        List<Tag> tagList = tags1.stream()
                .filter(item -> !set.contains(item))
                .map(item -> new Tag(item, Arrays.asList(addId)))
                .collect(Collectors.toList());
        this.saveBatch(tagList);
    }

    @Transactional
    void delCacheUpdateDataBase(Long id, String tag, List<Long> indexList) {
        redisTemplate.opsForHash().delete(AsyncDataBaseName.TAG_TABLE_ID, tag);
        redisTemplate.opsForHash().delete(AsyncDataBaseName.TAG_INDEX_LIST, tag);
        LambdaUpdateWrapper<Tag> luw = new LambdaUpdateWrapper<>();
        luw.eq(Tag::getId, id).set(Tag::getIndexs, indexList);
        this.update(luw);
    }
}
