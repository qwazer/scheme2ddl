package com.googlecode.scheme2ddl.dao;

import com.googlecode.scheme2ddl.ConfigurationIT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.testng.Assert.*;

/**
 * Created by Anton Reshetnikov on 12 Dec 2016.
 */
@SpringBootTest(classes = ConfigurationIT.class, properties = "test-default.properties")
public class UserObjectDaoImplIT extends AbstractTestNGSpringContextTests {



    private UserObjectDaoImpl userObjectDao;

    @Autowired
    protected JdbcTemplate dbaJdbcTemplate;

    @BeforeClass
    public void setUp()  {
        try {
            dbaJdbcTemplate.execute("select 1 from dual");
        }
        catch (CannotGetJdbcConnectionException e){
            logger.warn("Ignore all test due", e);
            throw new SkipException("Ignore all test due " +  e.getMessage());
        }

    }


    @BeforeClass
    public void initDao() throws Exception {
        userObjectDao = new UserObjectDaoImpl();

        userObjectDao.setLaunchedByDBA(false);
        userObjectDao.setJdbcTemplate(dbaJdbcTemplate);
        userObjectDao.setSchemaName("NONE");
        userObjectDao.setTransformParams(new HashMap<String, Boolean>());

    }

    @Test
    public void findRefGroupDDLNotDba(){
        userObjectDao.setLaunchedByDBA(false);
        String ddl = userObjectDao.findRefGroupDDL("REFRESH_GROUP", "testName");
        assertNull(ddl);

    }

    @Test(expectedExceptions = UncategorizedSQLException.class)
    public void findRefGroupDDLDba(){
        userObjectDao.setLaunchedByDBA(true);
        String ddl = userObjectDao.findRefGroupDDL("REFRESH_GROUP", "testName");

    }

}