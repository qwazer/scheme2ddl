package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.dao.ConnectionDao;
import oracle.jdbc.pool.OracleDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.Assert;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class Main {

    private static final Log log = LogFactory.getLog(Main.class);
    public static String outputPath = null;
    public static int parallelCount = 4;
    private static boolean justPrintUsage = false;
    private static boolean justPrintVersion = false;
    private static boolean justTestConnection = false;
    private static boolean skipPublicDbLinks = false;
    private static boolean stopOnWarning = false;
    private static String customConfigLocation = null;
    private static String defaultConfigLocation = "scheme2ddl.config.xml";
    private static String dbUrl = null;
    private static String schemas;
    private static boolean isLaunchedByDBA;
    private static List<String> schemaList;

    public static void main(String[] args) throws Exception {
        collectArgs(args);
        if (justPrintUsage) {
            printUsage();
            return;
        }
        if (justPrintVersion) {
            printVersion();
            return;
        }

        ConfigurableApplicationContext context = loadApplicationContext();

        modifyContext(context);

        validateContext(context);

        if (justTestConnection) {
            testDBConnection(context);
        } else {
            new UserObjectJobRunner().start(context, isLaunchedByDBA);
        }
    }

    private static void testDBConnection(ConfigurableApplicationContext context) throws SQLException {
        ConnectionDao connectionDao = (ConnectionDao) context.getBean("connectionDao");
        OracleDataSource dataSource = (OracleDataSource) context.getBean("dataSource");
        if (connectionDao.isConnectionAvailable()) {
            System.out.println("OK success connection to " + dataSource.getURL());
        } else {
            System.out.println("FAIL connect to " + dataSource.getURL());
        }
    }

    private static void modifyContext(ConfigurableApplicationContext context) {
        if (dbUrl != null) {
            String url = "jdbc:oracle:thin:" + dbUrl;
            String user = extractUserfromDbUrl(dbUrl);
            String password = extractPasswordfromDbUrl(dbUrl);
            OracleDataSource dataSource = (OracleDataSource) context.getBean("dataSource");
            dataSource.setURL(url);
            // for OracleDataSource in connectionCachingEnabled mode need explicitly set user and password
            dataSource.setUser(user);
            dataSource.setPassword(password);
        }
        if (outputPath != null) {
            UserObjectWriter writer = (UserObjectWriter) context.getBean("writer");
            writer.setOutputPath(outputPath);
        }
        if (parallelCount > 0) {
            SimpleAsyncTaskExecutor taskExecutor = (SimpleAsyncTaskExecutor) context.getBean("taskExecutor");
            taskExecutor.setConcurrencyLimit(parallelCount);
        }
        String userName = ((OracleDataSource) context.getBean("dataSource")).getUser();
        isLaunchedByDBA = userName.toLowerCase().matches(".+as +sysdba *");
        if (!isLaunchedByDBA){
            ConnectionDao connectionDao = (ConnectionDao) context.getBean("connectionDao");
            isLaunchedByDBA = connectionDao.hasSelectCatalogRole(); //todo rename isLaunchedByDBA -> processForeignSchema
        }
        //process schemas
        processSchemas(context);

        FileNameConstructor fileNameConstructor = retrieveFileNameConstructor(context);   //will create new one if not exist
        if (isLaunchedByDBA) {
            fileNameConstructor.setTemplate(fileNameConstructor.getTemplateForSysDBA());
            fileNameConstructor.afterPropertiesSet();
        }

        if (stopOnWarning){
            UserObjectProcessor processor = (UserObjectProcessor) context.getBean("processor");
            processor.setStopOnWarning(stopOnWarning);
        }

    }

    private static void processSchemas(ConfigurableApplicationContext context) {
        List<String> listFromContext = retrieveSchemaListFromContext(context);
        if (schemas == null) {
            if (listFromContext.size() == 0) {
                //get default schema from username
                schemaList = extactSchemaListFromUserName(context);
            } else {
                if (isLaunchedByDBA)
                    schemaList = new ArrayList<String>(listFromContext);
                else {
                    log.warn("Ignore 'schemaList' from advanced config, becouse oracle user is not connected as sys dba");
                    schemaList = extactSchemaListFromUserName(context);
                }
            }
        } else {
            String[] array = schemas.split(",");
            schemaList = new ArrayList<String>(Arrays.asList(array));
        }

        listFromContext.clear();
        for (String s : schemaList) {
            listFromContext.add(s.toUpperCase().trim());
        }

        //for compabality with old config
        if (listFromContext.size() == 1) {
            try {
                UserObjectReader userObjectReader = (UserObjectReader) context.getBean("reader");
                userObjectReader.setSchemaName(listFromContext.get(0));
            } catch (ClassCastException e) {
                // this mean that new config used, nothing to do
            }
        }
    }

    private static List<String> extactSchemaListFromUserName(ConfigurableApplicationContext context) {
        OracleDataSource dataSource = (OracleDataSource) context.getBean("dataSource");
        String schemaName = dataSource.getUser().split(" ")[0];
        List<String> list = new ArrayList<String>();
        list.add(schemaName);
        return list;
    }

    private static void fillSchemaListFromUserName(ConfigurableApplicationContext context) {
        OracleDataSource dataSource = (OracleDataSource) context.getBean("dataSource");
        String schemaName = dataSource.getUser().split(" ")[0];
        schemaList = new ArrayList<String>();
        schemaList.add(schemaName);
    }

    /**
     * @param context
     * @return existing bean 'schemaList', if this exists, or create and register new bean
     */
    private static List<String> retrieveSchemaListFromContext(ConfigurableApplicationContext context) {
        List list;
        try {
            list = (List) context.getBean("schemaList");
        } catch (NoSuchBeanDefinitionException e) {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
            beanFactory.registerBeanDefinition("schemaList", BeanDefinitionBuilder.rootBeanDefinition(ArrayList.class).getBeanDefinition());
            list = (List) context.getBean("schemaList");
        }
        return list;
    }

    /**
     * @param context
     * @return existing bean 'fileNameConstructor', if this exists, or create and register new bean
     */
    private static FileNameConstructor retrieveFileNameConstructor(ConfigurableApplicationContext context) {
        FileNameConstructor fileNameConstructor;
        try {
            fileNameConstructor = (FileNameConstructor) context.getBean("fileNameConstructor");
        } catch (NoSuchBeanDefinitionException e) {
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
            beanFactory.registerBeanDefinition("fileNameConstructor", BeanDefinitionBuilder.rootBeanDefinition(FileNameConstructor.class).getBeanDefinition());
            fileNameConstructor = (FileNameConstructor) context.getBean("fileNameConstructor");
            fileNameConstructor.afterPropertiesSet();
            //for compatability with old config without fileNameConstructor bean
            UserObjectProcessor userObjectProcessor = (UserObjectProcessor) context.getBean("processor");
            userObjectProcessor.setFileNameConstructor(fileNameConstructor);
        }
        return fileNameConstructor;
    }

    private static String extractUserfromDbUrl(String dbUrl) {
        return dbUrl.split("/")[0];
    }

    private static String extractPasswordfromDbUrl(String dbUrl) {
        //scott/tiger@localhost:1521:ORCL
        return dbUrl.split("/|@")[1];
    }

    private static void validateContext(ConfigurableApplicationContext context) {
        String userName = ((OracleDataSource) context.getBean("dataSource")).getUser().toUpperCase();
        List<String> schemaList = (List) context.getBean("schemaList");
        Assert.state(isLaunchedByDBA || schemaList.size() == 1, "Cannot process multiply schemas if oracle user is not connected as sysdba");
        if (!isLaunchedByDBA) {
            String schemaName = schemaList.get(0).toUpperCase();
            Assert.state(userName.startsWith(schemaName),
                    String.format("Cannot process schema '%s' with oracle user '%s', if it's not connected as sysdba", schemaName, userName.toLowerCase()));
        }
    }

    /**
     * Prints the usage information for this class to <code>System.out</code>.
     */
    private static void printUsage() {
        String lSep = System.getProperty("line.separator");
        StringBuffer msg = new StringBuffer();
        msg.append("java -jar scheme2ddl.jar [-url ] [-o] [-s]" + lSep);
        msg.append("util for export oracle schema from DB to DDL scripts (file per object)" + lSep);
        msg.append("internally call to dbms_metadata.get_ddl " + lSep);
        msg.append("more config options in scheme2ddl.config.xml " + lSep);
        msg.append("Options: " + lSep);
        msg.append("  -help, -h               print this message" + lSep);
        // msg.append("  -verbose, -v           be extra verbose" + lSep);
        msg.append("  -url,                   DB connection URL" + lSep);
        msg.append("                          example: scott/tiger@localhost:1521:ORCL" + lSep);

        msg.append("  -o, --output,           output dir" + lSep);
        msg.append("  -p, --parallel,         number of parallel thread (default 4)" + lSep);
        msg.append("  -s, --schemas,          a comma separated list of schemas for processing" + lSep);
        msg.append("                          (works only if connected to oracle as sysdba)" + lSep);
        msg.append("  -c, --config,           path to scheme2ddl config file (xml)" + lSep);
        msg.append("  --stop-on-warning,      stop on getting DDL error (skip by default)" + lSep);
        msg.append("  -tc,--test-connection,  test db connection available" + lSep);
        msg.append("  -version,               print version info and exit" + lSep);
        System.out.println(msg.toString());
    }

    private static void printVersion() {
        System.out.println("scheme2ddl version " + getVersion());
    }

    private static String getVersion() {
        return Main.class.getPackage().getImplementationVersion();
    }

    private static void collectArgs(String[] args) throws Exception {

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-help") || arg.equals("-h") || arg.equals("--help")) {
                justPrintUsage = true;
            } else if (arg.equals("-url") || arg.equals("--url")) {
                dbUrl = args[i + 1];
                //check for as sysdba connection
                if (args.length >= i + 3) {
                    if ((args[i + 2].toLowerCase().equals("as")) &&
                            (args[i + 3].toLowerCase().startsWith("sysdba"))) {
                        //isLaunchedByDBA = true;
                        dbUrl = args[i + 1] + " " + args[i + 2] + " " + args[i + 3];
                        i = i + 2;
                    }
                }
                i++;
            } else if (arg.equals("-o") || arg.equals("-output") || arg.equals("--output")) {
                outputPath = args[i + 1];
                i++;
            } else if (arg.equals("-s") || arg.equals("-schemas") || arg.equals("--schemas")) {
                schemas = args[i + 1];
                i++;
            } else if (arg.equals("-p") || arg.equals("--parallel") || arg.equals("-parallel")) {
                parallelCount = Integer.parseInt(args[i + 1]);
                i++;
            } else if (arg.equals("-tc") || arg.equals("--test-connection")) {
                justTestConnection = true;
            }else if (arg.equals("--stop-on-warning")) {
                stopOnWarning = true;
            } else if (arg.equals("-c") || arg.equals("--config")) {
                customConfigLocation = args[i + 1];
                i++;
            } else if (arg.equals("-version")) {
                justPrintVersion = true;
            } else if (arg.startsWith("-")) {
                // we don't have any more args to recognize!
                String msg = "Unknown argument: " + arg;
                System.err.println(msg);
                printUsage();
                throw new Exception("");
            }
        }
    }

    private static ConfigurableApplicationContext loadApplicationContext() {
        ConfigurableApplicationContext context = null;
        if (customConfigLocation != null)
            context = new FileSystemXmlApplicationContext(customConfigLocation);
        else
            context = new ClassPathXmlApplicationContext(defaultConfigLocation);
        return context;
    }
}
