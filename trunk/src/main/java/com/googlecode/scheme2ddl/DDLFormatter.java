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
        return ddl.trim() + "\n";
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
