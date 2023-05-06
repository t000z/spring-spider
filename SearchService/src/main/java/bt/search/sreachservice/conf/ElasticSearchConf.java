package bt.search.sreachservice.conf;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ElasticSearchConf {

    @Value("${Es.url}")
    private String url;

    @Bean(name = "restHighLevelClient", destroyMethod = "close")
    public RestHighLevelClient restClient() {
        return new RestHighLevelClient(RestClient.builder(
                HttpHost.create(url)
        ));
    }
}
