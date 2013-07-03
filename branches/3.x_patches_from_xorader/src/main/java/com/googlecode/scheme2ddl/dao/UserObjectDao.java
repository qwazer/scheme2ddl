package com.googlecode.scheme2ddl.dao;

import com.googlecode.scheme2ddl.domain.UserObject;
import com.googlecode.scheme2ddl.FileNameConstructor;

import java.util.Collection;
import java.util.List;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public interface UserObjectDao {

    List<UserObject> findListForProccessing();

    List<UserObject> findPublicDbLinks();

    List<UserObject> findDmbsJobs();

    List<UserObject> addUser();

    String findPrimaryDDL(String type, String name);

    String findDependentDLLByTypeName(String type, String name);

    String findDDLInPublicScheme(String type, String name);

    String findDbmsJobDDL(String name);

    String generateUserDDL(String name);

    void exportDataTable(UserObject userObject, int maxRowsExport, FileNameConstructor fileNameConstructor);

    boolean isConnectionAvailable();

}
