[ ![Download](https://api.bintray.com/packages/qwazer/maven/scheme2ddl/images/download.svg) ](https://bintray.com/qwazer/maven/scheme2ddl/_latestVersion) &nbsp; [![Build Status](https://travis-ci.org/qwazer/scheme2ddl.svg?branch=master)](https://travis-ci.org/qwazer/scheme2ddl) &nbsp; [![Coverage Status](https://coveralls.io/repos/github/qwazer/scheme2ddl/badge.svg?branch=master)](https://coveralls.io/github/qwazer/scheme2ddl?branch=master)

**scheme2ddl** is command line util for export oracle schema to set of ddl scripts. Provide a lot of configurations via basic command line options or advanced XML configuartion.

**scheme2ddl** is part of 
[oracle-ddl2svn](https://github.com/qwazer/oracle-ddl2svn) project.



### Benefits
**scheme2ddl** give ability to filter undesirable information, separate DDL in different files, pretty format output.

### How to start with minimal configuration
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
          -help, -h                  print this message
          -url,                      DB connection URL
                                     example: scott/tiger@localhost:1521:ORCL
          -o, --output,              output dir
          -p, --parallel,            number of parallel thread (default 4)
          -s, --schemas,             a comma separated list of schemas for processing
                                     (works only if connected to oracle as sysdba)
          -c, --config,              path to scheme2ddl config file (xml)
          -f, --filter,              filter for specific DDL objects
                                     every LIKE wildcard can be used
          -tf, --type-filter,        filter for specific DDL object types
          -tfm, --type-filtermode,   mode for type filter: include(default) or exclude
          --stop-on-warning,         stop on getting DDL error (skip by default)
          -rsv,                      replace actual sequence values with 1 
          --replace-sequence-values, 
          -tc,--test-connection,     test db connection available
          -version,                  print version info and exit

On Unix platform you can run `scheme2ddl.jar` as executable file:
    
    chmod +x scheme2ddl.jar
    ./scheme2ddl.jar 


### How it is work inside? 

 1.  First, get list of all user_object to export
 
    `select * from user_objects`

 2. then applying [dbms_metadata.set_transform_param](http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14258/d_metada.htm#i1000135)
 3. for every user object invoke [dbms_metadata.get_ddl](http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14258/d_metada.htm#i1019414) and [dbms_metadata.get_dependent_ddl](http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14258/d_metada.htm#i1019414)
 4. print every ddl to separate file grouped in folders like tables, views, procedures etc

*scheme2ddl* build on top of [spring-batch](http://static.springsource.org/spring-batch/) framework. 
