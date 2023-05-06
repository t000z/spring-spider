package bt.search.sreachservice.service.impl;

import bt.search.fegin.analyzer.AnalyzerClient;
import bt.search.fegin.dto.HashData;
import bt.search.fegin.dto.SpiderDescriptionInto;
import bt.search.fegin.dto.pair;
import bt.search.fegin.spider.JsoupSpiderClient;
import bt.search.sreachservice.pojo.SearchData;
import bt.search.sreachservice.service.SearchService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl extends BaseSearchServiceImpl implements SearchService {
    @Autowired
    private AnalyzerClient analyzerClient;

    @Autowired
    private JsoupSpiderClient jsoupSpiderClient;

    @Override
    public List<SearchData> title(String key) throws IOException, InterruptedException {
        SearchRequest request = new SearchRequest("hash_data");
        request.source().query(QueryBuilders.matchQuery("name", key));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        SearchHits searchHits = response.getHits();
        List<SearchData> searchData = null;
        if (searchHits.getTotalHits().value != 0) {  // Es搜索到数据
            searchData = handleHits(searchHits.getHits(), SearchData.class);

            if (searchData.get(0).getTag() == null) {  // 没有tag
                SpiderDescriptionInto spiderDescriptionInto = jsoupSpiderClient.descSearch(key);
                searchData.stream().forEach(item -> item.setTag(spiderDescriptionInto.getTag()));

                // 修改Es数据库
                EXECUTORSERVICE.execute(() -> {
                    try {
                        this.updateTag(spiderDescriptionInto.getTag(), response.getHits().getTotalHits().value);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        if (searchData == null) { // Es没有搜索到数据，调用其他服务获取
            List<HashData> hashDataList = analyzerClient.findByKey(key);
            List<String> tagList = analyzerClient.updateDescriptionAndTag(key);
            searchData = hashDataList.stream()
                    .map(item -> new SearchData(item, tagList))
                    .collect(Collectors.toList());
        }

        if (searchHits.getTotalHits().value == 0) {  // 说明数据不在Es库中
            List<SearchData> finalSearchData = searchData;
            EXECUTORSERVICE.execute(() -> {
                try {
                    this.addAll(finalSearchData);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return searchData;
    }

    @Override
    public List<SearchData> tag(String tag) throws IOException {
        pair<List<Long>> dataIndex = analyzerClient.getTag(tag);
        List<String> collect = dataIndex.getValue().stream().map(item -> item.toString()).collect(Collectors.toList());

        SearchRequest searchRequest = new SearchRequest("hash_data");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery().addIds(collect.toArray(new String[1]));
        boolQueryBuilder.must(idsQueryBuilder);
        searchRequest.source().query(boolQueryBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        List<SearchData> searchData = handleHits(response.getHits().getHits(), SearchData.class);
        return searchData;
    }

    @Override
    public List<SearchData> search(String key) throws IOException {
        String[] split = key.split(" ");

        SearchRequest searchRequest = new SearchRequest("hash_data");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        Arrays.stream(split)
                .map(item -> QueryBuilders.matchQuery("all", item))
                .forEach(boolQueryBuilder::must);
        searchRequest.source().query(boolQueryBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        List<SearchData> searchData = handleHits(response.getHits().getHits(), SearchData.class);
        return searchData;
    }

    private void addAll(List<SearchData> list) throws IOException {
        BulkRequest request = new BulkRequest();
        list.stream()
                .map(item -> (JSONObject) JSON.toJSON(item))
                .forEach(item -> {
                    request.add(new IndexRequest("hash_data")
                            .id(item.remove("id").toString())
                            .source(item.toString(), XContentType.JSON));
                });
        client.bulk(request, RequestOptions.DEFAULT);
    }

    private void updateTag(List<String> tags, long size) throws IOException {
        SearchRequest request = new SearchRequest("hash_data");
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().size((int) size);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits searchHits = response.getHits();

        List<String> ids = Arrays.stream(searchHits.getHits())
                .map(item -> item.getId())
                .collect(Collectors.toList());

        String tagList = Arrays.toString(tags.toArray());

        BulkRequest bulkRequest = new BulkRequest();
        ids.stream()
                .forEach(item -> {
                    bulkRequest.add(new UpdateRequest("hash_data", item)
                            .doc("tag", tagList));
                });
        client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }
}
