package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.dao.UserObjectDao;
import com.googlecode.scheme2ddl.domain.UserObject;
import com.googlecode.scheme2ddl.exception.CannotGetDDLException;
import com.googlecode.scheme2ddl.exception.NonSkippableException;
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
    private IFileNameConstructor fileNameConstructor;
    private Map<String, Set<String>> excludes;
    private Map<String, Set<String>> dependencies;
    private boolean stopOnWarning;
    private boolean replaceSequenceValues;

    public UserObject process(UserObject userObject) throws Exception {

        if (needToExclude(userObject)) {
            log.debug(String.format("Skipping processing of user object %s ", userObject));
            return null;
        }
        userObject.setDdl(map2Ddl(userObject));
        userObject.setFileName(fileNameConstructor.map2FileName(userObject));
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

    private String map2Ddl(UserObject userObject) throws CannotGetDDLException, NonSkippableException {
        try {
            if (userObject.getType().equals("DBMS JOB")) {
                return ddlFormatter.formatDDL(userObjectDao.findDbmsJobDDL(userObject.getName()));
            }
            if (userObject.getType().equals("PUBLIC DATABASE LINK")) {
                return ddlFormatter.formatDDL(userObjectDao.findDDLInPublicScheme(map2TypeForDBMS(userObject.getType()), userObject.getName()));
            }
			if (userObject.getType().equals("REFRESH_GROUP")) {
                return ddlFormatter.formatDDL(userObjectDao.findRefGroupDDL(userObject.getType(), userObject.getName()));
            }
            StringBuilder res = new StringBuilder(userObjectDao.findPrimaryDDL(map2TypeForDBMS(userObject.getType()), userObject.getName()));
            if (userObject.getType().equals("SEQUENCE") && replaceSequenceValues) {
                res = new StringBuilder(ddlFormatter.replaceActualSequenceValueWithOne(res.toString()));
            }
            Set<String> dependedTypes = dependencies.get(userObject.getType());
            if (dependedTypes != null) {
                for (String dependedType : dependedTypes) {
                    String dependentDLL = userObjectDao.findDependentDLLByTypeName(dependedType, userObject.getName());
                    if (dependedType.equals("INDEX")){
                        dependentDLL = ddlFormatter.sortIndexesInDDL(dependentDLL);
                    }
                    res.append(dependentDLL);
                }
            }
            return ddlFormatter.formatDDL(res.toString());
        } catch (Exception e) {
            log.warn(String.format("Cannot get DDL for object %s with error message %s", userObject, e.getMessage()));
            if (stopOnWarning) {
                throw new NonSkippableException(e);
            } else
                throw new CannotGetDDLException(e);
        }

    }

    public void setExcludes(Map excludes) {
        this.excludes = excludes;
    }

    public void setDependencies(Map dependencies) {
        this.dependencies = dependencies;
    }

    public void setUserObjectDao(UserObjectDao userObjectDao) {
        this.userObjectDao = userObjectDao;
    }

    public void setDdlFormatter(DDLFormatter ddlFormatter) {
        this.ddlFormatter = ddlFormatter;
    }

    public void setFileNameConstructor(IFileNameConstructor fileNameConstructor) {
        this.fileNameConstructor = fileNameConstructor;
    }

    public void setReplaceSequenceValues(boolean replaceSequenceValues) {
        this.replaceSequenceValues = replaceSequenceValues;
    }

    public void setStopOnWarning(boolean stopOnWarning) {
        this.stopOnWarning = stopOnWarning;
    }
}
