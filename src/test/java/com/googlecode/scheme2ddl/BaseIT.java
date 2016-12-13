package com.googlecode.scheme2ddl;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by Anton Reshetnikov on 12 Dec 2016.
 */
@SpringBootTest(classes = ConfigurationIT.class, properties = "test-default.properties")
public abstract class BaseIT extends AbstractTestNGSpringContextTests {

    @Value("${hrUrl}")
    protected String url;

    @Value("${dbaUrl}")
    protected String dbaUrl;

    @Value("${dbaAsSysdbaUrl}")
    protected String dbaAsSysdbaUrl;


    @Autowired
    protected JdbcTemplate dbaJdbcTemplate;

    protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    protected final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    protected final PrintStream outOriginal = System.out;
    protected final PrintStream errorOriginal = System.err;


    protected File tempOutput;


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

    @BeforeMethod
    public void resetDefaultsForStaticFields() throws Exception {
        ReflectionTestUtils.setField(Main.class, "justPrintUsage", false);
        ReflectionTestUtils.setField(Main.class, "justPrintVersion", false);
        ReflectionTestUtils.setField(Main.class, "justTestConnection", false);
        ReflectionTestUtils.setField(Main.class, "dbUrl", null);
        ReflectionTestUtils.setField(Main.class, "objectFilter", "%");
        ReflectionTestUtils.setField(Main.class, "typeFilter", "");
        ReflectionTestUtils.setField(Main.class, "typeFilterMode", "include");
        ReflectionTestUtils.setField(Main.class, "isLaunchedByDBA", false);
        ReflectionTestUtils.setField(Main.class, "schemas", null);
        ReflectionTestUtils.setField(Main.class, "schemaList", null);
        ReflectionTestUtils.setField(Main.class, "replaceSequenceValues", false);
        ReflectionTestUtils.setField(Main.class, "customConfigLocation", null);
        ReflectionTestUtils.setField(Main.class, "parallelCount", 4);
        ReflectionTestUtils.setField(Main.class, "outputPath", null);
    }

    @BeforeMethod
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterMethod
    public void cleanUpStreams() throws IOException {
        System.setOut(outOriginal);
        System.setErr(errorOriginal);
        outContent.reset();
        errContent.reset();
    }

    @BeforeMethod
    public void setUpTempOutputDir(){
        tempOutput = FileUtils.getFile(FileUtils.getTempDirectoryPath(),
                "scheme2ddl-test-tmp-output",
                UUID.randomUUID().toString().substring(0,8));
    }

    @AfterMethod
    public void cleanUpTempOutput() throws IOException {
        FileUtils.deleteDirectory(tempOutput);
    }

    protected static void assertEqualsFileContent(String path, String content) throws IOException {
        File file = new File(path);
        assertTrue(file.exists(), "file doesn't exists " + file );
        String fileContent = FileUtils.readFileToString(file, "UTF-8");
        assertEquals(fileContent.trim().replace("\r", ""), content.replace("\r", ""));

    }
}
