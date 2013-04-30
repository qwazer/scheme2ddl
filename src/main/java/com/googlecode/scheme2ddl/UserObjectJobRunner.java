package com.googlecode.scheme2ddl;

import oracle.jdbc.pool.OracleDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author A_Reshetnikov
 * @since Date: 22.10.2012
 */
public class UserObjectJobRunner {
    protected static final Log logger = LogFactory.getLog(UserObjectJobRunner.class);
    private JobLauncher launcher;

    int start(ConfigurableApplicationContext context) throws Exception {
        try {
            context.getAutowireCapableBeanFactory().autowireBeanProperties(this,
                    AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);

            Assert.state(launcher != null, "A JobLauncher must be provided.  Please add one to the configuration.");
            Job job = (Job) context.getBean("job1");

            JobParameters jobParameters = getJobParameters(context);

            JobExecution jobExecution = launcher.run(job, jobParameters);
            //write some log
            writeJobExecutionStatus(jobExecution);
            if (jobExecution.getStatus().isUnsuccessful()){
                throw new Exception("Job unsuccessful");
            }
            return 1;

        } catch (Exception e) {
            String message = "Job Terminated in error: " + e.getMessage();
            logger.error(message, e);
            throw e;
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    private static JobParameters getJobParameters(ConfigurableApplicationContext context) {
        JobParametersBuilder parametersBuilder = new JobParametersBuilder();
        String userName = ((OracleDataSource) context.getBean("dataSource")).getUser();
        boolean isLaunchedByDBA = false;
        String schemaName = userName;
        if (userName.toLowerCase().matches(".+as +sysdba *")) {
            System.out.println("Execute as SYSDBA user..."); //todo move to logging level
            schemaName = userName.split(" ")[0];
            isLaunchedByDBA = true;
        }
        parametersBuilder.addString("schemaName", schemaName.toUpperCase());
        parametersBuilder.addString("launchedByDBA", Boolean.toString(isLaunchedByDBA));
        return parametersBuilder.toJobParameters();
    }

    private void writeJobExecutionStatus(JobExecution jobExecution) {
        StepExecution step = jobExecution.getStepExecutions().toArray(new StepExecution[]{})[0];
        logger.info(String.format("Written %d ddls with user objects from total %d",
                step.getWriteCount(), step.getReadCount()));
        logger.info(String.format("Skip processing %d user objects from total %d",
                step.getFilterCount(), step.getReadCount()));
        long seconds = ((step.getEndTime().getTime()-step.getStartTime().getTime())/1000);
        logger.info(String.format("scheme2ddl %s in %d seconds", jobExecution.getStatus().toString().toLowerCase(), seconds));
    }

    public void setLauncher(JobLauncher launcher) {
        this.launcher = launcher;
    }
}
