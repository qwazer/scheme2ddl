package com.googlecode.scheme2ddl.domain;

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
    private long opToken;


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

    public long getOpToken() {
        return opToken;
    }

    public void setOpToken(long opToken) {
        this.opToken = opToken;
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
