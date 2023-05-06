package bt.search.jsoup.common;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class DownLoadUtils {
    public static Document downLoadGet(String url) throws IOException {
        return Jsoup.connect(url).timeout(20000).get();
    }

    public static Document downLoadGet(String url, int retry) throws IOException {
        Document document = null;
        for (int i = 1; i <= retry; i++) {
            try {
                document = DownLoadUtils.downLoadGet(url);
            } catch (IOException e) {
                if (i == retry) {
                    throw new IOException("downLoadGet " + url + " retry count:" + retry + " info: " + e.getMessage());
                }
            }
        }

        return document;
    }

    public static Document downLoadGetDisguise(String url, int retry) throws IOException {
        Connection connection = Jsoup.connect(url).timeout(20000);
        connection.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.header("accept-encoding", "gzip, deflate, sdch");
        connection.header("accept-language", "zh-CN,zh;q=0.9");
        connection.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");

        Document document = null;
        for (int i = 1; i <= retry; i++) {
            try {
                document = connection.get();
            } catch (IOException e) {
                if (i == retry) {
                    throw new IOException("downLoadGet " + url + " retry count:" + retry + " info: " + e.getMessage());
                }
            }
        }

        return document;
    }

    public static Document downLoadGetDisguise(String url) throws IOException {
        return DownLoadUtils.downLoadGetDisguise(url, 3);
    }
}
