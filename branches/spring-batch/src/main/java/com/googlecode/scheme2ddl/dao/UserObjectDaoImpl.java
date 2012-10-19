package com.googlecode.scheme2ddl.dao;

import com.googlecode.scheme2ddl.domain.UserObject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class UserObjectDaoImpl extends JdbcDaoSupport implements UserObjectDao {

    private Map<String,String> transformParams;


    public List<UserObject> findListForProccessing() {
        return getJdbcTemplate().query(
                "select t.object_name, t.object_type " +
                        "  from user_objects t " +
                        " where t.generated = 'N' " +
                        "   and not exists (select 1 " +
                        "          from user_nested_tables unt" +
                        "         where t.object_name = unt.table_name)",
                new RowMapper<UserObject>() {
                    public UserObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                        UserObject userObject = new UserObject();
                        userObject.setName(rs.getString("object_name"));
                        userObject.setType(rs.getString("object_type"));
                        return userObject;
                    }
                });
    }


    public String findPrimaryDDL(final String type, final String name) {
        final String query = "select dbms_metadata.get_ddl(?, ?) from dual";
        return (String) getJdbcTemplate().execute(new ConnectionCallback() {
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                applyTransformParameters(connection);
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, type);
                ps.setString(2, name);
                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        return rs.getString(1).trim();
                    }
                } finally {
                    rs.close();
                }
                return null;
            }
        });
    }


    public String findDependentDLLByTypeName(final String type, final String name) {

        return (String) getJdbcTemplate().execute(new ConnectionCallback() {
            final String query = "select dbms_metadata.get_dependent_ddl(?, ?) from dual";
            public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
                applyTransformParameters(connection);
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, type);
                ps.setString(2, name);
                ResultSet rs;
                try {
                    rs = ps.executeQuery();
                } catch (SQLException e) {
                    return "";
                }
                try {
                    if (rs.next()) {
                        return rs.getString(1).trim();
                    }
                } finally {
                    rs.close();
                }
                return null;
            }
        });
    }

    private void applyTransformParameters(Connection connection) throws SQLException {
        for (String parameterName: transformParams.keySet()) {
            connection.setAutoCommit(false);
            //  DBMS_METADATA.SESSION_TRANSFORM replaced by -1 because,
            // variables and constants in the package can only be accessed from the PL / SQL,
            // not from SQL as in my case.
            //(for oracle 10 it works)  //todo test for oracle 11
            String sql = "call DBMS_METADATA.SET_TRANSFORM_PARAM(-1,?,?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, parameterName);
            ps.setString(2, transformParams.get(parameterName) );
            ps.execute();
        }
    }


    public void setTransformParams(Map<String, String> transformParams) {
        this.transformParams = transformParams;
    }
}
