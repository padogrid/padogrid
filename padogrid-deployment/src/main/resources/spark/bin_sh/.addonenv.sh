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

# -------------------------------------------------------------------------------
# Source in .argenv.sh to set all default variables. This call is required.
# IMPORTANT: Do NOT remove this call.
# -------------------------------------------------------------------------------
. $SCRIPT_DIR/.argenv.sh "$@"
. $SCRIPT_DIR/.utilenv_spark.sh "$@"

#
# Source in setenv.sh that contains common variables
#
if [ -f "$SCRIPT_DIR/setenv.sh" ]; then
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

# ----------------------------------------------------------------------------------------------------
# CORE ENVIRONMENT VARIABLES:
# ----------------------------------------------------------------------------------------------------
# The following describes the core environment variables that are typically overriden in the
# 'setenv.sh' file by the user.
#
# Required:
# ---------
# SPARK_HOME         Spark root directory path
#
# Optional:
# ---------
# JAVA_OPTS              Any Java options such as standard and non-standard (-XX) options,
#                        system properties (-D), etc.
# CLASSPATH              Class paths that includes your server components such as data (domain) classes.
#                        This will be prepended to the padogrid class paths.
# DEFAULT_SPARK_MAJOR_VERSION_NUMBER  The default Spark major version number. This value is
#                        sparingly used by scripts that can be run without having a Spark product
#                        installed.
# DEFAULT_CLUSTER        The default cluster name. The default cluster can be managed without
#                        specifying the '-cluster' command option. Default: myspark
# DEFAULT_MASTER_MIN_HEAP_SIZE  Default master minimum heap size. Used initially when the cluster
#                        is created.
# DEFAULT_MASTER_MAX_HEAP_SIZE  Default master maximum heap size.
# ----------------------------------------------------------------------------------------------------

# Default Spark major version number
DEFAULT_SPARK_MAJOR_VERSION_NUMBER=3

# 
# Default Cluster - If the -cluster option is not specified in any of the commands, then
# the commands default to this cluster.
#
DEFAULT_CLUSTER="$DEFAULT_SPARK_CLUSTER"

#
# Default master heap min/max sizes. These values are initially set in $ETC_DIR/cluster.properties
# when a new cluster is created using the 'create_cluster" command. All members in 
# the cluster share the same sizes. You can change them later in the cluster.properties
# file.
#
DEFAULT_MASTER_MIN_HEAP_SIZE=512m
DEFAULT_MASTER_MAX_HEAP_SIZE=512m

# ----------------------------------------------------------------------------------------------------
# NON-CORE ENVIROMENT VARIABLES:
# ----------------------------------------------------------------------------------------------------

#
# Default master TCP start port. The value of ($MASTER_NUM-1) is added to the start port number to
# determine the master's TCP port number.
#
DEFAULT_MASTER_START_PORT=7077

#
# Default member TCP start port. The value of ($MEMBER_NUM-1) is added to the start port number to
# determine the member's TCP port number.
#
DEFAULT_MEMBER_START_PORT=60000

#
# Enable/disable Java remote debugging
# The port number is incremented by 1 starting from $DEBUG_START_PORT
#
DEFAULT_MASTER_DEBUG_ENABLED=false

#
# Enable/disable JMX
#
DEFAULT_MASTER_JMX_ENABLED=false

#
# Default Web UI port numbers. This values are initially set in $ETC_DIR/cluster.properties
# when a new cluster is created using the 'create_cluster' command. You can change them later
# in the cluster.properties file.
#
DEFAULT_MASTER_WEBUI_START_PORT=8580
DEFAULT_MEMBER_WEBUI_START_PORT=8581

# 
# Debug start port number. The ($MEMBER_NUM-1) is added to the start port number to
# determine the member's debug port number.
#
DEFAULT_MASTER_DEBUG_START_PORT=9601
DEFAULT_DEBUG_START_PORT=9701

# 
# Default JMX start port number. The ($MEMBER_NUM-1) is added to the JMX start port number to
# determine the member's JMX port number.
#
DEFAULT_MASTER_JMX_START_PORT=12601
DEFAULT_JMX_START_PORT=12501

#
# Default PROMETHEUS enable/disable flag.
#
DEFAULT_MASTER_PROMETHEUS_ENABLED=false

# 
# Default PROMETHEUS start port number. The ($MEMBER_NUM-1) is added to the Prometheus start port number to
# determine the member's Prometheus port number.
#
DEFAULT_MASTER_PROMETHEUS_START_PORT=8591
DEFAULT_PROMETHEUS_START_PORT=8691

#
# The max number of masters per cluster. The port number ranges are determined by this value.
# by this value. Defalut master port numbers begin from DEFAULT_MASTER_START_PORT and end at 
# DEFAULT_MASTER_START_PORT+MAX_MASTER_COUNT-1.
#
MAX_MASTER_COUNT=5

# Spark config file paths
CONFIG_FILE=$ETC_DIR/conf-env.sh

#
# log4j logging
#
LOG4J_FILE="$ETC_DIR/log4j.properties"
if [[ ${OS_NAME} == CYGWIN* ]]; then
   LOG4J_FILE="$(cygpath -wp "$LOG4J_FILE")"
fi
LOG_PROPERTIES="-Dlog4j.configurationFile=$LOG4J_FILE"

#
# PATH
#
export PATH="$SCRIPT_DIR:$SCRIPT_DIR/tools:$PADOGRID_HOME/bin_sh:$SPARK_HOME/sbin:$SPARK_HOME/bin:$PATH"

#
# SPARK_VERSION/PRODUCT_VERSION: Determine the Spark version
#
SPARK_VERSION=""
IS_SPARK_ENTERPRISE=false
if [ "$SPARK_HOME" != "" ]; then
   file=$(basename $SPARK_HOME)
   file=${file#*spark\-}
   SPARK_VERSION=${file%-bin*}
   SPARK_MAJOR_VERSION_NUMBER=`expr "$SPARK_VERSION" : '\([0-9]*\)'`
   PRODUCT_VERSION=$SPARK_VERSION
   PRODUCT_MAJOR_VERSION=$SPARK_MAJOR_VERSION_NUMBER
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

# Exclude slf4j and log4j included in PadoGrid distribution
for i in $PADOGRID_HOME/lib/*; do
  if [[ "$i" != *"slf4j"* ]] && [[ "$i" != *"log4j"* ]]; then
     __CLASSPATH="$__CLASSPATH:$i"
  fi
done

__CLASSPATH="$__CLASSPATH:$SPARK_HOME/jars/*"
export CLASSPATH="$__CLASSPATH"

#
# Source in cluster specific setenv.sh
#
RUN_SCRIPT=
if [ -f "$CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh" ] && [ "$1" != "-options" ]; then
   . "$CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh"
fi
