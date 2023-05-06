package bt.search.analyzer.pojo;

import bt.search.analyzer.dao.type.StringListTypeHandler;
import bt.search.fegin.dto.DMHYSearchData;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "after_processing_hash_data", autoResultMap = true)
public class HashData {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private LocalDate updateTime;

    private Integer hot;

    private Long size;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> torrents;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> entityWords;

    public HashData(String name, DMHYSearchData data) {
        this.name = name;
        this.updateTime = data.getUpdateTime();
        this.hot = data.getHot();
        this.size = data.getSize();
        this.torrents = data.getTorrents();
    }

    @Override
    public String toString() {
        return "HashData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", updateTime=" + updateTime +
                ", hot=" + hot +
                ", size=" + size +
                ", torrents=" + torrents +
                ", entityWords=" + entityWords +
                '}';
    }

    public HashData(DMHYSearchData data) {
        this(data.getName(), data);
    }
}
