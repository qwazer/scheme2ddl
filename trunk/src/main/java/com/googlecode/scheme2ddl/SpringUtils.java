/*
 *    Copyright (c) 2011 Reshetnikov Anton aka qwazer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.googlecode.scheme2ddl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Created by IntelliJ IDEA.
 * User: Reshetnikov AV resheto@gmail.com
 * Date: 19.02.11
 * Time: 15:19
 */
public class SpringUtils {

    private static ApplicationContext applicationContext;
    private static String configName = "scheme2ddl.config.xml";

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            try {
                applicationContext = new FileSystemXmlApplicationContext(configName);
            } catch (BeansException beansException) {
                applicationContext = new ClassPathXmlApplicationContext(configName);
            }
        }
        return applicationContext;
    }

    public static Object getSpringBean(String beanName) {
        return (Object) getApplicationContext().getBean(beanName);
    }
}
