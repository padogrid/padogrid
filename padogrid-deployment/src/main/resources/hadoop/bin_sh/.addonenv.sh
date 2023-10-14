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
. $PADOGRID_HOME/$PRODUCT_NAME/bin_sh/.utilenv_$PRODUCT_NAME.sh "$@"

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
# HADOOP_HOME     Hadoop root directory path
#
# Optional:
# ---------
# JAVA_OPTS              Any Java options such as standard and non-standard (-XX) options,
#                        system properties (-D), etc.
# CLASSPATH              Class paths that includes your server components such as data (domain) classes.
#                        This will be prepended to the padogrid class paths.
# DEFAULT_HADOOP_MAJOR_VERSION_NUMBER  The default Hadoop major version number. This value is
#                        sparingly used by scripts that can be run without having a Hadoop product
#                        installed.
# DEFAULT_CLUSTER        The default cluster name. The default cluster can be managed without
#                        specifying the '-cluster' command option. Default: myhadoop
# DEFAULT_NAMENODE_MIN_HEAP_SIZE  Default namenode minimum heap size. Used initially when the cluster
#                        is created.
# DEFAULT_NAMENODE_MAX_HEAP_SIZE  Default namenode maximum heap size. 
# ----------------------------------------------------------------------------------------------------

# Default Hadoop major version number
DEFAULT_HADOOP_MAJOR_VERSION_NUMBER=3

DEFAULT_CLUSTER="$DEFAULT_HADOOP_CLUSTER"

#
# Default name node heap min/max sizes. These values are initially set in $ETC_DIR/cluster.properties
# when a new cluster is created using the 'create_cluster" command. All members in 
# the cluster share the same sizes. You can change them later in the cluster.properties
# file.
#
DEFAULT_NAMENODE_MIN_HEAP_SIZE=1g
DEFAULT_NAMENODE_MAX_HEAP_SIZE=1g


# ----------------------------------------------------------------------------------------------------
# NON-CORE ENVIROMENT VARIABLES:
# ----------------------------------------------------------------------------------------------------

#
# Default namenode TCP start port. The value of ($NAMENODE_NUM-1) is added to the start port number to
# determine the namenode's TCP port number.
#
DEFAULT_NAMENODE_START_PORT=19000

#Daemon                   Default Port  Configuration Parameter
#-----------------------  ------------ ----------------------------------
#Namenode                 50070        dfs.http.address
#Datanodes                50075        dfs.datanode.http.address
#Secondarynamenode        50090        dfs.secondary.http.address
#Backup/Checkpoint node?  50105        dfs.backup.http.address
#Jobracker                50030        mapred.job.tracker.http.address
#Tasktrackers             50060        mapred.task.tracker.http.address
#Daemon      Default Port        Configuration Parameter
#------------------------------------------------------------
#Namenode    8020                fs.default.name
#Datanode    50010               dfs.datanode.address
#Datanode    50020               dfs.datanode.ipc.address
#Backupnode  50100               dfs.backup.address

#
# Default member TCP start port. The value of ($MEMBER_NUM-1) is added to the start port number to
# determine the member's TCP port number.
#
DEFAULT_MEMBER_START_PORT=9866
DEFAULT_DATANODE_START_PORT=$DEFAULT_MEMBER_START_PORT

#
# Enable/disable Java remote debugging
# The port number is incremented by 1 starting from $DEBUG_START_PORT
#
DEFAULT_NAMENODE_DEBUG_ENABLED=false

#
# Enable/disable JMX
#
DEFAULT_NAMENODE_JMX_ENABLED=false

#
# Default HTTP port numbers. This values are initially set in $ETC_DIR/cluster.properties
# when a new cluster is created using the 'create_cluster' command. You can change them later
# in the cluster.properties file.
#
DEFAULT_NAMENODE_HTTP_START_PORT=9870
DEFAULT_MEMBER_HTTP_START_PORT=9864

# DataNode port number increment. DataNode port numbers between members are incremented by this number.
DEFAULT_DATANODE_PORT_INCR=100

