package com.googlecode.scheme2ddl.aspect;


import com.googlecode.scheme2ddl.domain.UserObject;
import com.googlecode.scheme2ddl.exception.CannotGetDDLException;
import org.aspectj.lang.annotation.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;

import java.util.*;

/**
 * Collect statistics about skipped objects and
 * print detailed report after end of schema processing
 *
 * @author A_Reshetnikov
 * @since Date: 03.07.2013
 */
@Aspect
public class StatAspect {

    private List<UserObject> listExludedByConfig;
    private List<UserObject> listSkippedBySQLError;

    public StatAspect() {
        this.listExludedByConfig = Collections.synchronizedList(new ArrayList<UserObject>());
        this.listSkippedBySQLError = Collections.synchronizedList(new ArrayList<UserObject>());
    }

    @AfterReturning(pointcut = "execution(* com.googlecode.scheme2ddl.UserObjectProcessor.process(..)) &&"
            + "args(userObject)",
            returning = "retVal")
    public void exludedByConfig(UserObject userObject, Object retVal) {
        if (retVal == null) {
            listExludedByConfig.add(userObject);
        }
    }

    @AfterThrowing(pointcut = "execution(* com.googlecode.scheme2ddl.UserObjectProcessor.process(..)) &&"
            + "args(userObject)",
            throwing = "ex")
    public void skippedBySQLError(UserObject userObject, CannotGetDDLException ex) {
        listSkippedBySQLError.add(userObject);
    }

    @Before("execution(* org.springframework.batch.core.launch.JobLauncher.run(..))")
    public void clearStatistic() {
        this.listExludedByConfig = Collections.synchronizedList(new ArrayList<UserObject>());
        this.listSkippedBySQLError = Collections.synchronizedList(new ArrayList<UserObject>());
    }

    @After("execution(* org.springframework.batch.core.launch.JobLauncher.run(..)) &&" +
            " args(job, jobParameters)")
    public void printStatistic(Job job, JobParameters jobParameters) {
        String schemaName = jobParameters.getString("schemaName");
        prettyPrint(schemaName);
        //  System.out.println("schemaName = " + schemaName);
        //  System.out.println("listExludedByConfig = " + listExludedByConfig); //todo pretty print
        //  System.out.println("listSkippedBySQLError = " + listSkippedBySQLError);   //todo pretty print
    }

    /**
     * report something like this:
     * <pre>
     *    -------------------------------------------------------
     *       R E P O R T     S K I P P E D     O B J E C T S
     *    -------------------------------------------------------
     *    | skip rule |        object type        |    count    |
     *    -------------------------------------------------------
     *    |  config   |  INDEX                    |      2      |
     *    | sql error |  PUBLIC DATABASE LINK     |      4      |
     * </pre>
     *
     * @param schemaName
     * @param listExludedByConfig
     * @param listSkippedBySQLError
     */
    public void prettyPrint(String schemaName) {
        String lSep = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        sb.append(lSep);
        sb.append("-------------------------------------------------------");
        sb.append(lSep);
        sb.append("   R E P O R T     S K I P P E D     O B J E C T S     ");
        sb.append(lSep);
        sb.append("-------------------------------------------------------");

        if (listExludedByConfig.size() + listSkippedBySQLError.size() == 0) {
            sb.append("  No skipped objects  ");
            sb.append(lSep);
            return;
        }

        sb.append(lSep);
        sb.append("| skip rule |  object type              |    count    |");
        sb.append(lSep);
        sb.append("-------------------------------------------------------");
        sb.append(lSep);

        prettyPrintList(sb, " config  ", listExludedByConfig);
        prettyPrintList(sb, "sql error", listSkippedBySQLError);

        System.out.println(sb.toString());

    }

    private void prettyPrintList(StringBuilder sb, String ruleName, List<UserObject> listExludedByConfig) {
        String lSep = System.getProperty("line.separator");
        Map<String, Integer> groupByType = groupByType(listExludedByConfig);
        for (String type : groupByType.keySet()) {
            Formatter formatter = new Formatter();
            sb.append(formatter.format("| %s |  %-24s |      %-6s |", ruleName, type, groupByType.get(type)).toString());
            sb.append(lSep);
        }
    }

    private Map<String, Integer> groupByType(List<UserObject> list) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (UserObject userObject : list) {

            if (map.containsKey(userObject.getType())) {
                map.put(userObject.getType(), map.get(userObject.getType()) + 1);
            } else {
                map.put(userObject.getType(), 1);
            }
        }
        return map;
    }

}
