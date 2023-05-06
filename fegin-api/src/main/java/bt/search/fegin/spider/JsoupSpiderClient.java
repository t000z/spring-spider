package bt.search.fegin.spider;

import bt.search.fegin.dto.DMHYSearchData;
import bt.search.fegin.dto.SpiderDescriptionInto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("JsoupSpider")
public interface JsoupSpiderClient {
    @GetMapping("/dmhysearch/{id}")
    List<DMHYSearchData> findById(@PathVariable("id") Long id);

    @GetMapping("/bgmsearch")
    SpiderDescriptionInto descSearch(@RequestParam("key") String key);
}
