package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.dao.UserObjectDao;
import com.googlecode.scheme2ddl.domain.UserObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.Set;

import static com.googlecode.scheme2ddl.TypeNamesUtil.map2TypeForDBMS;

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
        if (userObject.getType().equals("DBMS JOB")) {
            return userObjectDao.findDbmsJobDDL(userObject.getName());
        }
        if (userObject.getType().equals("PUBLIC DATABASE LINK")) {
            return userObjectDao.findDDLInPublicScheme(map2TypeForDBMS(userObject.getType()), userObject.getName());
        }
        String res = userObjectDao.findPrimaryDDL(map2TypeForDBMS(userObject.getType()), userObject.getName());
        Set<String> dependedTypes = dependencies.get(userObject.getType());
        if (dependedTypes != null) {
            for (String dependedType : dependedTypes) {
                res += userObjectDao.findDependentDLLByTypeName(dependedType, userObject.getName());
            }
        }
        return ddlFormatter.formatDDL(res);
    }


    private String map2FileName(UserObject userObject) {
        String res = map2FolderName(userObject.getType()) + "/" + userObject.getName() + ".sql";
        return res.toLowerCase();
    }

    private String map2FolderName(String type) {
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
