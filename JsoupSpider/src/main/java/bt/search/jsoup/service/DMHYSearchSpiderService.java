package bt.search.jsoup.service;

import bt.search.jsoup.pojo.DMHYSearchData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface DMHYSearchSpiderService extends IService<DMHYSearchData> {
    String baseUrl = "";
    String searchUrl = "";

    List<DMHYSearchData> getSearchData(String key) throws IOException, ExecutionException, InterruptedException;

    Long getSearchDataIndex(String key) throws InterruptedException, ExecutionException, IOException;

    List<DMHYSearchData> getSearchDataById(Long id);
}
