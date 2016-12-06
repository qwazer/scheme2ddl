package com.googlecode.scheme2ddl;

import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.testng.Assert.*;

/**
 * Created by Anton Reshetnikov on 06 Dec 2016.
 */

public class MainCLITest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    private final PrintStream outOriginal = System.out;
    private final PrintStream errorOriginal = System.err;

    @BeforeMethod
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(Main.class, "justPrintUsage", false);
        ReflectionTestUtils.setField(Main.class, "justPrintVersion", false);
        ReflectionTestUtils.setField(Main.class, "justTestConnection", false);
        ReflectionTestUtils.setField(Main.class, "dbUrl", null);
    }

    @BeforeMethod
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterMethod
    public void cleanUpStreams() {
        System.setOut(outOriginal);
        System.setErr(errorOriginal);
        outContent.reset();
        errContent.reset();
    }


    @DataProvider
    public static Object[][] testPrintUsageOptionsParams() {
        return new Object[][]{
                new String[][]{{"-h"}},
                new String[][]{{"--help"}},
                new String[][]{{"-help"}},
                new String[][]{{"-h"}},
                new String[][]{{"-url", "1", "-tc", "-h"}},
                new String[][]{{"-tc", "--help"}},
        };
    }


    @Test(dataProvider = "testPrintUsageOptionsParams")
    public void testPrintUsageOptions(String[] args) throws Exception {
        Main.main(args);
        assertThat(outContent.toString(), containsString("java -jar scheme2ddl.jar"));
        assertThat(outContent.toString(), containsString("example: scott/tiger@localhost:1521:ORCL"));
    }

    @Test
    public void testPrintVersionOption() throws Exception {
        String[] args = {"-version"};
        Main.main(args);
        assertThat(outContent.toString(), containsString("scheme2ddl version "));
    }

    @Test(expectedExceptions = Exception.class, expectedExceptionsMessageRegExp = "Unknown argument: .*")
    public void testUnknownArgument() throws Exception {
        String[] args = {"-xYx"};
        Main.main(args);
    }


}