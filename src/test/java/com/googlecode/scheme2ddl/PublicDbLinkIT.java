package com.googlecode.scheme2ddl;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.BadSqlGrammarException;
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
import java.sql.SQLException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author A_Reshetnikov
 * @since Date: 17.09.2016
 */


public class PublicDbLinkIT extends BaseIT {

    @Value("${testUserUrl}")
    protected String testUserUrl;


    @BeforeClass
    public void createSchema() {
        try{
            dropUser();
        }catch (InvalidDataAccessResourceUsageException e) {
            logger.warn("", e);
        }
        try {
            createUser();
        } catch (CannotGetJdbcConnectionException e) {
            logger.warn("Ignore all test due", e);
            throw new SkipException("Ignore all test due " + e.getMessage());
        }

    }

    private void createUser() {
        dbaJdbcTemplate.execute("CREATE USER scheme2ddl_test02 IDENTIFIED BY pass");
        dbaJdbcTemplate.execute("GRANT CONNECT, SELECT_CATALOG_ROLE to scheme2ddl_test02");
        dbaJdbcTemplate.execute("ALTER USER scheme2ddl_test02 ACCOUNT UNLOCK IDENTIFIED BY pass");
    }


    // @AfterClass(alwaysRun = true)
    public void dropUser(){
        dbaJdbcTemplate.execute("drop USER scheme2ddl_test02");
    }

    @BeforeClass
    public void createDBLink(){
        dbaJdbcTemplate.execute("CREATE PUBLIC DATABASE LINK remote USING 'remote'");
    }

    @AfterClass
    public void dropDBLink(){
        dbaJdbcTemplate.execute("DROP PUBLIC DATABASE LINK remote");
    }


    @Test
    public void testPublicDbLinksWithSelectCatalogRole() throws Exception {
        String outputPath = tempOutput.getAbsolutePath();
        String[] args = {"-url", testUserUrl, "-o", outputPath};
        Main.main(args);
        String out = outContent.toString();

        assertThat(out, containsString("Written 1 ddls with user objects from total 1 in schema"));

        assertEqualsFileContent(outputPath + "/PUBLIC/public_db_links/remote.sql",
                "CREATE PUBLIC DATABASE LINK \"REMOTE\"\n" +
                        "   USING 'remote';");

    }


}