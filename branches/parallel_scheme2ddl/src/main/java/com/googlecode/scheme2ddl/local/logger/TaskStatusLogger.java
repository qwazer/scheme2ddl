package com.googlecode.scheme2ddl.local.logger;

/**
 * @author A_Reshetnikov
 * @since Date: 08.08.2012
 */
public interface TaskStatusLogger {
    void startTaskProcessing(String taskId);

    void endTaskProcessing(String taskId);

    void logError(String taskId, Exception e);
}
