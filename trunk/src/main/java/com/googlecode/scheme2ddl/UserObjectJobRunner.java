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

            JobParametersBuilder parametersBuilder = new JobParametersBuilder();
            String schemaName = ((OracleDataSource) context.getBean("dataSource")).getUser();
            parametersBuilder.addString("schemaName", schemaName);

            JobExecution jobExecution = launcher.run(job, parametersBuilder.toJobParameters());
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
