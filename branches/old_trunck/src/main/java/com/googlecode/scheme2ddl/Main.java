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

import oracle.jdbc.pool.OracleDataSource;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: Reshetnikov AV resheto@gmail.com
 * Date: 20.02.11
 * Time: 10:27
 */
public class Main {

    private static boolean justPrintUsage = false;
    private static boolean justPrintVersion = false;
    private static boolean justTestConnection = false;
    private static String dbUrl = null;
    public static String outputDir = null;
    public static boolean includeStorageInfo = false;

    public static void main(String[] args) throws Exception {

//        IWorker worker = (IWorker) SpringUtils.getSpringBean("worker");
//
//        worker.work();
        collectArgs(args);
        if (justPrintUsage) {
            printUsage();
            return;
        }
        if (justPrintVersion) {
            printVersion();
            return;
        }
        Worker worker = (Worker) SpringUtils.getSpringBean("worker");
        if (dbUrl != null || outputDir != null || includeStorageInfo) {
            modifyWorkerConfig(worker);
        }
        if (justTestConnection) {
            testDBConnection(worker);
        } else
            worker.work();

    }

    private static void modifyWorkerConfig(Worker worker) throws Exception {
        if (dbUrl != null) {
            OracleDataSource ds = new OracleDataSource();
            ds.setURL("jdbc:oracle:thin:" + dbUrl);
            worker.getDao().setDataSource(ds);
        }
        if (outputDir != null) {
            worker.getFileWorker().setOutputPath(outputDir);
        }
    }

    private static void testDBConnection(Worker worker) throws Exception {
        if (worker.getDao().connectionAvailable()) {
            System.out.println("OK success connection to " + getCurrentDBURL(worker));
        } else {
            System.out.println("FAIL connect to " + getCurrentDBURL(worker));
        }
    }

    private static String getCurrentDBURL(Worker worker) throws SQLException {
        OracleDataSource ods = (OracleDataSource) worker.getDao().getDataSource();
        return ods.getURL() ;
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
                createDir();
            } else if (arg.equals("-tc") || arg.equals("--test-connection")) {
                justTestConnection = true;
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

    private static void createDir() throws Exception {
        if (!outputDir.endsWith("\\")) {
            outputDir += "\\";
        }
        try {
            //check for creating dir todo
        } catch (Exception e) {
            System.err.println("Cannot create output directory with name, exit");
            throw new Exception("");
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
        msg.append("  -help, -h              print this message" + lSep);
        // msg.append("  -verbose, -v           be extra verbose" + lSep);
        msg.append("  -url,                  DB connection URL" + lSep);
        msg.append("                         example: scott/tiger@localhost:1521:ORCL" + lSep);

        msg.append("  -output, -o            output dir" + lSep);
        msg.append("  -s,                    include storage info in DDL scripts (default no include)" + lSep);
        msg.append("  --test-connection,-tc  test db connection available" + lSep);
        msg.append("  -version,              print version info and exit" + lSep);
        System.out.println(msg.toString());
    }

    private static void printVersion() {
        System.out.println(getVersion());
    }

    private static String getVersion() {
        return Main.class.getPackage().getImplementationVersion();
    }

}
