package com.googlecode.scheme2ddl;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author ar
 * @since Date: 18.04.2015
 */
public class DDLFormatterTest {

    private DDLFormatter ddlFormatter = new DDLFormatter();

    @Test
    public void testReplaceActualSequenceValueWithOne() throws Exception {

        String s = "CREATE SEQUENCE  \"TEST01\".\"SEQ_01\"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 2122 CACHE 20 NOORDER  NOCYCLE ;\n";
        String res = ddlFormatter.replaceActualSequenceValueWithOne(s);
        assertEquals(
                "CREATE SEQUENCE  \"TEST01\".\"SEQ_01\"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;\n" +
                        "\r\n" +
                        "/* -- actual sequence value was replaced by scheme2ddl to 1 */"
                , res);

    }

    @Test
    public void testReplaceActualSequenceValueWithOneOnWrongDDL() throws Exception {

        String s = "CREATE TABLE  \"TEST01\".\"SEQ_01\"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 2122 CACHE 20 NOORDER  NOCYCLE ;\n";
        String res = ddlFormatter.replaceActualSequenceValueWithOne(s);
        assertNotEquals(
                "CREATE TABLE  \"TEST01\".\"SEQ_01\"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;\n"
                , res);

    }
}