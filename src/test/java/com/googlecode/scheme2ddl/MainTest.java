package com.googlecode.scheme2ddl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * @author A_Reshetnikov
 * @since Date: 17.09.2016
 */
public class MainTest {

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

        String[] args = {"-url", "travis/travis@localhost:1521/XE", "--test-connection"};
        Main.main(args);
        Assert.assertEquals(
                "OK success connection to jdbc:oracle:thin:travis/travis@localhost:1521/XE\n",
                outContent.toString());
    }

}