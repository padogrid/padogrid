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
# PADOGRID_HOME   The padogrid root directory path.
# HAZELCAST_HOME         Hazelcast root directory path
#
# Optional:
# ---------
# PADOGRID_WORKSPACE 
#                        User's padogrid directory where user specifics such as cluster, pods, 
#                        apps and bundles are stored. If not specified then the padogrid home
#                        directory ($PADOGRID_HOME) is assigned.
# JAVA_HOME              Java root directory path. If not specified then the default java executable
#                        in your PATH will be used.
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
# DEFAULT_MIN_HEAP_SIZE  Default minimum heap size. Used initially when the cluster is created.
#                        The heap sizes can be changed in clusters/<cluster>/etc/cluster.properties.
# DEFAULT_MAX_HEAP_SIZE  Maximum heap size. Used initially when the cluster is created.  
# ----------------------------------------------------------------------------------------------------

# 
# Unset variables
# 
JAVA_OPTS=""

# 
# User padogrid directory
#
if [ -z $PADOGRID_WORKSPACE ]; then
   export PADOGRID_WORKSPACE=$BASE_DIR
fi

# 
# Hazelcast home directory
#
#HAZELCAST_HOME=

#
# JAVA_HOME
#
#JAVA_HOME=

#
# JAVA_OPTS - Java options.
#
#JAVA_OPTS=

# Default Hazelcast major version number
DEFAULT_HAZELCAST_MAJOR_VERSION_NUMBER=5

#
# Default workspace used when initializing workspaces by running create_workspace.
#
DEFAULT_WORKSPACE=myws

# 
# Default Cluster - If the -cluster option is not specified in any of the commands, then
# the commands default to this cluster.
#
DEFAULT_NONE_CLUSTER="none"
DEFAULT_PADO_CLUSTER="mypado"
DEFAULT_HAZELCAST_CLUSTER="myhz"
DEFAULT_JET_CLUSTER="myjet"
DEFAULT_GEODE_CLUSTER="mygeode"
DEFAULT_GEMFIRE_CLUSTER="mygemfire"
DEFAULT_REDIS_CLUSTER="myredis"
DEFAULT_SNAPPYDATA_CLUSTER="mysnappy"
DEFAULT_COHERENCE_CLUSTER="mycoherence"
DEFAULT_SPARK_CLUSTER="myspark"
DEFAULT_KAFKA_CLUSTER="mykafka"
DEFAULT_HADOOP_CLUSTER="myhadoop"
DEFAULT_CLUSTER="$DEFAULT_HAZELCAST_CLUSTER"

#
# Default pod type. The pod type determines the node envirionment in which
# the cluster is formed. The supported types are as follows:
#
#    "local"    Local environment. The cluster forms members running stricly only
#               in your physical machine. The is the default cluster type.
#
#    "vagrant"  Vagrant box environment. The cluster forms members running in
#               VMs managed by Vagrant. You must install vagrant before you can
#               use this cluster type.
#
DEFAULT_POD_TYPE="local"

#
# Default pod. The "local" pod is local to the OS (either guest OS or host OS) and 
# the cluster runs in that OS only. For non-local, i.e., any pod names other than
# "local", the members in the cluster runs on one or more guest OS machines.
#
DEFAULT_POD="local"

#
# Default primary node name. The primary node is a data node. It should be used to manage
# clusters and run client programs.
#
DEFAULT_NODE_NAME_PRIMARY="pnode"

#
# Default Data node name prefix. Each data node name begins with the prefix followed by
# a number assigned by the pod builder.
#
DEFAULT_NODE_NAME_PREFIX="node"

#
# The default last octet of the primary node IP address. The pod buillder assigns incremented
# IP addresses to all nodes starting from this octect. The first octect is assigned to
# the primary node and it is incremented thereafter for all data nodes. For example,
# if your host OS private IP address is 192.168.56.1, the last octet is 10, and
# 3 nodes are added, then the pod builder assignes IP addresses as follows:
# 
# Node Type  Node      IP
# ---------  -------   -------------
# primary    pnode     192.168.56.10       
# data       node-01   192.168.56.11
# data       node-02   192.168.56.12
# data       node-03   192.168.56.13
#
DEFAULT_NODE_IP_LAST_OCTET=10

# Default primary node memory size in MiB.
DEFAULT_NODE_PRIMARY_MEMORY_SIZE=2048

# Default data node memory size in MiB.
DEFAULT_NODE_MEMORY_SIZE=2048

