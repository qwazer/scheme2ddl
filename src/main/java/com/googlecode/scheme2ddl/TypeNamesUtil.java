package com.googlecode.scheme2ddl;

/**
 * Util for converting type names between 'package DBMS_METADATA' and 'table USER_OBJECTS' formats
 *
 * @author A_Reshetnikov
 * @since Date: 30.04.2013
 */
public class TypeNamesUtil {

  /**
   * Oracle types in user_table without underscore, for example PACKAGE BODY
   * but in DBMS_METADATA with underscore   PACKAGE_BODY
   *
   * @param type - type name from user_table and advanced config
   * @return type name for using in  DBMS_METADATA package
   */
  public static String map2TypeForDBMS(String type) {
    if (type.contains("DATABASE LINK"))
      return "DB_LINK";
    if (type.equals("JOB"))
      return "PROCOBJ";
    if (type.equals("SCHEDULE"))
      return "PROCOBJ";
    if (type.equals("PROGRAM"))
      return "PROCOBJ";
    if (type.equals("PACKAGE"))
      return "PACKAGE_SPEC";

    if (type.toUpperCase().contains("SUBPARTITION")) {
      return type.replaceAll("SUBPARTITION", "").trim();
    }

    if (type.toUpperCase().contains("PARTITION")) {
      return type.replaceAll("PARTITION", "").trim();
    }

    return type.replace(" ", "_");
  }


}
