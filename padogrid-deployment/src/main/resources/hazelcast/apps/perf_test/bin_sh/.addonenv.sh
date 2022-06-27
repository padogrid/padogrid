#!/usr/bin/env bash

# ========================================================================
# Copyright (c) 2020 Netcrest Technologies, LLC. All rights reserved.
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

# k8s pod
if [ "$NAMESPACE" != "" ] && [ "$HAZELCAST_SERVICE" != "" ]; then
   if [ "$HAZELCAST_MAJOR_VERSION_NUMBER" == "3" ]; then
      if [ "$HAZELCAST_GROUP_NAME" == "" ]; then
          HAZELCAST_GROUP_NAME="dev"
      fi
      K8S_PROPERTIES="-Dk8s.hazelcast.service=$HAZELCAST_SERVICE -Dk8s.namespace=$NAMESPACE -Dk8s.hazelcast.group.name=$HAZELCAST_GROUP_NAME"
   else
      if [ "$HAZELCAST_CLUSTER_NAME" == "" ]; then
          HAZELCAST_CLUSTER_NAME="dev"
      fi
      K8S_PROPERTIES="-Dk8s.hazelcast.service=$HAZELCAST_SERVICE -Dk8s.namespace=$NAMESPACE -Dk8s.hazelcast.cluster.name=$HAZELCAST_CLUSTER_NAME"
   fi
   HAZELCAST_CLIENT_CONFIG_FILE=$APP_ETC_DIR/hazelcast-client-k8s.xml
else
   HAZELCAST_CLIENT_CONFIG_FILE=$APP_ETC_DIR/hazelcast-client.xml
fi
HAZELCAST_CLIENT_FAILOVER_CONFIG_FILE=$APP_ETC_DIR/hazelcast-client-failover.xml
LOG_CONFIG_FILE=$APP_ETC_DIR/log4j2.properties
export LOG_DIR=$APP_DIR/log

if [[ ${OS_NAME} == CYGWIN* ]]; then
   HAZELCAST_CLIENT_CONFIG_FILE="$(cygpath -wp "$HAZELCAST_CLIENT_CONFIG_FILE")"
   HAZELCAST_CLIENT_FAILOVER_CONFIG_FILE="$(cygpath -wp "$HAZELCAST_CLIENT_FAILOVER_CONFIG_FILE")"
   LOG_CONFIG_FILE="$(cygpath -wp "$LOG_CONFIG_FILE")"
   export LOG_DIR="$(cygpath -wp "$LOG_DIR")"
fi

# Source in app specifics
. $APP_DIR/bin_sh/setenv.sh

# Log properties for log4j2. The log file name is set in executable scripts.
JAVA_OPTS="$JAVA_OPTS -Dhazelcast.logging.type=log4j2 \
-Dlog4j.configurationFile=$LOG_CONFIG_FILE"

# k8s
if [ "$K8S_PROPERTIES" != "" ]; then
   JAVA_OPTS="$JAVA_OPTS $K8S_PROPERTIES"
fi

# Set Hazelcast addon class path. This is to handle 'none' product.
if [[ "$CLASSPATH" != *"$PADOGRID_HOME/hazelcast/plugins"* ]]; then
   if [ "$HAZELCAST_VERSION" != "" ]; then
      MAJOR_VERSION_NUMBER=${HAZELCAST_VERSION:0:1}
      CLASSPATH="$PADOGRID_HOME/hazelcast/plugins/*:$PADOGRID_HOME/hazelcast/lib/*:$PADOGRID_HOME/hazelcast/plugins/v$MAJOR_VERSION_NUMBER/*:$PADOGRID_HOME/hazelcast/lib/v$MAJOR_VERSION_NUMBER/*:$CLASSPATH"
   fi
fi