# Default data node count
DEFAULT_DATA_NODE_COUNT=2

# Default vagrant box image
#DEFAULT_POD_BOX_IMAGE="hashicorp/precise64"
DEFAULT_POD_BOX_IMAGE="ubuntu/trusty64"

# Supported pod types
VALID_POD_TYPE_LIST="local vagrant"

# For help display
HELP_VALID_POD_TYPE_LIST="local|vagrant"

# Default Kubernetes
DEFAULT_K8S="minikube"

# Default Docker tool
DEFAULT_DOCKER="compose"

#
# Default heap min/max sizes. These values are initially set in $ETC_DIR/cluster.properties
# when a new cluster is created using the 'create_cluster" command. All members in 
# the cluster share the same sizes. You can change them later in the cluster.properties
# file.
#
DEFAULT_MIN_HEAP_SIZE=1g
DEFAULT_MAX_HEAP_SIZE=1g

# ----------------------------------------------------------------------------------------------------
# NON-CORE ENVIROMENT VARIABLES:
# ----------------------------------------------------------------------------------------------------

#
# Default GC logging flag. If true then GC information is logged.
#
DEFAULT_GC_LOG_ENABLED="true"

#
# Default GC log file flag. If true then GC information is logged in a separate log file. 
# named $LOG_DIR/${MEMBER}-gc.log. Otherwise, GC information is logged in the member log file.
DEFAULT_GC_LOG_FILE_ENABLED="true"

#
# CLASSPATH - Set your class path here. List all jars and folders that contain server-side
# classes such as data (domain) classes.
#
CLASSPATH=""

#
# Default member TCP start port. The value of ($MEMBER_NUM-1) is added to the start port number to
# determine the member's TCP port number.
#
DEFAULT_MEMBER_START_PORT=5701

#
# Enable/disable Java remote debugging
# The port number is incremented by 1 starting from $DEBUG_START_PORT
#
DEFAULT_DEBUG_ENABLED=true

#
# Enable/disable JMX
#
DEFAULT_JMX_ENABLED=true

#
# Enable/disable Management Center HTTPS
#
DEFAULT_MC_HTTPS_ENABLED=false

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
# Default PROMETHEUS enable/disable flag.
#
DEFAULT_PROMETHEUS_ENABLED=true

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

# Member health monitoring properties
DEFAULT_HEALTH_MONITOR_ENABLED="true"
HEALTH_MONITOR_PROPERTIES="-Dhazelcast.health.monitoring.level=NOISY \
-Dhazelcast.health.monitoring.delay.seconds=10 \
-Dhazelcast.health.monitoring.threshold.memory.percentage=70 \
-Dhazelcast.health.monitoring.threshold.cpu.percentage=70"

# -------------------------------------------------------------------------------
# Source in .argenv.sh to set all default variables. This call is required.
# IMPORTANT: Do NOT remove this call.
# -------------------------------------------------------------------------------
. $SCRIPT_DIR/.argenv.sh "$@"
. $SCRIPT_DIR/.utilenv_hazelcast.sh

# Disagnostics logging
DEFAULT_DIAGNOSTICS_ENABLED="true"
DIAGNOSTICS_PROPERTIES="-Dhazelcast.diagnostics.metric.distributed.datastructures=true \
-Dhazelcast.diagnostics.metric.level=Debug \
-Dhazelcast.diagnostics.invocation.sample.period.seconds=30 \
-Dhazelcast.diagnostics.pending.invocations.period.seconds=30 \
-Dhazelcast.diagnostics.slowoperations.period.seconds=30 \
-Dhazelcast.diagnostics.storeLatency.period.seconds=60"

# -----------------------------------------------------
# IMPORTANT: Do NOT modify below this line
# -----------------------------------------------------

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

#
# Set PADOGRID_WORKSPACE if remotely executed
#
if [ "$REMOTE_SPECIFIED" == "true" ] && [ "$WORKSPACE_ARG" != "" ]; then
   export PADOGRID_WORKSPACE=$WORKSPACE_ARG
fi

