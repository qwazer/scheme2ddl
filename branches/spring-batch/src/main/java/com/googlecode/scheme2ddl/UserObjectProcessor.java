package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.dao.UserObjectDao;
import com.googlecode.scheme2ddl.domain.UserObject;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.Set;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class UserObjectProcessor implements ItemProcessor<UserObject, UserObject> {

    private UserObjectDao userObjectDao;
    private DDLFormatter ddlFormatter;
    private Map<String, Set<String>> excludes;
    private Map<String, Set<String>> dependencies;

    public UserObject process(UserObject userObject) throws Exception {

        if (needToExclude(userObject))
            return null;
        userObject.setDdl(map2Ddl(userObject));
        //  userObject.setFolderName(map2FolderName(userObject.getType()));
        userObject.setFileName(map2FileName(userObject));
        return userObject;
    }

    private boolean needToExclude(UserObject userObject) {
        if (excludes == null || excludes.size() == 0) return false;
        for (String typeName : excludes.keySet()) {
            if (typeName.equalsIgnoreCase(userObject.getType())) {
                if (excludes.get(typeName) == null) return true;
                for (String pattern : excludes.get(typeName)) {
                    if (matchesByPattern(userObject.getName(), pattern))
                        return true;
                }
            }
        }
        if (excludes.get("*") != null) {
            for (String pattern : excludes.get("*")) {
                if (matchesByPattern(userObject.getName(), pattern))
                    return true;
            }
        }
        return false;
    }

    private boolean matchesByPattern(String s, String pattern) {
        pattern = pattern.replace("*", "(.*)").toLowerCase();
        return s.toLowerCase().matches(pattern);
    }

    private String map2Ddl(UserObject userObject) {
        String res = "";
        res = userObjectDao.findPrimaryDDL(map2TypeForDBMS(userObject.getType()), userObject.getName());
        Set<String> dependedTypes = dependencies.get(userObject.getType());
        if (dependedTypes != null) {
            for (String dependedType : dependedTypes) {
                res += userObjectDao.findDependentDLLByTypeName(dependedType, userObject.getName());
            }
        }
        return ddlFormatter.formatDDL(res);
    }

    private String map2TypeForDBMS(String type) {
        return type.replace(" ", "_");
    }

    private String map2FileName(UserObject userObject) {
        return map2FolderName(userObject.getType()) + "/" + userObject.getName() + ".sql";
    }

    private String map2FolderName(String type) {
        type = type.toLowerCase().replaceAll(" ", "_");
        if (type.endsWith("x") || type.endsWith("s")) {
            return type + "es";
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
