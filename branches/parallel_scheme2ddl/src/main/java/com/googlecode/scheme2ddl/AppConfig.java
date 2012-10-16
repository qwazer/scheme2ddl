package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.shared.ConfigHolder;
import com.googlecode.scheme2ddl.shared.impl.ConfigHolderImpl;
import com.googlecode.scheme2ddl.shared.TaskQueueHolder;
import com.googlecode.scheme2ddl.local.logger.TaskStatusLogger;
import com.googlecode.scheme2ddl.shared.impl.TaskQueueHolderImpl;
import com.googlecode.scheme2ddl.local.logger.impl.TaskStatusLoggerImpl;
import com.googlecode.scheme2ddl.local.worker.FilesWorker;
import com.googlecode.scheme2ddl.local.worker.Worker;
import com.googlecode.scheme2ddl.local.worker.impl.FilesWorkerImpl;
import com.googlecode.scheme2ddl.local.worker.impl.WorkerImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

/**
 * @author A_Reshetnikov
 * @since Date: 08.08.2012
 */
@Configuration()
@ImportResource("classpath:/scheme2ddl.config.xml")

public class AppConfig {

    // private @Value("${jdbc.url}") String url;
    private
    @Value("${myProp}")
    String myProp;
    //  private @Value("${jdbc.username}") String username;
    //  private @Value("${jdbc.password}") String password;


    @Bean
    @Scope("prototype")
    public Worker worker() {
        return new WorkerImpl();
    }

    @Bean
    public TaskQueueHolder taskQueueHolder() {
        return new TaskQueueHolderImpl();
    }

    @Bean
    public TaskStatusLogger taskStatusLogger() {
        return new TaskStatusLoggerImpl();
    }

    @Lazy
    @Bean
    public ConfigHolder propertyHolder() {
        return new ConfigHolderImpl();
    }

    @Bean
    @Scope("prototype")
    public FilesWorker filesWorker(){
        return new FilesWorkerImpl();
    }

    public void setMyProp(String myProp) {
        this.myProp = myProp;
    }
}
