package bt.search.jsoup.service.impl;

import bt.search.jsoup.common.DownLoadUtils;
import bt.search.jsoup.common.ServiceThreadPool;
import bt.search.jsoup.common.UrlPlaceholderReplace;
import bt.search.jsoup.pojo.SpiderDescriptionInto;
import bt.search.jsoup.service.BunGuMiAnimeSpiderService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class BunGuMiAnimeSpiderServiceImpl implements BunGuMiAnimeSpiderService {
    private static ExecutorService EXECUTORSERVICE = null;

    private UrlPlaceholderReplace urlReplace = new UrlPlaceholderReplace(searchUrl);

    @Override
    public SpiderDescriptionInto getSearchData(String key) throws IOException {
        String requestBaseUrl = this.urlReplace.replace(key);
        Document startIndex = DownLoadUtils.downLoadGetDisguise(requestBaseUrl);

        Element element = startIndex.body();

        // 爬取搜索页第一个标签 #browserItemList
        Elements elements = element.select("#browserItemList");
        Element e = elements.get(0);
        String shortUrl = e.getElementsByTag("a").get(0).attr("href");
        String url = BunGuMiAnimeSpiderService.baseUrl + shortUrl;

        Document document = DownLoadUtils.downLoadGetDisguise(url);
        Element body = document.body();

        // 描述 #subject_summary
        Element subject_summary = body.getElementById("subject_summary");
        String description = subject_summary.text();

        // 标签列表 #subject_detail > div.subject_tag_section > div
        Elements elements1 = body.select("#subject_detail > div.subject_tag_section > div > a");
        List<String> tag = elements1.stream()
                .filter(item -> !item.attr("href").equals("javascript:void(0)"))  // 去除添加功能标签
                .map(item -> item.getElementsByTag("span"))
                .map(item -> item.text())
                .collect(Collectors.toList());

        return new SpiderDescriptionInto(key, description, tag);
    }

    @PostConstruct
    public void init() {
        BunGuMiAnimeSpiderServiceImpl.EXECUTORSERVICE = ServiceThreadPool.createExecutorService();
    }

    @PreDestroy
    public void destroy() {
        if (BunGuMiAnimeSpiderServiceImpl.EXECUTORSERVICE != null) {
            ServiceThreadPool.shutdown(BunGuMiAnimeSpiderServiceImpl.EXECUTORSERVICE);
        }
    }
}
