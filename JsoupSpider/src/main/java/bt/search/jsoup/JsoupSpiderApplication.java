package bt.search.jsoup;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
@MapperScan(basePackages = "bt.search.jsoup.dao")
public class JsoupSpiderApplication {
    public static void main(String[] args) {
        SpringApplication.run(JsoupSpiderApplication.class, args);
    }
}
