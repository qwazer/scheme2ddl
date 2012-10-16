package com.googlecode.scheme2ddl;

import com.googlecode.scheme2ddl.local.worker.Worker;

/**
 * @author A_Reshetnikov
 * @since Date: 08.08.2012
 */
public class Main {

    private static boolean justPrintUsage = false;
    private static boolean justPrintVersion = false;
    private static int parallel = 1;
    private static boolean justTestConnection = false;

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
       // ConfigHolder propertyHolder = (ConfigHolder) SpringUtils.getSpringBean("propertyHolder");
        for (int i = 0; i < parallel; i++) {
            Worker worker = SpringUtils.getSpringBean(Worker.class);
            worker.setWorkerName("worker " + i);
            new Thread(worker).start();
        }

        SpringUtils.testAppContext();


//        if (justTestConnection) {
//            testConnection(worker);
//            return;
//        }
    }

    private static void printVersion() {
        //todo implement printVersion in Main
    }

    private static void collectArgs(String[] args) throws Exception {

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-help") || arg.equals("-h")) {
                justPrintUsage = true;
            }   else if (arg.equals("-version")) {
                justPrintVersion = true;
            } else if (arg.equals("-parallel")) {
                parallel = Integer.valueOf(args[i + 1]);
                i++;
            } else if (arg.startsWith("-")) {
                // we don't have any more args to recognize!
                String msg = "Unknown argument: " + arg;
                System.err.println(msg);
                printUsage();
                throw new Exception("");
            }
        }
    }


    /**
     * Prints the usage information for this class to <code>System.out</code>.
     */
    private static void printUsage() {
        String lSep = System.getProperty("line.separator");
        StringBuffer msg = new StringBuffer();
        msg.append("java -jar scheme2ddl.jar " + lSep);    //templated
        msg.append("Options: " + lSep);
        msg.append("  -help, -h              print this message" + lSep);
        msg.append("  -parallel              number of threads" + lSep);
        msg.append("  -version,              print version info and exit" + lSep);
        System.out.println(msg.toString());
    }



}
