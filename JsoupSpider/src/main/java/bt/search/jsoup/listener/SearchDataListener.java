package bt.search.jsoup.listener;

import bt.search.jsoup.service.DMHYSearchSpiderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class SearchDataListener {
    @Autowired
    private DMHYSearchSpiderService dmhySearchSpiderService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "simple.subject")
    public void listenSimpleSubjectMessage(String msg) throws InterruptedException, IOException, ExecutionException {
        log.info("异步调用，开始查询：" + msg);
        String[] key = msg.split(":");
        Long index = dmhySearchSpiderService.getSearchDataIndex(key[1]);
        rabbitTemplate.convertAndSend("simple.subject.response", key[0] + ":" + index);
    }
}
