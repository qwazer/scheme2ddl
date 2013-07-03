package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.domain.UserObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author A_Reshetnikov
 * @since Date: 01.05.2013
 */
public class FileNameConstructor implements InitializingBean {

    private static final Log log = LogFactory.getLog(FileNameConstructor.class);
    public static String kw_schema_lower = "schema";
    public static String kw_type_lower = "type";
    public static String kw_types_plural_lower = "types_plural";
    public static String kw_objectname_lower = "object_name";
    public static String kw_rowid = "row_id";
    public static String kw_extension_lower = "ext";
    public static String kw_schema_UPPER = "SCHEMA";
    public static String kw_type_UPPER = "TYPE";
    public static String kw_types_plural_UPPER = "TYPES_PLURAL";
    public static String kw_objectname_UPPER = "OBJECT_NAME";
    public static String kw_extension_UPPER = "EXT";
    public static String nonOracleChar = "%"; //char not used in Oracle names
    public static String templateDefault = "types_plural/object_name.ext";
    public static String templateDataLobDefault = "types_plural/object_name/row_id.ext";
    private String template;
    private String templateForSysDBA = "SCHEMA/types_plural/object_name.ext";
    private String templateDataLob;
    private String templateDataLobForSysDBA = "SCHEMA/types_plural/object_name/row_id.ext";
    private String preparedTemplate;
    private String preparedTemplateDataLob;
    private Map<String, String> extensionMap;

    /**
     * prepare template
     * replace keywords with %keyword
     *
     * @param template
     * @return
     */
    private static String prepareTemplate(String template) {
        String[] keywords = new String[]{
                kw_schema_lower, kw_schema_UPPER, kw_rowid,
                kw_types_plural_lower, kw_types_plural_UPPER,
                kw_objectname_lower, kw_objectname_UPPER,
                kw_extension_lower, kw_extension_UPPER};
        for (int i = 0; i < keywords.length; i++) {
            template = template.replace(keywords[i], nonOracleChar + keywords[i]);
        }
        // keyword kw_type_lower is substring of  kw_types_plural_lower so we need additional preparing
        String typesPluralTail = kw_types_plural_lower.replace(kw_type_lower, "");
        template = template.replaceAll(kw_type_lower + "(?!" + typesPluralTail + ")", nonOracleChar + kw_type_lower);
        typesPluralTail = kw_types_plural_UPPER.replace(kw_type_UPPER, "");
        template = template.replaceAll(kw_type_UPPER + "(?!S_PLURAL)", nonOracleChar + kw_type_UPPER);
        return template;
    }

    public static String abbreviate(String type) {
        type = type.replace("DATABASE", "DB");
        type = type.replace("database", "db");
        return type;
    }

    public static String pluralaze(String type) {
        type = type.toLowerCase();
        if (type.endsWith("x") || type.endsWith("s")) {
            return type + "es";
        }
        if (type.endsWith("y")) {
            return type.substring(0, type.length() - 1) + "ies";
        }
        return type + "s";
    }

    public String map2FileName(UserObject userObject) {
        return map2FileNameRaw(userObject.getSchema(), userObject.getType(), userObject.getName());
    }

    public String map2FileNameRaw(String schema, String type, String name) {
        String typeName = abbreviate(type).replace(" ", "_");
        String extension = extensionMap.get(typeName.toUpperCase());
        if (extension == null) {
            extension = extensionMap.get("DEFAULT");
            Assert.state(extension != null, String.format("No file extension rule for type %s and no DEFAULT rule", typeName.toUpperCase()));
        }

        return map2FileNameStatic(schema, type, name, preparedTemplate, null, extension);
    }

    public static String map2FileNameStatic(String schema, String type, String name, String rawTemplate, String rowid, String extension) {
        String filename = rawTemplate;

        filename = filename.replace(nonOracleChar + kw_schema_lower, schema.toLowerCase());
        filename = filename.replace(nonOracleChar + kw_schema_UPPER, schema.toUpperCase());

        String typeName = abbreviate(type).replace(" ", "_");

        //process kw_types_plural before kw_type
        filename = filename.replace(nonOracleChar + kw_types_plural_lower, pluralaze(typeName).toLowerCase());
        filename = filename.replace(nonOracleChar + kw_types_plural_UPPER, pluralaze(typeName).toUpperCase());

        filename = filename.replace(nonOracleChar + kw_type_lower, typeName.toLowerCase());
        filename = filename.replace(nonOracleChar + kw_type_UPPER, typeName.toUpperCase());

        filename = filename.replace(nonOracleChar + kw_objectname_lower, name.toLowerCase());
        filename = filename.replace(nonOracleChar + kw_objectname_UPPER, name.toUpperCase());

        if (rowid != null) {
            filename = filename.replace(nonOracleChar + kw_rowid, rowid);
        }

        filename = filename.replace(nonOracleChar + kw_extension_lower, extension.toLowerCase());
        filename = filename.replace(nonOracleChar + kw_extension_UPPER, extension.toUpperCase());

        return filename;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setTemplateDataLob(String templateDataLob) {
        this.templateDataLob = templateDataLob;
    }

    public String getTemplateForSysDBA() {
        return templateForSysDBA;
    }

    public String getTemplateDataLobForSysDBA() {
        return templateDataLobForSysDBA;
    }

    public String getPreparedTemplate() {
        return preparedTemplate;
    }

    public String getPreparedTemplateDataLob() {
        return preparedTemplateDataLob;
    }

    public void setTemplateForSysDBA(String templateForSysDBA) {
        this.templateForSysDBA = templateForSysDBA;
    }

    public void setTemplateDataLobForSysDBA(String templateDataLobForSysDBA) {
        this.templateDataLobForSysDBA = templateDataLobForSysDBA;
    }

    //for compability with old configs
    public void afterPropertiesSet() {
        String s;
        if (this.template == null) s = templateDefault;
        else s = template;
        preparedTemplate = prepareTemplate(s);

        String sLob;
        if (this.templateDataLob == null) sLob = templateDataLobDefault;
        else sLob = templateDataLob;
        preparedTemplateDataLob = prepareTemplate(sLob);

        if (extensionMap == null) {
            extensionMap = new HashMap<String, String>();
            extensionMap.put("DEFAULT", "sql");
        }
    }

    public void setExtensionMap(Map<String, String> extensionMap) {
        this.extensionMap = extensionMap;
    }
}
