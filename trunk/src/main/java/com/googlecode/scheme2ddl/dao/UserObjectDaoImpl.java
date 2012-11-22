package com.googlecode.scheme2ddl.dao;

import com.googlecode.scheme2ddl.domain.UserObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class UserObjectDaoImpl extends JdbcDaoSupport implements UserObjectDao {

    private static final Log log = LogFactory.getLog(UserObjectDaoImpl.class);
    private Map<String, Boolean> transformParams;

    public List<UserObject> findListForProccessing() {
        return getJdbcTemplate().query(
                "select t.object_name, t.object_type " +
                        "  from user_objects t " +
                        " where t.generated = 'N' " +
                        "   and not exists (select 1 " +
                        "          from user_nested_tables unt" +
                        "         where t.object_name = unt.table_name)",
                new UserObjectRowMapper());
    }

    public List<UserObject> findPublicDbLinks() {
        return getJdbcTemplate().query(
                "select db_link as object_name, 'PUBLIC DATABASE LINK' as object_type " +
                        "from DBA_DB_LINKS " +
                        "where owner='PUBLIC'",
                new UserObjectRowMapper());
    }

    public List<UserObject> findDmbsJobs() {
        return getJdbcTemplate().query(
                "select job || '' as object_name, 'DBMS JOB' as object_type " +
                        "from user_jobs " +
                        "where schema_user != 'SYSMAN'",
                new UserObjectRowMapper());
    }

    public String findPrimaryDDL(final String type, final String name) {
        return executeDbmsMetadataGetDdl("select dbms_metadata.get_ddl(?, ?) from dual", type, name);
    }

    private String executeDbmsMetadataGetDdl(final String query, final String type, final String name) {
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
                    log.trace(String.format("Error during select dbms_metadata.get_dependent_ddl(%s, %s) from dual", type, name));
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

    public String findDDLInPublicScheme(String type, String name) {
        return executeDbmsMetadataGetDdl("select dbms_metadata.get_ddl(?, ?, 'PUBLIC') from dual", type, name);
    }

    public String findDbmsJobDDL(String name) {
        return (String) getJdbcTemplate().execute("DECLARE\n" +
                " callstr VARCHAR2(4096);\n" +
                "BEGIN\n" +
                "  dbms_job.user_export(" + name + ", callstr);\n" +
                ":done := callstr; " +
                "END;", new CallableStatementCallbackImpl());
    }

    public boolean isConnectionAvailable() {
        try {
            getJdbcTemplate().queryForInt("select 1 from dual");
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    public void applyTransformParameters(Connection connection) throws SQLException {
        for (String parameterName : transformParams.keySet()) {
            connection.setAutoCommit(false);
            // setBoolean doesn't convert java boolean to pl/sql boolean, so used such query building
            String sql = String.format(
                    "BEGIN " +
                            " dbms_metadata.set_transform_param(DBMS_METADATA.SESSION_TRANSFORM,'%s',%s);" +
                            " END;", parameterName, transformParams.get(parameterName));
            PreparedStatement ps = connection.prepareCall(sql);
            //  ps.setString(1, parameterName);
            //  ps.setBoolean(2, transformParams.get(parameterName) );  //In general this doesn't work
            ps.execute();
        }
    }

    public void setTransformParams(Map<String, Boolean> transformParams) {
        this.transformParams = transformParams;
    }

    private class CallableStatementCallbackImpl implements CallableStatementCallback {
        public Object doInCallableStatement(CallableStatement callableStatement) throws SQLException, DataAccessException {
            callableStatement.registerOutParameter(1, java.sql.Types.VARCHAR);
            callableStatement.executeUpdate();
            return callableStatement.getString(1);
        }
    }

    private class UserObjectRowMapper implements RowMapper {
        public UserObject mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserObject userObject = new UserObject();
            userObject.setName(rs.getString("object_name"));
            userObject.setType(rs.getString("object_type"));
            return userObject;
        }
    }
}
