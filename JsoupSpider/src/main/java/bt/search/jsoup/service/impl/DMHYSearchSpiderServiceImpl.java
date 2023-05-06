package bt.search.jsoup.service.impl;

import bt.search.jsoup.common.DownLoadUtils;
import bt.search.jsoup.common.ServiceThreadPool;
import bt.search.jsoup.common.TorrentUtils;
import bt.search.jsoup.dao.DMHYSearchDao;
import bt.search.jsoup.pojo.DMHYSearchData;
import bt.search.jsoup.pojo.DMHYSearchIndex;
import bt.search.jsoup.pojo.NameToHashData;
import bt.search.jsoup.service.DMHYSearchSpiderService;
import bt.search.jsoup.service.NameToHashDataMapService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_DATE;

@Service
public class DMHYSearchSpiderServiceImpl extends ServiceImpl<DMHYSearchDao, DMHYSearchData> implements DMHYSearchSpiderService {
    private static ExecutorService executorService = null;

    // 只会向该容器添加或移除，不会修改因此不需要线程安全
    private static Map<Long, List<DMHYSearchData>> searchDataCache = null;

    @Autowired
    private NameToHashDataMapService nameToHashDataMapService;

    @Transactional
    @Override
    public List<DMHYSearchData> getSearchData(String key) throws IOException, ExecutionException, InterruptedException {
        List<DMHYSearchData> data = null;
        LambdaQueryWrapper<NameToHashData> lqw = new LambdaQueryWrapper<>();
        lqw.eq(NameToHashData::getName, key);
        NameToHashData indexTable = nameToHashDataMapService.getOne(lqw);

        if (indexTable == null) {
            String requestBaseUrl = DMHYSearchSpiderService.searchUrl + key + "/";
            Document startIndex = DownLoadUtils.downLoadGet(requestBaseUrl, 3);

            // 获取总页面数
            DMHYSearchIndex dmhySearchIndex = this.getTotalPage(startIndex);

            // 获取每个页面的详情页链接
            List<String> links = this.getAllDetailPageLink(startIndex, dmhySearchIndex, requestBaseUrl);

            // 根据详情页链接获取描述数据
            data = this.getAllDetailPageInto(links);

            // 预处理后数据存入数据库，将key与预处理后数据关联
            List<DMHYSearchData> finalData = data;
            DMHYSearchSpiderServiceImpl.executorService.execute(() -> this.saveData(key, finalData));

        } else {
            data = this.listByIds(indexTable.getIndexs());
        }

        return data;
    }

    @Transactional
    @Override
    public Long getSearchDataIndex(String key) throws InterruptedException, ExecutionException, IOException {
        LambdaQueryWrapper<NameToHashData> lqw = new LambdaQueryWrapper<>();
        lqw.eq(NameToHashData::getName, key);
        NameToHashData indexTable = nameToHashDataMapService.getOne(lqw);

        if (indexTable == null) {
            String requestBaseUrl = DMHYSearchSpiderService.searchUrl + key + "/";
            Document startIndex = DownLoadUtils.downLoadGet(requestBaseUrl, 3);

            // 获取总页面数
            DMHYSearchIndex dmhySearchIndex = this.getTotalPage(startIndex);

            // 获取每个页面的详情页链接
            List<String> links = this.getAllDetailPageLink(startIndex, dmhySearchIndex, requestBaseUrl);

            // 根据详情页链接获取描述数据
            List<DMHYSearchData> data = this.getAllDetailPageInto(links);

            // 预处理后数据存入数据库，将key与预处理后数据关联，并指定数据行索引
            Random random = new Random(System.currentTimeMillis());
            indexTable = new NameToHashData(random.nextLong());
            NameToHashData finalIndexTable = indexTable;
            DMHYSearchSpiderServiceImpl.executorService.execute(() -> this.saveDataById(finalIndexTable.getId(), key, data));
            searchDataCache.put(indexTable.getId(), data);
        }

        return indexTable.getId();
    }

