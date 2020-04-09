#!/usr/bin/env bash

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
APP_DIR="$(dirname "$SCRIPT_DIR")"
APPS_DIR="$(dirname "$APP_DIR")"
BASE_DIR=$PADOGRID_HOME/$PRODUCT
pushd  $BASE_DIR/bin_sh > /dev/null 2>&1
. $BASE_DIR/bin_sh/.addonenv.sh
popd > /dev/null 2>&1

APP_ETC_DIR=$APP_DIR/etc
APP_LOG_DIR=$APP_DIR/log
if [ ! -d "$APP_LOG_DIR" ]; then
   mkdir -p "$APP_LOG_DIR"
fi

HAZELCAST_CLIENT_CONFIG_FILE=$APP_ETC_DIR/hazelcast-client.xml
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
