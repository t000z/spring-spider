package bt.search.analyzer.service.impl;

import bt.search.analyzer.common.*;
import bt.search.analyzer.dao.HashDataDescriptionMsgDao;
import bt.search.analyzer.pojo.HashDataDescriptionMsg;
import bt.search.analyzer.pojo.Tag;
import bt.search.analyzer.service.HashDataDescriptionMsgService;
import bt.search.analyzer.service.TagService;
import bt.search.fegin.dto.SpiderDescriptionInto;
import bt.search.fegin.spider.JsoupSpiderClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
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
public class HashDataDescriptionMsgServiceImpl extends ServiceImpl<HashDataDescriptionMsgDao, HashDataDescriptionMsg> implements HashDataDescriptionMsgService {
    @Autowired
    private JsoupSpiderClient jsoupSpiderClient;

    @Autowired
    private TagService tagService;

    @Autowired
    private RedisTemplate redisTemplate;

    private static ExecutorService EXECUTORSERVICE;

    @PostConstruct
    public void init() {
        EXECUTORSERVICE = ServiceThreadPool.createExecutorService();
    }

    @PreDestroy
    public void destroy() {
        ServiceThreadPool.shutdown(EXECUTORSERVICE);
    }

    @Override
    @Transactional
    public List<String> updateDescriptionAndTag(String key) throws InterruptedException {
        LambdaQueryWrapper<HashDataDescriptionMsg> lqw = new LambdaQueryWrapper<>();
        lqw.eq(HashDataDescriptionMsg::getName, key)
            .select(HashDataDescriptionMsg::getId,
                    HashDataDescriptionMsg::getDescription,
                    HashDataDescriptionMsg::getTags);
        HashDataDescriptionMsg one = this.getOne(lqw);

        // 等待需要的表数据更新完毕
        Long id = (Long) redisTemplate.opsForHash().get(AsyncDataBaseName.HASH_DATA_UPDATE_LOCK, key);
        if (id != null) {
            // 获得异步锁，说明还在进行更新操作
            redisTemplate.opsForHash().put(AsyncDataBaseName.HASH_DATA_UPDATE_LOCK, key, null);  // 表明有线程在等待更新操作完成
            AsyncLock.getLockAndWait(id);
            Long descId = (Long) redisTemplate.opsForHash().get(AsyncDataBaseName.HASH_DATA_INDEX, id.toString());
            one = new HashDataDescriptionMsg(descId, null);
            redisTemplate.opsForHash().delete(AsyncDataBaseName.HASH_DATA_INDEX, id.toString());
        } else {
            AsyncLock.remove(id);
        }

        SpiderDescriptionInto spiderDescriptionInto = null;
        List<String> tag = null;
        if (one.getDescription() == null) {
            spiderDescriptionInto = jsoupSpiderClient.descSearch(key);
            tag = spiderDescriptionInto.getTag().stream().collect(Collectors.toList());

            HashDataDescriptionMsg finalOne1 = one;
            SpiderDescriptionInto finalSpiderDescriptionInto1 = spiderDescriptionInto;
            List<String> finalTag = tag;
            EXECUTORSERVICE.execute(() -> this.updateById(new
                    HashDataDescriptionMsg(finalOne1.getId(), null, finalSpiderDescriptionInto1.getDescription(),
                    null,
                    finalTag)));

            SpiderDescriptionInto finalSpiderDescriptionInto = spiderDescriptionInto;
            HashDataDescriptionMsg finalOne = one;
            EXECUTORSERVICE.execute(() -> tagService.updateTag(key, finalOne, finalSpiderDescriptionInto));
        } else {
            tag = one.getTags();
        }

        return tag;
    }
}
