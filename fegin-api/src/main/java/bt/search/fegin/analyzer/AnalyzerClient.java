package bt.search.fegin.analyzer;

import bt.search.fegin.dto.HashData;
import bt.search.fegin.dto.pair;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("Ananlyzer")
public interface AnalyzerClient {
    @GetMapping("/hashdata")
    List<HashData> findByKey(@RequestParam("key") String key);

    @PostMapping("/hddm")
    List<String> updateDescriptionAndTag(@RequestParam("key") String key);

    @GetMapping("/tag/list")
    List<String> getTagList();

    @GetMapping("/tag")
    pair<List<Long>> getTag(@RequestParam("tag") String tag);
}
