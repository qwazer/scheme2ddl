/*
 *    Copyright (c) 2011 Reshetnikov Anton aka qwazer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.googlecode.scheme2ddl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * DAO class for working with database
 * User: Reshetnikov AV resheto@gmail.com
 * Date: 19.02.11
 * Time: 15:01
 */
public class Dao extends JdbcDaoSupport {

    private Map<String, Set<String>> map;
    private Map<String,String> transformParams;
    private Set<String> filterTypes;
    private Map<String,Set<String>> excludeMap;
    private int objectsAge;

    public UserObject fillDDL(UserObject obj) {
        String ddl = "";
        ddl += getPrimaryDDL(obj);
        ddl += getDependedDDL(obj);
        obj.setDdl(ddl);
        return obj;
    }

    /**
     * There is  primary and depended DDL in DMBS_METADATA package
     * Example of primary is TABLE, example of depended is INDEX
     * @param obj
     * @return
     */
    private String getPrimaryDDL(final UserObject obj) {
        return (String) getJdbcTemplate().execute(new ConnectionCallback() {
            public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
                setTransformParameters(connection);
                PreparedStatement ps = connection.prepareStatement("select dbms_metadata.get_ddl(?, ?) from dual");
                ps.setString(1, obj.getType4DBMS());
                ps.setString(2, obj.getName());
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

    private String getDependedDDL(UserObject obj) {
        String res = "";
        Set<String> dependedTypes = map.get(obj.getType());
        if (dependedTypes != null) {
            for (String dependedType : dependedTypes) {
                res += getDependentDLLByTypeName(dependedType, obj.getName()).trim();
            }
        }
        return res;

    }

    private String getDependentDLLByTypeName(final String type, final String name) {

        return (String) getJdbcTemplate().execute(new ConnectionCallback() {
            public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
                setTransformParameters(connection);
                PreparedStatement ps = connection.prepareStatement("select dbms_metadata.get_dependent_ddl(?, ?) from dual");
                ps.setString(1, type);
                ps.setString(2, name);
                ResultSet rs;
                try {
                    rs = ps.executeQuery();
                } catch (SQLException e) {
                    //System.err.println("error of get_dependent_ddl for object type " + type + " of object name " + name);
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

    /**
     *  Get user object list for processing;
     * @return  List of ru.qwazer.scheme2ddl.UserObject
     */
    public List<UserObject> getUserObjectList() {
        String whereAdd = null;
        if (filterTypes != null && !filterTypes.isEmpty()) {
            whereAdd = " and object_type in ( ";
            for (String type : filterTypes) {
                whereAdd += "'" + type.toUpperCase() + "',";
            }
            whereAdd += "'')";
        }
         List<UserObject> list = getUserObjectListPrivate(whereAdd);
        System.out.println("list.size() before filter = " + list.size());
        filterFromSystemTypes(list);
        filterFromExcludedTypesPrefixes(list);
        return list;
    }

    /**
     * Remove exluded types specified by prefixes in config
     * @param list
     */
    private void filterFromExcludedTypesPrefixes(List<UserObject> list) {
        if (excludeMap == null || excludeMap.size()==0) return;
        List<UserObject> removed = new ArrayList<UserObject>();
        for (UserObject obj : list) {
            for (String typeName : excludeMap.keySet()) {
                for (String prefix : excludeMap.get(typeName)) {
                    if (obj.getType().equalsIgnoreCase(typeName) &&
                            obj.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                        removed.add(obj);
                    }
                }
            }
        }
        list.removeAll(removed);
    }

    /**
     * For removing system types http://www.sql.ru/forum/actualthread.aspx?bid=3&tid=542661&hl=
     * @param list
     */
    private void filterFromSystemTypes(List<UserObject> list) {
        List<UserObject> removed= new ArrayList<UserObject>();
        for (UserObject obj : list ){
            if (obj.getType().equalsIgnoreCase("TYPE")
                    && obj.getName().startsWith("SYSTP")
                    && obj.getName().endsWith("==")){
                removed.add(obj);
            }
        }
        list.removeAll(removed);
    }

//    public List<UserObject> getUserObjectList(List<String> types) {
//        String whereAdd = null;
//        if (types != null && !types.isEmpty()) {
//            whereAdd = "and object_type in ( ";
//            for (String type : types) {
//                whereAdd += "'" + type.toUpperCase() + "',";
//            }
//            whereAdd += "'')";
//        }
//        return getUserObjectListPrivate(whereAdd);
//    }

    private List<UserObject> getUserObjectListPrivate(String whereAdd) {

        String select_sql =
                "select t.object_name, t.object_type " +
                "from user_objects t " +
                "where t.generated='N' and" +
                "      not exists (select 1 " +
                "                  from user_nested_tables unt " +
                "                  where t.object_name = unt.table_name) ";
        if (objectsAge>0){
            select_sql += " and last_ddl_time>=sysdate-"+objectsAge + " ";
        }
        final String sql;
        if (whereAdd != null && !whereAdd.equals("")) {
            sql = select_sql + whereAdd;
        } else sql = select_sql;

        List<UserObject> list = (List<UserObject>) getJdbcTemplate().execute(new ConnectionCallback() {
            public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
                PreparedStatement ps = connection.prepareStatement(sql);

                ResultSet rs = ps.executeQuery();
                List<UserObject> res = new ArrayList<UserObject>();
                try {
                    while (rs.next()) {
                        UserObject obj = new UserObject(rs.getString("object_type"), rs.getString("object_name"), null);
                        res.add(obj);
                    }
                } finally {
                    rs.close();
                }
                return res;
            }
        });

        return list;
    }

    private void setTransformParameters(Connection connection) throws SQLException {
        String sql;
        for (String param: transformParams.keySet()) {
           connection.setAutoCommit(false);
       //    sql = "call DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'" + param + "',"+transformParams.get(param)+")";
           sql = "call DBMS_METADATA.SET_TRANSFORM_PARAM(-1,'" + param + "',"+transformParams.get(param)+")";
            //  DBMS_METADATA.SESSION_TRANSFORM replaced by -1 because,
            // variables and constants in the package can only be accessed from the PL / SQL,
            // not from SQL as in my case.
            //(for oracle 10 it works)  //todo test for oracle 11
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.execute();
        }
    }

    /**
     * Test db connection
     * @return
     */
    public boolean connectionAvailable() {
        try {
            getJdbcTemplate().queryForInt("select 1 from dual");
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    public void setMap(Map<String, Set<String>> map) {
        this.map = map;
    }

    public void setTransformParams(Map<String, String> transformParams) {
        this.transformParams = transformParams;
    }

    public void setFilterTypes(Set<String> types) {
        this.filterTypes = types;
    }

     public int getLast_ddl_time_age() {
        return objectsAge;
    }

    public void setLast_ddl_time_age(int howLong) {
        this.objectsAge = howLong;
    }

    public void setExcludeMap(Map<String, Set<String>> excludeMap) {
        this.excludeMap = excludeMap;
    }
}
