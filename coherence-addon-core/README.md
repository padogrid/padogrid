# Coherence Library

This package contains an open source Coherence addon API that supplements the Coherence API.

## Maven

Oracle requires you to register at their site before you can access their Maven repository. You must configure Maven as described in [1] and [2] before you can build `coherence-addon`. If you wish to skip the Oracle Maven configuration steps then you can manually install the `coherence.jar` found in the Coherence installation directory as shown in the examples below.

:exclamation: Note that Oracle uses the `artifactId` notation of `x.y.z-a-b` with hyphens for the build number and qualifier whereas the version number is in the format of `x.y.z.a.b` without hyphens.

### Installing Coherence Library to Local Maven Repo with the `.pom` File

**Windows:**

```
set ORACLE_HOME=%USERPROFILE%\Work\products\Oracle\Middleware\Oracle_Home
set COHERENCE_HOME=%ORACLE_HOME%\coherence
mvn install:install-file -Dfile=%COHERENCE_HOME%\lib\coherence.jar -DpomFile=%COHERENCE_HOME%\plugins\maven\com\oracle\coherence\coherence\14.1.1\coherence.14.1.1.pom
```

**Unix:**

```
export ORACLE_HOME=~/Work/products/Oracle/Middleware/Oracle_Home
export COHERENCE_HOME=$ORACLE_HOME/coherence
mvn install:install-file -Dfile=$COHERENCE_HOME/lib/coherence.jar -DpomFile=$COHERENCE_HOME/plugins/maven/com/oracle/coherence/coherence/14.1.1/coherence.14.1.1.pom
```

### Installing Coherence to Local Maven Repo without the `.pom` File

**Windows:**

```dos
set COHERENCE_JAR_FILE_PATH=c:\Users\dpark\Work\products\Oracle\Middleware\Oracle_Home\coherence\lib\coherence.jar
mvn install:install-file -Dfile=%COHERENCE_JAR_FILE_PATH% -DgroupId=com.oracle.coherence -DartifactId=coherence -Dversion=14.1.1-0-0 -Dpackaging=jar
```

**Unix:**

```
export COHERENCE_JAR_FILE_PATH=~/Work/products/Oracle/Middleware/Oracle_Home/coherence/lib/coherence.jar
mvn install:install-file -Dfile=%COHERENCE_JAR_FILE_PATH% -DgroupId=com.oracle.coherence -DartifactId=coherence -Dversion=14.1.1-0-0 -Dpackaging=jar
```

## References

1. Getting Started with the Oracle Maven Repository, [https://redstack.files.wordpress.com/2015/03/otn-vts-oraclemavenrepository-labguide.pdf](https://redstack.files.wordpress.com/2015/03/otn-vts-oraclemavenrepository-labguide.pdf)
2. Configuring the Oracle Maven Repository, [https://docs.oracle.com/middleware/1213/core/MAVEN/config_maven_repo.htm#MAVEN9010](https://docs.oracle.com/middleware/1213/core/MAVEN/config_maven_repo.htm#MAVEN9010)
