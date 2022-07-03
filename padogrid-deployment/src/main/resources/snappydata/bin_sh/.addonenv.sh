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

if [ "$1" == "-script_dir" ]; then
   SCRIPT_DIR="$2"
else
   SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
fi
BASE_DIR="$(dirname "$SCRIPT_DIR")"

# ----------------------------------------------------------------------------------------------------
# CORE ENVIRONMENT VARIABLES:
# ----------------------------------------------------------------------------------------------------
# The following describes the core environment variables that are typically overriden in the
# 'setenv.sh' file by the user.
#
# Required:
# ---------
# SNAPPYDATA_HOME        SnappyData root directory path
#
# Optional:
# ---------
# JAVA_OPTS              Any Java options such as standard and non-standard (-J-XX) options,
#                        system properties (-J-D), etc.
# CLASSPATH              Class paths that includes your server components such as data (domain) classes.
#                        This will be prepended to the padogrid class paths.
# DEFAULT_SNAPPYDATA_MAJOR_VERSION_NUMBER  The default SnappyData major version number. This value is
#                        sparingly used by scripts that can be run without having a SnappyData product
#                        installed.
# DEFAULT_CLUSTER        The default cluster name. The default cluster can be managed without
#                        specifying the '-cluster' command option. Default: mygeode
# DEFAULT_LOCATOR_MIN_HEAP_SIZE  Default locator minimum heap size. Used initially when the cluster
#                        is created.
# DEFAULT_LOCATOR_MAX_HEAP_SIZE  Default locator maximum heap size.
# ----------------------------------------------------------------------------------------------------

# 
# SnappyData home directory
#
#SNAPPYDATA_HOME=

# Default SnappyData major version number
DEFAULT_SNAPPYDATA_MAJOR_VERSION_NUMBER=1

# 
# Default Cluster - If the -cluster option is not specified in any of the commands, then
# the commands default to this cluster.
#
DEFAULT_CLUSTER="$DEFAULT_SNAPPYDATA_CLUSTER"

#
# Default heap min/max sizes. These values are initially set in $ETC_DIR/cluster.properties
# when a new cluster is created using the 'create_cluster" command. All members in 
# the cluster share the same sizes. You can change them later in the cluster.properties
# file.
#
DEFAULT_LOCATOR_MIN_HEAP_SIZE=512m
DEFAULT_LOCATOR_MAX_HEAP_SIZE=512m

# ----------------------------------------------------------------------------------------------------
# NON-CORE ENVIROMENT VARIABLES:
# ----------------------------------------------------------------------------------------------------

#
# Default locator TCP start port. The value of ($LOCATOR_NUM-1) is added to the start port number to
# determine the locator's TCP port number.
#
DEFAULT_LOCATOR_START_PORT=10334

#
# Default client TCP start port.
#
DEFAULT_CLIENT_PORT=1527

#
# Default member TCP start port. The value of ($MEMBER_NUM-1) is added to the start port number to
# determine the member's TCP port number.
#
DEFAULT_MEMBER_START_PORT=40404

#
# Default Spark UI port (Pulse SnappyData Monitoring).
#
DEFAULT_SPARK_UI_PORT=5050

#
# Default Spark job server port.
#
DEFAULT_SPARK_JOBSERVER_PORT=8090

#
# Enable/disable Java remote debugging
# The port number is incremented by 1 starting from $DEBUG_START_PORT
#
DEFAULT_LOCATOR_DEBUG_ENABLED=false
DEFAULT_LEADER_DEBUG_ENABLED=false

#
# Enable/disable JMX
#
DEFAULT_LOCATOR_JMX_ENABLED=false
DEFAULT_LEADER_JMX_ENABLED=false

#
# Default Pulse port numbers. These values are initially set in $ETC_DIR/cluster.properties
# when a new cluster is created using the 'create_cluster' command. You can change them later
# in the cluster.properties file.
#
DEFAULT_JMX_MANAGER_HTTP_START_PORT=7070
DEFAULT_JMX_MANAGER_START_PORT=9051

#
# Default REST API port for members
#
DEFAULT_MEMBER_HTTP_ENABLED=true
DEFAULT_MEMBER_HTTP_START_PORT=7080

# 
# Debug start port number. The ($MEMBER_NUM-1) is added to the start port number to
# determine the member's debug port number.
#
DEFAULT_LOCATOR_DEBUG_START_PORT=9201
DEFAULT_LEADER_DEBUG_START_PORT=9206
DEFAULT_DEBUG_START_PORT=9101

# 
# Default JMX start port number. The ($MEMBER_NUM-1) is added to the JMX start port number to
# determine the member's JMX port number.
#
DEFAULT_LOCATOR_JMX_START_PORT=12101
DEFAULT_LEADER_JMX_START_PORT=12106
DEFAULT_JMX_START_PORT=12001

#
# Default PROMETHEUS enable/disable flag.
#
DEFAULT_LOCATOR_PROMETHEUS_ENABLED=false
DEFAULT_LEADER_PROMETHEUS_ENABLED=false

# 
# Default PROMETHEUS start port number. The ($MEMBER_NUM-1) is added to the Prometheus start port number to
# determine the member's Prometheus port number.
#
DEFAULT_LOCATOR_PROMETHEUS_START_PORT=8191
DEFAULT_LEADER_PROMETHEUS_START_PORT=8196
DEFAULT_PROMETHEUS_START_PORT=8091

