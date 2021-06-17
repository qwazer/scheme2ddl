package com.googlecode.scheme2ddl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.googlecode.scheme2ddl.domain.UserObject;

/**
 * @author A_Reshetnikov
 * @since Date: 01.05.2013
 */
public class FileNameConstructor implements IFileNameConstructor, InitializingBean {
    public static final String KW_SCHEMA_LOWER = "schema";
    public static final String KW_TYPE_LOWER = "type";
    public static final String KW_TYPES_PLURAL_LOWER = "types_plural";
    public static final String KW_OBJECTNAME_LOWER = "object_name";
    public static final String KW_EXTENSION_LOWER = "ext";
    public static final String KW_SCHEMA_UPPER = "SCHEMA";
    public static final String KW_TYPE_UPPER = "TYPE";
    public static final String KW_TYPES_PLURAL_UPPER = "TYPES_PLURAL";
    public static final String KW_OBJECTNAME_UPPER = "OBJECT_NAME";
    public static final String KW_EXTENSION_UPPER = "EXT";
    public static final String NONORACLECHAR = "%"; //char not used in Oracle names
    public static final String TEMPLATEDEFAULT = "types_plural/object_name.ext";

    private String template;
    private String templateForSysDBA = "SCHEMA/types_plural/object_name.ext";
    private String preparedTemplate;
    private Map<String, String> extensionMap;
    private boolean combinePackage;

    private boolean needToReplaceWindowsReservedFileNames = false;

    @Resource
    private Map<String,String> windowsReservedNamesReplacements;

    /**
     * prepare template
     * replace keywords with %keyword
     *
     * @param template
     * @return
     */
    private static String prepareTemplate(String template) {
        var keywords = new String[]{
                KW_SCHEMA_LOWER, KW_SCHEMA_UPPER,
                KW_TYPES_PLURAL_LOWER, KW_TYPES_PLURAL_UPPER,
                KW_OBJECTNAME_LOWER, KW_OBJECTNAME_UPPER,
                KW_EXTENSION_LOWER, KW_EXTENSION_UPPER};
        for (int i = 0; i < keywords.length; i++) {
            template = template.replace(keywords[i], NONORACLECHAR + keywords[i]);
        }
        // keyword kw_type_lower is substring of  kw_types_plural_lower so we need additional preparing
        String typesPluralTail = KW_TYPES_PLURAL_LOWER.replace(KW_TYPE_LOWER, "");
        template = template.replaceAll(KW_TYPE_LOWER + "(?!" + typesPluralTail + ")", NONORACLECHAR + KW_TYPE_LOWER);
        typesPluralTail = KW_TYPES_PLURAL_UPPER.replace(KW_TYPE_UPPER, "");
        template = template.replaceAll(KW_TYPE_UPPER + "(?!S_PLURAL)", NONORACLECHAR + KW_TYPE_UPPER);
        return template;
    }

    @Override
    public void useSysDBATemplate() {
        template = templateForSysDBA;
        afterPropertiesSet();
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
        String filename = preparedTemplate;

        filename = filename.replace(NONORACLECHAR + KW_SCHEMA_LOWER, userObject.getSchema().toLowerCase());
        filename = filename.replace(NONORACLECHAR + KW_SCHEMA_UPPER, userObject.getSchema().toUpperCase());

        String typeName = abbreviate(userObject.getType()).replace(" ", "_");
		
		String typeName_backup = typeName;
		if (combinePackage && typeName.equals("PACKAGE_BODY")) {
			typeName = "PACKAGE";
		}

        //process kw_types_plural before kw_type
        filename = filename.replace(NONORACLECHAR + KW_TYPES_PLURAL_LOWER, pluralaze(typeName).toLowerCase());
        filename = filename.replace(NONORACLECHAR + KW_TYPES_PLURAL_UPPER, pluralaze(typeName).toUpperCase());

        filename = filename.replace(NONORACLECHAR + KW_TYPE_LOWER, typeName.toLowerCase());
        filename = filename.replace(NONORACLECHAR + KW_TYPE_UPPER, typeName.toUpperCase());

		if (combinePackage) {
			typeName = typeName_backup;
		}

        String userObjectName = userObject.getName();

        if (needToReplaceWindowsReservedFileNames){
            if (windowsReservedNamesReplacements.get(userObjectName) != null){
                userObjectName = windowsReservedNamesReplacements.get(userObjectName);
            }
        }

        filename = filename.replace(NONORACLECHAR + KW_OBJECTNAME_LOWER, userObjectName.toLowerCase());
        filename = filename.replace(NONORACLECHAR + KW_OBJECTNAME_UPPER, userObjectName.toUpperCase());

        String extension = extensionMap.get(typeName.toUpperCase());
        if (extension == null) {
            extension = extensionMap.get("DEFAULT");
            Assert.state(extension != null, String.format("No file extension rule for type %s and no DEFAULT rule", typeName.toUpperCase()));
        }
        filename = filename.replace(NONORACLECHAR + KW_EXTENSION_LOWER, extension.toLowerCase());
        filename = filename.replace(NONORACLECHAR + KW_EXTENSION_UPPER, extension.toUpperCase());

        return filename;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getTemplateForSysDBA() {
        return templateForSysDBA;
    }

    public void setTemplateForSysDBA(String templateForSysDBA) {
        this.templateForSysDBA = templateForSysDBA;
    }

    //for compability with old configs
    public void afterPropertiesSet() {
        preparedTemplate = prepareTemplate(this.template == null ? TEMPLATEDEFAULT : template);

        if (extensionMap == null) {
            extensionMap = new HashMap<>();
            extensionMap.put("DEFAULT", "sql");
        }


        if (windowsReservedNamesReplacements == null){
           needToReplaceWindowsReservedFileNames=false;

        }
    }

    public void setExtensionMap(Map<String, String> extensionMap) {
        this.extensionMap = extensionMap;
    }
	
	public void setCombinePackage(boolean combinePackage) {
        this.combinePackage = combinePackage;
    }

    public void setNeedToReplaceWindowsReservedFileNames(boolean needToReplaceWindowsReservedFileNames) {
        this.needToReplaceWindowsReservedFileNames = needToReplaceWindowsReservedFileNames;
    }
}
