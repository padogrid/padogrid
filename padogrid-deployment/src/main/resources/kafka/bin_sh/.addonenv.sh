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
. $SCRIPT_DIR/.utilenv_kafka.sh "$@"

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
# KAFKA_HOME      Kafka root directory path
#
# Optional:
# ---------
# JAVA_OPTS              Any Java options such as standard and non-standard (-XX) options,
#                        system properties (-D), etc.
# CLASSPATH              Class paths that includes your server components such as data (domain) classes.
#                        This will be prepended to the padogrid class paths.
# DEFAULT_KAFKA_MAJOR_VERSION_NUMBER  The default Kafka major version number. This value is
#                        sparingly used by scripts that can be run without having a Kafka product
#                        installed.
# DEFAULT_CLUSTER        The default cluster name. The default cluster can be managed without
#                        specifying the '-cluster' command option. Default: mykafka
# ----------------------------------------------------------------------------------------------------

# Default Kafka major version number
DEFAULT_KAFKA_MAJOR_VERSION_NUMBER=3

# 
# Default Cluster - If the -cluster option is not specified in any of the commands, then
# the commands default to this cluster.
#
DEFAULT_CLUSTER="$DEFAULT_KAFKA_CLUSTER"

# ----------------------------------------------------------------------------------------------------
# NON-CORE ENVIROMENT VARIABLES:
# ----------------------------------------------------------------------------------------------------

#
# Default member TCP start port. The value of ($MEMBER_NUM-1) is added to the start port number to
# determine the member's TCP port number.
#
DEFAULT_MEMBER_START_PORT=9092

# 
# Debug start port number. The ($MEMBER_NUM-1) is added to the start port number to
# determine the member's debug port number.
#
DEFAULT_DEBUG_START_PORT=9801

# 
# Default JMX start port number. The ($MEMBER_NUM-1) is added to the JMX start port number to
# determine the member's JMX port number.
#
DEFAULT_JMX_START_PORT=12701

# 
# Default PROMETHEUS start port number. The ($MEMBER_NUM-1) is added to the Prometheus start port number to
# determine the member's Prometheus port number.
#
DEFAULT_PROMETHEUS_START_PORT=8791

#
# Default HTTP port (Confluent)
#
DEFAULT_MEMBER_HTTP_START_PORT=8090

# Kafka config file paths
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
# PATH Depends on PRODUCT_HOME due to switch_workspace which does not have cluster info.
# We need to change that accordingly here.
# Also, set PRODUCT to "kafka" to override "confluent". This is required due to both products
# sharing the same resources under the name "kafka".
# 6/29/22 - CLUSTER_TYPE setting done here is removed. This may affect older versions of PadoGrid.
export PRODUCT="kafka"
#if [ "$CLUSTER_TYPE_SPECIFIED" == "false" ]; then
#   if [[ "$PRODUCT_HOME" == *"confluent"* ]]; then
#      export CLUSTER_TYPE="confluent"
#   elif [[ "$PRODUCT_HOME" == *"kafka"* ]]; then
#      export CLUSTER_TYPE="kafka"
#   fi
#fi


IS_KAFKA_ENTERPRISE=false
if [ "$CLUSTER_TYPE_ARG" == "confluent" ] || [ "$CLUSTER_TYPE" == "confluent" ]; then
   if [ -f "$CONFLUENT_HOME/bin/confluent-hub" ]; then
      IS_KAFKA_ENTERPRISE=true
   fi
   export CLUSTER_TYPE="confluent"
   export PRODUCT_HOME="$CONFLUENT_HOME"
   export PATH="$SCRIPT_DIR:$SCRIPT_DIR/tools:$PADOGRID_HOME/bin_sh:$CONFLUENT_HOME/bin:$PATH"
else
   IS_KAFKA_ENTERPRISE=false
   export CLUSTER_TYPE="kafka"
   export PRODUCT_HOME="$KAFKA_HOME"
   export PATH="$SCRIPT_DIR:$SCRIPT_DIR/tools:$PADOGRID_HOME/bin_sh:$KAFKA_HOME/bin:$PATH"
fi

#
# KAFKA_VERSION/PRODUCT_VERSION: Determine the Kafka version
# Geode and GemFire share the same 'kafka' prefix for jar names.
#
KAFKA_VERSION=""
if [ "$PRODUCT_HOME" == "" ]; then
   KAFKA_VERSION=""
   KAFKA_MAJOR_VERSION_NUMBER=""
elif [ "$CLUSTER_TYPE" == "confluent" ]; then
   file=$(basename $CONFLUENT_HOME)
   file=${file#*confluent-}
   KAFKA_VERSION=${file#*\-}
   KAFKA_MAJOR_VERSION_NUMBER=`expr "$KAFKA_VERSION" : '\([0-9]*\)'`
else
   file=$(basename $KAFKA_HOME)
   file=${file#*kafka_}
   KAFKA_VERSION=${file#*\-}
   KAFKA_MAJOR_VERSION_NUMBER=`expr "$KAFKA_VERSION" : '\([0-9]*\)'`
fi
PRODUCT_VERSION=$KAFKA_VERSION
PRODUCT_MAJOR_VERSION=$KAFKA_MAJOR_VERSION_NUMBER

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

# Exclude slf4j (included in kafka distribution)
for i in $PADOGRID_HOME/lib/*; do
  if [[ "$i" != *"slf4j"* ]]; then
     __CLASSPATH="$__CLASSPATH:$i"
  fi
done

if [ "$CLUSTER_TYPE" == "confluent" ]; then
   __CLASSPATH="$__CLASSPATH:$CONFLUENT_HOME/share/java/kafka/*"
else
   __CLASSPATH="$__CLASSPATH:$KAFKA_HOME/libs/*"
fi
export CLASSPATH="$__CLASSPATH"

#
# Source in cluster specific setenv.sh
#
RUN_SCRIPT=
if [ -f $CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh ] && [ "$1" != "-options" ]; then
   . $CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh
fi

#
# Aliases. Kafka scripts end with '.sh' and Confluent scripts do not use file extension.
#
KAFKA_SERVER_START="kafka-server-start"
KAFKA_STORAGE="kafka-storage"
if [ ! -f "$KAFKA_HOME/bin/$KAFKA_SERVER_START" ]; then
   KAFKA_SERVER_START=${KAFKA_SERVER_START}.sh
   KAFKA_STORAGE=${KAFKA_STORAGE}.sh
fi
