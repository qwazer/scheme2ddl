package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.dao.UserObjectDao;
import oracle.jdbc.pool.OracleDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.sql.SQLException;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class Main {

    public static String outputPath = null;
    public static int parallelCount = 4;
    private static boolean justPrintUsage = false;
    private static boolean justPrintVersion = false;
    private static boolean justTestConnection = false;
    private static String customConfigLocation = null;
    private static String defaultConfigLocation = "scheme2ddl.config.xml";
    private static String dbUrl = null;

    private static final Log log = LogFactory.getLog(Main.class);

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

        if (justTestConnection) {
            testDBConnection(context);
        } else {
            new UserObjectJobRunner().start(context);
        }
    }

    private static void testDBConnection(ConfigurableApplicationContext context) throws SQLException {
        UserObjectDao dao = (UserObjectDao) context.getBean("userObjectDao");
        OracleDataSource dataSource = (OracleDataSource) context.getBean("dataSource");
        if (dao.isConnectionAvailable()) {
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
    }

    private static String extractUserfromDbUrl(String dbUrl) {
        return dbUrl.split("/")[0];
    }

    private static String extractPasswordfromDbUrl(String dbUrl) {
        //scott/tiger@localhost:1521:ORCL
        return dbUrl.split("/|@")[1];
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
        msg.append("  -help, -h              print this message" + lSep);
        // msg.append("  -verbose, -v           be extra verbose" + lSep);
        msg.append("  -url,                  DB connection URL" + lSep);
        msg.append("                         example: scott/tiger@localhost:1521:ORCL" + lSep);

        msg.append("  -o, --output            output dir" + lSep);
        msg.append("  -p, --parallel,        number of parallel thread (default 4)" + lSep);
        msg.append("  -c, --config,          path to scheme2ddl config file (xml)" + lSep);
        msg.append("  --test-connection,-tc  test db connection available" + lSep);
        msg.append("  -version,              print version info and exit" + lSep);
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
                i++;
            } else if (arg.equals("-o") || arg.equals("-output") || arg.equals("--output")) {
                outputPath = args[i + 1];
                i++;
            } else if (arg.equals("-p") || arg.equals("--parallel") || arg.equals("-parallel")) {
                parallelCount = Integer.parseInt(args[i + 1]);
                i++;
            } else if (arg.equals("-tc") || arg.equals("--test-connection")) {
                justTestConnection = true;
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
