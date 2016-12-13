package com.googlecode.scheme2ddl;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author A_Reshetnikov
 * @since Date: 17.09.2016
 */

//@SpringBootTest(classes = ConfigurationIT.class, properties = "test-default.properties")
public class MainIT extends BaseIT {



    @BeforeClass
    public void setUp()  {
        try {
            dbaJdbcTemplate.execute("ALTER USER HR ACCOUNT UNLOCK IDENTIFIED BY pass");
        }
        catch (CannotGetJdbcConnectionException e){
            logger.warn("Ignore all test due", e);
            throw new SkipException("Ignore all test due " +  e.getMessage());
        }

    }




    @DataProvider
    public static Object[][] justTestConnectionParamNames() {
        return new Object[][]{
                {"-url", "-tc"},
                {"-url", "--test-connection"},
        };
    }


    @Test(dataProvider = "justTestConnectionParamNames")
    public void testJustTestConnectionPositive(String urlParamName, String testConnParamName) throws Exception {
        String[] args = {urlParamName, url, testConnParamName};
        Main.main(args);
        Assert.assertEquals(
                outContent.toString(),
                "OK success connection to jdbc:oracle:thin:" + url + "\n"
        );
    }

    @Test(expectedExceptions = CannotGetJdbcConnectionException.class)
    public void testJustTestConnectionNegative() throws Exception {
        String[] args = {"-url", "1/1@127.0.0.1:789789", "-tc"};
        Main.main(args);
    }



    @Test
    public void testExportHRSchemaDefault() throws Exception {
        String[] args = {"-url", url};
        Main.main(args);
        assertHRSchemaDefault(
                FileUtils.getFile(new File("output")).getAbsolutePath(),
                outContent.toString());
    }

