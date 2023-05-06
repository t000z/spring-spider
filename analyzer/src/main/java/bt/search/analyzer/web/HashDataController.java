package bt.search.analyzer.web;

import bt.search.analyzer.pojo.HashData;
import bt.search.analyzer.service.HashDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("hashdata")
@Slf4j
public class HashDataController {
    @Autowired
    private HashDataService hashDataService;

    @GetMapping
    public List<HashData> searchData(@RequestParam String key) throws InterruptedException {
        log.info("开始处理：" + key);
        return hashDataService.getSearch(key);
    }
}