#
# Source in the rwe and workspace setenv.sh files (for license keys and workspace specifics)
#
# First, reset product paths for local pods. This is required in case the user
# switches contexts.
if [ "$IN_POD" != "true" ]; then
   export PADOGRID_HOME=""
   export PADO_HOME=""
   export JAVA_HOME=""
   export COHERENCE_HOME=""
   export GEMFIRE_HOME=""
   export GEODE_HOME=""
   export HAZELCAST_HOME=""
   export HAZELCAST_MC_HOME=""
   export JET_HOME=""
   export JET_MC_HOME=""
   export REDIS_HOME=""
   export SNAPPYDATA_HOME=""
   export SPARK_HOME=""
   export KAFKA_HOME=""
   export HADOOP_HOME=""
   export PRODUCT_HOME=""
fi
# Source in setenv.sh
if [ -f "$PADOGRID_WORKSPACES_HOME/setenv.sh" ]; then
   __SCRIPT_DIR=$SCRIPT_DIR
   __PADOGRID_WORKSPACE=$PADOGRID_WORKSPACE
   . $PADOGRID_WORKSPACES_HOME/setenv.sh
   if [ -f "$PADOGRID_WORKSPACE/setenv.sh" ]; then
      . $PADOGRID_WORKSPACE/setenv.sh
   fi
   SCRIPT_DIR=$__SCRIPT_DIR
   export PADOGRID_WORKSPACE=$__PADOGRID_WORKSPACE
fi

#
# Source in the workspace setenv.sh file that contains user configured variables
#
if [ -d $PADOGRID_WORKSPACE ] && [ -f $PADOGRID_WORKSPACE/setenv.sh ] && [ "$PADOGRID_WORKSPACE" != "$BASE_DIR" ]; then
   # SCRIPT_DIR, CLUSTER and POD options override setenv.sh
   __SCRIPT_DIR=$SCRIPT_DIR
   __CLUSTER=$CLUSTER
   __POD=$POD

   . $PADOGRID_WORKSPACE/setenv.sh

   if [ "$CLUSTER_SPECIFIED" == "true" ]; then
      CLUSTER=$__CLUSTER
   fi
   if [ "$POD_SPECIFIED" == "true" ]; then
      POD=$__POD
   fi
   SCRIPT_DIR=$__SCRIPT_DIR
fi

# 
# If running in a Vagrant pod, then source in the .nodeenv.sh file which overrides
# the environment variables set in the above workspace setenv.sh file. 
#
if [ -f "/vagrant/.nodeenv.sh" ]; then
   . /vagrant/.nodeenv.sh
fi

#
# The directory path where the required products are installed in the host OS. The default
# path is <padogrid-env-dir>/products". This path is mounted as "~/products" in the
# guest OS.
#
DEFAULT_HOST_PRODUCTS_DIR="$PADOGRID_ENV_BASE_PATH/products"

# Supported Bundle Products
BUNDLE_PRODUCT_LIST="coherence gemfire geode hadoop hazelcast jet kafka none redis snappydata spark"

# Supported Docker Products
DOCKER_PRODUCT_LIST="geode hazelcast jet snappydata"

# Supported Kubernetes Products
K8S_PRODUCT_LIST="geode hazelcast jet"

# Supported App Products
APP_PRODUCT_LIST="coherence gemfire geode hazelcast jet redis"

# Pod variables
if [ -z $POD_BOX_IMAGE ]; then
  POD_BOX_IMAGE=$DEFAULT_POD_BOX_IMAGE
fi

if [ -z $POD ]; then
   POD=$DEFAULT_POD
fi
if [ -z $POD_TYPE ]; then
   POD_TYPE=$DEFAULT_POD_TYPE
fi
if [ -z $PODS_DIR ]; then
   PODS_DIR=$PADOGRID_WORKSPACE/pods
fi
if [ -z $POD_DIR ]; then
   POD_DIR=$PODS_DIR/$POD
fi
if [ -z $DOCKER_DIR ]; then
   DOCKER_DIR=$PADOGRID_WORKSPACE/docker
fi
if [ -z $K8S_DIR ]; then
   K8S_DIR=$PADOGRID_WORKSPACE/k8s
fi
if [ -z $APPS_DIR ]; then
   APPS_DIR=$PADOGRID_WORKSPACE/apps
fi

# Pod node variables
if [ -z $NODE_NAME_PRIMARY ]; then
   NODE_NAME_PRIMARY=$DEFAULT_NODE_NAME_PRIMARY
fi
if [ -z $NODE_NAME_PREFIX ]; then
   NODE_NAME_PREFIX=$DEFAULT_NODE_NAME_PREFIX
fi
if [ -z $NODE_IP_LAST_OCTET ]; then
   NODE_IP_LAST_OCTET=$DEFAULT_NODE_IP_LAST_OCTET
