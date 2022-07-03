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
#
# Optional:
# ---------
# PADOGRID_WORKSPACE 
#                        User's padogrid directory where user specifics such as cluster, pods, 
#                        apps and bundles are stored. If not specified then the padogrid home
#                        directory ($PADOGRID_HOME) is assigned.
# JAVA_HOME              Java root directory path. If not specified then the default java executable
#                        in your PATH will be used.

# DEFAULT_MIN_HEAP_SIZE  Default minimum heap size. Used initially when the cluster is created.
#                        The heap sizes can be changed in clusters/<cluster>/etc/cluster.properties.
# DEFAULT_MAX_HEAP_SIZE  Maximum heap size. Used initially when the cluster is created.  
# ----------------------------------------------------------------------------------------------------

#
# Determine the PadoGrid environment base path. Default is "$HOME/Padogrid".
#
if [ "$PADOGRID_ENV_BASE_PATH" == "" ]; then
   if [ "$PADOGRID_HOME" == "" ]; then
      export PADOGRID_ENV_BASE_PATH="$HOME/Padogrid"
   else
      export PADOGRID_ENV_BASE_PATH="$(dirname $(dirname $PADOGRID_WORKSPACES_HOME))"
   fi
fi

# 
# Unset variables
# 
JAVA_OPTS=""
CLASSPATH=""

# 
# User padogrid directory
#
if [ -z $PADOGRID_WORKSPACE ]; then
   export PADOGRID_WORKSPACE=$BASE_DIR
fi

#
# Default workspace used when initializing workspaces by running create_workspace.
#
DEFAULT_WORKSPACE=myws

# 
# Default Cluster - If the -cluster option is not specified in any of the commands, then
# the commands default to this cluster.
#
DEFAULT_COHERENCE_CLUSTER="mycoherence"
DEFAULT_GEMFIRE_CLUSTER="mygemfire"
DEFAULT_GEODE_CLUSTER="mygeode"
DEFAULT_HADOOP_CLUSTER="myhadoop"
DEFAULT_HAZELCAST_CLUSTER="myhz"
DEFAULT_JET_CLUSTER="myjet"
DEFAULT_KAFKA_CLUSTER="mykafka"
DEFAULT_NONE_CLUSTER="mynone"
DEFAULT_PADO_CLUSTER="mypado"
DEFAULT_REDIS_CLUSTER="myredis"
DEFAULT_SNAPPYDATA_CLUSTER="mysnappy"
DEFAULT_SPARK_CLUSTER="myspark"

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

# -----------------------------------------------------
# IMPORTANT: Do NOT modify below this line
# -----------------------------------------------------

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

# Downloadable products
DOWNLOADABLE_PRODUCTS="padogrid pado padodesktop padoweb geode hazelcast-enterprise hazelcast-oss hazelcast-mc hazelcast-desktop jet-enterprise jet-oss redis-oss snappydata spark kafka hadoop"

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
# PADOGRID_VERSION: Determine the padogrid version
#
for file in $BASE_DIR/../lib/padogrid-common-*; do
   file=${file#*padogrid\-common\-}
   PADOGRID_VERSION=${file%.jar}
done
