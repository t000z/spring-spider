package bt.search.analyzer.pojo;

import bt.search.analyzer.dao.type.StringSetEnterSplitTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Set;

@Data
@TableName(value = "regex_config", autoResultMap = true)
public class RegexConfig {
    private String classes;

    @TableField(typeHandler = StringSetEnterSplitTypeHandler.class)
    private Set<String> regexList;

    @TableField(typeHandler = StringSetEnterSplitTypeHandler.class)
    private Set<String> uselessChar;
}
