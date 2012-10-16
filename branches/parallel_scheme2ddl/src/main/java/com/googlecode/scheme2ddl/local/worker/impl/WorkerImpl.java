package com.googlecode.scheme2ddl.local.worker.impl;

import com.googlecode.scheme2ddl.shared.TaskQueueHolder;
import com.googlecode.scheme2ddl.local.logger.TaskStatusLogger;
import com.googlecode.scheme2ddl.local.worker.Worker;

import javax.inject.Inject;

/**
 * @author A_Reshetnikov
 * @since Date: 08.08.2012
 */
public class WorkerImpl implements Worker {

    private String workerName;
    @Inject
    private TaskQueueHolder taskQueueHolder;
    @Inject
    private TaskStatusLogger statusLogger;



    public void run() {
        System.out.println("Worker name = " + workerName);
        while (taskQueueHolder.hasTasks()) {
            processSingleWithLogWrap(taskQueueHolder.getTaskId());
        }
    }


    public boolean processSingleWithLogWrap(String taskId) {
        try {
            statusLogger.startTaskProcessing(taskId);
            processSingle(taskId);
            statusLogger.endTaskProcessing(taskId);

        } catch (Exception e) {
            // jobLogger.error(String.format("Stop processing of record with id %s", recordId), e);
            statusLogger.logError(taskId, e);
            System.out.println(String.format("[%s] Error in processing recordId = %s\n%s ", workerName, taskId, e));
            return false;
        } finally {
            // implement if you need some rollback actions
        }
        return true;
    }

    private void processSingle(String taskId) {
        //todo implement processSingle in WorkerImpl
    }


    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

}
