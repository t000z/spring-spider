package bt.search.analyzer.pojo;

import bt.search.analyzer.dao.type.LongListTypeHandler;
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
@TableName(value = "tag", autoResultMap = true)
public class Tag {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String tag;

    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> indexs;

    public Tag(String tag, List<Long> indexs) {
        this.tag = tag;
        this.indexs = indexs;
    }
}
