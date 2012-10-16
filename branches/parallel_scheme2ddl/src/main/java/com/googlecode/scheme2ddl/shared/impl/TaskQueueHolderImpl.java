package com.googlecode.scheme2ddl.shared.impl;

import com.googlecode.scheme2ddl.shared.TaskQueueHolder;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author A_Reshetnikov
 * @since Date: 08.08.2012
 */
public class TaskQueueHolderImpl implements TaskQueueHolder {


    @Resource(name="myMap")
    private Map myMap;


    @Resource(name="myList")
    private List myList;

    public boolean hasTasks() {
        System.out.println("myMap = " + myMap);
        return false;  //todo implement hasTasks in TaskQueueHolderImpl
    }

    public String getTaskId() {
        return null;  //todo implement getTaskId in TaskQueueHolderImpl
    }


}
