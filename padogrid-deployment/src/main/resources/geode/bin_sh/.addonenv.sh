#!/usr/bin/env bash

# ========================================================================
# Copyright (c) 2020-2024 Netcrest Technologies, LLC. All rights reserved.
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
. $PADOGRID_HOME/bin_sh/.argenv.sh "$@"

#
# Source in the target product utilenv
#
PRODUCT_NAME=$(getCommonProductName $PRODUCT_ARG)
if [ "$PRODUCT_NAME" == "" ]; then
   PRODUCT_NAME="$PRODUCT"
fi
if [ -f "$PADOGRID_HOME/$PRODUCT_NAME/bin_sh/.utilenv_$PRODUCT_NAME.sh" ]; then
   . $PADOGRID_HOME/$PRODUCT_NAME/bin_sh/.utilenv_$PRODUCT_NAME.sh "$@"
fi

#
# Source in setenv.sh that contains user configured variables
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

# ----------------------------------------------------------------------------------------------------
# CORE ENVIRONMENT VARIABLES:
# ----------------------------------------------------------------------------------------------------
# The following describes the core environment variables that are typically overriden in the
# 'setenv.sh' file by the user.
#
# Required:
# ---------
# GEODE_HOME         Geode root directory path
#
# Optional:
# ---------
# JAVA_OPTS              Any Java options such as standard and non-standard (--J=-XX) options,
#                        system properties (--J=-D), etc.
# CLASSPATH              Class paths that includes your server components such as data (domain) classes.
#                        This will be prepended to the padogrid class paths.
# DEFAULT_GEODE_MAJOR_VERSION_NUMBER  The default Geode major version number. This value is
#                        sparingly used by scripts that can be run without having a Geode product
#                        installed.
# DEFAULT_CLUSTER        The default cluster name. The default cluster can be managed without
#                        specifying the '-cluster' command option. Default: mygeode
# DEFAULT_LOCATOR_MIN_HEAP_SIZE  Default locator minimum heap size. Used initially when the cluster
#                        is created.
# DEFAULT_LOCATOR_MAX_HEAP_SIZE  Default locator maximum heap size.
# ----------------------------------------------------------------------------------------------------

# Default Geode major version number
DEFAULT_GEODE_MAJOR_VERSION_NUMBER=1

# 
# Default Cluster - If the -cluster option is not specified in any of the commands, then
# the commands default to this cluster.
#
DEFAULT_CLUSTER="$DEFAULT_GEODE_CLUSTER"

#
# Locator default min/max heap sizes. Member min/max heap sizes are imported from 'setenv.sh'
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
# Default member TCP start port. The value of ($MEMBER_NUM-1) is added to the start port number to
# determine the member's TCP port number.
#
DEFAULT_MEMBER_START_PORT=40404

#
# Enable/disable Java remote debugging
# The port number is incremented by 1 starting from $DEBUG_START_PORT
#
DEFAULT_LOCATOR_DEBUG_ENABLED=false

#
# Enable/disable JMX
#
DEFAULT_LOCATOR_JMX_ENABLED=false

#
# Enable/disable PadoWeb HTTPS
#
DEFAULT_PADOWEB_HTTPS_ENABLED=false

#
# Default PadoWeb host and ports. These values are initially set
# in $ETC_DIR/cluster.properties when a new cluster is created using the 'create_cluster'
# command. You can change them later in the cluster.properties file.
#
DEFAULT_PADOWEB_HOST=localhost
DEFAULT_PADOWEB_HTTP_PORT=8080
DEFAULT_PADOWEB_HTTPS_PORT=8443
DEFAULT_PADOWEB_CONTEXT_PATH="/"

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
DEFAULT_DEBUG_START_PORT=9101

# 
# Default JMX start port number. The ($MEMBER_NUM-1) is added to the JMX start port number to
# determine the member's JMX port number.
#
DEFAULT_LOCATOR_JMX_START_PORT=12101
DEFAULT_JMX_START_PORT=12001

#
# Default PROMETHEUS enable/disable flag.
#
DEFAULT_LOCATOR_PROMETHEUS_ENABLED=false

# 
# Default PROMETHEUS start port number. The ($MEMBER_NUM-1) is added to the Prometheus start port number to
# determine the member's Prometheus port number.
#
DEFAULT_LOCATOR_PROMETHEUS_START_PORT=8191
DEFAULT_PROMETHEUS_START_PORT=8091

