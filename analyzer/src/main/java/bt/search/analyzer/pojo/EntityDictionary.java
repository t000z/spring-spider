package bt.search.analyzer.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("entity_dictionary")
public class EntityDictionary {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String word;

    // 一般没用
    private String description;

    public EntityDictionary(String word) {
        this.word = word;
    }

    public EntityDictionary(String word, String description) {
        this.word = word;
        this.description = description;
    }
}