fi
if [ -z $NODE_PRIMARY_MEMORY_SIZE ]; then
   NODE_PRIMARY_MEMORY_SIZE=$DEFAULT_NODE_PRIMARY_MEMORY_SIZE
fi
if [ -z $NODE_MEMORY_SIZE ]; then
   NODE_MEMORY_SIZE=$DEFAULT_NODE_MEMORY_SIZE
fi
if [ -z $DATA_NODE_COUNT ]; then
   DATA_NODE_COUNT=$DEFAULT_DATA_NODE_COUNT
fi

# Pod host variables
if [ -z $HOST_PRODUCTS_DIR ]; then
   HOST_PRODUCTS_DIR=$DEFAULT_HOST_PRODUCTS_DIR
fi

# Set CLUSTER to the default cluster set in setenv.sh if it 
# is not specified.
if [ -z $CLUSTER ]; then
   retrieveWorkspaceEnvFile
fi

if [ -z $CLUSTERS_DIR ]; then
   if [ "$PADOGRID_WORKSPACE" == "" ]; then
      CLUSTERS_DIR=$BASE_DIR/clusters
   else
      CLUSTERS_DIR=$PADOGRID_WORKSPACE/clusters
   fi
fi

CLUSTER_DIR=$CLUSTERS_DIR/$CLUSTER

# Source in cluster file to get the product and cluster type
THIS_PRODUCT=$PRODUCT
THIS_CLUSTER_TYPE=$CLUSTER_TYPE

# Retrieve PRODUCT and CLUSTER_TYPE
retrieveClusterEnvFile

# Parent directory of member working directories
RUN_DIR=$CLUSTERS_DIR/$CLUSTER/run

# ETC_DIR
ETC_DIR=$CLUSTERS_DIR/$CLUSTER/etc

# LOG_DIR
LOG_DIR=$CLUSTERS_DIR/$CLUSTER/log

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

#
# Remove the previous paths from PATH to prevent duplicates
#
CLEANED_PATH=""
__IFS=$IFS
IFS=":"
PATH_ARRAY=($PATH)
for i in "${PATH_ARRAY[@]}"; do
   if [ "$JAVA_HOME" != "" ] && [ "$i" == "$JAVA_HOME/bin" ]; then
      continue;
   elif [[ "$i" == **"padogrid_"** ]] && [[ "$i" == **"bin_sh"** ]]; then
      continue;
   elif [ "$PRODUCT_HOME" != "" ] && [[ "$i" == "$PRODUCT_HOME"** ]]; then
      continue;
   elif [ "$COHERENCE_HOME" != "" ] && [[ "$i" == "$COHERENCE_HOME"** ]]; then
      continue;
   elif [ "$GEODE_HOME" != "" ] && [[ "$i" == "$GEODE_HOME"** ]]; then
      continue;
   elif [ "$GEMFIRE_HOME" != "" ] && [[ "$i" == "$GEMFIRE_HOME"** ]]; then
      continue;
   elif [ "$HAZELCAST_HOME" != "" ] && [[ "$i" == "$HAZELCAST_HOME"** ]]; then
      continue;
   elif [ "$JET_HOME" != "" ] && [[ "$i" == "$JET_HOME"** ]]; then
      continue;
   elif [ "$REDIS_HOME" != "" ] && [[ "$i" == "$REDIS_HOME"** ]]; then
      continue;
   elif [ "$SNAPPYDATA_HOME" != "" ] && [[ "$i" == "$SNAPPYDATA_HOME"** ]]; then
      continue;
   elif [ "$SPARK_HOME" != "" ] && [[ "$i" == "$SPARK_HOME"** ]]; then
      continue;
   elif [ "$KAFKA_HOME" != "" ] && [[ "$i" == "$KAFKA_HOME"** ]]; then
      continue;
   elif [ "$HADOOP_HOME" != "" ] && [[ "$i" == "$HADOOP_HOME"** ]]; then
      continue;
   fi
   if [ "$CLEANED_PATH" == "" ]; then
      CLEANED_PATH="$i"
   else
      CLEANED_PATH="$CLEANED_PATH:$i"
   fi
done
IFS=$__IFS

# Export cleaned PATH
PATH="$CLEANED_PATH"

#
# PATH
#
if [ "$JAVA_HOME" != "" ] && [[ "$PATH" != "$JAVA_HOME"** ]]; then
   export PATH="$JAVA_HOME/bin:$PATH"
