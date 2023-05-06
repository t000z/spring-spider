package bt.search.fegin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DMHYSearchData implements Serializable {
    private Long id;

    private String name;

    private LocalDate updateTime;

    private int hot;

    // 以MB表示，实际数值 * 100
    private Long size;

    private List<String> torrents;

    private String indexUrl;

    public DMHYSearchData(String name, LocalDate updateTime, int hot, long size, List<String> torrents, String indexUrl) {
        this.name = name;
        this.updateTime = updateTime;
        this.hot = hot;
        this.size = size;
        this.torrents = torrents;
        this.indexUrl = indexUrl;
    }
}
