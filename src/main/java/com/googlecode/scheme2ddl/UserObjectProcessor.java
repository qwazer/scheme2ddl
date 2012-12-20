package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.dao.UserObjectDao;
import com.googlecode.scheme2ddl.domain.UserObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.Set;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class UserObjectProcessor implements ItemProcessor<UserObject, UserObject> {

    private static final Log log = LogFactory.getLog(UserObjectProcessor.class);
    private UserObjectDao userObjectDao;
    private DDLFormatter ddlFormatter;
    private Map<String, Set<String>> excludes;
    private Map<String, Set<String>> dependencies;

    public UserObject process(UserObject userObject) throws Exception {

        if (needToExclude(userObject)) {
            log.debug(String.format("Skipping processing of user object %s ", userObject));
            return null;
        }
        userObject.setDdl(map2Ddl(userObject));
        userObject.setFileName(map2FileName(userObject));
        return userObject;
    }

    private boolean needToExclude(UserObject userObject) {
        if (excludes == null || excludes.size() == 0) return false;
        if (excludes.get("*") != null) {
            for (String pattern : excludes.get("*")) {
                if (matchesByPattern(userObject.getName(), pattern))
                    return true;
            }
        }
        for (String typeName : excludes.keySet()) {
            if (typeName.equalsIgnoreCase(userObject.getType())) {
                if (excludes.get(typeName) == null) return true;
                for (String pattern : excludes.get(typeName)) {
                    if (matchesByPattern(userObject.getName(), pattern))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean matchesByPattern(String s, String pattern) {
        pattern = pattern.replace("*", "(.*)").toLowerCase();
        return s.toLowerCase().matches(pattern);
    }

    private String map2Ddl(UserObject userObject) {
        String type = userObject.getType();
        if (type.equals("DBMS JOB")) {
            boolean asSysDba = userObject.getSchema() != null;
            return userObjectDao.findDbmsJobDDL(userObject.getName(), asSysDba);
        }
        if (type.equals("PUBLIC DATABASE LINK")) {
            return userObjectDao.findDDLInPublicScheme(map2TypeForDBMS(type), userObject.getName());
        }
        String res = userObjectDao.findPrimaryDDL(map2TypeForDBMS(type), userObject.getName(), userObject.getSchema());
        Set<String> dependedTypes = dependencies.get(type);
        if (dependedTypes != null) {
            for (String dependedType : dependedTypes) {
                if (ddlFormatter.getIsMorePrettyFormat())
                    res += ddlFormatter.newline;
                res += userObjectDao.findDependentDLLByTypeName(dependedType, userObject.getName(), userObject.getSchema());
            }
        }
        return ddlFormatter.formatDDL(res);
    }

    /**
     * Oracle types in user_table without underscore, for example PACKAGE BODY
     * but in DBMS_METADATA with underscore   PACKAGE_BODY
     *
     * @return type name for using in  DBMS_METADATA package
     */
    private String map2TypeForDBMS(String type) {
        if (type.contains("DATABASE LINK"))
            return "DB_LINK";
        if (type.equals("JOB"))
            return "PROCOBJ";
        return type.replace(" ", "_");
    }

    private String map2FileName(UserObject userObject) {
        String res = map2FolderName(userObject.getType()) + "/" + userObject.getName() + ".sql";
        String schema = userObject.getSchema();
        if (schema != null)
            res = schema + "/" + res;
        if (ddlFormatter.getIsFilenameToLowerCase())
            return res.toLowerCase();
        else
            return res;
    }

    private String map2FolderName(String type) {
        if (type.equals("DATABASE LINK")) {
            if (ddlFormatter.getIsFilenameToLowerCase())
                return "db_links";
            else
                return "DATABASE_LINKS";
        }
        if (type.equals("PUBLIC DATABASE LINK")) {
            if (ddlFormatter.getIsFilenameToLowerCase())
                return "public_db_links";
            else
                return "PUBLIC_DATABASE_LINKS";
        }
        type = type.toLowerCase().replaceAll(" ", "_");
        if (type.endsWith("x") || type.endsWith("s")) {
            type += "es";
        } else if (type.endsWith("y")) {
            type = type.substring(0, type.length() - 1) + "ies";
        } else {
            type += "s";
        }

        if (ddlFormatter.getIsFilenameToLowerCase())
            return type;
        else
            return type.toUpperCase();
    }

    public void setExcludes(Map excludes) {
        this.excludes = excludes;
    }

    public void setDependencies(Map<String, Set<String>> dependencies) {
        this.dependencies = dependencies;
    }

    public void setUserObjectDao(UserObjectDao userObjectDao) {
        this.userObjectDao = userObjectDao;
    }

    public void setDdlFormatter(DDLFormatter ddlFormatter) {
        this.ddlFormatter = ddlFormatter;
    }
}
