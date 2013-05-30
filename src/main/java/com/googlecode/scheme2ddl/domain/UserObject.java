package com.googlecode.scheme2ddl.domain;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class UserObject {

    private String name;
    private String type;
    private String schema;
    private String ddl;
    private String fileName;
    /* DDL strings and filenames for dependencies */
    private Map<String, String> dependentDDLs;      /* DEPEND_SQL_TYPE : DDL */
    private Map<String, String> dependentFilenames; /* DEPEND_SQL_TYPE : FILENAME */


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getSchema() {
        return schema;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getDdl() {
        return ddl;
    }

    public void setDdl(String ddl) {
        this.ddl = ddl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDependentDDL(String dependentType, String ddl, String filename) {
        if (dependentDDLs == null) {
            dependentDDLs = new HashMap();
        }
        if (dependentFilenames == null) {
            dependentFilenames = new HashMap();
        }
        dependentDDLs.put(dependentType, ddl);
        dependentFilenames.put(dependentType, filename);
    }

    public String getDependentDDL(String dependentType) {
        return dependentDDLs.get(dependentType);
    }

    public String getDependentFilename(String dependentType) {
        return dependentFilenames.get(dependentType);
    }

    public Set<String> getDependentDDLsKeySet() {
        if (dependentDDLs == null) {
            return (new HashSet<String>());
        }

        return dependentDDLs.keySet();
    }

    @Override
    public String toString() {
        return "UserObject{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", schema='" + schema + '\'' +
                ", ddl='" + ddl + '\'' +
                '}';
    }
}
