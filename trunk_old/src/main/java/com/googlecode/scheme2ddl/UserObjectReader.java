package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.dao.UserObjectDao;
import com.googlecode.scheme2ddl.domain.UserObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.List;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class UserObjectReader implements ItemReader<UserObject> {

    private static final Log log = LogFactory.getLog(UserObjectReader.class);
    private static List<UserObject> list;
    private UserObjectDao userObjectDao;
    private boolean processPublicDbLinks = false;
    private boolean processDmbsJobs = false;
    private String processSchemas = null;
    private boolean isLaunchedByDBA = false;

    public synchronized UserObject read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (list == null) {
            fillList();
            log.info(String.format("Found %s items for processing", list.size()));
        }
        if (list.size() == 0)
            return null;
        else
            return list.remove(0);
    }

    private synchronized void fillList() {
        if (processSchemas == null || processSchemas.equals("")) {
            if (isLaunchedByDBA) {
                log.info("You must fill the 'processSchemas' config option for DBA user. Exit...");
                System.exit(1);
            }
            log.info("Start getting of user object list for processing");
            fillListForSchema(null);
        } else {
            if (!isLaunchedByDBA) {
                log.info("The 'processSchemas' config option is supported only for DBA users. Exit...");
                System.exit(1);
            }
            String[] schemas_array = processSchemas.split(", *");

            for(int i =0; i < schemas_array.length ; i++) {
                log.info(String.format("[%d/%d] Start getting of user object list for processing for '%s' schema.",
                        i+1, schemas_array.length, schemas_array[i]));
                fillListForSchema(schemas_array[i]);
            }
        }

        if (processPublicDbLinks) {
            list.addAll(userObjectDao.findPublicDbLinks());
        }
    }

    private synchronized void fillListForSchema(String schema) {
        if (list == null)
            list = userObjectDao.findListForProccessing(schema);
        else
            list.addAll(userObjectDao.findListForProccessing(schema));
        if (processDmbsJobs){
            list.addAll(userObjectDao.findDmbsJobs(schema));
        }
    }

    public void setUserObjectDao(UserObjectDao userObjectDao) {
        this.userObjectDao = userObjectDao;
    }

    public void setProcessPublicDbLinks(boolean processPublicDbLinks) {
        this.processPublicDbLinks = processPublicDbLinks;
    }

    public void setProcessDmbsJobs(boolean processDmbsSchedulerJobs) {
        this.processDmbsJobs = processDmbsSchedulerJobs;
    }

    public void setProcessSchemas(String processSchemas) {
        this.processSchemas = processSchemas;
    }

    public void setIsLaunchedByDBA(boolean isLaunchedByDBA) {
        this.isLaunchedByDBA = isLaunchedByDBA;
    }
}
