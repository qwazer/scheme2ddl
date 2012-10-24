package com.googlecode.scheme2ddl;

import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author A_Reshetnikov
 * @since Date: 17.10.2012
 */
public class Main {

    public static String outputDir = null;
    public static int parallelCount = 4;
    private static boolean justPrintUsage = false;
    private static boolean justPrintVersion = false;
    private static boolean justTestConnection = false;
    private static String configLocation = "scheme2ddl.config.xml";
    private static String dbUrl = null;

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

        ConfigurableApplicationContext context = findApplicationContext(configLocation);

        modifyContext(context);

        if (justTestConnection) {
            testDBConnection(context);
        } else {
            new UserObjectJobRunner().start(context);
        }
    }

    private static void testDBConnection(ConfigurableApplicationContext context) {
        //todo implement testDBConnection in Main
    }

    private static void modifyContext(ConfigurableApplicationContext context) {
        //todo implement modifyContext in Main
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

        msg.append("  -output, -o            output dir" + lSep);
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
            if (arg.equals("-help") || arg.equals("-h")) {
                justPrintUsage = true;
            } else if (arg.equals("-url")) {
                dbUrl = args[i + 1];
                i++;
            } else if (arg.equals("-o") || arg.equals("-output")) {
                outputDir = args[i + 1];
                i++;
                // createDir();  //todo test
            } else if (arg.equals("-p") || arg.equals("--parallel")) {
                parallelCount = Integer.parseInt(args[i + 1]);
                i++;
            } else if (arg.equals("-tc") || arg.equals("--test-connection")) {
                justTestConnection = true;
            } else if (arg.equals("-c") || arg.equals("--config")) {
                configLocation = args[i + 1];
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

    private static ConfigurableApplicationContext findApplicationContext(String location) {
        //todo add default config in jar
        //make FileSystem first
        ConfigurableApplicationContext context = null;
        try {
            context = new ClassPathXmlApplicationContext(configLocation);
        } catch (BeansException e) {
            context = new FileSystemXmlApplicationContext(location);
        }
        return context;
    }
}
