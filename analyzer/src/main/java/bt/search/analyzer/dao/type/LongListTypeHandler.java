package bt.search.analyzer.dao.type;

import bt.search.analyzer.common.TypeConvert;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class LongListTypeHandler extends BaseTypeHandler<List<Long>> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<Long> longs, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, TypeConvert.longListToString(longs));
    }

    @Override
    public List<Long> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return TypeConvert.stringToLongList(resultSet.getString(s));
    }

    @Override
    public List<Long> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return TypeConvert.stringToLongList(resultSet.getString(i));
    }

    @Override
    public List<Long> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return TypeConvert.stringToLongList(callableStatement.getString(i));
    }
}
