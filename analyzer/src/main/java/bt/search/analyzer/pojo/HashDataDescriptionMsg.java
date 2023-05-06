package bt.search.analyzer.pojo;

import bt.search.analyzer.dao.type.LongListTypeHandler;
import bt.search.analyzer.dao.type.StringListTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "hash_data_and_description_map", autoResultMap = true)
public class HashDataDescriptionMsg {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    // 描述信息
    private String description;

    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> indexs;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> tags;

    public HashDataDescriptionMsg(Long id, String name, List<Long> indexs) {
        this.id = id;
        this.name = name;
        this.indexs = indexs;
    }

    public HashDataDescriptionMsg(String name, String description, List<Long> indexs, List<String> tags) {
        this.name = name;
        this.description = description;
        this.indexs = indexs;
        this.tags = tags;
    }

    public HashDataDescriptionMsg(Long id, String description) {
        this.id = id;
        this.description = description;
    }
}
