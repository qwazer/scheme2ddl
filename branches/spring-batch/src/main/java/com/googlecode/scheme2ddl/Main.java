package com.googlecode.scheme2ddl;

import org.springframework.batch.core.launch.support.CommandLineJobRunner;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class Main {

    public static void main(String[] args) throws Exception {
        CommandLineJobRunner.main(new String[]{"scheme2ddl.config.xml", "job1"});
    }
}
