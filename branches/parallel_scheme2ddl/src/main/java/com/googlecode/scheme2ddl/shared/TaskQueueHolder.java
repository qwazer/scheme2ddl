package com.googlecode.scheme2ddl.shared;

/**
 * @author A_Reshetnikov
 * @since Date: 08.08.2012
 */
public interface TaskQueueHolder {
    boolean hasTasks();

    String getTaskId();
}
