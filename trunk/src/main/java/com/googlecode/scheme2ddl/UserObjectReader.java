package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.dao.UserObjectDao;
import com.googlecode.scheme2ddl.domain.UserObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class UserObjectReader implements ItemReader<UserObject> {

    private static final Log log = LogFactory.getLog(UserObjectReader.class);
    private List<UserObject> list;

    @Autowired
    private UserObjectDao userObjectDao;
    private boolean processPublicDbLinks = false;
    private boolean processDmbsJobs = false;
    private boolean processConstraint = false;

    @Value("#{jobParameters['schemaName']}")
    private String schemaName;


    public synchronized UserObject read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (list == null) {
            fillList();
            log.info(String.format("Found %s items for processing in schema %s", list.size(), schemaName));
        }
        if (list.size() == 0) {
            return null;
        } else
            return list.remove(0);
    }

    private synchronized void fillList() {
        log.info(String.format("Start getting of user object list in schema %s for processing", schemaName));
        list = userObjectDao.findListForProccessing();
        if (processPublicDbLinks) {
            list.addAll(userObjectDao.findPublicDbLinks());
        }
        if (processDmbsJobs) {
            list.addAll(userObjectDao.findDmbsJobs());
        }
        if (processConstraint){
            list.addAll(userObjectDao.findConstaints());
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

    public void setProcessConstraint(boolean processConstraint) {
        this.processConstraint = processConstraint;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}
