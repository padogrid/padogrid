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
. $SCRIPT_DIR/.argenv.sh "$@"
. $SCRIPT_DIR/.utilenv_hazelcast.sh "$@"

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
# HAZELCAST_HOME         Hazelcast root directory path
#
# Optional:
# ---------
# JAVA_OPTS              Any Java options such as standard and non-standard (-XX) options,
#                        system properties (-D), etc.
# CLASSPATH              Class paths that includes your server components such as data (domain) classes.
#                        This will be prepended to the padogrid class paths.
# HAZELCAST_MC_HOME      Hazelcast Management Center directory path. This needs
#                        to be set only if you have the MC module separately installed.
# JET_MC_HOME            Hazelcast Jet Management Center directory path. This needs
#                        to be set only if you have the MC module separately installed.
# DEFAULT_HAZELCAST_MAJOR_VERSION_NUMBER  The default Hazelcast major version number. This value is
#                        sparingly used by scripts that can be run without having a Hazelcast product
#                        installed.
# DEFAULT_CLUSTER        The default IMDG cluster name. The default cluster can be managed without
#                        specifying the '-cluster' command option. Default: myhz
# DEFAULT_JET_CLUSTER    The default Jet cluster name. The default cluster can be managed without
#                        specifying the '-cluster' command option. Default: myjet  
# ----------------------------------------------------------------------------------------------------

# Default Hazelcast major version number
DEFAULT_HAZELCAST_MAJOR_VERSION_NUMBER=5

# 
# Default Cluster - If the -cluster option is not specified in any of the commands, then
# the commands default to this cluster.
#
DEFAULT_CLUSTER="$DEFAULT_HAZELCAST_CLUSTER"

#
# Enable/disable Management Center HTTPS
#
DEFAULT_MC_HTTPS_ENABLED=false

# ----------------------------------------------------------------------------------------------------
# NON-CORE ENVIROMENT VARIABLES:
# ----------------------------------------------------------------------------------------------------

#
# Default Hazelcast IMDG Management Center host and ports. These values are initially set
# in $ETC_DIR/cluster.properties when a new cluster is created using the 'create_cluster' 
# command. You can change them later in the cluster.properties file.
#
DEFAULT_MC_HOST=localhost
DEFAULT_MC_HTTP_PORT=8080
DEFAULT_MC_HTTPS_PORT=8443
DEFAULT_MC_JMX_PORT=9301
DEFAULT_MC_JMX_RMI_PORT=9351

# 
# Debug start port number. The ($MEMBER_NUM-1) is added to the start port number to
# determine the member's debug port number.
#
DEFAULT_DEBUG_START_PORT=9401

# 
# Default JMX start port number. The ($MEMBER_NUM-1) is added to the start port number to
# determine the member's debug port number.
#
DEFAULT_JMX_START_PORT=12201

# 
# Default PROMETHEUS start port number. The ($MEMBER_NUM-1) is added to the start port number to
# determine the member's debug port number.
#
DEFAULT_PROMETHEUS_START_PORT=8291

#
# The max number of members per cluster. The port number ranges are determined by this value.
# All port numbers begin from DEFAULT_*_START_PORT and end at DEFAULT_*_START_PORT+MAX_MEMBER_COUNT-1.
#
MAX_MEMBER_COUNT=20

#
# HAZELCAST_VERSION/PROUDCT_VERSION: Determine the Hazelcast version
#
HAZELCAST_VERSION=""
HAZELCAST_MC_VERSION=""
IS_HAZELCAST_ENTERPRISE=false

