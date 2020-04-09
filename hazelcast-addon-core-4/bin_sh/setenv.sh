#!/bin/bash

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
BASE_DIR="$(dirname "$SCRIPT_DIR")"

# -------------------------------
# Set the following env variables
# -------------------------------

if [ "`uname`" == "Darwin" ]; then
   # Mac
   export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
elif [[ `uname -s` =~ ^CYGWIN ]]; then
   export JAVA_HOME="/cygdrive/c/Program Files/Java/jdk1.8.0_211"
else
   export JAVA_HOME=~/Work/products/jdk1.8.0_211
fi


export HAZELCAST_ADDON_DIR=$BASE_DIR
export ANTLR4_COMPLETE_JAR_PATH=~/Work/git/antlr4/tool/target/antlr4-4.7.2-complete.jar
export CLASSPATH=.:$ANTLR4_COMPLETE_JAR_PATH

#
# HQL specifics
#
HQL_G4_PATH=$HAZELCAST_ADDON_DIR/src/main/resources/Hql.g4
PACKAGE_NAME=org.hazelcast.addon.hql.internal.antlr4.generated
PACKAGE_DIR=$HAZELCAST_ADDON_DIR/src/main/java/org/hazelcast/addon/hql/internal/antlr4/generated

# -------------- DO NOT TOUCH BELOW ------------

export PATH=$JAVA_HOME/bin:`pwd`:$PATH

if [[ `uname -s` =~ ^CYGWIN ]]; then
  # Replace ':' with ';' for cygwin
   export CLASSPATH=${CLASSPATH//:/;}
fi
