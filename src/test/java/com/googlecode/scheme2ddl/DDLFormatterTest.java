package com.googlecode.scheme2ddl;


import org.testng.annotations.Test;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;


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


    @Test
    public void testSortPreserveOriginalDDLIfNoSort() {
        String s = "\n  CREATE UNIQUE INDEX \"HR\".\"REG_ID_PK\" ON \"HR\".\"REGIONS\" (\"REGION_ID\") \n" +
                "  ;";
        String res = ddlFormatter.sortIndexesInDDL(s);
        assertEquals(s, res);
    }

    @Test
    public void testSortIndexes() {
        String s = "  CREATE INDEX \"HR\".\"A_IDX2\" ON \"HR\".\"A\" (\"C2\") \n" +
                "  ;\n" +
                "  CREATE INDEX \"HR\".\"A_IDX1\" ON \"HR\".\"A\" (\"C3\") \n" +
                "  ;";
        String res = ddlFormatter.sortIndexesInDDL(s);
        assertEquals(
                "\n  CREATE INDEX \"HR\".\"A_IDX1\" ON \"HR\".\"A\" (\"C3\") \n" +
                        "  ;\n" +
                        "CREATE INDEX \"HR\".\"A_IDX2\" ON \"HR\".\"A\" (\"C2\") \n" +
                        "  ;"
                , res);
    }

    @Test
    public void testSortIndexesUniq() {
        String s =
                "  \n" +
                "CREATE UNIQUE INDEX \"HR\".\"A_UNIQ2\" ON \"HR\".\"A\" (\"C1\", \"B2\") \n" +
                "  ;\n" +
                "  CREATE INDEX \"HR\".\"A_IDX1\" ON \"HR\".\"A\" (\"C1\") \n" +
                "  ; " +
                "CREATE BITMAP INDEX \"HR\".\"A_BITMAP_IDX\" ON \"HR\".\"A\" (\"C1\", \"C5\") \n" +
                "  ;\n";
        String res = ddlFormatter.sortIndexesInDDL(s);
        assertEquals("\n  " +
                "CREATE BITMAP INDEX \"HR\".\"A_BITMAP_IDX\" ON \"HR\".\"A\" (\"C1\", \"C5\") \n" +
                "  ;\n" +
                "CREATE INDEX \"HR\".\"A_IDX1\" ON \"HR\".\"A\" (\"C1\") \n" +
                "  ;\n" +
                "CREATE UNIQUE INDEX \"HR\".\"A_UNIQ2\" ON \"HR\".\"A\" (\"C1\", \"B2\") \n" +
                "  ;", res);
    }
}