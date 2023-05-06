package bt.search.jsoup.web;

import bt.search.jsoup.pojo.SpiderDescriptionInto;
import bt.search.jsoup.service.BunGuMiAnimeSpiderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("bgmsearch")
public class BunGuMiAnimeSpiderController {
    @Autowired
    private BunGuMiAnimeSpiderService bunGuMiAnimeSpiderService;

    @GetMapping
    public SpiderDescriptionInto searchData(@RequestParam String key) throws IOException {
        log.info("开始描述查询：" + key);
        return bunGuMiAnimeSpiderService.getSearchData(key);
    }
}
