package com.googlecode.scheme2ddl.local.worker;

/**
 * @author A_Reshetnikov
 * @since Date: 08.08.2012
 */
public interface Worker extends Runnable {
    void setWorkerName(String workerName);

}
