package com.googlecode.scheme2ddl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.googlecode.scheme2ddl.domain.UserObject;

public class AlternateFileNameConstructor implements IFileNameConstructor, InitializingBean {
    public static final String KW_SCHEMA_LOWER = "%s";
    public static final String KW_SCHEMA_UPPER = "%S";
    public static final String KW_TYPE_LOWER = "%a";
    public static final String KW_TYPE_UPPER = "%A";
    public static final String KW_TYPES_PLURAL_LOWER = "%t";
    public static final String KW_TYPES_PLURAL_UPPER = "%T";
    public static final String KW_OBJECTNAME_LOWER = "%o";
    public static final String KW_OBJECTNAME_UPPER = "%O";
    public static final String KW_EXTENSION_LOWER = "%e";
    public static final String KW_EXTENSION_UPPER = "%E";

    public static final String TEMPLATEDEFAULT = "%t/%o.%e";

    private String template;
    private String templateForSysDBA = "%S/%t/%o.%e";
    private Map<String, String> extensionMap;
    private boolean combinePackage;
    private boolean needToReplaceWindowsReservedFileNames = false;

    private boolean sysDbaTmpl = false;

    @Resource
    private Map<String,String> windowsReservedNamesReplacements;

    @Override
    public void useSysDBATemplate() {
        this.sysDbaTmpl = true;
    }

    @Override
    public String map2FileName(UserObject userObject) {
        String filename = sysDbaTmpl ? templateForSysDBA : template;

        filename = filename.replace(KW_SCHEMA_LOWER, userObject.getSchema().toLowerCase());
        filename = filename.replace(KW_SCHEMA_UPPER, userObject.getSchema().toUpperCase());

        String typeName = abbreviate(userObject.getType()).replace(" ", "_");
        String typeNameBackup = typeName;
        if (combinePackage && typeName.equals("PACKAGE_BODY")) {
            typeName = "PACKAGE";
        }

        filename = filename.replace(KW_TYPES_PLURAL_LOWER, pluralize(typeName).toLowerCase());
        filename = filename.replace(KW_TYPES_PLURAL_UPPER, pluralize(typeName).toUpperCase());
        filename = filename.replace(KW_TYPE_LOWER, typeName.toLowerCase());
        filename = filename.replace(KW_TYPE_UPPER, typeName.toUpperCase());

        if (combinePackage) {
            typeName = typeNameBackup;
        }

        String userObjectName = userObject.getName();
        if (needToReplaceWindowsReservedFileNames && (windowsReservedNamesReplacements.get(userObjectName) != null)) {
            userObjectName = windowsReservedNamesReplacements.get(userObjectName);
        }

        filename = filename.replace(KW_OBJECTNAME_LOWER, userObjectName.toLowerCase());
        filename = filename.replace(KW_OBJECTNAME_UPPER, userObjectName.toUpperCase());

        String extension = extensionMap.get(typeName.toUpperCase());
        if (extension == null) {
            extension = extensionMap.get("DEFAULT");
            Assert.state(extension != null,
                    String.format("No file extension rule for type %s and no DEFAULT rule", typeName.toUpperCase()));
        }
        filename = filename.replace(KW_EXTENSION_LOWER, extension.toLowerCase());
        filename = filename.replace(KW_EXTENSION_UPPER, extension.toUpperCase());

        return filename;
    }

    @Override
    public void afterPropertiesSet() {
        if ((this.template == null) || this.template.isBlank()) {
            this.template = TEMPLATEDEFAULT;
        }
        if (extensionMap == null) {
            extensionMap = new HashMap<>();
            extensionMap.put("DEFAULT", "sql");
        }
        if (windowsReservedNamesReplacements == null) {
            needToReplaceWindowsReservedFileNames = false;
        }
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setTemplateForSysDBA(String templateForSysDBA) {
        this.templateForSysDBA = templateForSysDBA;
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

    private String abbreviate(String type) {
        type = type.replace("DATABASE", "DB");
        type = type.replace("database", "db");
        return type;
    }

    private String pluralize(String type) {
        type = type.toLowerCase();
        if (type.endsWith("x") || type.endsWith("s")) {
            return type + "es";
        } else if (type.endsWith("y")) {
            return type.substring(0, type.length() - 1) + "ies";
        } else {
            return type + "s";
        }
    }

}
