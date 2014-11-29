package com.googlecode.scheme2ddl;

import org.junit.Test;

import static org.junit.Assert.*;

public class MainTestDb2 {

    @Test
    public void testExtractPortFromDbUrl (){
        String url = "user/pass@serverName:123:DbName";
        int port = Main.extractPortFromDbUrl(url);
        assertEquals(123, port);
    }




}