#
# Additional Hadoop default port numbers.
#
DEFAULT_NAMENODE_SECONDARY_HTTP_PORT=9868
DEFAULT_NAMENODE_SECONDARY_HTTPS_PORT=9869
DEFAULT_DATANODE_PORT=9866
DEFAULT_DATANODE_HTTP_PORT=9864
DEFAULT_DATANODE_IPC_PORT=9867
DEFAULT_NAMENODE_HTTP_PORT=9870
DEFAULT_DATANODE_HTTPS_PORT=9865
DEFAULT_NAMENODE_HTTPS_PORT=9871
DEFAULT_NAMENODE_BACKUP_PORT=50100
DEFAULT_NAMENODE_BACKUP_HTTP_PORT=50105
DEFAULT_JOURNALNODE_RPC_PORT=8485
DEFAULT_JOURNALNODE_HTTP_PORT=8480
DEFAULT_JOURNALNODE_HTTPS_PORT=8481

# 
# Debug start port number. The ($MEMBER_NUM-1) is added to the start port number to
# determine the member's debug port number.
#
DEFAULT_NAMENODE_DEBUG_START_PORT=9951
DEFAULT_DEBUG_START_PORT=9901

# 
# Default JMX start port number. The ($MEMBER_NUM-1) is added to the JMX start port number to
# determine the member's JMX port number.
#
DEFAULT_NAMENODE_JMX_START_PORT=12851
DEFAULT_JMX_START_PORT=12801

#
# Default PROMETHEUS enable/disable flag.
#
DEFAULT_NAMENODE_PROMETHEUS_ENABLED=false

# 
# Default PROMETHEUS start port number. The ($MEMBER_NUM-1) is added to the Prometheus start port number to
# determine the member's Prometheus port number.
#
DEFAULT_NAMENODE_PROMETHEUS_START_PORT=8991
DEFAULT_PROMETHEUS_START_PORT=8891

#
# The max number of namenodes per cluster. The port number ranges are determined by this value.
# by this value. Defalut namenode port numbers begin from DEFAULT_NAMENODE_START_PORT and end at 
# DEFAULT_NAMENODE_START_PORT+MAX_NAMENODE_COUNT-1.
#
MAX_NAMENODE_COUNT=5

#
# log4j logging
#
LOG4J_FILE="$ETC_DIR/log4j.properties"
if [[ ${OS_NAME} == CYGWIN* ]]; then
   LOG4J_FILE="$(cygpath -wp "$LOG4J_FILE")"
fi
LOG_PROPERTIES="-Dlog4j.configurationFile=$LOG4J_FILE"

# PATH Depends on PRODUCT_HOME due to switch_workspace which does not have cluster info.
# We need to change that accordingly here.
export PRODUCT="hadoop"
export CLUSTER_TYPE="pseudo"
export PATH="$PADOGRID_HOME/$PRODUCT_NAME/bin_sh:$PADOGRID_HOME/$PRODUCT_NAME/bin_sh/tools:$PADOGRID_HOME/bin_sh:$HADOOP_HOME/sbin:$HADOOP_HOME/bin:$PATH"

#
# HADOOP_VERSION/PRODUCT_VERSION: Determine the Hadoop version
#
HADOOP_VERSION=""
IS_HADOOP_ENTERPRISE=false
if [ "$HADOOP_HOME" != "" ]; then
   file=$(basename $HADOOP_HOME)
   file=${file#*hadoop\-}
   HADOOP_VERSION=${file%-bin*}
   HADOOP_MAJOR_VERSION_NUMBER=`expr "$HADOOP_VERSION" : '\([0-9]*\)'`
   PRODUCT_VERSION=$HADOOP_VERSION
   PRODUCT_MAJOR_VERSION=$HADOOP_MAJOR_VERSION_NUMBER
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
__CLASSPATH="$__CLASSPATH:$HADOOP_HOME/lib/native/*"
__CLASSPATH="$__CLASSPATH:$HADOOP_HOME/share/hadoop/common/*"
__CLASSPATH="$__CLASSPATH:$HADOOP_HOME/share/hadoop/common/lib/*"
__CLASSPATH="$__CLASSPATH:$HADOOP_HOME/share/hadoop/mapreduce/*"
__CLASSPATH="$__CLASSPATH:$HADOOP_HOME/share/hadoop/client/*"
__CLASSPATH="$__CLASSPATH:$HADOOP_HOME/share/hadoop/tools/lib/*"
export CLASSPATH="$__CLASSPATH"

#
# Source in cluster specific setenv.sh
#
RUN_SCRIPT=
if [ -f $CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh ] && [ "$1" != "-options" ]; then
   . $CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh
fi
