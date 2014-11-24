package com.googlecode.scheme2ddl.dao;

import com.googlecode.scheme2ddl.domain.UserObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.googlecode.scheme2ddl.TypeNamesUtil.map2TypeForConfig;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class UserObjectDaoImpl extends JdbcDaoSupport implements UserObjectDao {

    private static final Log log = LogFactory.getLog(UserObjectDaoImpl.class);
    private Map<String, Boolean> transformParams;
    @Value("#{jobParameters['schemaName']}")
    private String schemaName;
    @Value("#{jobParameters['launchedByDBA']}")
    private boolean isLaunchedByDBA = false;

    public List<UserObject> findListForProccessing() {
        String sql;
        if (isLaunchedByDBA)
            sql = "select t.object_name, t.object_type " +
                    "  from dba_objects t " +
                    " where t.generated = 'N' " +
                    "   and t.owner = '" + schemaName + "' " +
                    "   and not exists (select 1 " +
                    "          from user_nested_tables unt" +
                    "         where t.object_name = unt.table_name)" +
                    " UNION ALL " +
                    " select rname as object_name, 'REFRESH_GROUP' as object_type " +
                    " from dba_refresh a " +
                    " where a.rowner = '" + schemaName + "' ";
        else
            sql = "select tabname as object_name, 'table' as object_type from syscat.tables where tabschema = 'MY' ";
        return getJdbcTemplate().query(sql, new UserObjectRowMapper());
    }

    public List<UserObject> findPublicDbLinks() {
        List<UserObject> list = new ArrayList<UserObject>();
        try {
            list = getJdbcTemplate().query(
                    "select db_link as object_name, 'PUBLIC DATABASE LINK' as object_type " +
                            "from DBA_DB_LINKS " +
                            "where owner='PUBLIC'",
                    new UserObjectRowMapper());
        } catch (BadSqlGrammarException sqlGrammarException) {
            if (sqlGrammarException.getSQLException().getErrorCode() == 942) {
                String userName = null;
                try {
                    userName = getDataSource().getConnection().getMetaData().getUserName();
                } catch (SQLException e) {
                }
                log.warn("WARNING: processing of 'PUBLIC DATABASE LINK' will be skipped because " + userName + " no access to view it" +
                        "\n Possible decisions:\n\n" +
                        " 1) Exclude processPublicDbLinks option in advanced config to disable this warning\n    " +
                        " <bean id=\"reader\" ...>\n" +
                        "        <property name=\"processPublicDbLinks\" value=\"false\"/>\n" +
                        "        ...\n" +
                        "    </bean>\n" +
                        "\n" +
                        " 2) Or try give access to user " + userName + " with sql command\n " +
                        " GRANT SELECT_CATALOG_ROLE TO " + userName + "; \n\n");
            }
            return list;
        }

        for (UserObject userObject : list) {
            userObject.setSchema("PUBLIC");
        }
        return list;
    }

    public List<UserObject> findDmbsJobs() {
//        String tableName = isLaunchedByDBA ? "dba_jobs" : "user_jobs";
//        String whereClause = isLaunchedByDBA ? "schema_user = '" + schemaName + "'" : "schema_user != 'SYSMAN'";
//        String sql = "select job || '' as object_name, 'DBMS JOB' as object_type " +
//                "from  " + tableName + " where " + whereClause;
//        return getJdbcTemplate().query(sql, new UserObjectRowMapper());
        return new ArrayList<UserObject>();
    }

    public List<UserObject> findConstaints() {
        String sql;
        if (isLaunchedByDBA)
            sql = " select constraint_name as object_name, 'CONSTRAINT' as object_type" +
                    " from all_constraints " +
                    " where constraint_type != 'R' and owner = '" + schemaName + "'" +
                    " UNION ALL " +
                    " select constraint_name as object_name, 'REF_CONSTRAINT' as object_type" +
                    " from all_constraints " +
                    " where constraint_type = 'R' and owner = '" + schemaName + "'";
        else
            sql = " select constraint_name as object_name, 'CONSTRAINT' as object_type" +
                    " from user_constraints where  constraint_type != 'R'" +
                    " UNION ALL " +
                    " select constraint_name as object_name, 'REF_CONSTRAINT' as object_type" +
                    " from user_constraints where constraint_type = 'R'";
        return getJdbcTemplate().query(sql, new UserObjectRowMapper());
    }

    public String findPrimaryDDL(final String type, final String name) {
        if (isLaunchedByDBA)
            return executeDbmsMetadataGetDdl("select dbms_metadata.get_ddl(?, ?, ?) from dual", type, name, schemaName);
        else
            return executeDbmsMetadataGetDdl("select dbms_metadata.get_ddl(?, ?) from dual", type, name, null);
    }

    private String executeDbmsMetadataGetDdl(final String query, final String type, final String name, final String schema) {
        return (String) getJdbcTemplate().execute(new ConnectionCallback() {
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                applyTransformParameters(connection);
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, type);
                ps.setString(2, name);
                if (schema != null) {
                    ps.setString(3, schema);
                }
                ResultSet rs = null;
                try {
                    rs = ps.executeQuery();
                } catch (SQLException e) {
//                    log.trace(String.format("Error during select dbms_metadata.get_ddl('%s', '%s') from dual\n" +
//                            "Try to exclude type '%s' in advanced config excludes section\n", type, name, map2TypeForConfig(type)));
//                    log.trace(String.format("Sample:\n\n" +
//                            " <util:map id=\"excludes\">\n" +
//                            "...\n" +
//                            "         <entry key=\"%s\">\n" +
//                            "            <set>\n" +
//                            "                <value>%s</value>\n" +
//                            "            </set>\n" +
//                            "        </entry>\n" +
//                            "...\n" +
//                            "</util:map>", map2TypeForConfig(type), name));
                    throw e;
                }
                try {
                    if (rs.next()) {
                        return rs.getString(1);
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
            final String query = "select dbms_metadata.get_dependent_ddl(?, ?, ?) from dual";

            public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
                applyTransformParameters(connection);
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, type);
                ps.setString(2, name);
                ps.setString(3, isLaunchedByDBA ? schemaName : null);
                ResultSet rs;
                try {
                    rs = ps.executeQuery();
                } catch (SQLException e) {
                    log.trace(String.format("Error during select dbms_metadata.get_dependent_ddl(%s, %s) from dual", type, name));
                    return "";
                }
                try {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                } finally {
                    rs.close();
                }
                return null;
            }
        });
    }

    public String findDDLInPublicScheme(String type, String name) {
        return executeDbmsMetadataGetDdl("select dbms_metadata.get_ddl(?, ?, ?) from dual", type, name, "PUBLIC");
    }

    public String findDbmsJobDDL(String name) {
        String sql;
        if (isLaunchedByDBA)
            // The 'dbms_job.user_export' function does not work with sys/dba users (can't find users jobs). :(
            sql = "DECLARE\n" +
                    " callstr VARCHAR2(4096);\n" +
                    "BEGIN\n" +
                    "  dbms_job.full_export(" + name + ", callstr);\n" +
                    ":done := callstr; END;";
        else
            sql = "DECLARE\n" +
                    " callstr VARCHAR2(4096);\n" +
                    "BEGIN\n" +
                    "  dbms_job.user_export(" + name + ", callstr);\n" +
                    ":done := callstr; " +
                    "END;";

        return (String) getJdbcTemplate().execute(sql, new CallableStatementCallbackImpl());
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

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setLaunchedByDBA(boolean launchedByDBA) {
        this.isLaunchedByDBA = launchedByDBA;
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
            userObject.setSchema(schemaName == null ? "" : schemaName);
            return userObject;
        }
    }
}
