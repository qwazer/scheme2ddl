package com.googlecode.scheme2ddl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author A_Reshetnikov
 * @since Date: 22.10.2012
 */
public class UserObjectJobRunner {
    protected static final Log logger = LogFactory.getLog(UserObjectJobRunner.class);
    private JobLauncher launcher;

    int start(ConfigurableApplicationContext context ) throws Exception {
        try {
            context.getAutowireCapableBeanFactory().autowireBeanProperties(this,
                    AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);

            Assert.state(launcher != null, "A JobLauncher must be provided.  Please add one to the configuration.");
            Job job = (Job) context.getBean("job1");

            List<String> schemaList = (List<String>) context.getBean("schemaList");
            Assert.state(schemaList != null && schemaList.size()!=0, "schemaList must be provided.  Please add one to the configuration. ");

            logger.info(String.format("Will try to process schema %s %s ", schemaList.size() > 1 ? "list" : "", schemaList));

            for (String schemaName : schemaList){
                JobParametersBuilder parametersBuilder = new JobParametersBuilder();
                parametersBuilder.addString("schemaName", schemaName);
                JobParameters jobParameters = parametersBuilder.toJobParameters();
                logger.trace(String.format("Start spring batch job with parameters %s", jobParameters));
                JobExecution jobExecution = launcher.run(job, jobParameters);
                //write some log
                writeJobExecutionStatus(jobExecution, jobParameters);
                if (jobExecution.getStatus().isUnsuccessful()){
                    throw new Exception(String.format("Job %s unsuccessful", jobParameters));
                }
            }

            logger.info(String.format("Processing schema %s %s completed ", schemaList.size() > 1 ? "list" : "", schemaList));

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

    private JobParameters getJobParameters(String schemaName, boolean launchedByDBA) {
        JobParametersBuilder parametersBuilder = new JobParametersBuilder();
        parametersBuilder.addString("schemaName", schemaName.toUpperCase());
        return parametersBuilder.toJobParameters();
    }

    private void writeJobExecutionStatus(JobExecution jobExecution, JobParameters jobParameters) {
        StepExecution step = jobExecution.getStepExecutions().toArray(new StepExecution[]{})[0];
        String schemaName = jobParameters.getString("schemaName");
        logger.info(String.format("Written %d ddls with user objects from total %d in schema %s",
                step.getWriteCount(), step.getReadCount(), schemaName));
        logger.info(String.format("Skip processing %d user objects from total %d in schema %s",
                step.getFilterCount(), step.getReadCount(), schemaName));
        long seconds = ((step.getEndTime().getTime()-step.getStartTime().getTime())/1000);
        logger.info(String.format("scheme2ddl of schema %s %s in %d seconds", schemaName, jobExecution.getStatus().toString().toLowerCase(), seconds));
    }

    public void setLauncher(JobLauncher launcher) {
        this.launcher = launcher;
    }
}
