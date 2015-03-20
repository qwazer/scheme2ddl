[ ![Download](https://api.bintray.com/packages/qwazer/maven/scheme2ddl/images/download.svg) ](https://bintray.com/qwazer/maven/scheme2ddl/_latestVersion)

**scheme2ddl** is command line util for export oracle schema to set of ddl scripts. Provide a lot of configurations via basic command line options or advanced XML configuartion.

**scheme2ddl** is part of 
[oracle-ddl2svn](http://code.google.com/p/oracle-ddl2svn) project.



###Benefits
**scheme2ddl** give ability to filter undesirable information, separate DDL in different files, pretty format output.

###How to start with minimal configuration
Java must be installed on your computer.

For exporting oracle scheme you must provide

   - DB connection string
   - output directory
   
Usage example. Command

    java -jar scheme2ddl.jar -url scott/tiger@localhost:1521:ORCL -o C:/temp/oracle-ddl2svn/


will produce directory tree 

     views/
           view1.sql
           view2.sql
     tables/
           table1.sql
     functions
          /f1.sql  


More command line options 

    java -jar scheme2ddl.jar -help
    ...
    Options: 
      -help, -h               print this message
      -url,                   DB connection URL
                              example: scott/tiger@localhost:1521:ORCL
      -o, --output,           output dir
      -p, --parallel,         number of parallel thread (default 4)
      -s, --schemas,          a comma separated list of schemas for processing
                              (works only if connected to oracle as sysdba)
      -c, --config,           path to scheme2ddl config file (xml)
      --stop-on-warning,      stop on getting DDL error (skip by default)
      -tc,--test-connection,  test db connection available
      -version,               print version info and exit



###How it is work inside? 

 1.  First, get list of all user_object to export
 
    `select * from user_objects`

 2. then applying [dbms_metadata.set_transform_param](http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14258/d_metada.htm#i1000135)
 3. for every user object invoke [dbms_metadata.get_ddl](http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14258/d_metada.htm#i1019414) and [dbms_metadata.get_dependent_ddl](http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14258/d_metada.htm#i1019414)
 4. print every ddl to separate file grouped in folders like tables, views, procedures etc

*scheme2ddl* build on top of [spring-batch](http://static.springsource.org/spring-batch/) framework. 


###See it in action in cloud 
You can see it in action as [Jenkins build](http://jenkins-ddl2svn.rhcloud.com/job/amazon-rds-oracle-schema2ddl/lastSuccessfulBuild). Look at console output and build artifacts.

> Jenkins hosting provided by Red Hat's OpenShift.
> Oracle hosting provided by Amazon's RDS.

