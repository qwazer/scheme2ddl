package com.googlecode.scheme2ddl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.CommandLineJobRunner;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class Main {

    private static final Log log = LogFactory.getLog(Main.class);
    private static JobLauncher launcher;


    public static void main(String[] args) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        ConfigurableApplicationContext context = null;
        context = new ClassPathXmlApplicationContext("scheme2ddl.config.xml");
        Job job = (Job) context.getBean("job1");
        UserObjectJobRunner  jobRunner = new UserObjectJobRunner();
        jobRunner.start(job, context);
     //   return exitCodeMapper.intValue(jobExecution.getExitStatus().getExitCode());
    }


    public static void main2(String[] args) {

        try {
            CommandLineJobRunner.main(new String[]{"scheme2ddl.config.xml", "job1"});
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static JobLauncher getLauncher() {
        return launcher;
    }

    //todo add reading config filename from args
}
