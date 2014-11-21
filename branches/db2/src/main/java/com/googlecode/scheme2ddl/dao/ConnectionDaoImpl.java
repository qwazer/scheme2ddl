package com.googlecode.scheme2ddl.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.util.List;

/**
 * @author A_Reshetnikov
 * @since Date: 23.07.2013
 */
public class ConnectionDaoImpl extends JdbcDaoSupport implements ConnectionDao {

    public boolean isConnectionAvailable() {
        try {
            getJdbcTemplate().queryForInt("select 1 from dual");
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    public boolean hasSelectCatalogRole() {
        List list = getJdbcTemplate().queryForList("select 1 from session_roles where role = 'SELECT_CATALOG_ROLE'");
        return  (list.size() == 1);
    }

}
