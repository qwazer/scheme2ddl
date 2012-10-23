package com.googlecode.scheme2ddl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

/**
 * @author A_Reshetnikov
 * @since Date: 22.10.2012
 */
public class UserObjectJobRunner {
    protected static final Log logger = LogFactory.getLog(UserObjectJobRunner.class);
    private JobLauncher launcher;

    int start(ConfigurableApplicationContext context) {
        try {
            context.getAutowireCapableBeanFactory().autowireBeanProperties(this,
                    AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);

            Assert.state(launcher != null, "A JobLauncher must be provided.  Please add one to the configuration.");
            Job job = (Job) context.getBean("job1");
            JobExecution jobExecution = launcher.run(job, new JobParameters());

            //write sime log
            StepExecution step = jobExecution.getStepExecutions().toArray(new StepExecution[]{})[0];
            logger.info(String.format("Writed %d ddls of user objects from total %d",
                    step.getWriteCount(), step.getReadCount()));
            logger.info(String.format("Skip processing %d user objects from total %d",
                    step.getFilterCount(), step.getReadCount()));
            long seconds = ((step.getEndTime().getTime()-step.getStartTime().getTime())/1000);
            logger.info(String.format("scheme2ddl %s in %d seconds", jobExecution.getStatus().toString().toLowerCase(), seconds));
            return 1;

        } catch (Throwable e) {
            String message = "Job Terminated in error: " + e.getMessage();
            logger.error(message, e);
            return -1;
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    public void setLauncher(JobLauncher launcher) {
        this.launcher = launcher;
    }
}
