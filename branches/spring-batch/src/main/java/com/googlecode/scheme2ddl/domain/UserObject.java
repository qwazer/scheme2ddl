package com.googlecode.scheme2ddl.domain;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class UserObject {

    private String name;
    private String type;
    private String type4DBMS; //todo add mapping
    private boolean specialObject; //todo?
    private String ddl;
    private String dependedDdl;    //todo
    private String folderName;   //todo replace by fileName
    private String fileName;     //todo


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDdl() {
        return ddl;
    }

    public void setDdl(String ddl) {
        this.ddl = ddl;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDependedDdl() {
        return dependedDdl;
    }

    public void setDependedDdl(String dependedDdl) {
        this.dependedDdl = dependedDdl;
    }

    @Override
    public String toString() {
        return "UserObject{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", ddl='" + ddl + '\'' +
                ", folderName='" + folderName + '\'' +
                '}';
    }
}