#
# The max number of locators per cluster. The port number ranges are determined by this value.
# by this value. Defalut locator port numbers begin from DEFAULT_LOCATOR_START_PORT and end at 
# DEFAULT_LOCATOR_START_PORT+MAX_LOCATOR_COUNT-1.
#
MAX_LOCATOR_COUNT=5

# STATS_DIR
STATS_DIR=$CLUSTERS_DIR/$CLUSTER/stats

# Geode config file paths
CONFIG_FILE=$ETC_DIR/cache.xml
CLIENT_CONFIG_FILE=$ETC_DIR/cache-client.xml

#
# log4j2 logging
#
#if [[ ${OS_NAME} == CYGWIN* ]]; then
#   __ETC_DIR="$(cygpath -wp "$ETC_DIR")"
#else
#   __ETC_DIR=$ETC_DIR
#fi
#LOG4J_FILE="$ETC_DIR/log4j2.properties"
#if [[ ${OS_NAME} == CYGWIN* ]]; then
#   LOG4J_FILE="$(cygpath -wp "$LOG4J_FILE")"
#fi
#LOG_PROPERTIES="--J=-Dlog4j.configurationFile=$LOG4J_FILE"

# PATH Depends on PRODUCT_HOME due to switch_workspace which does not have cluster info.
# We need to change that accordingly here.
# Also, set PRODUCT to "geode" to override "gemfire". This is required due to both products
# sharing the same resources under the name "geode".
# 6/29/22 - CLUSTER_TYPE setting done here is removed. This may affect older versions of PadoGrid.
export PRODUCT="geode"
#if [ "$CLUSTER_TYPE_SPECIFIED" == "false" ]; then
#   if [[ "$PRODUCT_HOME" == *"gemfire"* ]]; then
#      export CLUSTER_TYPE="gemfire"
#   elif [[ "$PRODUCT_HOME" == *"geode"* ]]; then
#      export CLUSTER_TYPE="geode"
#   fi
#fi
__PATH="$PADOGRID_HOME/geode/bin_sh:$PADOGRID_HOME/geode/bin_sh/tools:$PADOGRID_HOME/bin_sh"
if [ "$CLUSTER_TYPE_ARG" == "gemfire" ] || [ "$CLUSTER_TYPE" == "gemfire" ]; then
   IS_GEODE_ENTERPRISE=true
   export CLUSTER_TYPE="gemfire"
   export PRODUCT_HOME="$GEMFIRE_HOME"
   export PATH="$__PATH:$GEMFIRE_HOME/bin:$PATH"
else
   IS_GEODE_ENTERPRISE=false
   export CLUSTER_TYPE="geode"
   export PRODUCT_HOME="$GEODE_HOME"
   export PATH="$__PATH:$GEODE_HOME/bin:$PATH"
fi
# Unset __PATH so that it is not consumed by commands such as switch_rwe
unset __PATH

#
# GEODE_VERSION/PRODUCT_VERSION: Determine the Geode/GemFire version
# Geode and GemFire share the same 'geode' prefix for jar names.
#
if [ "$PRODUCT_HOME" == "" ]; then
   GEODE_VERSION=""
   RUN_TYPE="default"
   GEODE_MAJOR_VERSION_NUMBER=""
   PRODUCT_VERSION=""
   PRODUCT_MAJOR_VERSION=""
else
   CORE_JAR=$(ls "$PRODUCT_HOME/lib/geode-core-"* 2> /dev/null)
   if [ "$CORE_JAR" == "" ]; then
      CORE_JAR=$(ls "$PRODUCT_HOME/lib/gemfire-core-"* 2> /dev/null)
   fi
   for file in "$CORE_JAR"; do
      file=${file##/*/}
      file=${file##*g*-core\-}
      GEODE_VERSION=${file%.jar}
   done
   if [ -f "$CLUSTER_DIR/bin_sh/import_csv" ]; then
      RUN_TYPE="pado"
   else
      RUN_TYPE="default"
   fi
   GEODE_MAJOR_VERSION_NUMBER=`expr "$GEODE_VERSION" : '\([0-9]*\)'`
   PRODUCT_VERSION=$GEODE_VERSION
   PRODUCT_MAJOR_VERSION=$GEODE_MAJOR_VERSION_NUMBER
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

if [ "$RUN_TYPE" == "pado" ]; then
   __CLASSPATH="$__CLASSPATH:$PADO_HOME/plugins/*"
fi
export CLASSPATH="$__CLASSPATH"

#
# Source in cluster specific setenv.sh
#
RUN_SCRIPT=
if [ -f "$CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh" ] && [ "$1" != "-options" ]; then
   . "$CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh"
fi