    private static void assertHRSchemaDefault(String dirPath, String out) throws IOException {
        assertThat(out, containsString("Will try to process schema  [HR]"));
        assertThat(out, containsString("Start getting of user object list in schema HR for processing"));
        assertThat(out, containsString("WARNING: processing of 'PUBLIC DATABASE LINK' will be skipped because HR no access to view it"));
        assertThat(out, containsString("Found 34 items for processing in schema HR"));
        assertThat(out, containsString(String.format("Saved sequence hr.locations_seq to file %s/sequences/locations_seq.sql", dirPath)));
        assertThat(out, containsString(String.format("Saved sequence hr.employees_seq to file %s/sequences/employees_seq.sql", dirPath)));
        assertThat(out, containsString(String.format("Saved trigger hr.update_job_history to file %s/triggers/update_job_history.sql", dirPath)));
        assertThat(out, containsString(String.format("Saved procedure hr.add_job_history to file %s/procedures/add_job_history.sql", dirPath)));
        assertThat(out, containsString(String.format("Saved table hr.locations to file %s/tables/locations.sql", dirPath)));
        assertThat(out, containsString(String.format("Saved procedure hr.secure_dml to file %s/procedures/secure_dml.sql", dirPath)));
        assertThat(out, containsString(String.format("Saved view hr.emp_details_view to file %s/views/emp_details_view.sql", dirPath)));


        assertThat(out, containsString(
                "-------------------------------------------------------\n" +
                "   R E P O R T     S K I P P E D     O B J E C T S     \n" +
                "-------------------------------------------------------\n" +
                "| skip rule |  object type              |    count    |\n" +
                "-------------------------------------------------------\n" +
                "|  config   |  INDEX                    |      19     |"));


        assertThat(out, containsString("Written 15 ddls with user objects from total 34 in schema HR"));
        assertThat(out, containsString("Skip processing 19 user objects from total 34 in schema HR"));
        assertThat(out, containsString("scheme2ddl of schema HR completed"));


        assertEqualsFileContent(dirPath + "/sequences/locations_seq.sql", "CREATE SEQUENCE  \"HR\".\"LOCATIONS_SEQ\"" +
                "  MINVALUE 1 MAXVALUE 9900 INCREMENT BY 100 START WITH 3300 NOCACHE  NOORDER  NOCYCLE ;");


        assertEqualsFileContent(dirPath + "/tables/regions.sql", "CREATE TABLE \"HR\".\"REGIONS\" \n" +
                "   (\t\"REGION_ID\" NUMBER CONSTRAINT \"REGION_ID_NN\" NOT NULL ENABLE, \n" +
                "\t\"REGION_NAME\" VARCHAR2(25)\n" +
                "   ) ;\n" +
                "  ALTER TABLE \"HR\".\"REGIONS\" ADD CONSTRAINT \"REG_ID_PK\" PRIMARY KEY (\"REGION_ID\") ENABLE;\n" +
                "  CREATE UNIQUE INDEX \"HR\".\"REG_ID_PK\" ON \"HR\".\"REGIONS\" (\"REGION_ID\") \n" +
                "  ;");

        assertEqualsFileContent(dirPath + "/triggers/secure_employees.sql",
                "CREATE OR REPLACE TRIGGER \"HR\".\"SECURE_EMPLOYEES\" \n" +
                "  BEFORE INSERT OR UPDATE OR DELETE ON employees\n" +
                "BEGIN\n" +
                "  secure_dml;\n" +
                "END secure_employees;\n" +
                "/\n" +
                "ALTER TRIGGER \"HR\".\"SECURE_EMPLOYEES\" DISABLE;");
    }


    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "Cannot process schema \'PUBLIC\' with oracle user \'hr\', " +
                    "if it\'s not connected as sysdba")
    public void testProcessForeignSchemaNegative() throws Exception {
        String[] args = {"-url", url, "-s", "PUBLIC"};
        Main.main(args);
    }

    @Test
    public void testProcessForeignSchema() throws Exception {
        String outputPath = tempOutput.getAbsolutePath();

        String[] args = {"-url", dbaUrl, "-s", "HR,OUTLN", "-o", outputPath};

        Main.main(args);
        String out = outContent.toString();

        assertThat(out, containsString("Will try to process schema list [HR, OUTLN]"));
        assertThat(out, containsString("Found 34 items for processing in schema HR"));
        assertThat(out, containsString("Found 8 items for processing in schema OUTLN"));

        assertEqualsFileContent(outputPath + "/OUTLN/procedures/ora$grant_sys_select.sql",
                "CREATE OR REPLACE PROCEDURE \"OUTLN\".\"ORA$GRANT_SYS_SELECT\" as\n" +
                "begin\n" +
                "  EXECUTE IMMEDIATE 'GRANT SELECT ON OUTLN.OL$ TO SELECT_CATALOG_ROLE';\n" +
                "  EXECUTE IMMEDIATE 'GRANT SELECT ON OUTLN.OL$HINTS TO SELECT_CATALOG_ROLE';\n" +
                "  EXECUTE IMMEDIATE 'GRANT SELECT ON OUTLN.OL$NODES TO SELECT_CATALOG_ROLE';\n" +
                "  EXECUTE IMMEDIATE 'GRANT SELECT ON OUTLN.OL$ TO SYS WITH GRANT OPTION';\n" +
                "  EXECUTE IMMEDIATE 'GRANT SELECT ON OUTLN.OL$HINTS TO SYS WITH GRANT OPTION';\n" +
                "  EXECUTE IMMEDIATE 'GRANT SELECT ON OUTLN.OL$NODES TO SYS WITH GRANT OPTION';\n" +
                "end;\n" +
                "/");


        assertEqualsFileContent(outputPath +"/HR/tables/regions.sql",
                "CREATE TABLE \"HR\".\"REGIONS\" \n" +
                "   (\t\"REGION_ID\" NUMBER CONSTRAINT \"REGION_ID_NN\" NOT NULL ENABLE, \n" +
                "\t\"REGION_NAME\" VARCHAR2(25)\n" +
                "   ) ;\n" +
                "  ALTER TABLE \"HR\".\"REGIONS\" ADD CONSTRAINT \"REG_ID_PK\" PRIMARY KEY (\"REGION_ID\") ENABLE;\n" +
                "  CREATE UNIQUE INDEX \"HR\".\"REG_ID_PK\" ON \"HR\".\"REGIONS\" (\"REGION_ID\") \n" +
                "  ;");

    }

    @Test
    public void testProcessForeignSchemaParallel() throws Exception {
        String outputPath = tempOutput.getAbsolutePath();

        String[] args = {"-url", dbaUrl, "--schemas", "OUTLN", "--output", outputPath, "--parallel", "2"};

        Main.main(args);
        String out = outContent.toString();

        assertThat(out, containsString("Will try to process schema  [OUTLN]"));
        assertThat(out, containsString("Found 8 items for processing in schema OUTLN"));

        assertEqualsFileContent(outputPath + "/OUTLN/procedures/ora$grant_sys_select.sql",
                "CREATE OR REPLACE PROCEDURE \"OUTLN\".\"ORA$GRANT_SYS_SELECT\" as\n" +
                "begin\n" +
                "  EXECUTE IMMEDIATE 'GRANT SELECT ON OUTLN.OL$ TO SELECT_CATALOG_ROLE';\n" +
                "  EXECUTE IMMEDIATE 'GRANT SELECT ON OUTLN.OL$HINTS TO SELECT_CATALOG_ROLE';\n" +
                "  EXECUTE IMMEDIATE 'GRANT SELECT ON OUTLN.OL$NODES TO SELECT_CATALOG_ROLE';\n" +
                "  EXECUTE IMMEDIATE 'GRANT SELECT ON OUTLN.OL$ TO SYS WITH GRANT OPTION';\n" +
                "  EXECUTE IMMEDIATE 'GRANT SELECT ON OUTLN.OL$HINTS TO SYS WITH GRANT OPTION';\n" +
                "  EXECUTE IMMEDIATE 'GRANT SELECT ON OUTLN.OL$NODES TO SYS WITH GRANT OPTION';\n" +
                "end;\n" +
                "/");
    }

    @Test
    public void testFilterAndReplaceSeqValue() throws Exception {
        String outputPath = tempOutput.getAbsolutePath();

        String[] args = {"-url", url, "-f", "LOCATIONS_SEQ", "-o", outputPath};
        Main.main(args);

        String out = outContent.toString();
        assertThat(out, containsString("Found 1 items for processing in schema HR"));

        assertEqualsFileContent(outputPath + "/sequences/locations_seq.sql", "CREATE SEQUENCE  \"HR\".\"LOCATIONS_SEQ\"" +
                "  MINVALUE 1 MAXVALUE 9900 INCREMENT BY 100 START WITH 3300 NOCACHE  NOORDER  NOCYCLE ;");


        String[] args2 = {"-url", url, "--filter", "LOCATIONS_SEQ", "--output", outputPath, "--replace-sequence-values"};
        Main.main(args2);
        out = outContent.toString();
        assertThat(out, containsString("Found 1 items for processing in schema HR"));

        assertEqualsFileContent(outputPath + "/sequences/locations_seq.sql", "CREATE SEQUENCE  \"HR\".\"LOCATIONS_SEQ\"  MINVALUE 1 MAXVALUE 9900 INCREMENT BY 100 START WITH 1 NOCACHE  NOORDER  NOCYCLE ;\n" +
                "/* -- actual sequence value was replaced by scheme2ddl to 1 */");

    }

    @Test
    public void testRunWithConfigPath() throws Exception {
        String outputPath = tempOutput.getAbsolutePath();
        String[] args = {"-url", url, "-c", "src/main/resources/scheme2ddl.config.xml", "-o", outputPath};
        Main.main(args);
        assertHRSchemaDefault(
                outputPath,
                outContent.toString());
    }


    @Test
    public void testRunAsSysDbaTestConnection() throws Exception {
        String outputPath = tempOutput.getAbsolutePath();
        String dba[] = dbaAsSysdbaUrl.split(" ");
        assertTrue(dba.length==3);
        String[] args = {"-url", dba[0], dba[1], dba[2], "-tc"};
        Main.main(args);
        Assert.assertEquals(
                outContent.toString(),
                "OK success connection to jdbc:oracle:thin:" + dbaAsSysdbaUrl + "\n");

    }


    @Test
    public void testRunAsSysDbaWithTypeFilter() throws Exception {
        String outputPath = tempOutput.getAbsolutePath();
        String dba[] = dbaAsSysdbaUrl.split(" ");
        assertTrue(dba.length==3);
        String[] args = {"-url", dba[0], dba[1], dba[2], "--type-filter", "'SCHEDULE', 'JOB'"};
        Main.main(args);
        String out = outContent.toString();
        assertThat(out, containsString("Will try to process schema  [SYS]"));
        assertThat(out, containsString("Found 13 items for processing in schema SYS"));
        assertThat(out, containsString("Cannot get DDL for object UserObject"));

    }

    @Test
    public void testRunWithTestCustomConfig() throws Exception {
        String outputPath = tempOutput.getAbsolutePath();
        String[] args = {"-url", url, "-c", "src/test/resources/test.config.xml", "-o", outputPath};
        Main.main(args);
        String out = outContent.toString();
        assertThat(out, containsString("Found 68 items for processing in schema HR"));
        assertThat(out, containsString(
                "Cannot get DDL for object UserObject{name='SYS_C004102', type='CONSTRAINT', schema='HR', ddl='null'} " +
                        "with error message ConnectionCallback; uncategorized SQLException for SQL [];" +
                        " SQL state [99999]; error code [31603];" +
                        " ORA-31603: object \"SYS_C004102\" of type CONSTRAINT not found in schema \"HR\"\n"));


        assertThat(out, containsString(
                "-------------------------------------------------------\n" +
                        "   R E P O R T     S K I P P E D     O B J E C T S     \n" +
                        "-------------------------------------------------------\n" +
                        "| skip rule |  object type              |    count    |\n" +
                        "-------------------------------------------------------\n" +
                        "|  config   |  INDEX                    |      19     |\n" +
                        "| sql error |  CONSTRAINT               |      1      |"
        ));
    }


    @Test
    public void testCustomConfigWithSchemaList() throws Exception {
        String outputPath = tempOutput.getAbsolutePath();
        String[] args = {"-url", url, "-c", "src/test/resources/test_schema_list.config.xml", "-o", outputPath};
        Main.main(args);
        String out = outContent.toString();
        assertThat(out, containsString("Ignore 'schemaList' from advanced config, because oracle user is not connected as sys dba"));
        assertThat(out, containsString("Found 68 items for processing in schema HR"));
        assertThat(out, containsString(
                "Cannot get DDL for object UserObject{name='SYS_C004102', type='CONSTRAINT', schema='HR', ddl='null'} " +
                        "with error message ConnectionCallback; uncategorized SQLException for SQL [];" +
                        " SQL state [99999]; error code [31603];" +
                        " ORA-31603: object \"SYS_C004102\" of type CONSTRAINT not found in schema \"HR\"\n"));


        assertThat(out, containsString(
                "-------------------------------------------------------\n" +
                        "   R E P O R T     S K I P P E D     O B J E C T S     \n" +
                        "-------------------------------------------------------\n" +
                        "| skip rule |  object type              |    count    |\n" +
                        "-------------------------------------------------------\n" +
                        "|  config   |  INDEX                    |      19     |\n" +
                        "| sql error |  CONSTRAINT               |      1      |"
        ));
    }

    @Test
    public void testCustomConfigWithSchemaListAsDba() throws Exception {
        String outputPath = tempOutput.getAbsolutePath();
        String dba[] = dbaAsSysdbaUrl.split(" ");
        assertTrue(dba.length==3);
        String[] args = {"-url", dba[0], dba[1], dba[2], "-c", "src/test/resources/test_schema_list.config.xml", "-o", outputPath};
        Main.main(args);
        String out = outContent.toString();
        assertThat(out, not(containsString("Ignore 'schemaList' from advanced config, because oracle user is not connected as sys dba")));
        assertThat(out, containsString("Will try to process schema list [SCOTT, HR]"));
        assertThat(out, containsString("Found 0 items for processing in schema SCOTT"));
        assertThat(out, containsString("Found 68 items for processing in schema HR"));
        assertThat(out, containsString(
                "Cannot get DDL for object UserObject{name='SYS_C004102', type='CONSTRAINT', schema='HR', ddl='null'} " +
                        "with error message ConnectionCallback; uncategorized SQLException for SQL [];" +
                        " SQL state [99999]; error code [31603];" +
                        " ORA-31603: object \"SYS_C004102\" of type CONSTRAINT not found in schema \"HR\"\n"));


        assertThat(out, containsString(
                "-------------------------------------------------------\n" +
                        "   R E P O R T     S K I P P E D     O B J E C T S     \n" +
                        "-------------------------------------------------------\n" +
                        "| skip rule |  object type              |    count    |\n" +
                        "-------------------------------------------------------\n" +
                        "|  config   |  INDEX                    |      19     |\n" +
                        "| sql error |  CONSTRAINT               |      1      |"
        ));
    }

    @Test
    public void testStopOnWarning() throws Exception {
        String outputPath = tempOutput.getAbsolutePath();
        String[] args = {"-url", url, "-c", "src/test/resources/test.config.xml", "-o", outputPath, "--stop-on-warning"};
        try {
            Main.main(args);
        }
        catch (Exception e){

        }
        String out = outContent.toString();
        assertThat(out, containsString("Found 68 items for processing in schema HR"));
        assertThat(out, containsString("scheme2ddl of schema HR failed"));
        assertThat(out, containsString("com.googlecode.scheme2ddl.exception.NonSkippableException"));

    }



}