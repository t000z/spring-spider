package bt.search.analyzer.web;

import bt.search.analyzer.service.HashDataDescriptionMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("hddm")
@Slf4j
public class HashDataDescriptionMsgController {
    @Autowired
    private HashDataDescriptionMsgService hashDataDescriptionMsgService;

    @PostMapping
    public List<String> updateDescriptionAndTag(@RequestParam String key) throws InterruptedException {
        log.info("开始更新描述与标签");
        return hashDataDescriptionMsgService.updateDescriptionAndTag(key);
    }
}
