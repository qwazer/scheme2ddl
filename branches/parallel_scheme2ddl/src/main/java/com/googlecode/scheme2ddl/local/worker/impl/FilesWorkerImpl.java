package com.googlecode.scheme2ddl.local.worker.impl;

import com.googlecode.scheme2ddl.shared.ConfigHolder;
import com.googlecode.scheme2ddl.local.worker.FilesWorker;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author A_Reshetnikov
 * @since Date: 09.08.2012
 */
public class FilesWorkerImpl implements FilesWorker {

    @Inject
    private ConfigHolder configHolder;

    private @Value("${myProp}") String myProp;

    @PostConstruct
//    public void init(){
//        myProp = configHolder.getMyProp()+ "@PostConstruct";
//    }


//    public void afterPropertiesSet() throws Exception {
//        myProp = configHolder.getMyProp() + "InitializingBean";
//    }

    @Override
    public String toString() {
        return "FilesWorkerImpl{" +
                "configHolder=" + configHolder +
                ", myProp='" + myProp + '\'' +
                '}';
    }
}
