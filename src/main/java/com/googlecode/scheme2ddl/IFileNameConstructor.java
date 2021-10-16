package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.domain.UserObject;

public interface IFileNameConstructor {
    /** Switch to SYSDBA naming scheme */
    void useSysDBATemplate();
    /** Returns filename based on UserObject */
    String map2FileName(UserObject userObject);
}
