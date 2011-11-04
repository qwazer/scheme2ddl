package com.googlecode.scheme2ddl;

/**
 * User: Reshetnikov AV resheto@gmail.com
 * Date: 10.03.11
 * Time: 11:04
 */
public class DDLFormatter {


    private Boolean noFormat ;
    private Boolean statementOnNewLine ;
  //  private Boolean removeLastSlash;


    public void formatDDL(UserObject obj){
        if (noFormat) return;

        String ddl = obj.getDdl();
        if (statementOnNewLine){
            // Get a new line specific to the system
            String newline = System.getProperty("line.separator");

            ddl = ddl.replace(newline, ";");
            ddl = ddl.replace(";GRANT", ";" + newline + "GRANT");
            ddl = ddl.replace(";COMMENT", ";" + newline + "COMMENT");
            ddl = ddl.replace(";CREATE", ";" + newline + "CREATE");
        }

//        if (removeLastSlash){
//            if (ddl.endsWith("/")){
//                ddl = ddl.substring(0, ddl.lastIndexOf("/"));
//            }
//        }
        obj.setDdl(ddl);
    }

    public Boolean getStatementOnNewLine() {
        return statementOnNewLine;
    }

    public void setStatementOnNewLine(Boolean statementOnNewLine) {
        this.statementOnNewLine = statementOnNewLine;
    }

//    public Boolean getRemoveLastSlash() {
//        return removeLastSlash;
//    }
//
//    public void setRemoveLastSlash(Boolean removeLastSlash) {
//        this.removeLastSlash = removeLastSlash;
//    }

    public Boolean getNoFormat() {
        return noFormat;
    }

    public void setNoFormat(Boolean noFormat) {
        this.noFormat = noFormat;
    }
}
