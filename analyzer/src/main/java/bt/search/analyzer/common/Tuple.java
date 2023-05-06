package bt.search.analyzer.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tuple<K, V> {
    private K key;

    private V value;

    public Tuple(K key) {
        this.key = key;
    }
}
