package com.googlecode.scheme2ddl.shared.impl;

import com.googlecode.scheme2ddl.shared.ConfigHolder;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author A_Reshetnikov
 * @since Date: 08.08.2012
 */
public class ConfigHolderImpl implements ConfigHolder {

    private @Value("${oracleUrl}") String oracleUrl = "";
    private @Value("${outputDir}") String outputDir = "";

    public String getOracleUrl() {
        return oracleUrl;
    }

    public void setOracleUrl(String oracleUrl) {
        this.oracleUrl = oracleUrl;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
