@echo off

set BASE_DIR=C:\Users\dpark\Work\git\Hazelcast\hazelcast-addon

REM -------------------------------
REM Set the following env variables
REM -------------------------------

set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_144
set HAZELCAST_ADDON_DIR=%BASE_DIR%
set ANTLR4_COMPLETE_JAR_PATH=C:\Users\dpark\Work\products\antlr4-4.7.2\antlr-4.7.2-complete.jar
set CLASSPATH=.;%ANTLR4_COMPLETE_JAR_PATH%

REM
REM HQL specifics
REM
set HQL_G4_PATH=%HAZELCAST_ADDON_DIR%\src\main\resources\Hql.g4
set PACKAGE_NAME=org.hazelcast.addon.hql.internal.antlr4.generated
set PACKAGE_DIR=%HAZELCAST_ADDON_DIR%\src\main\java\org\hazelcast\addon\hql\internal\antlr4\generated

REM -------------- DO NOT TOUCH BELOW ------------

set PATH=%JAVA_HOME%\bin;%CD%;%PATH%
