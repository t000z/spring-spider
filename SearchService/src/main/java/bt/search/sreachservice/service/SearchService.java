package bt.search.sreachservice.service;

import bt.search.sreachservice.pojo.SearchData;

import java.io.IOException;
import java.util.List;

public interface SearchService {
    List<SearchData> title(String key) throws IOException, InterruptedException;

    List<SearchData> tag(String tag) throws IOException;

    List<SearchData> search(String key) throws IOException;
}
