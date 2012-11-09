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
        log.info("Start getting of user object list for processing");
        list = userObjectDao.findListForProccessing();
        if (processPublicDbLinks) {
            list.addAll(userObjectDao.findPublicDbLinks());
        }
        if (processDmbsJobs){
            list.addAll(userObjectDao.findDmbsJobs());
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
}
