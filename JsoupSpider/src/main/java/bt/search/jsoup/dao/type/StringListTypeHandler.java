package bt.search.jsoup.dao.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class StringListTypeHandler extends BaseTypeHandler<List<String>> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<String> strings, JdbcType jdbcType) throws SQLException {
        StringBuffer buffer = new StringBuffer();
        strings.stream().map(item -> item + ",").forEach(buffer::append);
        buffer.deleteCharAt(buffer.length() - 1);
        preparedStatement.setString(i, buffer.toString());
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return this.convertToList(resultSet.getString(s));
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return this.convertToList(resultSet.getString(i));
    }

    @Override
    public List<String> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return this.convertToList(callableStatement.getString(i));
    }

    private List<String> convertToList(String rolesStr) {
        List<String> roleList = null;
        if (!"".equals(rolesStr)) {
            String[] roleIdStrArray = rolesStr.split(",");
            roleList = Arrays.asList(roleIdStrArray);
        }

        return roleList;
    }
}
