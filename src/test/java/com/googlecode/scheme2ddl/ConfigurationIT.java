package com.googlecode.scheme2ddl;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * @author A_Reshetnikov
 * @since Date: 18.09.2016
 */
@Configuration
@PropertySource("classpath:test-${spring.profiles.active:default}.properties")
public class ConfigurationIT {
}
