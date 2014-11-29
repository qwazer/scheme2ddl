package com.googlecode.scheme2ddl.domain;

import java.sql.Clob;

/**
 * Object to represent some fields of SYSTOOLS.DB2LOOK_INFO table in DB2
 * @author ar
 * @since Date: 29.11.2014
 */
public class Db2LookInfo {

    /*
    CREATE TABLE DB2LOOK_INFO
(
    OP_TOKEN INT NOT NULL,
    OP_SEQUENCE INT NOT NULL,
    SQL_OPERATION VARCHAR(129) NOT NULL,
    OBJ_ID INT,
    OBJ_TYPE VARCHAR(129) NOT NULL,
    OBJ_SUBTYPE CHAR(1),
    OBJ_SCHEMA VARCHAR(129),
    OBJ_NAME VARCHAR(129) NOT NULL,
    OBJ_DEFINER VARCHAR(129),
    OBJ_QUALIFIER VARCHAR(129),
    OBJ_ATTRIBUTE VARCHAR(129),
    RELATED_OBJ_INFO VARCHAR(254),
    CREATION_TIME TIMESTAMP,
    SQL_STMT CLOB(2097152) NOT NULL
);
CREATE UNIQUE INDEX DB2LK_SEQUENCE_IND ON DB2LOOK_INFO (OP_TOKEN, OP_SEQUENCE, CREATION_TIME);
CREATE UNIQUE INDEX DB2LOOK_IND ON DB2LOOK_INFO (OP_TOKEN, SQL_OPERATION, OBJ_TYPE, OBJ_SCHEMA, OBJ_NAME);

     */

    private long opToken;
    private long opSequence;
    private String objSchema;
    private String objName;
    private String objType;
    private String sqlOperation;
    private String sqlStmt;
    private Clob sqlStmtClob;

    public long getOpToken() {
        return opToken;
    }

    public void setOpToken(long opToken) {
        this.opToken = opToken;
    }

    public long getOpSequence() {
        return opSequence;
    }

    public void setOpSequence(long opSequence) {
        this.opSequence = opSequence;
    }

    public String getObjSchema() {
        return objSchema;
    }

    public void setObjSchema(String objSchema) {
        this.objSchema = objSchema;
    }

    public String getObjName() {
        return objName;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

    public String getObjType() {
        return objType;
    }

    public void setObjType(String objType) {
        this.objType = objType;
    }

    public String getSqlOperation() {
        return sqlOperation;
    }

    public void setSqlOperation(String sqlOperation) {
        this.sqlOperation = sqlOperation;
    }

    public String getSqlStmt() {
        return sqlStmt;
    }

    public void setSqlStmt(String sqlStmt) {
        this.sqlStmt = sqlStmt;
    }

    public Clob getSqlStmtClob() {
        return sqlStmtClob;
    }

    public void setSqlStmtClob(Clob sqlStmtClob) {
        this.sqlStmtClob = sqlStmtClob;
    }

    @Override
    public String toString() {
        return "Db2LookInfo{" +
                "opToken=" + opToken +
                ", opSequence=" + opSequence +
                ", objSchema=" + objSchema +
                ", objName=" + objName +
                ", objType=" + objType +
                ", sqlOperation='" + sqlOperation + '\'' +
                ", sqlStmt='" + sqlStmt + '\'' +
                '}';
    }
}
