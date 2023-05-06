package bt.search.sreachservice.conf;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.protocol.HttpContext;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MyRestClientBuilderCustomizer implements RestClientBuilderCustomizer {
    @Override
    public void customize(RestClientBuilder builder) {
        builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setKeepAliveStrategy(CustomConnectionKeepAliveStrategy.INSTANCE));
    }

    public static class CustomConnectionKeepAliveStrategy extends DefaultConnectionKeepAliveStrategy {
        public static final CustomConnectionKeepAliveStrategy INSTANCE = new CustomConnectionKeepAliveStrategy();

        private CustomConnectionKeepAliveStrategy() {
            super();
        }

        private final long MAX_KEEP_ALIVE_MINUTES = 10;

        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            long keepAliveDuration = super.getKeepAliveDuration(response, context);
            // <0 为无限期keepalive
            // 将无限期替换成一个默认的时间
            if(keepAliveDuration < 0){
                return TimeUnit.MINUTES.toMillis(MAX_KEEP_ALIVE_MINUTES);
            }
            return keepAliveDuration;
        }
    }
}
