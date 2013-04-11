package com.googlecode.scheme2ddl;

/**
 * @author A_Reshetnikov
 * @since Date: 18.10.2012
 */
public class DDLFormatter {

    private boolean noFormat;
    private boolean statementOnNewLine;
    private String filenameCase = "lower";
    private boolean isMorePrettyFormat = false;
    // Get a new line specific to the system
    static String newline = System.getProperty("line.separator");

    public String formatDDL(String ddl) {
        if (noFormat) return ddl;

        if (isMorePrettyFormat) {
            ddl = ddl.replaceAll(newline + "GRANT ", newline + newline + "  GRANT ");
            ddl = ddl.replaceAll(newline + "COMMENT ", newline + newline + "   COMMENT ");
            ddl = ddl.replaceAll(newline + "  CREATE ", newline + "CREATE ");
        } else if (statementOnNewLine) {

            ddl = ddl.replace(newline, ";");
            ddl = ddl.replace(";GRANT", ";" + newline + "GRANT");
            ddl = ddl.replace(";COMMENT", ";" + newline + "COMMENT");
            ddl = ddl.replace(";CREATE", ";" + newline + "CREATE");
        }
        return ddl;
    }

    public void setNoFormat(boolean noFormat) {
        this.noFormat = noFormat;
    }

    public void setStatementOnNewLine(boolean statementOnNewLine) {
        this.statementOnNewLine = statementOnNewLine;
    }

    public void setFilenameCase(String filenameCase) {
        this.filenameCase = filenameCase;
    }

    public String getFilenameCase() {
        return filenameCase;
    }

    public void setIsMorePrettyFormat(boolean isMorePrettyFormat) {
        this.isMorePrettyFormat = isMorePrettyFormat;
    }

    public boolean getIsMorePrettyFormat() {
        return isMorePrettyFormat;
    }
}
