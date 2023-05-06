package bt.search.analyzer.dao.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringSetEnterSplitTypeHandler extends BaseTypeHandler<Set<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Set<String> strings, JdbcType jdbcType) throws SQLException {
        StringBuffer buffer = new StringBuffer();
        strings.stream().forEach(item -> buffer.append(item + "\n"));
        preparedStatement.setString(i, buffer.toString());
    }

    @Override
    public Set<String> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return this.convertStringSet(resultSet.getString(s));
    }

    @Override
    public Set<String> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return this.convertStringSet(resultSet.getString(i));
    }

    @Override
    public Set<String> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return this.convertStringSet(callableStatement.getString(i));
    }

    private Set<String> convertStringSet(String str) {
        String[] strings = str.split("\n");
        Set<String> result = new HashSet<>();
        Arrays.stream(strings).forEach(result::add);
        return result;
    }

}
