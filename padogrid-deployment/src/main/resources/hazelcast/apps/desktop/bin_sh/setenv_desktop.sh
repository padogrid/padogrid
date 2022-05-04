#!/bin/bash

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"

# Set BASE_DIR here to overwrite .addonenv.sh
BASE_DIR="$(dirname "$SCRIPT_DIR")"

# OS_NAME in uppercase
OS_NAME=`uname`
OS_NAME=`echo "$OS_NAME"|awk '{print toupper($0)}'`

#
# Set JAVA_HOME to the Java home (root) directory. If undefined then
# the default java executable in PATH is used.
#
#if [[ ${OS_NAME} == DARWIN* ]]; then
#   # Mac
#   export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
#   #export JAVA_HOME=`/usr/libexec/java_home -v 11`
#   #export JAVA_HOME=`/usr/libexec/java_home -v 12`
#elif [[ ${OS_NAME} == CYGWIN* ]]; then
#   export JAVA_HOME="/cygdrive/c/Program Files/Java/jdk1.8.0_212"
#else
#   export JAVA_HOME="/Users/dpark/Work/products/jdk1.8.0_212"
#fi

if [[ ${OS_NAME} == CYGWIN* ]]; then
   __BASE_DIR="$(cygpath -wp "$BASE_DIR")"
   BACK="\\\\"
   FORWARD="\/"
   CODEBASE_URL=`echo "$__BASE_DIR" | sed -e "s/$BACK/$FORWARD/g"`
   CODEBASE_URL="file:///${CODEBASE_URL}/"
else
   CODEBASE_URL=file://localhost/$BASE_DIR/
fi

if [ "$JAVA_HOME" == "" ]; then
   JAVA=java
else
   export PATH=$JAVA_HOME/bin:$PATH
   JAVA=$JAVA_HOME/bin/java
fi

DESKTOP_HOME=$HAZELCAST_DESKTOP_HOME
NAF_HOME=$DESKTOP_HOME

LOG_DIR=$BASE_DIR/log

# log directory
if [ ! -d $LOG_DIR ]; then
  mkdir -p $LOG_DIR
fi

HAZELCAST_CLIENT_CONFIG_FILE=$APP_DIR/etc/hazelcast-client.xml 

if [[ ${OS_NAME} == CYGWIN* ]]; then
   HAZELCAST_CLIENT_CONFIG_FILE="$(cygpath -wp "$HAZELCAST_CLIENT_CONFIG_FILE")"
fi

# Set version to 4 if >=5
if [ $HAZELCAST_MAJOR_VERSION_NUMBER -ge 5 ]; then
   __MAJOR_VERSION_NUMBER=4
else
   __MAJOR_VERSION_NUMBER=$HAZELCAST_MAJOR_VERSION_NUMBER
fi
MAJOR_VERSION_DIR=v$__MAJOR_VERSION_NUMBER

SHARED_CACHE_CLASS=com.netcrest.pado.ui.swing.pado.hazelcast.${MAJOR_VERSION_DIR}.HazelcastSharedCacheV${__MAJOR_VERSION_NUMBER}

JAVA_OPTS="-Xms256m -Xmx1024m -client 
-DcodeBaseURL=$CODEBASE_URL \
-DpreferenceURL=etc/desktop.properties \
-Dhazelcast.client.config=$HAZELCAST_CLIENT_CONFIG_FILE
-Dhazelcast.diagnostics.metric.distributed.datastructures=true \
-Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl \
-Dpado.sharedCache.class=$SHARED_CACHE_CLASS"

# 
# class path
#
PLUGIN_JARS=$NAF_HOME/plugins/*
LIB_JARS=$NAF_HOME/lib/*:$NAF_HOME/lib/${MAJOR_VERSION_DIR}/*
NAF_JARS=$NAF_HOME/lib/naf/*
PADO_JARS=$NAF_HOME/lib/pado/*
DEMO_JARS=$NAF_HOME/lib/demo/*

export CLASSPATH=$DESKTOP_HOME:$DESKTOP_HOME/classes:$PLUGIN_JARS:$LIB_JARS:$PADO_JARS:$NAF_JARS:$DEMO_JARS:$CLASSPATH
export CLASSPATH=$CLASSPATH:$HAZELCAST_HOME/lib/*:$PADOGRID_HOME/hazelcast/plugins/$MAJOR_VERSION_DIR/*:$PADOGRID_HOME/lib/*:$PADOGRID_HOME/hazelcast/lib/$MAJOR_VERSION_DIR/*

if [[ ${OS_NAME} == CYGWIN* ]]; then
   export CLASSPATH="$(cygpath -wp "$CLASSPATH")"
fi
