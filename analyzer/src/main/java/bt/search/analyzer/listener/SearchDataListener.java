package bt.search.analyzer.listener;

import bt.search.analyzer.common.AsyncDataBaseName;
import bt.search.analyzer.common.AsyncLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SearchDataListener {
    @Autowired
    private RedisTemplate redisTemplate;

    @RabbitListener(queues = "simple.subject.response")
    public void simpleSubjectResponseListener(String msg) {
        log.info("获取异步调用值 " + msg);
        String[] key = msg.split(":");
        Long id = Long.valueOf(key[0]);

        if (AsyncLock.exist(id)) {
            redisTemplate.opsForHash().put(AsyncDataBaseName.SpiderResultIndex, key[0], Long.valueOf(key[1]));
            AsyncLock.signalAll(id);
        }
    }
}
