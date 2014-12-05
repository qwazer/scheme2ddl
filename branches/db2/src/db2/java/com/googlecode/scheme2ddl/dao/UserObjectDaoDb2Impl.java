package com.googlecode.scheme2ddl.dao;

import com.googlecode.scheme2ddl.domain.Db2LookInfo;
import com.googlecode.scheme2ddl.domain.Db2LookInfoComparator;
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

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class UserObjectDaoDb2Impl extends JdbcDaoSupport implements UserObjectDao {

    private static final Log log = LogFactory.getLog(UserObjectDaoDb2Impl.class);
    private Map<String, Boolean> transformParams;
    @Value("#{jobParameters['schemaName']}")
    private String schemaName;
    @Value("#{jobParameters['launchedByDBA']}")
    private boolean isLaunchedByDBA = false;

    public List<UserObject> findListForProccessing() {


        long opToken = call_DB2LK_GENERATE_DDL("-e -xd -z " + schemaName);

        final String sql;
           // sql = "select OBJECT_NAME, OBJECT_TYPE from SYSIBMADM.ALL_OBJECTS where OBJECT_SCHEMA = '"+schemaName+"' ";
            sql = "SELECT DISTINCT OBJ_TYPE, OBJ_NAME, OP_TOKEN  " +
                    "FROM SYSTOOLS.DB2LOOK_INFO WHERE OP_TOKEN=? AND OBJ_SCHEMA=? ";
        return getJdbcTemplate().query(sql, new Object[]{opToken, schemaName}, new UserObjectRowMapper());
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



    private long call_DB2LK_GENERATE_DDL(String db2lookinfoParams){   //todo rename
        long opToken = 0;
        Connection con =null;

        try {
            con = getDataSource().getConnection();
            CallableStatement cstmt;
            ResultSet rs;
            cstmt = con.prepareCall("CALL SYSPROC.DB2LK_GENERATE_DDL(?, ?)");
            cstmt.setString(1, db2lookinfoParams);
            cstmt.registerOutParameter(2, Types.BIGINT);
            cstmt.executeUpdate();
            opToken = cstmt.getLong(2);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (con!=null){
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return opToken;

    }

    public String findPrimaryDDL(final String type, final String name) {


        int opToken = 0;
        Connection con =null;

        String result = "";

        try {
             con= getDataSource().getConnection();
            CallableStatement cstmt;
            ResultSet rs;
            String db2lookinfoParams;
            if (type.equals("TABLE")) db2lookinfoParams = "-e -t " + schemaName + "." + name +" -xd";
            else if (type.equals("VIEW")) db2lookinfoParams = "-e -v " + schemaName + "." + name +" -xd";
            else  db2lookinfoParams = "-e -xd";
            cstmt = con.prepareCall("call SYSPROC.DB2LK_GENERATE_DDL(?, ?)");
            cstmt.setString(1, db2lookinfoParams);
            cstmt.registerOutParameter (2, Types.INTEGER);
            cstmt.executeUpdate();
            opToken = cstmt.getInt(2);

            List<Db2LookInfo> list = null;
            if (type.equals("TABLE") || type.equals("VIEW"))   {
                list = getJdbcTemplate().query("select OP_SEQUENCE, SQL_STMT, OBJ_SCHEMA, OBJ_TYPE, OBJ_NAME, SQL_OPERATION " +
                                "FROM SYSTOOLS.DB2LOOK_INFO where OP_TOKEN=? and OBJ_SCHEMA=? ",
                        new Object[]{opToken, schemaName},
                        new RowMapper<Db2LookInfo>() {
                            public Db2LookInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                                Db2LookInfo db2LookInfo = new Db2LookInfo();
                                db2LookInfo.setObjName(rs.getString("OBJ_NAME"));
                                db2LookInfo.setObjType(rs.getString("OBJ_TYPE"));
                                db2LookInfo.setObjSchema(rs.getString("OBJ_SCHEMA").trim());
                                db2LookInfo.setOpSequence(rs.getLong("OP_SEQUENCE"));
                                db2LookInfo.setSqlOperation(rs.getString("SQL_OPERATION"));
                                db2LookInfo.setSqlStmtClob(rs.getClob("SQL_STMT"));

                                if (db2LookInfo.getSqlStmtClob() != null) {

                                    if ((int) db2LookInfo.getSqlStmtClob().length() > 0) {
                                        String s = db2LookInfo.getSqlStmtClob().getSubString(1, (int) db2LookInfo.getSqlStmtClob().length());
                                        db2LookInfo.setSqlStmt(s);
                                    }
                                }

                                return db2LookInfo;
                            }
                        });
            }   else {
                list = getJdbcTemplate().query("select OP_SEQUENCE, SQL_STMT, OBJ_SCHEMA, OBJ_TYPE, OBJ_NAME, SQL_OPERATION " +
                                "FROM SYSTOOLS.DB2LOOK_INFO where OP_TOKEN=? and OBJ_SCHEMA=? and OBJ_TYPE=? and OBJ_NAME=?",
                        new Object[]{opToken, schemaName, type, name},
                        new RowMapper<Db2LookInfo>() {
                            public Db2LookInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                                Db2LookInfo db2LookInfo = new Db2LookInfo();
                                db2LookInfo.setObjName(rs.getString("OBJ_NAME"));
                                db2LookInfo.setObjType(rs.getString("OBJ_TYPE"));
                                db2LookInfo.setObjSchema(rs.getString("OBJ_SCHEMA").trim());
                                db2LookInfo.setOpSequence(rs.getLong("OP_SEQUENCE"));
                                db2LookInfo.setSqlOperation(rs.getString("SQL_OPERATION"));
                                db2LookInfo.setSqlStmtClob(rs.getClob("SQL_STMT"));

                                if (db2LookInfo.getSqlStmtClob() != null) {

                                    if ((int) db2LookInfo.getSqlStmtClob().length() > 0) {
                                        String s = db2LookInfo.getSqlStmtClob().getSubString(1, (int) db2LookInfo.getSqlStmtClob().length());
                                        db2LookInfo.setSqlStmt(s);
                                    }
                                }

                                return db2LookInfo;
                            }
                        });
            }



            list.sort(new Db2LookInfoComparator());
            for (Db2LookInfo db2LookInfo : list){
                result = result + db2LookInfo.getSqlStmt() + "\n;";  //todo config format options
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (con!=null){
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

         return result ;

    }

    public List<Db2LookInfo> findDDL(UserObject userObject) {
        return  getJdbcTemplate().query("select OP_SEQUENCE, SQL_STMT, OBJ_SCHEMA, OBJ_TYPE, OBJ_NAME, SQL_OPERATION " +
                        "FROM SYSTOOLS.DB2LOOK_INFO where OP_TOKEN=? and OBJ_SCHEMA=? and OBJ_TYPE=? and OBJ_NAME=?",
                new Object[]{userObject.getOpToken(), schemaName, userObject.getType(), userObject.getName()},
                new RowMapper<Db2LookInfo>() {
                    public Db2LookInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Db2LookInfo db2LookInfo = new Db2LookInfo();
                        db2LookInfo.setObjName(rs.getString("OBJ_NAME"));
                        db2LookInfo.setObjType(rs.getString("OBJ_TYPE"));
                        db2LookInfo.setObjSchema(rs.getString("OBJ_SCHEMA").trim());
                        db2LookInfo.setOpSequence(rs.getLong("OP_SEQUENCE"));
                        db2LookInfo.setSqlOperation(rs.getString("SQL_OPERATION"));
                        db2LookInfo.setSqlStmtClob(rs.getClob("SQL_STMT"));

                        if (db2LookInfo.getSqlStmtClob() != null) {

                            if ((int) db2LookInfo.getSqlStmtClob().length() > 0) {
                                String s = db2LookInfo.getSqlStmtClob().getSubString(1, (int) db2LookInfo.getSqlStmtClob().length());
                                db2LookInfo.setSqlStmt(s);
                            }
                        }

                        return db2LookInfo;
                    }
                });

    }

    private String executeDbmsMetadataGetDdl(final String query, final String type, final String name, final String schema) {


        return (String) getJdbcTemplate().execute(new ConnectionCallback() {
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                applyTransformParameters(connection);
                PreparedStatement ps = connection.prepareStatement("call SYSPROC.DB2LK_GENERATE_DDL('-e -t MY.MAIN_TABLE', ?) ");
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


        if (type.equals("VIEW"))   {
            int opToken = 0;
            Connection con =null;

            String result = "";

            try {
                con= getDataSource().getConnection();
                CallableStatement cstmt;
                ResultSet rs;
                String db2lookinfoParams = "-e -t " + schemaName + "." + name +" -xd";

                cstmt = con.prepareCall("call SYSPROC.DB2LK_GENERATE_DDL(?, ?)");
                cstmt.setString(1, db2lookinfoParams);
                cstmt.registerOutParameter (2, Types.INTEGER);
                cstmt.executeUpdate();
                opToken = cstmt.getInt(2);

                List<Db2LookInfo> list = null;

                    list = getJdbcTemplate().query("select OP_SEQUENCE, SQL_STMT, OBJ_SCHEMA, OBJ_TYPE, OBJ_NAME, SQL_OPERATION " +
                                    "FROM SYSTOOLS.DB2LOOK_INFO where OP_TOKEN=? and OBJ_SCHEMA=? ",
                            new Object[]{opToken, schemaName},
                            new RowMapper<Db2LookInfo>() {
                                public Db2LookInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    Db2LookInfo db2LookInfo = new Db2LookInfo();
                                    db2LookInfo.setObjName(rs.getString("OBJ_NAME"));
                                    db2LookInfo.setObjType(rs.getString("OBJ_TYPE"));
                                    db2LookInfo.setObjSchema(rs.getString("OBJ_SCHEMA").trim());
                                    db2LookInfo.setOpSequence(rs.getLong("OP_SEQUENCE"));
                                    db2LookInfo.setSqlOperation(rs.getString("SQL_OPERATION"));
                                    db2LookInfo.setSqlStmtClob(rs.getClob("SQL_STMT"));

                                    if (db2LookInfo.getSqlStmtClob() != null) {

                                        if ((int) db2LookInfo.getSqlStmtClob().length() > 0) {
                                            String s = db2LookInfo.getSqlStmtClob().getSubString(1, (int) db2LookInfo.getSqlStmtClob().length());
                                            db2LookInfo.setSqlStmt(s);
                                        }
                                    }

                                    return db2LookInfo;
                                }
                            });


                list.sort(new Db2LookInfoComparator());
                for (Db2LookInfo db2LookInfo : list){
                    result = result + db2LookInfo.getSqlStmt() + "\n;";  //todo config format options
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }
            finally {
                if (con!=null){
                    try {
                        con.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            return result ;
        }


        else
            return "";

//        return (String) getJdbcTemplate().execute(new ConnectionCallback() {
//            final String query = "select dbms_metadata.get_dependent_ddl(?, ?, ?) from dual";
//
//            public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
//                applyTransformParameters(connection);
//                PreparedStatement ps = connection.prepareStatement(query);
//                ps.setString(1, type);
//                ps.setString(2, name);
//                ps.setString(3, isLaunchedByDBA ? schemaName : null);
//                ResultSet rs;
//                try {
//                    rs = ps.executeQuery();
//                } catch (SQLException e) {
//                    log.trace(String.format("Error during select dbms_metadata.get_dependent_ddl(%s, %s) from dual", type, name));
//                    return "";
//                }
//                try {
//                    if (rs.next()) {
//                        return rs.getString(1);
//                    }
//                } finally {
//                    rs.close();
//                }
//                return null;
//            }
//        });
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
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.executeUpdate();
            return callableStatement.getString(1);
        }
    }

    private class UserObjectRowMapper implements RowMapper {
        public UserObject mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserObject userObject = new UserObject();
            userObject.setName(rs.getString("OBJ_NAME"));
            userObject.setType(rs.getString("OBJ_TYPE"));
            userObject.setOpToken(rs.getLong("OP_TOKEN"));
            userObject.setSchema(schemaName == null ? "" : schemaName);
            return userObject;
        }
    }
}
