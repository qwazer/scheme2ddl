package com.googlecode.scheme2ddl.dao;

/**
 * Check some properties of connection
 *
 * @author A_Reshetnikov
 * @since Date: 23.07.2013
 */
public interface ConnectionDao {

    boolean isConnectionAvailable();

    boolean hasSelectCatalogRole();
}
