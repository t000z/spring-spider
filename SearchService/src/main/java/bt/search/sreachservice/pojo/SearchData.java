package bt.search.sreachservice.pojo;

import bt.search.fegin.dto.HashData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchData {
    private Long id;

    private String name;

    private LocalDate updateTime;

    private Integer hot;

    private Long size;

    private List<String> torrents;

    private List<String> entityWords;

    private List<String> tag;

    public SearchData(HashData hashData) {
        this.id = hashData.getId();
        this.name = hashData.getName();
        this.updateTime = hashData.getUpdateTime();
        this.hot = hashData.getHot();
        this.size = hashData.getSize();
        this.torrents = hashData.getTorrents();
        this.entityWords = hashData.getEntityWords();
    }

    public SearchData(HashData hashData, List<String> tags) {
        this(hashData);
        this.tag = tags;
    }
}