    public List<DMHYSearchData> getSearchDataById(Long id) {
        List<DMHYSearchData> data = searchDataCache.get(id);
        // 先查找缓存，若是缓存中有数据，则使用缓存数据
        if (data == null) {
            LambdaQueryWrapper<NameToHashData> lqw = new LambdaQueryWrapper<>();
            lqw.eq(NameToHashData::getId, id).select(NameToHashData::getIndexs);
            NameToHashData indexTable = nameToHashDataMapService.getOne(lqw);
            data = this.listByIds(indexTable.getIndexs());
        }

        return data;
    }

    @PostConstruct
    public void init() {
        DMHYSearchSpiderServiceImpl.executorService = ServiceThreadPool.createExecutorService();
        DMHYSearchSpiderServiceImpl.searchDataCache = new HashMap<>();
    }

    @PreDestroy
    public void destroy() {
        if (DMHYSearchSpiderServiceImpl.executorService != null) {
            ServiceThreadPool.shutdown(DMHYSearchSpiderServiceImpl.executorService);
        }
    }

    private DMHYSearchIndex getTotalPage(Document document) {
        // body > div.uk-container.uk-margin-top.uk-text-left > ul:nth-child(2)
        Elements element = document.getElementsByTag("body");
        Elements elements = element.select("div[class=uk-container uk-margin-top uk-text-left]")
                                .select("ul:nth-child(2)")
                                .select("li");

        int totalPage = elements.size() - 4;  // 减去功能标签

        DMHYSearchIndex dmhySearchIndex = new DMHYSearchIndex(totalPage);

        return dmhySearchIndex;
    }

    private List<String> getDetailPageLink(Document document) {
        // body > div.uk-container.uk-margin-top.uk-text-left >
        // ul.uk-list.uk-list-divider > li > span.uk-width-expand > a
        Elements element = document.getElementsByTag("body");
        Elements elements = element.select("div.uk-container.uk-margin-top.uk-text-left")
                                .select("ul.uk-list.uk-list-divider")
                                .select("li")
                                .select("span.uk-width-expand")
                                .select("a");

        List<String> links = new Vector<>();
        for (Element e: elements) {
            links.add(DMHYSearchSpiderService.baseUrl + e.attr("href"));
        }

        return links;
    }

    private DMHYSearchData getDetailPageInto(String url) throws IOException {
        Document detailPage = DownLoadUtils.downLoadGet(url, 3);
        Elements body = detailPage.getElementsByTag("body");

        /* 数据标签
          div.uk-container.uk-margin-top.uk-text-left.uk-text-break
          > div */
        Elements dataLabel = body.select("div[class=uk-container uk-margin-top uk-text-left uk-text-break]")
                                .select("div[class=uk-grid]");

        // 更新时间与热度 div:nth-child(1) > p
        Elements temp = dataLabel.select("div:nth-child(1)")
                            .select("p");
        // 更新时间 a:nth-child(1)
        // body > div.uk-container.uk-margin-top.uk-text-left.uk-text-break > div > div:nth-child(1) > p > a:nth-child(1)
        LocalDate updateTime = null;
        try {
            updateTime = LocalDate.parse(detailPage.select("body > " +
                    "div.uk-container.uk-margin-top.uk-text-left.uk-text-break > " +
                    "div > div:nth-child(1) > p > a:nth-child(1)")
                    .text());
//            updateTime = LocalDate.parse(temp.select("a:nth-child(1)").text(), ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException(url, e.getMessage(), 0);
        }
        // 热度 a:nth-child(2)
        Integer hot = Integer.valueOf(temp.select("a:nth-child(2)").text());

        // 手动采用广度优先遍历选择节点
        Element element = dataLabel.select("div.uk-width-1-1.uk-margin-top.content_box")
                            .first()
                            .child(1)
                            .child(0);
        // name shortUrl
        String name = element.text();
        String shortUrl = element.attributes()
                                .get("href");

        // 磁力链 div:nth-child(8)
        temp = dataLabel.select("div:nth-child(8)");
        // torrent1 p:nth-child(2) > a
        String torrent1 = TorrentUtils.torrentSplitHex(temp.select("p:nth-child(2)").select("a").text());
        // torrent2 p:nth-child(3) > a
        String torrent2 = TorrentUtils.torrentSplitHex(temp.select("p:nth-child(3)").select("a").text());
        List<String> torrents = Arrays.asList(torrent1, torrent2);

        // body > div.uk-container.uk-margin-top.uk-text-left.uk-text-break > div > div:nth-child(5) > p
        // div:nth-child(5) > p
        Long size = null;
        try {
            size = TorrentUtils.inMB(detailPage.select("body > " +
                    "div.uk-container.uk-margin-top.uk-text-left.uk-text-break > " +
                    "div > div:nth-child(5) > p")
                    .text());
            /*size = TorrentUtils.inMB(dataLabel.select("div:nth-child(5)")
                    .select("p")
                    .text());*/
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException(url);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(url);
        }

        return new DMHYSearchData(name, updateTime, hot, size, torrents, shortUrl);
    }

