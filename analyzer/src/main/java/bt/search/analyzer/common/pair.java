package bt.search.analyzer.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class pair<T>{
    private String key;

    private T value;
}
