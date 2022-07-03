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
# REDIS_HOME         Redis root directory path
#
# Optional:
# ---------
# JAVA_OPTS              Any Java options such as standard and non-standard (-XX) options,
#                        system properties (-D), etc.
# CLASSPATH              Class paths that includes your server components such as data (domain) classes.
#                        This will be prepended to the padogrid class paths.
# DEFAULT_REDIS_MAJOR_VERSION_NUMBER  The default Redis major version number. This value is
#                        sparingly used by scripts that can be run without having a Redis product
#                        installed.
# DEFAULT_CLUSTER        The default cluster name. The default cluster can be managed without
#                        specifying the '-cluster' command option. Default: myredis
# DEFAULT_MIN_HEAP_SIZE  Default minimum heap size. Used initially when the cluster is created.
#                        The heap sizes can be changed in clusters/<cluster>/etc/cluster.properties.
# DEFAULT_MAX_HEAP_SIZE  Maximum heap size. Used initially when the cluster is created.  
# ----------------------------------------------------------------------------------------------------


# Default Redis major version number
DEFAULT_REDIS_MAJOR_VERSION_NUMBER=7

# 
# Default Cluster - If the -cluster option is not specified in any of the commands, then
# the commands default to this cluster.
#
DEFAULT_CLUSTER="$DEFAULT_REDIS_CLUSTER"

#
# Default member TCP start port. The value of ($MEMBER_NUM-1) is added to the start port number to
# determine the member's TCP port number.
#
DEFAULT_MEMBER_START_PORT=6379

#
# Default REST API port for members
#
DEFAULT_MEMBER_HTTP_ENABLED=true
DEFAULT_MEMBER_HTTP_START_PORT=7080

#
# Default number of Redis node rpelicas
#
DEFAULT_REPLICAS=1

#
# The max number of members per cluster. The port number ranges are determined
# by this value. All default port numbers begin from DEFAULT_*_START_PORT and end at 
# DEFAULT_*_START_PORT+MAX_MEMBER_COUNT-1.
#
MAX_MEMBER_COUNT=20

#
# Max number of Redis node replicas
#
MAX_REPLICAS=3

# -------------------------------------------------------------------------------
# Source in .argenv.sh to set all default variables. This call is required.
# IMPORTANT: Do NOT remove this call.
# -------------------------------------------------------------------------------
. $SCRIPT_DIR/.argenv.sh "$@"
. $SCRIPT_DIR/.utilenv_redis.sh "$@"

# -----------------------------------------------------
# IMPORTANT: Do NOT modify below this line
# -----------------------------------------------------

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

# Redis config file paths
CONFIG_FILE=$ETC_DIR/redis.conf
CLIENT_CONFIG_FILE=$ETC_DIR/redis-client.conf

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

#
# PATH
#
export PATH="$SCRIPT_DIR:$PADOGRID_HOME/bin_sh:$REDIS_HOME/bin:$REDIS_HOME/src:$PATH"

#
# REDIS_VERSION/PRODUCT_VERSION: Determine the Redis version
#
REDIS_VERSION=""
IS_REDIS_ENTERPRISE=false
# Redis may be unavailable during build. If so, ignore.
if [ "$(which redis-server 2> /dev/null)" != "" ]; then
   REDIS_VERSION=$(redis-server -v | sed -e 's/^.*v=//' -e 's/ .*//')
fi
REDIS_MAJOR_VERSION_NUMBER=`expr "$REDIS_VERSION" : '\([0-9]*\)'`
PRODUCT_VERSION=$REDIS_VERSION
PRODUCT_MAJOR_VERSION=$REDIS_MAJOR_VERSION_NUMBER

#
# CLASSPATH
#
__CLASSPATH=""
if [ "$CLASSPATH" != "" ]; then
   __CLASSPATH="$CLASSPATH"
fi
# include the etc dir in the class path (required by redis for picking up the config files)
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
#__CLASSPATH="$__CLASSPATH:$REDIS_HOME/lib/*"
export CLASSPATH="$__CLASSPATH"

#
# Source in cluster specific setenv.sh
#
RUN_SCRIPT=
if [ -f $CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh ] && [ "$1" != "-options" ]; then
   . $CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh
fi
