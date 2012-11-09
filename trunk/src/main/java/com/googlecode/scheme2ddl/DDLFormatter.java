package com.googlecode.scheme2ddl;

/**
 * @author A_Reshetnikov
 * @since Date: 18.10.2012
 */
public class DDLFormatter {

    private Boolean noFormat;
    private Boolean statementOnNewLine;

    public String formatDDL(String ddl) {
        if (noFormat) return ddl;


        if (statementOnNewLine) {
            // Get a new line specific to the system
            String newline = System.getProperty("line.separator");

            ddl = ddl.replace(newline, ";");
            ddl = ddl.replace(";GRANT", ";" + newline + "GRANT");
            ddl = ddl.replace(";COMMENT", ";" + newline + "COMMENT");
            ddl = ddl.replace(";CREATE", ";" + newline + "CREATE");
        }
        return ddl;
    }

    public void setNoFormat(Boolean noFormat) {
        this.noFormat = noFormat;
    }

    public void setStatementOnNewLine(Boolean statementOnNewLine) {
        this.statementOnNewLine = statementOnNewLine;
    }
}
