package bt.search.analyzer.pojo;

import bt.search.analyzer.dao.type.StringListTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "unknown_entity", autoResultMap = true)
public class UnknownEntity {
    @TableId
    private String word;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> relevancy;

    public UnknownEntity(String word) {
        this.word = word;
    }
}
