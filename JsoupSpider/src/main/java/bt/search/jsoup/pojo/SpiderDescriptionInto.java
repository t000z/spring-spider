package bt.search.jsoup.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpiderDescriptionInto {
    private String name;

    private String description;

    private List<String> tag;
}