    @Data
    @AllArgsConstructor
    private class DetailPageInto implements Callable<DMHYSearchData> {
        private String url;

        @Override
        public DMHYSearchData call() throws Exception {
            return getDetailPageInto(url);
        }
    }

    private List<DMHYSearchData> getAllDetailPageInto(List<String> urls) throws InterruptedException, ExecutionException {
        List<DMHYSearchData> results = new Vector<>();
        List<Callable<DMHYSearchData>> takes = new Vector<>();

        for (String url : urls) {
            takes.add(this.new DetailPageInto(url));
        }

        List<Future<DMHYSearchData>> futures = new
                ArrayList<>(DMHYSearchSpiderServiceImpl.executorService.invokeAll(takes));

        for (Future<DMHYSearchData> data : futures) {
            results.add(data.get());
        }

        return results;
    }

    @Data
    @AllArgsConstructor
    private class DetailPageLink implements Callable<List<String>> {
        private String url;

        @Override
        public List<String> call() throws Exception {
            Document document = DownLoadUtils.downLoadGet(url, 3);
            return getDetailPageLink(document);
        }
    }

    private List<String> getAllDetailPageLink(Document startIndex,
                                                    DMHYSearchIndex pageCount,
                                                    String requestBaseUrl) throws IOException,
            ExecutionException, InterruptedException {
        List<List<String>> links = new Vector<>(pageCount.getTotalPage());
        List<Future<List<String>>> returns = new Vector<>(pageCount.getTotalPage());

        returns.add(DMHYSearchSpiderServiceImpl.executorService.submit(() -> this.getDetailPageLink(startIndex)));

        List<Callable<List<String>>> takes = new ArrayList<>(pageCount.getTotalPage());

        // 使用多线程获取多个页面，并提取其中的链接
        while (pageCount.hash() != 0) {
            takes.add(this.new DetailPageLink(requestBaseUrl + pageCount.getNextPage() + ".html"));
            pageCount.next();
        }

        returns.addAll(DMHYSearchSpiderServiceImpl.executorService.invokeAll(takes)); // 保证获取了所有页面

        for (Future<List<String>> result : returns) {
            links.add(result.get());
        }

        List<String> results = new Vector<>();
        for (List<String> urls : links) {
            results.addAll(urls);
        }
        return results;
    }

    @Transactional
    void saveData(String key, List<DMHYSearchData> data) {
        List<Long> ids = new Vector<>();
        int size = data.size();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < size; i++) {
            ids.add(random.nextLong());
        }
        NameToHashData nameToHashData = new NameToHashData(key, ids);

        for (int i = 0; i < size; i++) {
            data.get(i).setId(ids.get(i));
        }
        this.saveBatch(data);

        nameToHashDataMapService.save(nameToHashData);
    }

    @Transactional
    void saveDataById(Long id, String key, List<DMHYSearchData> data) {
        List<Long> ids = new Vector<>();
        int size = data.size();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < size; i++) {
            ids.add(random.nextLong());
        }
        NameToHashData nameToHashData = new NameToHashData(id, key, ids);

        for (int i = 0; i < size; i++) {
            data.get(i).setId(ids.get(i));
        }
        this.saveBatch(data);

        nameToHashDataMapService.save(nameToHashData);

        searchDataCache.remove(id);  // 数据存入数据库后，移除缓存节约资源
    }
}
