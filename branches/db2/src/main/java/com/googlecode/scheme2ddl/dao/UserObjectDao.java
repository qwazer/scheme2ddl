package com.googlecode.scheme2ddl.dao;

import com.googlecode.scheme2ddl.domain.Db2LookInfo;
import com.googlecode.scheme2ddl.domain.UserObject;

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

    List<UserObject> findConstaints();

    String findPrimaryDDL(String type, String name);

    List<Db2LookInfo> findDDL(UserObject userObject);

    String findDependentDLLByTypeName(String type, String name);

    String findDDLInPublicScheme(String type, String name);

    String findDbmsJobDDL(String name);

}
