package bt.search.analyzer.service;

import bt.search.analyzer.pojo.HashData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface HashDataService extends IService<HashData> {
    List<HashData> getSearch(String key) throws InterruptedException;

    Long getSearchIndex(String key) throws InterruptedException;

    List<HashData> getSearchById(Long id);
}
