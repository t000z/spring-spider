package bt.search.sreachservice.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("elasticsearch_structure")
public class ElasticSearchStructure {
    @TableId(type = IdType.ASSIGN_ID)
    private Integer id;

    // 索引名
    private String indexName;

    // 索引结构
    private String indexStructure;
}
