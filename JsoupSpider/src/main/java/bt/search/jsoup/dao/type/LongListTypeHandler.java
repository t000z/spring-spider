package bt.search.jsoup.dao.type;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

@Slf4j
public class LongListTypeHandler extends BaseTypeHandler<List<Long>> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<Long> longs, JdbcType jdbcType) throws SQLException {
        StringBuffer buffer = new StringBuffer();
        longs.stream().map(item -> Long.toString(item) + ",").forEach(buffer::append);
        buffer.deleteCharAt(buffer.length() - 1);
        preparedStatement.setString(i, buffer.toString());
    }

    @Override
    public List<Long> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return this.convertToList(resultSet.getString(s));
    }

    @Override
    public List<Long> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return this.convertToList(resultSet.getString(i));
    }

    @Override
    public List<Long> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return this.convertToList(callableStatement.getString(i));
    }

    private List<Long> convertToList(String rolesStr) {
        List<Long> roleList = null;
        if (!"".equals(rolesStr)) {
            String[] roleIdStrArray = rolesStr.split(",");
            roleList = Arrays.stream(roleIdStrArray)
                    .map(item -> Long.valueOf(item))
                    .collect(Collectors.toList());
        }

        return roleList;
    }
}
