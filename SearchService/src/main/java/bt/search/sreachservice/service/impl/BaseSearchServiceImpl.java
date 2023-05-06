package bt.search.sreachservice.service.impl;

import bt.search.sreachservice.common.ServiceThreadPool;
import bt.search.sreachservice.pojo.ElasticSearchStructure;
import bt.search.sreachservice.pojo.SearchData;
import bt.search.sreachservice.service.ElasticSearchStructureService;
import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public abstract class BaseSearchServiceImpl {
    @Qualifier("restHighLevelClient")
    @Autowired
    protected RestHighLevelClient client;

    static protected ExecutorService EXECUTORSERVICE;

    @Autowired
    protected ElasticSearchStructureService elasticSearchStructureService;

    @PostConstruct
    public void init() throws ExecutionException, InterruptedException {
        BaseSearchServiceImpl.EXECUTORSERVICE = ServiceThreadPool.createExecutorService();

        // 创建不存在的索引库
        List<ElasticSearchStructure> structures = elasticSearchStructureService.list();
        List<Future<Boolean>> futures = structures.stream()
                .map(item -> EXECUTORSERVICE.submit(new CreateIndexIfNotExists(item)))
                .collect(Collectors.toList());

        // 等待所有索引库初始化完成
        for (Future<Boolean> future : futures) {
            future.get();
        }
    }

    @PreDestroy
    public void destroy() {
        if (BaseSearchServiceImpl.EXECUTORSERVICE != null) {
            ServiceThreadPool.shutdown(BaseSearchServiceImpl.EXECUTORSERVICE);
        }
    }

    protected <T> List<T> handleHits(SearchHit[] hits, Class<T> clazz) {
        List<T> data = Arrays.stream(hits)
                .map(item -> JSON.parseObject(item.getSourceAsString(), clazz))
                .collect(Collectors.toList());

        return data;
    }

    @Data
    @AllArgsConstructor
    private class CreateIndexIfNotExists implements Callable<Boolean> {
        private ElasticSearchStructure structure;

        @Override
        public Boolean call() throws IOException {
            GetIndexRequest getIndexRequest = new GetIndexRequest(structure.getIndexName());
            boolean exists = false;
            exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

            if (!exists) {  // 索引不存在，则创建
                CreateIndexRequest request = new CreateIndexRequest(structure.getIndexName());
                request.source(structure.getIndexStructure(), XContentType.JSON);
                client.indices().create(request, RequestOptions.DEFAULT);
            }

            return true;
        }
    }
}
