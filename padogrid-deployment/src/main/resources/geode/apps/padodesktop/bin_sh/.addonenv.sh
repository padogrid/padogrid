#!/usr/bin/env bash

# ========================================================================
# Copyright (c) 2020-2022 Netcrest Technologies, LLC. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ========================================================================

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
APP_DIR="$(dirname "$SCRIPT_DIR")"
APPS_DIR="$(dirname "$APP_DIR")"
BASE_DIR=$PADOGRID_HOME/$PRODUCT
pushd  $BASE_DIR/bin_sh > /dev/null 2>&1
. $BASE_DIR/bin_sh/.addonenv.sh
popd > /dev/null 2>&1

APP_NAME="$(basename "$APP_DIR")"

APP_ETC_DIR=$APP_DIR/etc
APP_LOG_DIR=$APP_DIR/log
if [ ! -d "$APP_LOG_DIR" ]; then
   mkdir -p "$APP_LOG_DIR"
fi

# Source in app specifics
JAVA_OPTS=""
CLASSPATH=""
. $APP_DIR/bin_sh/pado_env.sh
. $APP_DIR/bin_sh/setenv.sh

#
# IMPORTANT: CODEBASE_URL must be set with the trailing '/'.
#
CODEBASE_URL=file://localhost${APP_DIR}/

DESKTOP_HOME=$APP_DIR
NAF_HOME=$DESKTOP_HOME

ETC_DIR=$APP_DIR/etc
LOG_DIR=$APP_DIR/log
STATS_DIR=$APP_DIR/stats

# log directory
if [ ! -d $LOG_DIR ]; then
  mkdir -p $LOG_DIR
fi

# stats directory
if [ ! -d $STATS_DIR ]; then
  mkdir -p $STATS_DIR
fi

LOG_FILE="$APP_DIR/log/desktop.log"

#
# Set SECURITY_PROPERTIES
#
if [ "$GEMFIRE_SECURITY_PROPERTY_FILE" == "" ]; then
   GEMFIRE_SECURITY_PROPERTY_FILE=$ETC_DIR/gfsecurity.properties
fi
GEMFIRE_SECURITY_PROPERTY_SYSTEM=
if [ -f $GEMFIRE_SECURITY_PROPERTY_FILE ]; then
   if [ "$SECURITY_ENABLED" == "true" ]; then
      GEMFIRE_SECURITY_PROPERTY_SYSTEM=-DgemfireSecurityPropertyFile=$GEMFIRE_SECURITY_PROPERTY_FILE
   fi
else
   if [ "$SECURITY_ENABLED" == "true" ]; then
      echo ""
      echo "Security is enabled but the following security file does not exist:"
      echo "   $GEMFIRE_SECURITY_PROPERTY_FILE"
      echo "start_server Aborted."
      echo ""
      exit
   fi
fi
if [ "$SECURITY_ENABLED" == "true" ]; then
   SECURITY_PROPERTIES=-Dpado.security.enabled=true
else
   SECURITY_PROPERTIES=-Dpado.security.enabled=false
fi
SECURITY_PROPERTIES="$SECURITY_PROPERTIES $GEMFIRE_SECURITY_PROPERTY_SYSTEM"

#
# cache.xml if exists (must be cache.xml not client-cache.xml. Pado does not use ClientCache API.)
#
if [ -f "$ETC_DIR/cache.xml" ]; then
   JAVA_OPTS="-Dgemfire.cache-xml-file=$ETC_DIR/cache.xml $JAVA_OPTS"
fi
 
#
# Application library path
#
# Append all jar files found in the $PADODESKTOP_HOME/lib directory and
# its subdirectories in the class path.
#
APP_JARS=
for file in `find $PADODESKTOP_HOME/lib -name *.jar`
do
  if [ "${APP_JARS}" ]; then
    APP_JARS=${APP_JARS}:${file}
  else
    APP_JARS=${file}
  fi
done

# 
# class path
#
if [ "$GEODE_HOME" == "" ]; then
   GEODE_HOME="$GEMFIRE_HOME"
fi

CLASSPATH="$APP_DIR:$APP_DIR/plugins/*:$APP_DIR/lib/*:$APP_JARS:$GEODE_HOME/lib/*"
if [ "$PADOGRID_HOME" != "" ]; then
   CLASSPATH="$CLASSPATH:$PADOGRID_HOME/geode/plugins/*:$PADOGRID_HOME/geode/lib/*"
fi
if [ "$PADOGRID_WORKSPACE" != "" ]; then
   CLASSPATH="$CLASSPATH:$PADOGRID_WORKSPACE/plugins/*:$PADOGRID_WORKSPACE/lib/*"
fi

JAVA="$JAVA_HOME/bin/java"
