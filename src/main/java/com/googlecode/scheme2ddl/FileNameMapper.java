package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.domain.UserObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Matcher;

/**
 * @author A_Reshetnikov
 * @since Date: 01.05.2013
 */
public class FileNameMapper {

    private static final Log log = LogFactory.getLog(FileNameMapper.class);
    public static String kw_schema_lower = "schema";
    public static String kw_type_lower = "type";
    public static String kw_types_plural_lower = "types_plural";
    public static String kw_objectname_lower = "object_name";
    public static String kw_extension_lower = "ext";
    public static String kw_schema_UPPER = "SCHEMA";
    public static String kw_type_UPPER = "TYPE";
    public static String kw_types_plural_UPPER = "TYPES_PLURAL";
    public static String kw_objectname_UPPER = "OBJECT_NAME";
    public static String kw_extension_UPPER = "EXT";
    public static String nonOracleChar = "%"; //char not user in Oracle names
    public static String patternDefault = "type/object_name.ext";
    private String pattern;

    /**
     * prepare for escaping
     *
     * @param pattern
     * @return
     */
    private static String preparePattern(String pattern) {
        String[] keywords = new String[]{
                kw_schema_lower, kw_schema_UPPER,
                kw_type_lower, kw_type_UPPER,
                kw_types_plural_lower, kw_types_plural_UPPER,
                kw_objectname_lower, kw_objectname_UPPER,
                kw_extension_lower, kw_extension_UPPER};
        for (int i = 0; i < keywords.length; i++) {
            pattern = pattern.replaceAll(keywords[i], nonOracleChar + keywords[i]);
        }
        return pattern;
    }

    public String map2FileName(UserObject userObject) {   //todo unit test
        String filename;
        if (pattern == null) filename = patternDefault;
        else filename = pattern;
        try {

            filename = preparePattern(filename);

            filename = filename.replaceAll(nonOracleChar + kw_schema_lower, userObject.getSchema().toLowerCase());
            filename = filename.replaceAll(nonOracleChar + kw_schema_UPPER, userObject.getSchema().toUpperCase());

            filename = filename.replaceAll(nonOracleChar + kw_type_lower, userObject.getType().toLowerCase());
            filename = filename.replaceAll(nonOracleChar + kw_type_UPPER, userObject.getType().toUpperCase());

            filename = filename.replaceAll(nonOracleChar + kw_types_plural_lower, map2FolderName(userObject.getType()).toLowerCase());
            filename = filename.replaceAll(nonOracleChar + kw_types_plural_UPPER, map2FolderName(userObject.getType()).toUpperCase());

            filename = filename.replaceAll(nonOracleChar + kw_objectname_lower, Matcher.quoteReplacement(userObject.getName().toLowerCase()));
            filename = filename.replaceAll(nonOracleChar + kw_objectname_UPPER, Matcher.quoteReplacement(userObject.getName().toUpperCase()));

            filename = filename.replaceAll(nonOracleChar + kw_extension_lower, "sql");
            filename = filename.replaceAll(nonOracleChar + kw_objectname_UPPER, "SQL");
        } catch (IllegalArgumentException e) {
            log.error(String.format("Error in processing %s", userObject), e);
        }


        return filename;
    }

    private String map2FolderName(String type) {   //todo rename
        if (type.equals("DATABASE LINK"))
            return "db_links";
        if (type.equals("PUBLIC DATABASE LINK"))
            return "public_db_links";
        type = type.toLowerCase().replaceAll(" ", "_");
        if (type.endsWith("x") || type.endsWith("s")) {
            return type + "es";
        }
        if (type.endsWith("y")) {
            return type.substring(0, type.length() - 1) + "ies";
        }
        return type + "s";
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
