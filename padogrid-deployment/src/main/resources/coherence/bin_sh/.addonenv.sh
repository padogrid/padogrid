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
# COHERENCE_HOME         Coherence root directory path
#
# Optional:
# ---------
# JAVA_OPTS              Any Java options such as standard and non-standard (-XX) options,
#                        system properties (-D), etc.
# CLASSPATH              Class paths that includes your server components such as data (domain) classes.
#                        This will be prepended to the padogrid class paths.
# DEFAULT_COHERENCE_MAJOR_VERSION_NUMBER  The default Coherence major version number. This value is
#                        sparingly used by scripts that can be run without having a Coherence product
#                        installed.
# DEFAULT_CLUSTER        The default cluster name. The default cluster can be managed without
#                        specifying the '-cluster' command option. Default: mycoherence
# ----------------------------------------------------------------------------------------------------

# Default Coherence major version number
DEFAULT_COHERENCE_MAJOR_VERSION_NUMBER=14

# 
# Default Cluster - If the -cluster option is not specified in any of the commands, then
# the commands default to this cluster.
#
DEFAULT_CLUSTER="$DEFAULT_COHERENCE_CLUSTER"

#
# Default member TCP start port. The value of ($MEMBER_NUM-1) is added to the start port number to
# determine the member's TCP port number.
#
DEFAULT_MEMBER_START_PORT=9000

#
# Default REST API port for members
#
DEFAULT_MEMBER_HTTP_ENABLED=true
DEFAULT_MEMBER_HTTP_START_PORT=7080

# 
# Debug start port number. The ($MEMBER_NUM-1) is added to the start port number to
# determine the member's debug port number.
#
DEFAULT_DEBUG_START_PORT=9501

# 
# Default JMX start port number. The ($MEMBER_NUM-1) is added to the JMX start port number to
# determine the member's JMX port number.
#
DEFAULT_JMX_START_PORT=12401

# 
# Default PROMETHEUS start port number. The ($MEMBER_NUM-1) is added to the Prometheus start port number to
# determine the member's Prometheus port number.
#
DEFAULT_PROMETHEUS_START_PORT=8391

# Coherence config file paths
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
LOG4J_FILE="$ETC_DIR/log4j2.properties"
if [[ ${OS_NAME} == CYGWIN* ]]; then
   LOG4J_FILE="$(cygpath -wp "$LOG4J_FILE")"
fi
LOG_PROPERTIES="-Dlog4j.configurationFile=$LOG4J_FILE"

export PATH="$PADOGRID_HOME/$PRODUCT_NAME/bin_sh:$PADOGRID_HOME/$PRODUCT_NAME/bin_sh/tools:$PADOGRID_HOME/bin_sh:$COHERENCE_HOME/bin:$PATH"

#
# COHERENCE_VERSION/PRODUCT_VERSION: Determine the Coherence version
#
COHERENCE_VERSION=""
IS_COHERENCE_ENTERPRISE=false
if [ -f "$COHERENCE_HOME/product.xml" ]; then
   COHERENCE_VERSION=$(grep "version value" "$COHERENCE_HOME/product.xml" | sed -e 's/^.*="//' -e 's/".*//')
fi
COHERENCE_MAJOR_VERSION_NUMBER=`expr "$COHERENCE_VERSION" : '\([0-9]*\)'`
PRODUCT_VERSION=$COHERENCE_VERSION
PRODUCT_MAJOR_VERSION=$COHERENCE_MAJOR_VERSION_NUMBER

#
# CLASSPATH
#
__CLASSPATH=""
if [ "$CLASSPATH" != "" ]; then
   __CLASSPATH="$CLASSPATH"
fi
# include the etc dir in the class path (required by coherence for picking up the config files)
if [ "$__CLASSPATH" == "" ]; then
   __CLASSPATH="$ETC_DIR:$CLUSTER_DIR/plugins/*:$CLUSTER_DIR/lib/*"
else
   __CLASSPATH="$__CLASSPATH:$ETC_DIR:$CLUSTER_DIR/plugins/*:$CLUSTER_DIR/lib/*"
fi
if [ "$PADOGRID_WORKSPACE" != "" ] && [ "$PADOGRID_WORKSPACE" != "$BASE_DIR" ]; then
   __CLASSPATH="$__CLASSPATH:$PADOGRID_WORKSPACE/plugins/*:$PADOGRID_WORKSPACE/lib/*"
fi
__CLASSPATH="$__CLASSPATH:$BASE_DIR/plugins/*:$BASE_DIR/lib/*"
__CLASSPATH="$__CLASSPATH:$PADOGRID_HOME/lib/*"
#__CLASSPATH="$__CLASSPATH:$COHERENCE_HOME/lib/coherence.jar:$COHERENCE_HOME/lib/jline.jar"
__CLASSPATH="$__CLASSPATH:$COHERENCE_HOME/lib/*"
export CLASSPATH="$__CLASSPATH"

#
# Source in cluster specific setenv.sh
#
RUN_SCRIPT=
if [ -f "$CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh" ] && [ "$1" != "-options" ]; then
   . "$CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh"
fi