#
# The max number of locators per cluster. The port number ranges are determined by this value.
# by this value. Defalut locator port numbers begin from DEFAULT_LOCATOR_START_PORT and end at 
# DEFAULT_LOCATOR_START_PORT+MAX_LOCATOR_COUNT-1.
#
MAX_LOCATOR_COUNT=5

#
# The max number of leaders per cluster. The port number ranges are determined by this value.
# by this value. Defalut locator port numbers begin from DEFAULT_LEADER_START_PORT and end at 
# DEFAULT_LEADER_START_PORT+MAX_LEADER_COUNT-1.
#
MAX_LEADER_COUNT=5

#
# The max number of members per cluster. The port number ranges are determined
# by this value. All default port numbers begin from DEFAULT_*_START_PORT and end at 
# DEFAULT_*_START_PORT+MAX_MEMBER_COUNT-1.
#
MAX_MEMBER_COUNT=99

# -------------------------------------------------------------------------------
# Source in .argenv.sh to set all default variables. This call is required.
# IMPORTANT: Do NOT remove this call.
# -------------------------------------------------------------------------------
. $SCRIPT_DIR/.argenv.sh "$@"
. $SCRIPT_DIR/.utilenv_snappydata.sh

# -----------------------------------------------------
# IMPORTANT: Do NOT modify below this line
# -----------------------------------------------------

LOCATOR_OPTS=""
LEAD_OPTS=""
MEMBER_OPTS=""

#
# Source in setenv.sh that contains common variables
#
if [ -f $SCRIPT_DIR/setenv.sh ]; then
   # CLUSTER and POD options override setenv.sh
   __CLUSTER=$CLUSTER
   __POD=$POD

   . $SCRIPT_DIR/setenv.sh

   if [ "$CLUSTER_SPECIFIED" == "true" ]; then
      CLUSTER=$__CLUSTER
   fi
   if [ "$POD_SPECIFIED" == "true" ]; then
      POD=$__POD
   fi
fi

# STATS_DIR
STATS_DIR=$CLUSTERS_DIR/$CLUSTER/stats

# Geode config file paths
CONFIG_FILE=$ETC_DIR/cache.xml
CLIENT_CONFIG_FILE=$ETC_DIR/cache-client.xml

#
# log4j2 logging
#
LOG4J_FILE="$ETC_DIR/log4j2.properties"
if [[ ${OS_NAME} == CYGWIN* ]]; then
   LOG4J_FILE="$(cygpath -wp "$LOG4J_FILE")"
fi
LOG_PROPERTIES="-J-Dlog4j.configurationFile=$LOG4J_FILE"

#
# PATH
#
export PATH="$SCRIPT_DIR:$PADOGRID_HOME/bin_sh:$SNAPPYDATA_HOME/bin:$SNAPPYDATA_HOME/sbin:$PATH"

# SPARK_HOME
# This must be set to prevent it from conflicting with a separate instance of Spark.
SPARK_HOME=$SNAPPYDATA_HOME

#
# SNAPPYDATA_VERSION/PROUDCT_VERSION: Determine the SnappyData version
#
IS_ENTERPRISE=false
if [ "$SNAPPYDATA_HOME" == "" ]; then
   SNAPPYDATA_VERSION=""
   SNAPPYDATA_MAJOR_VERSION_NUMBER=""
   PRODUCT_VERSION=""
   PRODUCT_MAJOR_VERSION=""
else
   for file in $SNAPPYDATA_HOME/jars/snappydata-core*; do
      file=${file##*snappydata\-core*\-}
      SNAPPYDATA_VERSION=${file%.jar}
   done
   SNAPPYDATA_MAJOR_VERSION_NUMBER=`expr "$SNAPPYDATA_VERSION" : '\([0-9]*\)'`
   PRODUCT_VERSION=$SNAPPYDATA_VERSION
   PRODUCT_MAJOR_VERSION=$SNAPPYDATA_MAJOR_VERSION_NUMBER
fi

#
# CLASSPATH
#
__CLASSPATH=""
if [ "$CLASSPATH" != "" ]; then
   __CLASSPATH="$CLASSPATH"
fi
if [ "$__CLASSPATH" == "" ]; then
   __CLASSPATH="$CLUSTER_DIR/plugins/*:$CLUSTER_DIR/lib/*"
else
   __CLASSPATH="$__CLASSPATH:$CLUSTER_DIR/plugins/*:$CLUSTER_DIR/lib/*"
fi
if [ "$PADOGRID_WORKSPACE" != "" ] && [ "$PADOGRID_WORKSPACE" != "$BASE_DIR" ]; then
   __CLASSPATH="$__CLASSPATH:$PADOGRID_WORKSPACE/plugins/*:$PADOGRID_WORKSPACE/lib/*"
fi
__CLASSPATH="$__CLASSPATH:$BASE_DIR/plugins/*:$BASE_DIR/lib/*"
__CLASSPATH="$__CLASSPATH:$PADOGRID_HOME/lib/*"
__CLASSPATH="$__CLASSPATH:$SNAPPYDATA_HOME/jars/*"
export CLASSPATH="$__CLASSPATH"

#
# Source in cluster specific setenv.sh
#
RUN_SCRIPT=
if [ -f "$CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh" ] && [ "$1" != "-options" ]; then
   . "$CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh"
fi
