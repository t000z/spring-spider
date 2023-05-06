package bt.search.jsoup.web;

import bt.search.jsoup.pojo.DMHYSearchData;
import bt.search.jsoup.service.DMHYSearchSpiderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("dmhysearch")
public class DMHYSearchSpiderController {
    @Autowired
    DMHYSearchSpiderService dmhySearchSpiderService;

    @GetMapping
    public List<DMHYSearchData> searchData(@RequestParam String key) throws IOException, ExecutionException, InterruptedException {
        log.info("开始查询：" + key);
        return dmhySearchSpiderService.getSearchData(key);
    }

    @GetMapping("/{id}")
    public List<DMHYSearchData> searchData(@PathVariable Long id) {
        log.info("索引：" + id);
        return dmhySearchSpiderService.getSearchDataById(id);
    }
}
