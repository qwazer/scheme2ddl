/*
 *    Copyright (c) 2011 Reshetnikov Anton aka qwazer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.googlecode.scheme2ddl;

/**
 * Class for storing database user object
 * It's not simple bean, it also provide additional get method for file worker class
 * Date: 19.02.11
 * Time: 14:53
 */
public class UserObject {

    public UserObject(String type, String name, String ddl) {
        this.type = type;
        this.name = name;
        this.ddl = ddl;
    }


    public String getName() {
        return name;
    }

    /**
     * Get name for filename
     *
     * @return name for filename
     */
    public String getName4Filename() {
        return name.toLowerCase().replaceAll("/", "");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    /**
     * used if sortByDirectory = true
     * for creating directory names
     *
     * @return
     */
    public String getTypePlural() {
        String s = getType4DBMS().toLowerCase();
        if (s.endsWith("x") || s.endsWith("s")) {
            return s + "es";
        }
        return s + "s";
    }

    /**
     * Oracle types in user_table without underscore, for example PACKAGE BODY
     * but in DBMS_METADATA with underscore   PACKAGE_BODY
     *
     * @return type name foe using in  DBMS_METADATA package
     */
    public String getType4DBMS() {
        if (type.equalsIgnoreCase("DATABASE LINK"))
            return "DB_LINK";
        return type.replaceAll(" ", "_");
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

    String name;
    String type;
    String ddl;

    @Override
    public String toString() {
        return "UserObject{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", ddl='" + ddl + '\'' +
                '}';
    }
}
