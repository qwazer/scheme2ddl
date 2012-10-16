package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.shared.ConfigHolder;
import com.googlecode.scheme2ddl.local.worker.FilesWorker;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author A_Reshetnikov
 * @since Date: 08.08.2012
 */
public class SpringUtils {

    private static ApplicationContext applicationContext;
    private static String configName = "app.cfg.xml"; //templated

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

//            try {
//                applicationContext = new FileSystemXmlApplicationContext(configName);
//            } catch (BeansException beansException) {
//                applicationContext = new ClassPathXmlApplicationContext(configName);
//            }
        }
        return applicationContext;
    }

    public static Object getSpringBean(String beanName) {
        return (Object) getApplicationContext().getBean(beanName);
    }

    public static <T> T getSpringBean(Class<T> requiredType){
        return getApplicationContext().getBean(requiredType);
    }

    public static void testAppContext(){

        getApplicationContext().getBean(ConfigHolder.class).setMyProp("1234");
        FilesWorker filesWorker = getApplicationContext().getBean(FilesWorker.class);
        System.out.println("filesWorker = " + filesWorker);
      //  System.out.println("p = " + p);
    }
}
