package com.googlecode.scheme2ddl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author A_Reshetnikov
 * @since Date: 17.09.2016
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConfigurationIT.class)
public class MainIT {

    @Value("${url}")
    private String url;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
        System.setErr(null);
    }

    @Test
    public void testMainJustTestConnectionOption() throws Exception {
        String[] args = {"-url", url, "--test-connection"};
        Main.main(args);
        Assert.assertEquals(
                "OK success connection to jdbc:oracle:thin:"+url +"\n",
                outContent.toString());
    }


}