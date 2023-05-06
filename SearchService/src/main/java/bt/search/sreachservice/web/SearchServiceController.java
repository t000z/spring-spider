package bt.search.sreachservice.web;

import bt.search.sreachservice.pojo.SearchData;
import bt.search.sreachservice.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("search")
@Slf4j
public class SearchServiceController {
    @Autowired
    private SearchService searchService;

    @GetMapping("title")
    public List<SearchData> title(@RequestParam String key) throws IOException, InterruptedException {
        List<SearchData> searchData = searchService.title(key);
        return searchData;
    }

    @GetMapping("tag")
    public List<SearchData> tag(@RequestParam String tag) throws IOException {
        List<SearchData> searchData = searchService.tag(tag);
        return searchData;
    }

    @GetMapping
    public List<SearchData> search(@RequestParam String key) throws IOException {
        List<SearchData> searchData = searchService.search(key);
        return searchData;
    }
}
