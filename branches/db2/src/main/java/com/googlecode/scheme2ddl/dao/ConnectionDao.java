package com.googlecode.scheme2ddl.dao;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Check some properties of connection
 *
 * @author A_Reshetnikov
 * @since Date: 23.07.2013
 */
public interface ConnectionDao {

    boolean isConnectionAvailable();

    boolean hasSelectCatalogRole();

    List<String> findAvailableSchemas();
}
