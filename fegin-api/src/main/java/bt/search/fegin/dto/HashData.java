package bt.search.fegin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HashData {
    private Long id;

    private String name;

    private LocalDate updateTime;

    private Integer hot;

    private Long size;

    private List<String> torrents;

    private List<String> entityWords;

    public HashData(String name, DMHYSearchData data) {
        this.name = name;
        this.updateTime = data.getUpdateTime();
        this.hot = data.getHot();
        this.size = data.getSize();
        this.torrents = data.getTorrents();
    }

    public HashData(DMHYSearchData data) {
        this(data.getName(), data);
    }
}
