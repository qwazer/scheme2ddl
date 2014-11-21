package com.googlecode.scheme2ddl;

/**
 * Util for converting type names between 'package DBMS_METADATA' and 'table USER_OBJECTS' formats
 * @author A_Reshetnikov
 * @since Date: 30.04.2013
 */
public class TypeNamesUtil {

    /**
     * Oracle types in user_table without underscore, for example PACKAGE BODY
     * but in DBMS_METADATA with underscore   PACKAGE_BODY
     * @param type - type name from user_table and advanced config
     *
     * @return type name for using in  DBMS_METADATA package
     */
    public static String map2TypeForDBMS(String type) {
        if (type.contains("DATABASE LINK"))
            return "DB_LINK";
        if (type.equals("JOB"))
            return "PROCOBJ";
        return type.replace(" ", "_");
    }

    /**
     * Oracle types in user_table without underscore, for example PACKAGE BODY
     * but in DBMS_METADATA with underscore   PACKAGE_BODY
     * @param type - type name from DBMS_METADATA representation
     *
     * @return type name for using in  advanced config
     */
    public static String map2TypeForConfig(String type) {
        if (type.equals("DB_LINK"))
            return "DATABASE LINK";
        if (type.equals("PROCOBJ"))
            return "JOB";
        return type.replace("_", " ");
    }

}
