#!/usr/bin/env bash

# ========================================================================
# Copyright (c) 2020-2023 Netcrest Technologies, LLC. All rights reserved.
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
# __DESKTOP_DIR is intentional. Do NOT change.
# It is manipulated for hazelcast-desktop during the build time. 
__DESKTOP_DIR=$SCRIPT_DIR
APP_DIR="$(dirname "$__DESKTOP_DIR")"
APPS_DIR="$(dirname "$APP_DIR")"
BASE_DIR=$PADOGRID_HOME/$PRODUCT
pushd  $BASE_DIR/bin_sh > /dev/null 2>&1
. $BASE_DIR/bin_sh/.addonenv.sh
popd > /dev/null 2>&1

APP_NAME="$(basename "$APP_DIR")"

# Need to reset SCRIPT_DIR. It has a different value due to the above calls. 
SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"

APP_ETC_DIR=$APP_DIR/etc
APP_LOG_DIR=$APP_DIR/log
if [ ! -d "$APP_LOG_DIR" ]; then
   mkdir -p "$APP_LOG_DIR"
fi
if [ ! -d "$APP_DIR/plugins" ]; then
   mkdir -p "$APP_DIR/plugins"
fi
if [ ! -d "$APP_DIR/lib" ]; then
   mkdir -p "$APP_DIR/lib"
fi

# Source in app specifics
. $APP_DIR/bin_sh/setenv.sh

# Log properties for lo4j2. The log file name is set in executable scripts.
# Do not use the add log. Use the desktop log instead.
#JAVA_OPT="$JAVA_OPT -Dhazelcast.logging.type=log4j2 \
#-Dlog4j.configurationFile=$APP_ETC_DIR/log4j2.properties \
#-DlogDir=$APP_DIR/log"

# Set Hazelcast addon class path. This is to handle 'none' and non-hazelcast clusters
if [ "$HAZELCAST_HOME" != "" ]; then
   if [ -f "$HAZELCAST_HOME/lib/hazelcast-enterprise-all-"* ]; then
      for file in $HAZELCAST_HOME/lib/hazelcast-enterprise-all-*; do
         file=${file##*hazelcast\-enterprise\-all\-}
         HAZELCAST_VERSION=${file%.jar}
         IS_HAZELCAST_ENTERPRISE=true
      done
   elif [ -f "$HAZELCAST_HOME/lib/hazelcast-enterprise-"* ]; then
      for file in $HAZELCAST_HOME/lib/hazelcast-enterprise-*; do
         file=${file##*hazelcast\-enterprise\-}
         HAZELCAST_VERSION=${file%.jar}
         IS_HAZELCAST_ENTERPRISE=true
      done
   elif [ -f "$HAZELCAST_HOME/lib/hazelcast-all-"* ]; then
      for file in $HAZELCAST_HOME/lib/hazelcast-all-*; do
         file=${file##*hazelcast\-all\-}
         HAZELCAST_VERSION=${file%.jar}
      done
   else
      # hazelcast- is not unique. scan 5-10 versions
      #for i in $(seq 5 10); do
      # seq not working due to IFS change?
         i="5"
         if [ -f "$HAZELCAST_HOME/lib/hazelcast-$i."* ]; then
            for file in "$HAZELCAST_HOME/lib/hazelcast-$i."*; do
               file=${file##*hazelcast\-}
               HAZELCAST_VERSION=${file%.jar}
               break;
            done
            #break;
         fi
      #done
   fi
fi
if [ "$HAZELCAST_VERSION" != "" ]; then
   HAZELCAST_MAJOR_VERSION_NUMBER=${HAZELCAST_VERSION:0:1}
   CLASSPATH="$PADOGRID_HOME/hazelcast/plugins/*:$PADOGRID_HOME/hazelcast/lib/*:$PADOGRID_HOME/hazelcast/plugins/v$HAZELCAST_MAJOR_VERSION_NUMBER/*:$PADOGRID_HOME/hazelcast/lib/v$HAZELCAST_MAJOR_VERSION_NUMBER/*:$CLASSPATH"
fi
CLASSPATH=$APP_DIR/plugins/*:$APP_DIR/lib/*:$CLASSPATH