fi
if [ "$CLUSTER_TYPE" == "jet" ]; then
   export PATH="$SCRIPT_DIR:$SCRIPT_DIR/cp_sub:$SCRIPT_DIR/tools:$PADOGRID_HOME/bin_sh:$JET_HOME/bin:$PATH"
else
   export PATH="$SCRIPT_DIR:$SCRIPT_DIR/cp_sub:$SCRIPT_DIR/tools:$PADOGRID_HOME/bin_sh:$HAZELCAST_HOME/bin:$PATH"
fi

#
# Java executable
#
if [ "$JAVA_HOME" == "" ]; then
   JAVA=java
else
   JAVA=$JAVA_HOME/bin/java
fi

#
# Java version
#
if [ "$(which $JAVA 2> /dev/null)" == "" ]; then
   JAVA_VERSION=""
   JAVA_MAJOR_VERSION_NUMBER=""
else
   __COMMAND="\"$JAVA\" -version 2>&1 | grep version"
   JAVA_VERSION=$(eval $__COMMAND)
   JAVA_VERSION=$(echo $JAVA_VERSION |  sed -e 's/.*version//' -e 's/"//g' -e 's/ //g')
   JAVA_MAJOR_VERSION_NUMBER=`expr "$JAVA_VERSION" : '\([0-9]*\)'`
fi

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
         for i in $(seq 5 10); do
            if [ -f "$HAZELCAST_HOME/lib/hazelcast-$i."* ]; then
               for file in "$HAZELCAST_HOME/lib/hazelcast-$i."*; do
                  file=${file##*hazelcast\-}
                  HAZELCAST_VERSION=${file%.jar}
                  break;
               done
               break;
            fi
         done
      fi
   fi
   if [ "$HAZELCAST_MC_HOME" != "" ]; then
      for file in "$HAZELCAST_MC_HOME/hazelcast-management-center-"*; do
         file=${file##*hazelcast\-management\-center\-}
         HAZELCAST_MC_VERSION=${file%.jar}
         break;
      done
   fi
fi
HAZELCAST_MAJOR_VERSION_NUMBER=$(echo $HAZELCAST_VERSION | awk '{split($0,a,"."); print a[1]'})
HAZELCAST_MINOR_VERSION_NUMBER=$(echo $HAZELCAST_VERSION | awk '{split($0,a,"."); print a[2]'})
PRODUCT_VERSION=$HAZELCAST_VERSION
PRODUCT_MAJOR_VERSION=$HAZELCAST_MAJOR_VERSION_NUMBER

#
# PADOGRID_VERSION: Determine the padogrid version
#
for file in $BASE_DIR/lib/v4/hazelcast-addon-core-4-*; do
   file=${file#*hazelcast\-addon\-core\-4\-}
   PADOGRID_VERSION=${file%.jar}
done

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
__CLASSPATH="$__CLASSPATH:$PADOGRID_HOME/lib/*"

if [ "$HAZELCAST_VERSION" != "" ]; then
   if [ "$CLUSTER_TYPE" == "jet" ]; then
      if [ "$IS_HAZELCAST_ENTERPRISE" == "true" ]; then
         __CLASSPATH="$__CLASSPATH:$JET_HOME/lib/hazelcast-jet-enterprise-${HAZELCAST_VERSION}.jar"
      else
         __CLASSPATH="$__CLASSPATH:$JET_HOME/lib/hazelcast-jet-${HAZELCAST_VERSION}.jar"
      fi
   else
      if [ $HAZELCAST_MAJOR_VERSION_NUMBER -ge 5 ]; then
         __CLASSPATH="$__CLASSPATH:$HAZELCAST_HOME/lib:$__CLASSPATH:$HAZELCAST_HOME/lib/*:$HAZELCAST_HOME/user-lib:$HAZELCAST_HOME/user-lib/*"
      elif [ "$IS_HAZELCAST_ENTERPRISE" == "true" ]; then
         __CLASSPATH="$__CLASSPATH:$HAZELCAST_HOME/lib/hazelcast-enterprise-all-${HAZELCAST_VERSION}.jar:$HAZELCAST_HOME/user-lib/*"
      else
         __CLASSPATH="$__CLASSPATH:$HAZELCAST_HOME/lib/hazelcast-all-${HAZELCAST_VERSION}.jar:$HAZELCAST_HOME/user-lib/*"
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
