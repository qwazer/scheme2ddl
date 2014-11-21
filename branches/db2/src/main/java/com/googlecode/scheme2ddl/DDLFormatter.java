package com.googlecode.scheme2ddl;

/**
 * @author A_Reshetnikov
 * @since Date: 18.10.2012
 */
public class DDLFormatter {

    private boolean noFormat;
    private boolean statementOnNewLine;
    private boolean isMorePrettyFormat = false;

    static String newline = "\r\n"; //may be use "\n"

    public String formatDDL(String ddl) {
        if (noFormat)
            return ddl;

        ddl = ddl.trim() + "\n";

        if (!isMorePrettyFormat)
            return ddl;

        /* smart formatting */
        ddl = ddl.replaceAll(newline + "GRANT ", newline + newline + "  GRANT ");
        ddl = ddl.replaceAll(newline + "COMMENT ", newline + newline + "   COMMENT ");
        ddl = ddl.replaceAll(newline + "  CREATE ", newline + "CREATE ");
        ddl = ddl.replaceAll(newline + "  ALTER ", newline + "ALTER ");
        return ddl;
    }

    public void setNoFormat(Boolean noFormat) {
        this.noFormat = noFormat;
    }

    @Deprecated
    public void setStatementOnNewLine(Boolean statementOnNewLine) {
        this.statementOnNewLine = statementOnNewLine;
    }

    public void setIsMorePrettyFormat(boolean isMorePrettyFormat) {
        this.isMorePrettyFormat = isMorePrettyFormat;
    }
}
