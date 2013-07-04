package com.googlecode.scheme2ddl.aspect;


import com.googlecode.scheme2ddl.domain.UserObject;
import com.googlecode.scheme2ddl.exception.CannotGetDDLException;
import org.aspectj.lang.annotation.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
      //  System.out.println("schemaName = " + schemaName);
      //  System.out.println("listExludedByConfig = " + listExludedByConfig); //todo pretty print
      //  System.out.println("listSkippedBySQLError = " + listSkippedBySQLError);   //todo pretty print
    }

}
