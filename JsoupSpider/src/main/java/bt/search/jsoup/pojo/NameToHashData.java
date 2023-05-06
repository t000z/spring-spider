package bt.search.jsoup.pojo;

import bt.search.jsoup.dao.type.LongListTypeHandler;
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
@TableName(value = "name_to_hashdata_map", autoResultMap = true)
public class NameToHashData {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> indexs;

    public NameToHashData(String name, List<Long> indexs) {
        this.name = name;
        this.indexs = indexs;
    }

    public NameToHashData(Long id) {
        this.id = id;
    }
}