if [ "$CLUSTER_TYPE" == "jet" ]; then
   if [ "$HAZELCAST_HOME" != "" ]; then
     if [ `ls -1 "$HAZELCAST_HOME/lib/hazelcast-enterprise-all-"* 2>/dev/null | wc -l ` -gt 0 ]; then
         for file in "$HAZELCAST_HOME/lib/hazelcast-enterprise-all-"*; do
            file=${file##*hazelcast\-enterprise\-all\-}
            HAZELCAST_VERSION=${file%.jar}
         done
     elif [ `ls -1 "$HAZELCAST_HOME/lib/hazelcast-enterprise-"* 2>/dev/null | wc -l ` -gt 0 ]; then
         for file in "$HAZELCAST_HOME/lib/hazelcast-enterprise-"*; do
            file=${file##*hazelcast\-enterprise\-}
            HAZELCAST_VERSION=${file%.jar}
         done
     elif [ `ls -1 "$HAZELCAST_HOME/lib/hazelcast-all-"* 2>/dev/null | wc -l ` -gt 0 ]; then
         for file in "$HAZELCAST_HOME/lib/hazelcast-all-"*; do
            file=${file##*hazelcast\-all\-}
            HAZELCAST_VERSION=${file%.jar}
         done
      fi
   elif [ "$JET_HOME" != "" ]; then
      if [ `ls -1 "$JET_HOME/lib/hazelcast-jet-enterprise-all-"* 2>/dev/null | wc -l ` -gt 0 ]; then
         for file in "$JET_HOME/lib/hazelcast-jet-enterprise-all-"*; do
            file=${file##*hazelcast\-jet\-enterprise\-all\-}
            HAZELCAST_VERSION=${file%.jar}
            IS_HAZELCAST_ENTERPRISE=true
            break;
         done
      elif [ `ls -1 "$JET_HOME/lib/hazelcast-jet-enterprise-"* 2>/dev/null | wc -l ` -gt 0 ]; then
         for file in "$JET_HOME/lib/hazelcast-jet-enterprise-"*; do
            file=${file##*hazelcast\-jet\-enterprise\-}
            HAZELCAST_VERSION=${file%.jar}
            IS_HAZELCAST_ENTERPRISE=true
            break;
         done
      elif [ `ls -1 "$JET_HOME/lib/hazelcast-jet-"* 2>/dev/null | wc -l ` -gt 0 ]; then
         for file in "$JET_HOME/lib/hazelcast-jet-"*; do
            file=${file##*hazelcast\-jet\-}
            file=${file##*-}
            HAZELCAST_VERSION=${file%%.jar}
            break;
         done
      fi
   fi

   # Set Jet MC jar
   if [ "$JET_MC_HOME" != "" ]; then
      if [[ "$JET_MC_HOME" == **"202"** ]]; then
         for file in "$JET_MC_HOME/hazelcast-management-center-"*; do
            file=${file##*hazelcast\-management\-center\-}
            JET_MC_VERSION=${file%.jar}
            break;
         done
         JET_MC_JAR=hazelcast-management-center-${JET_MC_VERSION}.jar
      else
         # TODO: Drop the following support before 2021 ends
         for file in "$JET_MC_HOME/hazelcast-jet-management-center-"*; do
            file=${file##*hazelcast\-jet\-management\-center\-}
            JET_MC_VERSION=${file%.jar}
            break;
         done
         JET_MC_JAR=hazelcast-jet-management-center-${JET_MC_VERSION}.jar 
      fi
   fi
else
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
   if [ "$HAZELCAST_MC_HOME" != "" ]; then
      for file in "$HAZELCAST_MC_HOME/hazelcast-management-center-"*; do
         HAZELCAST_MC_JAR=$file
         file=${file##*hazelcast\-management\-center\-}
         HAZELCAST_MC_VERSION=${file%.jar}
         break;
      done
   fi
fi
HAZELCAST_MAJOR_VERSION_NUMBER=$(echo $HAZELCAST_VERSION | awk '{split($0,a,"."); print a[1]'})
HAZELCAST_MINOR_VERSION_NUMBER=$(echo $HAZELCAST_VERSION | awk '{split($0,a,"."); print a[2]'})
HAZELCAST_MC_MAJOR_VERSION_NUMBER=$(echo $HAZELCAST_MC_VERSION | awk '{split($0,a,"."); print a[1]'})
HAZELCAST_MC_MINOR_VERSION_NUMBER=$(echo $HAZELCAST_MC_VERSION | awk '{split($0,a,"."); print a[2]'})
PRODUCT_VERSION=$HAZELCAST_VERSION
PRODUCT_MAJOR_VERSION=$HAZELCAST_MAJOR_VERSION_NUMBER

# Member health monitoring properties
DEFAULT_HEALTH_MONITOR_ENABLED="true"
HEALTH_MONITOR_PROPERTIES="-Dhazelcast.health.monitoring.level=NOISY \
-Dhazelcast.health.monitoring.delay.seconds=10 \
-Dhazelcast.health.monitoring.threshold.memory.percentage=70 \
-Dhazelcast.health.monitoring.threshold.cpu.percentage=70"

# Diagnostics logging
DEFAULT_DIAGNOSTICS_ENABLED="true"
if [ "$HAZELCAST_MAJOR_VERSION_NUMBER" != "" ] && [ "$HAZELCAST_MAJOR_VERSION_NUMBER" -eq 3 ]; then
   DIAGNOSTICS_PROPERTIES="-Dhazelcast.diagnostics.metric.distributed.datastructures=true \
-Dhazelcast.diagnostics.metric.level=Debug"
else
   DIAGNOSTICS_PROPERTIES="-Dhazelcast.metrics.datastructures.enabled=true \
-Dhazelcast.metrics.debug.enabled=true"
fi
DIAGNOSTICS_PROPERTIES="$DIAGNOSTICS_PROPERTIES \
-Dhazelcast.diagnostics.invocation.sample.period.seconds=30 \
-Dhazelcast.diagnostics.pending.invocations.period.seconds=30 \
-Dhazelcast.diagnostics.slowoperations.period.seconds=30 \
-Dhazelcast.diagnostics.storeLatency.period.seconds=60"

# Hazelcast config file paths
#CONFIG_FILE=$ETC_DIR/hazelcast.yaml
CONFIG_FILE=$ETC_DIR/hazelcast.xml
CLIENT_CONFIG_FILE=$ETC_DIR/hazelcast-client.xml
#JET_CONFIG_FILE=$ETC_DIR/hazelcast-jet.yaml
JET_CONFIG_FILE=$ETC_DIR/hazelcast-jet.xml
JET_MC_APPLICATION_PROPERTIES_FILE=$ETC_DIR/jet-mc-application.properties

#
# log4j2 logging
#
if [[ ${OS_NAME} == CYGWIN* ]]; then
   __ETC_DIR="$(cygpath -wp "$ETC_DIR")"
else
   __ETC_DIR=$ETC_DIR
fi
LOG_PROPERTIES="-Dhazelcast.logging.type=log4j2 \
-Dlog4j.configurationFile=$__ETC_DIR/log4j2.properties"

#
# Shutdown hook - shutdown members gracefully with "kill -15", i.e. stop_member
#
SHUTDOWN_HOOK_PROPERTIES="-Dhazelcast.shutdownhook.enabled=true \
-Dhazelcast.shutdownhook.policy=GRACEFUL"

#
# Set Hazelcast IMDG Management Center home directory if undefined in setenv.sh
#
if [ "$CLUSTER_TYPE" == "jet" ]; then
   if [ "$JET_MC_HOME" == "" ]; then
      JET_MC_HOME=$JET_HOME/hazelcast-jet-management-center
   fi
else
   if [ "$HAZELCAST_MC_HOME" == "" ]; then
      HAZELCAST_MC_HOME=$HAZELCAST_HOME/management-center
   fi
fi

if [ "$CLUSTER_TYPE" == "jet" ]; then
   export PATH="$SCRIPT_DIR:$SCRIPT_DIR/cp_sub:$SCRIPT_DIR/tools:$PADOGRID_HOME/bin_sh:$JET_HOME/bin:$PATH"
else
   export PATH="$SCRIPT_DIR:$SCRIPT_DIR/cp_sub:$SCRIPT_DIR/tools:$PADOGRID_HOME/bin_sh:$HAZELCAST_HOME/bin:$PATH"
fi

#
# JAVA_OPTS
#
if [ "$HAZELCAST_MAJOR_VERSION_NUMBER" != "" ] && [ $HAZELCAST_MAJOR_VERSION_NUMBER -ge 5 ]; then
   JAVA_OPTS="$JAVA_OPTS -Djet.custom.lib.dir=$HAZELCAST_HOME/custom-lib"
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
__VERSION_DIR=v${HAZELCAST_VERSION:0:1}
__CLASSPATH="$__CLASSPATH:$BASE_DIR/plugins/$__VERSION_DIR/*:$BASE_DIR/lib/$__VERSION_DIR/*"

# Exclude slf4j and log4j included in PadoGrid distribution
for i in $PADOGRID_HOME/lib/*; do
  if [[ "$i" != *"slf4j"* ]] && [[ "$i" != *"log4j"* ]]; then
     __CLASSPATH="$__CLASSPATH:$i"
  fi
done

if [ "$HAZELCAST_VERSION" != "" ]; then
   if [ "$CLUSTER_TYPE" == "jet" ]; then
      if [ "$IS_HAZELCAST_ENTERPRISE" == "true" ]; then
         __CLASSPATH="$__CLASSPATH:$JET_HOME/lib/hazelcast-jet-enterprise-${HAZELCAST_VERSION}.jar"
      else
         __CLASSPATH="$__CLASSPATH:$JET_HOME/lib/hazelcast-jet-${HAZELCAST_VERSION}.jar"
      fi
   else
      if [ "$HAZELCAST_MAJOR_VERSION_NUMBER" != "" ] && [ $HAZELCAST_MAJOR_VERSION_NUMBER -ge 5 ]; then
         __CLASSPATH="$__CLASSPATH:$HAZELCAST_HOME/lib:$HAZELCAST_HOME/lib/*:$HAZELCAST_HOME/bin/user-lib/*"
      elif [ "$IS_HAZELCAST_ENTERPRISE" == "true" ]; then
         __CLASSPATH="$__CLASSPATH:$HAZELCAST_HOME/lib/hazelcast-enterprise-all-${HAZELCAST_VERSION}.jar:$HAZELCAST_HOME/bin/user-lib/*"
      else
         __CLASSPATH="$__CLASSPATH:$HAZELCAST_HOME/lib/hazelcast-all-${HAZELCAST_VERSION}.jar:$HAZELCAST_HOME/bin/user-lib/*"
      fi
   fi
fi
export CLASSPATH="$__CLASSPATH"

#
# Source in cluster specific setenv.sh
#
RUN_SCRIPT=
if [ -f "$CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh" ] && [ "$1" != "-options" ]; then
   . "$CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh"
fi
