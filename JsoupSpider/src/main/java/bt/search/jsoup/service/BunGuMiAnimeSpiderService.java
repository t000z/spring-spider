package bt.search.jsoup.service;

import bt.search.jsoup.pojo.DMHYSearchData;
import bt.search.jsoup.pojo.SpiderDescriptionInto;

import java.io.IOException;
import java.util.List;

public interface BunGuMiAnimeSpiderService {
    String baseUrl = "";
    String searchUrl = "";

    SpiderDescriptionInto getSearchData(String key) throws IOException;
}
