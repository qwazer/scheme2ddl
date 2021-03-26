package com.googlecode.scheme2ddl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author A_Reshetnikov
 * @since Date: 18.10.2012
 */
public class DDLFormatter {

  static String newline = "\r\n"; //may be use "\n"
  private boolean noFormat;
  private boolean sortCreateIndexStatements = true;
  private boolean statementOnNewLine;
  private boolean isMorePrettyFormat = true;

  public String formatDDL(String ddl) {
    if (noFormat) {
      return ddl;
    }

    ddl = ddl.trim() + "\n";

    // replace unix line endings with windows
    ddl = ddl.replaceAll("\r\n", "\n");
    ddl = ddl.replaceAll("\n", "\r\n");

    if (!isMorePrettyFormat) {
      return ddl;
    }

    // replace "
    ddl = ddl.replaceAll("\"", "");

    // replace owner
    ddl = ddl.replaceAll(Main.schemaList.get(0).toUpperCase() + "\\.", "");

    /* smart formatting */
//    ddl = ddl.replaceAll(newline + "GRANT ", newline + newline + "GRANT ");
//    ddl = ddl.replaceAll(newline + "COMMENT ", newline + newline + "COMMENT ");
    ddl = ddl.replaceAll(newline + "  CREATE ", newline + "CREATE ");
    ddl = ddl.replaceAll(newline + "  ALTER ", newline + "ALTER ");

    // replace tab
    ddl = ddl.replaceAll("\\t", "  ");

    return ddl;
  }

  public String formatTableDDL(String ddl) {
    // for table
    ddl = ddl.replaceAll(newline + "\\s*\\(", newline + "(");
    ddl = ddl.replaceAll(newline + "\\s*\\)", newline + ")");
    ddl = ddl.replaceAll(" ENABLE,", ",");
    ddl = ddl.replaceAll(",0\\)", ")");
    ddl = ddl.replaceAll(newline + "\\s*COMMENT ", newline + "COMMENT ");
//    ddl = ddl.replaceAll(newline + "" + newline + "\\s*;", newline + "COMMENT ");

    String output;

    // find "alter table CONSTRAINT"
    Pattern p = Pattern.compile(newline + "ALTER TABLE (.*)");
    Matcher m = p.matcher(ddl);
    if (m.find()) {
      output = m.replaceFirst("");
      output = output.replaceAll(newline + "\\s*USING .*", "");
      output = output + newline + "alter table " + m.group(1) + " using index;";
      ddl = output;
    }

    // find "alter table CONSTRAINT"
    do {
      p = Pattern.compile(newline + "CREATE (.*)INDEX (.*)" + newline + "\\s*;");
      m = p.matcher(ddl);

      if (m.find()) {
        output = m.replaceFirst(newline + "CREATE " + m.group(1) + "INDEX " + m.group(2).trim() + ";");
        ddl = output;
      } else {
        break;
      }
    } while (true);

    // remove SUPPLEMENTAL LOG
    ddl = ddl.replaceAll(newline + ".*SUPPLEMENTAL LOG.*", "");

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

  public void setSortCreateIndexStatements(boolean sortCreateIndexStatements) {
    this.sortCreateIndexStatements = sortCreateIndexStatements;
  }

  public String replaceActualSequenceValueWithOne(String res) {

    String output;
    Pattern p = Pattern.compile("CREATE SEQUENCE (.*) MINVALUE 1 .*");
//    Pattern p = Pattern.compile("CREATE SEQUENCE (.*) MINVALUE 1 START WITH (\\d+) (.*)");
    Matcher m = p.matcher(res);
    if (m.find()) {
      output = m.replaceFirst("CREATE SEQUENCE " + m.group(1) + ";");
//      output = m.replaceFirst("CREATE SEQUENCE " + m.group(1) + " START WITH 1 " + m.group(3));
//      if (!"1".equals(m.group(2)))
//        output = output + newline + "/* -- actual sequence value was replaced by scheme2ddl to 1 */";
    } else {
      output = res;
    }
    return output;
  }

  /**
   * Read input string with list of 'create index' statements and, tokenize and sort alphabetically.
   *
   * @param dependentDLL -string with list of 'create index' statements
   * @return string with sorted alphabetically 'create index' statements
   */
  public String sortIndexesInDDL(String dependentDLL) {
    if (noFormat || !sortCreateIndexStatements) {
      return dependentDLL;
    }
    String[] parts = dependentDLL.split("(?=CREATE INDEX)|(?=CREATE UNIQUE INDEX)|(?=CREATE BITMAP INDEX)");
    List<String> list = new ArrayList<String>();
    for (String part : parts) {
      if (part != null && !part.trim().isEmpty()) {
        list.add(part.trim());
      }
    }
    Collections.sort(list);
    StringBuilder s = new StringBuilder();
    String prefix = "\n  "; //to preserve formatting  of dbms_metadata.get_depended_ddl output
    for (String statement : list) {
      s.append(prefix);
      prefix = "\n";
      s.append(statement);
    }
    return s.toString();
  }
}
