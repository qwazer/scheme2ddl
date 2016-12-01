package com.googlecode.scheme2ddl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

/**
 * @author A_Reshetnikov
 * @since Date: 17.09.2016
 */

@SpringBootTest(classes = ConfigurationIT.class)
public class MainIT extends AbstractTestNGSpringContextTests {

    @Value("${url}")
    private String url;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();



    @BeforeMethod
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterMethod
    public void cleanUpStreams() {
        System.setOut(null);
        System.setErr(null);
        outContent.reset();
        errContent.reset();
    }

    @Test(dataProvider = "justTestConnectionParamNames")
    public void testMainJustTestConnectionOption(String urlParamName, String testConnParamName) throws Exception {
        String[] args = {urlParamName, url, testConnParamName};
        Main.main(args);
        Assert.assertEquals(
                outContent.toString(),
                "OK success connection to jdbc:oracle:thin:" + url + "\n"
        );
    }

    @DataProvider
    public static Object[][] justTestConnectionParamNames() {
        return new Object[][]{
                {"-url", "-tc"},
                {"-url", "--test-connection"},
        };
    }


    @Test
    public void testPrintVersionOption() throws Exception {
        String[] args = {"-version"};
        Main.main(args);
        assertThat(outContent.toString(), containsString("scheme2ddl version "));
    }

    @Test(expectedExceptions = Exception.class, expectedExceptionsMessageRegExp = "Job (.*) unsuccessful")
    public void testStopOnWarning() throws Exception {
        String[] args = {"-url", url, "--stop-on-warning"};
        Main.main(args);
        //todo will need to fix after properly catching exception

    }
}