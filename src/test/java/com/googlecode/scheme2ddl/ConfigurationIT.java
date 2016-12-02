package com.googlecode.scheme2ddl;

import oracle.jdbc.driver.OracleDriver;
import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.pool.OracleDataSourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author A_Reshetnikov
 * @since Date: 18.09.2016
 */
@Configuration
@PropertySource("classpath:test-${spring.profiles.active:default}.properties")
public class ConfigurationIT {


    @Value("${dba.datasource.url}")
    private String url;
    @Value("${dba.datasource.username}")
    private String username;
    @Value("${dba.datasource.password}")
    private String password;


    @Bean
    public DataSource dbaDataSource() throws SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    public JdbcTemplate dbaJdbcTemplate(DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }
}
