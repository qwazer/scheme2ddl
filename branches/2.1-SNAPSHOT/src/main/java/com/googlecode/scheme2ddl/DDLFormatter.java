package com.googlecode.scheme2ddl;

/**
 * @author A_Reshetnikov
 * @since Date: 18.10.2012
 */
public class DDLFormatter {

    private boolean noFormat;
    private boolean statementOnNewLine;
    private boolean isMorePrettyFormat = false;

    public String formatDDL(String ddl) {
        if (noFormat) return ddl;
        // Get a new line specific to the system
        String newline = System.getProperty("line.separator");

        if (isMorePrettyFormat) {
            ddl = ddl.replaceAll(newline + "GRANT ", newline + newline + "  GRANT ");
            ddl = ddl.replaceAll(newline + "COMMENT ", newline + newline + "   COMMENT ");
            ddl = ddl.replaceAll(newline + "  CREATE ", newline + "CREATE ");
        }
        else if (statementOnNewLine) {
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

    public void setMorePrettyFormat(boolean morePrettyFormat) {
        isMorePrettyFormat = morePrettyFormat;
    }
}
