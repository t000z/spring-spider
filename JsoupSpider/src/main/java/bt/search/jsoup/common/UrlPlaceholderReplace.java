package bt.search.jsoup.common;

import lombok.Data;

@Data
public class UrlPlaceholderReplace {
    private String sourceUrl;

    private Integer start;

    private Integer end;

    public UrlPlaceholderReplace(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        this.start = sourceUrl.indexOf('{');
        this.end = sourceUrl.lastIndexOf('}') + 1;
    }

    public String replace(String str) {
        StringBuffer stringBuffer = new StringBuffer(this.sourceUrl);
        stringBuffer.replace(start, end, str);
        return stringBuffer.toString();
    }
}
