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

# -----------------------------------------------------
# This file contains utility functions Do NOT modify!
# -----------------------------------------------------

# 
# Returns "true" if number, else "false"
# @param number
#
function isNumber
{
   num=$1
   [ ! -z "${num##*[!0-9]*}" ] && echo "true" || echo "false";
}

#
# Returns trimmed string
# @param String to trim
#
function trimString
{
    local var="$1"
    # remove leading whitespace characters
    var="${var#"${var%%[![:space:]]*}"}"
    # remove trailing whitespace characters
    var="${var%"${var##*[![:space:]]}"}"   
    echo -n "$var"
}

#
# Trims double quotes that enclose string
# @param string enclosed in double quotes
#
function trimDoubleQuotes
{
    echo $1 | sed -e 's/^"//' -e 's/"$//'
}

#
# Removes the leading zero if exists.
# @param String value begins with 0.
#
function trimLeadingZero
{
   echo ${1#0}
}

#
# Returns "true" if the specified space-separated string contains the specified word.
# @param string   Space-speparated string
# @param word     Word to search in the specified string
#
function containsWord
{
   local string="$1"
   local word="$2"
   local retval="false"
   for i in $string; do
      if [ "$i" == "$word" ]; then
         retval="true"
         break;
      fi
    done
    echo "$retval"
}

#
# Returns the absolute path of the specified file path.
# If the file does not exist then it returns -1.
# @param filePath
#
function getAbsPath()
{
   __FILE_PATH=$1

   __IS_DIR=false
   if [ -d $__FILE_PATH ]; then
      __IS_DIR="true"
   else
      if [ ! -f $__FILE_PATH ]; then
         echo "-1"
         return
      fi
   fi

   if [ -d $__FILE_PATH ]; then
      pushd $__FILE_PATH > /dev/null 2>&1
      __ABS_PATH=`pwd`
      popd > /dev/null 2>&1
   else
      __FILE_NAME=$(basename "$__FILE_PATH")
      __FILE_DIR=$(dirname "$__FILE_PATH")
      __ABS_PATH=$(cd $(dirname "$__FILE_DIR"); pwd)/$(basename "$__FILE_DIR")
      pushd $__ABS_PATH > /dev/null 2>&1
      __ABS_PATH=`pwd`
      popd > /dev/null 2>&1
      __ABS_PATH=$__ABS_PATH/$__FILE_NAME
   fi
   echo $__ABS_PATH
}

#
# Returns the member number that includes the leading zero.
# @param memberNumber
#
function getMemberNumWithLeadingZero
{
   if [ $1 -lt 10 ]; then
      echo "0$1"
   else
      echo "$1"
   fi
}

# 
# Returns the OS environment information as follows:
# @param hostName  Host name.
#
# Returned  Description
# --------  -----------------------------------------------
#  hh       host on host   host os viewed from itself (local)
#  hg       host on guest  host (or unknown) os viewed from guest os
#  gh       guest on host  guest os viewed from host os
#  gg       guest on guest guest os viewed from itself
#
function getOsEnv
{
   __HOSTNAME=`hostname`
   if [ "$HOST_OS_HOSTNAME" == "" ]; then
      if [[ $1 == $__HOSTNAME* ]]; then
         echo "hh"   # host viewed from itself (local)
      else
         echo "gh"  # guest viewd from host
      fi
   else
      if [[ $1 == $__HOSTNAME* ]] || [[ $1 == $NODE_NAME_PRIMARY* ]]; then
         echo "gg"  # guest viewed from itself
      else
         echo "hg"   # host (or unknonw) viewed from guest
      fi
   fi
}

# 
# Returns the OS environment information as follows:
# @required NODE_NAME_PREFIX  Node name prefix.
#
# Returned  Description
# --------  -----------------------------------------------
#  hg       host on guest  (host os viewed from guest os)
#  hh       host on host   (host os viewed from host os)
#  gg       guest on guest (guest os viewed from guest os)
#  gh       guest on host  (guest os viewed from host os)
#
function getOsEnv2
{
   __HOSTNAME=`hostname`
   if [ "$HOST_OS_HOSTNAME" == "" ]; then
      if [[ $__HOSTNAME == $NODE_NAME_PREFIX* ]]; then
         echo "hg"  
      else
         echo "hh"
      fi
   else
      if [[ $__HOSTNAME == $NODE_NAME_PREFIX* ]]; then
         echo "gg"  
      else
         echo "gh"
      fi
   fi
}

#
# Returns "true" if the current node runs in a guest OS.
# @required NODE_NAME_PREFIX  Node name prefix.
# @requried NODE_NAME_PRIMARY Primary node name.
# @param    hostName          Optional. Host name to determine whether it runs a guest OS.
#                             If not specified then it default to the OS host name.
#
function isGuestOs
{
   if [ "$1" == "" ]; then
      __HOSTNAME=`hostname`
   else
      __HOSTNAME=$1
   fi
   if [[ $__HOSTNAME == $NODE_NAME_PREFIX* ]] || [[ $__HOSTNAME == $NODE_NAME_PRIMARY* ]]; then
      echo "true"
   else 
      echo "false"
   fi
}

#
# Returns the node name recognized by the OS.
#
# Pod        Pod Type   OS     Node
# -----      ---------  -----  ---------------------------
# local      local      guest  $__HOST_OS_HOSTNAME
# local      local      host   $__HOSTNAME
# local      vagrant    guest  $__HOSTNAME.local
# local      vagrant    host   $NODE_NAME_PREFIX-01.local
# non-local  local      guest  $__HOSTNAME.local
# non-local  local      host   $NODE_NAME_PREFIX-01.local
# non-local  vagrant    guest  $__HOSTNAME.local
# non-local  vagrant    host   $NODE_NAME_PREFIX-01.local
#
# @required POD               Pod name.
# @required POD_TYPE          Pod type.
# @required NODE_NAME_PREFIX  Node name prefix.
# @param    nodeName          Optional. Node name without the .local extension.
#                             If not specified then it default to the OS host name.
#
function getOsNodeName
{
   if [ "$1" == "" ]; then
      __HOSTNAME=`hostname`
   else
      __HOSTNAME=$1
   fi
   __IS_GUEST_OS_NODE=`isGuestOs $__HOSTNAME`
   if [ "$HOST_OS_HOSTNAME" == "" ]; then
      if [ "$__IS_GUEST_OS_NODE" == "true" ]; then
         __HOST_OS_HOSTNAME="${__HOSTNAME}.local"
      else
         __HOST_OS_HOSTNAME="$__HOSTNAME"
      fi
   else
      __HOST_OS_HOSTNAME="$HOST_OS_HOSTNAME"
   fi
   if [ "$POD" == "local" ]; then
      if [ "$POD_TYPE" == "local" ]; then
         if [ "$__IS_GUEST_OS_NODE" == "true" ]; then
            __NODE="$__HOST_OS_HOSTNAME"
         else
            __NODE="$__HOSTNAME"
         fi
      else
         if [ "$__IS_GUEST_OS_NODE" == "true" ]; then
            __NODE="$__HOSTNAME.local"
         else
            __NODE="$NODE_NAME_PREFIX-01.local"
         fi
      fi
   else
      if [ "$POD_TYPE" == "local" ]; then
         if [ "$__IS_GUEST_OS_NODE" == "true" ]; then
            __NODE="$__HOSTNAME.local"
         else
            __NODE="$NODE_NAME_PREFIX-01.local"
         fi
      else
         if [ "$__IS_GUEST_OS_NODE" == "true" ]; then
            __NODE="$__HOSTNAME.local"
         else
            __NODE="$NODE_NAME_PREFIX-01.local"
         fi
      fi
   fi
   echo "$__NODE"
}

#
# Returns a list of RWE names found in the specified RWE home path.
# If the RWE home path is not specified then returns the current
# RWE home path extracted from $PADOGRID_WORKSPACES_HOME
# @required PADOGRID_WORKSPACES_HOME Required only if the rweHomePath is not specified.
# @param rweHomePath RWE home path where RWEs are stored. Optional.
#
function getRweList
{
   local RWE_HOME="$1"
   if [ "$RWE_HOME" == "" ]; then
      if [ "$PADOGRID_WORKSPACES_HOME" != "" ] && [ -d "$PADOGRID_WORKSPACES_HOME" ]; then
         RWE_HOME="$(dirname "$PADOGRID_WORKSPACES_HOME")"
      fi
   fi
   local ROOTS=""
   if [ "$RWE_HOME" != "" ] &&  [ -d "$RWE_HOME" ]; then
      local ROOT_DIRS=$(ls "$RWE_HOME")
      pushd $RWE_HOME > /dev/null 2>&1
      for i in $ROOT_DIRS; do
         if [ -f "$i/initenv.sh" ] && [ -f "$i/.addonenv.sh" ] && [ -f "$i/setenv.sh" ]; then
          if [ "$ROOTS" == "" ]; then
             ROOTS="$i"
          else
             ROOTS="$ROOTS $i"
          fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo "$ROOTS"
}

#
# Returns "true" if the specified RWE exists in the current RWE environments and is valid.
# @required PADOGRID_WORKSPACES_HOME
# @param rweName     RWE name.
#
function isValidRwe
{
   local RWE="$1"
   if [ "$RWE" != "" ]; then
      local RWE_LIST=$(getRweList)
      for i in $RWE_LIST; do
         if [ "$i" == "$RWE" ]; then
            echo "true"
            return 0
         fi
      done
   fi
   echo "false"
}

# 
# Returns a complete list of workspaces found in the specified workspaces (RWE) path.
# If the workspaces path is not specified then it returns the workspaces in 
# PADOGRID_WORKSPACES_HOME.
# @required PADOGRID_WORKSPACES_HOME
# @param workspacesPath Workspaces (RWE) path.
#
function getWorkspaces
{
   local WORKSPACES_HOME="$1"
   if [ "$WORKSPACES_HOME" == "" ]; then
      WORKSPACES_HOME="$PADOGRID_WORKSPACES_HOME"
   fi
   local __WORKSPACES=""
   if [ "$WORKSPACES_HOME" != "" ]; then
      pushd "$WORKSPACES_HOME" > /dev/null 2>&1
      local __COUNT=0
      for i in *; do
         if [ -d "$i" ]; then
            if [ -f "$i/initenv.sh" ] && [ -f "$i/.addonenv.sh" ] && [ -f "$i/setenv.sh" ]; then
               let __COUNT=__COUNT+1
               if [ $__COUNT -eq 1 ]; then
                  __WORKSPACES="$i"
               else
                  __WORKSPACES="$__WORKSPACES $i"
               fi
       fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $__WORKSPACES
}

#
# Returns "true" if the specified workspace exists in the specified RWE and is valid.
# Note that the first argument in the workspace name.
# @required PADOGRID_WORKSPACES_HOME
# @param workspaceName Workspace name.
# @param rweName       Optional RWE name. If not specified then the current RWE is checked.
#
function isValidWorkspace
{
   local WORKSPACE="$1"
   local RWE="$2"
   if [ "$WORKSPACE" != "" ]; then
      if [ "$RWE" == "" ]; then
         local WORKSPACE_LIST=$(getWorkspaces)
      else
         local WORKSPACES_TOP_PATH="$(dirname "$PADOGRID_WORKSPACES_HOME")"
         local RWE_PATH="$WORKSPACES_TOP_PATH/$RWE"
         local WORKSPACE_LIST=$(getWorkspaces "$RWE_PATH")
      fi
      for i in $WORKSPACE_LIST; do
         if [ "$i" == "$WORKSPACE" ]; then
            echo "true"
            return 0
         fi
      done
   fi
   echo "false"
}

#
# Returns a comma separated list of VM hosts of the specified workspace. Returns an empty
# string if the workspace does not exist.
# @param    workspaceName    Workspace name
#
function getVmWorkspaceHosts
{
   local __WORKSPACE=$1
   local __VM_HOSTS=""
   if [ "$__WORKSPACE" != "" ]; then
      local __VM_HOSTS=$(grep "^VM_HOSTS=" $PADOGRID_WORKSPACES_HOME/$__WORKSPACE/setenv.sh)
      __VM_HOSTS=${__VM_HOSTS##"VM_HOSTS="}
      __VM_HOSTS=$(trimDoubleQuotes "$__VM_HOSTS")
   fi
   echo $__VM_HOSTS
}

#
# Returns a comma separated list of VM hosts of the specified workspace. Returns an empty
# string if the workspace does not exist.
# @param    workspaceName    Workspace name
#
function getVmWorkspacesHome
{
   local __WORKSPACE=$1
   local __VM_WORKSPACES_HOME=""
   if [ "$__WORKSPACE" != "" ]; then
      local __VM_WORKSPACES_HOME=$(grep "^VM_PADOGRID_WORKSPACES_HOME=" $PADOGRID_WORKSPACES_HOME/$__WORKSPACE/setenv.sh)
      __VM_WORKSPACES_HOME=${__VM_WORKSPACES_HOME##"VM_PADOGRID_WORKSPACES_HOME="}
      __VM_WORKSPACES_HOME=$(trimDoubleQuotes "$__VM_WORKSPACES_HOME")
   fi
   echo $__VM_WORKSPACES_HOME
}

#
# Returns a complete list of clusters found in the speciefied cluster environment.
# @required PADOGRID_WORKSPACE  Workspace directory path.
# @param clusterEnv   Optional cluster environment.
#                     Valid values: "groups", "clusters", "pods", "k8s", "docker", and "apps".
#                     If unspecified then defaults to "clusters".
# @param workspace    Optional workspace name. If unspecified, then defaults to
#                     the current workspace.
#
function getClusters
{
   local __ENV="$1"
   local __WORKSPACE="$2"
   if [ "$__ENV" == "" ]; then
      __ENV="clusters"
   fi
   local __WORKSPACE_DIR
   if [ "$__WORKSPACE" == "" ]; then
      __WORKSPACE_DIR=$PADOGRID_WORKSPACE
   else
      __WORKSPACE_DIR=$PADOGRID_WORKSPACES_HOME/$__WORKSPACE
   fi
   local __CLUSTERS_DIR="$__WORKSPACE_DIR/$__ENV"
   __CLUSTERS=""
   if [ -d "$__CLUSTERS_DIR" ]; then
      pushd $__CLUSTERS_DIR > /dev/null 2>&1
      __COUNT=0
      for i in *; do
         if [ "$i" != "local" ] && [ -d "$i" ]; then
            let __COUNT=__COUNT+1
            if [ $__COUNT -eq 1 ]; then
               __CLUSTERS="$i"
            else
               __CLUSTERS="$__CLUSTERS $i"
            fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $__CLUSTERS
}

# 
# Returns a complete list of pods found in PODS_DIR.
# @required PODS_DIR
#
function getPods {
   local __PODS=""
   if [ -d "$PODS_DIR" ]; then
      pushd $PODS_DIR > /dev/null 2>&1
      __COUNT=0
      for i in *; do
         if [ "$i" != "local" ] && [ -d "$i" ]; then
            let __COUNT=__COUNT+1
            if [ $__COUNT -eq 1 ]; then
               __PODS="$i"
            else
               __PODS="$__PODS $i"
            fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $__PODS
}

# 
# Returns a complete list of k8s components found in K8S_DIR
# @required K8S_DIR
#
function getK8s {
   local __K8S=""
   if [ -d "$K8S_DIR" ]; then
      pushd $K8S_DIR > /dev/null 2>&1
      __COUNT=0
      for i in *; do
         if [ -d "$i" ]; then
            let __COUNT=__COUNT+1
            if [ $__COUNT -eq 1 ]; then
               __K8S="$i"
            else
               __K8S="$__K8S $i"
            fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $__K8S
}

#
# Returns a space-sparated list of supported '-k8s' options for the 'create_k8s' command.
# @param product  Product name. Supported are hazelcast, jet, geode
#
function getK8sOptions 
{
   local __PRODUCT="$1"
   if [ "$__PRODUCT" == "hazelcast" ]; then
      echo "minikube minishift openshift gke"
   elif [ "$__PRODUCT" == "jet" ]; then
      echo "openshift"
   elif [ "$__PRODUCT" == "geode" ]; then
      echo "minikube"
   else
      echo ""
   fi
}

# 
# Returns a complete list of Docker components found in DOCKER_DIR
# @required DOCKER_DIR
#
function getDockers {
   local __DOCKERS=""
   if [ -d "$DOCKER_DIR" ]; then
      pushd $DOCKER_DIR > /dev/null 2>&1
      __COUNT=0
      for i in *; do
         if [ -d "$i" ]; then
            let __COUNT=__COUNT+1
            if [ $__COUNT -eq 1 ]; then
               __DOCKERS"$i"
            else
               __DOCKERS="$__DOCKERS $i"
            fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $__DOCKERS
}

# 
# Returns a complete list of apps found in APPS_DIR.
# @required APPS_DIR
#
function getApps {
   __APPS=""
   if [ -d "$APPS_DIR" ]; then
      pushd $APPS_DIR > /dev/null 2>&1
      __COUNT=0
      for i in *; do
         if [ -d "$i" ]; then
            let __COUNT=__COUNT+1
            if [ $__COUNT -eq 1 ]; then
               __APPS="$i"
            else
               __APPS="$__APPS $i"
            fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $__APPS
}

#
# Returns a space-sparated list of supported '-app' options for the 'create_app' command.
# @param product  Product name.
#
function getAppOptions 
{
   local __PRODUCT="$1"
   if [ "$__PRODUCT" == "hazelcast" ]; then
      echo "derby desktop grafana perf_test"
   elif [ "$__PRODUCT" == "jet" ]; then
      echo "derby desktop jet_demo"
   elif [ "$__PRODUCT" == "geode" ] || [ "$__PRODUCT" == "gemfire" ]; then
      echo "derby grafana padodesktop perf_test"
   elif [ "$__PRODUCT" == "coherence" ]; then
      echo "derby perf_test"
   elif [ "$__PRODUCT" == "redis" ]; then
      echo "derby perf_test"
   else
      echo "derby"
   fi
}

#
# Returns the default port number of the specified product.
# @param product  Product name.
#
function getClusterPortOptions 
{
   local __PRODUCT="$1"
   local __DEFAULT_PORT

   case "$__PRODUCT" in
   coherence)
      __DEFAULT_PORT="$DEFAULT_COHERENCE_START_PORT";;
   gemfire|geode|snappydata)
      __DEFAULT_PORT="$DEFAULT_GEODE_START_PORT";;
   hadoop)
      __DEFAULT_PORT="$DEFAULT_HADOOP_START_PORT";;
   hazelcast|jet)
      __DEFAULT_PORT="$DEFAULT_HAZELCAST_START_PORT";;
   kafka|confluent)
      __DEFAULT_PORT="$DEFAULT_KAFKA_START_PORT";;
   redis)
      __DEFAULT_PORT="$DEFAULT_REDIS_START_PORT";;
   spark)
      __DEFAULT_PORT="$DEFAULT_SPARK_START_PORT";;
    *)
      __DEFAULT_PORT=""
   esac

   echo "$__DEFAULT_PORT"
}

# 
# Returns "true" if the specified cluster exists. Otherwise, "false".
# @required CLUSTERS_DIR
# @param clusterName
#
function isClusterExist
{
   if [ -d "$CLUSTERS_DIR/$1" ]; then
      echo "true"
   else
      echo "false"
   fi
}

# 
# Returns "true" if the specified pod exists. Otherwise, "false".
# @required PODS_DIR
# @param    podName
#
function isPodExist
{
   if [ "$1" == "local" ] || [ -d "$PODS_DIR/$1" ]; then
      echo "true"
   else
      echo "false"
   fi
}

#
# Returns "true" if the specified docker cluster exists. Otherwise, "false".
# @required DOCKER_DIR
# @param    dockerClusterName
#
function isDockerExist
{
   if [ -d "$DOCKER_DIR/$1" ]; then
      echo "true"
   else
      echo "false"
   fi
}

#
# Returns "true" if the specified k8s cluster exists. Otherwise, "false".
# @required K8S_DIR
# @param    dockerClusterName
#
function isK8sExist
{
   if [ -d "$K8S_DIR/$1" ]; then
      echo "true"
   else
      echo "false"
   fi
}

# 
# Returns "true" if the specified app exists. Othereise, "false".
# @required APPS_DIR
# @param clusterName
#
function isAppExist
{
   if [ -d "$APPS_DIR/$1" ]; then
      echo "true"
   else
      echo "false"
   fi
}

#
#
# Returns "true" if the specified pod is running. Otherwise, "false".
# @param pod  Pod name.
#
function isPodRunning
{
   if [ "$1" == "local" ]; then
      __POD_RUNNING="true"
   else
      if [[ $OS_NAME == CYGWIN* ]]; then
         __POD_DIR=$PODS_DIR/$1
         __POD_RUNNING="false"

         if [ -d "$__POD_DIR/.vagrant/machines" ]; then 
            __TMP_DIR=$__POD_DIR/tmp
            if [ ! -d "$__TMP_DIR" ]; then
               mkdir -p $__TMP_DIR
            fi
            __TMP_FILE=$__TMP_DIR/tmp.txt
            vagrant global-status > $__TMP_FILE

            pushd $__POD_DIR/.vagrant/machines > /dev/null 2>&1
            for i in *; do
               if [ -f "$i/virtualbox/index_uuid" ]; then
                  __VB_ID=`cat $i/virtualbox/index_uuid`
                  __VB_ID=${__VB_ID:0:7}
                  __VB_ID_PROCESS=`cat $__TMP_FILE | grep $__VB_ID | grep "running" | grep -v grep`
                  if [ "$__VB_ID_PROCESS" != "" ]; then
                     __POD_RUNNING="true"
                     break;
                  fi
               fi
            done
            popd > /dev/null 2>&1
         fi 
      else
         __POD_RUNNING="true"
         __POD_DIR=$PODS_DIR/$1
         __POD_RUNNING="false"

         if [ -d "$__POD_DIR/.vagrant/machines" ]; then 

            pushd $__POD_DIR/.vagrant/machines > /dev/null 2>&1
            for i in *; do
               if [ -f "$i/virtualbox/id" ]; then
                  __VB_ID=`cat $i/virtualbox/id`
                  __VB_ID_PROCESS=`ps -wwef |grep $__VB_ID | grep -v grep`
                  if [ "$__VB_ID_PROCESS" != "" ]; then
                     __POD_RUNNING="true"
                     break;
                  fi
               fi
            done
            popd > /dev/null 2>&1
         fi 
      fi
   fi
   echo "$__POD_RUNNING"
}

#
# Returns the number of active (or running) members in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# This function works for both VM and non-VM workspaces.
# @param clusterName   Cluster name.
# @param workspaceName Workspace name.
# @param rweName       Optional RWE name.
#
function getActiveMemberCount
{
   # Members
   local __CLUSTER=$1
   local __WORKSPACE=$2
   local __RWE=$3

   if [ "$__CLUSTER" == "" ] || [ "$__WORKSPACE" == "" ] || [ "$RWE" == "" ]; then
      echo 0
   fi
   local MEMBER
   local MEMBER_COUNT=0
   local MEMBER_RUNNING_COUNT=0
   local VM_ENABLED=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.enabled")
   if [ "$VM_ENABLED" == "true" ]; then
      local VM_HOSTS=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.hosts")
      for VM_HOST in ${VM_HOSTS}; do
         let MEMBER_COUNT=MEMBER_COUNT+1
         MEMBER=`getVmMemberName $VM_HOST`
         pid=`getVmMemberPid $VM_HOST $MEMBER $__WORKSPACE $__RWE`
         if [ "$pid" != "" ]; then
             let MEMBER_RUNNING_COUNT=MEMBER_RUNNING_COUNT+1
         fi
      done
   else
      local RUN_DIR=$PADOGRID_WORKSPACES_HOME/$__WORKSPACE/clusters/$__CLUSTER/run
      pushd $RUN_DIR > /dev/null 2>&1
      MEMBER_PREFIX=$(getMemberPrefix "$__CLUSTER")
      for i in ${MEMBER_PREFIX}*; do
         if [ -d "$i" ]; then
            MEMBER=$i
            MEMBER_NUM=${MEMBER##$MEMBER_PREFIX}
            let MEMBER_COUNT=MEMBER_COUNT+1
            pid=`getMemberPid $MEMBER $WORKSPACE $__RWE`
            if [ "$pid" != "" ]; then
               let MEMBER_RUNNING_COUNT=MEMBER_RUNNING_COUNT+1
       fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $MEMBER_RUNNING_COUNT
}

#
# Returns the member name prefix that is used in constructing the unique member
# name for a given member number. See getMemberName.
# @param clusterName    Optional cluster name. If not specified then it defaults to CLUSTER.
# @param podName        Optional pod name. If not specified then it defaults to POD.
# @param nodeNamePrefix Optional node name prefix. If not specified then it defaults to NODE_NAME_PREFIX.
#
function getMemberPrefix
{
   local __CLUSTER="$1"
   local __POD="$2"
   local __NODE_NAME_PREFIX="$3"

   if [ "$__CLUSTER" == "" ]; then
     __CLUSTER=$CLUSTER
   fi
   if [ "$__POD" == "" ]; then
     __POD=$POD
   fi
   if [ "$__NODE_NAME_PREFIX" == "" ]; then
     __NODE_NAME_PREFIX=$NODE_NAME_PREFIX
   fi

   if [ "$__POD" != "local" ]; then
      echo "${__CLUSTER}-${__NODE_NAME_PREFIX}-"
   elif [ "$PRODUCT" == "spark" ]; then
      echo "${__CLUSTER}-worker-`hostname`-"
   elif [ "$PRODUCT" == "hadoop" ]; then
      echo "${__CLUSTER}-datanode-`hostname`-"
   else
      echo "${__CLUSTER}-`hostname`-"
   fi
}

#
# Returns the unique member name (ID) for the specified member number.
# @param memberNumber
#
function getMemberName
{
   local __MEMBER_NUM=`trimString "$1"`
   len=${#__MEMBER_NUM}
   if [ $len == 1 ]; then
      __MEMBER_NUM=0$__MEMBER_NUM
   else
      __MEMBER_NUM=$__MEMBER_NUM
   fi
   echo "`getMemberPrefix`$__MEMBER_NUM"
}

#
# Returns the member number of the specified member name. By convention, member
# names end with a member number, i.e., "-01". It returns an empty string if
# the trailing string is not a number.
#
# @param memberName
#
function getMemberNumber
{
   local __MEMBER_NAME="$1"
   local __MEMBER_NUMBER=${__MEMBER_NAME##*-}
   if [ "__MEMBER_NUMBER$__MEMBER_NUMBER" != "" ]; then
      __MEMBER_NUMBER=$(trimLeadingZero $__MEMBER_NUMBER)
      if [ $(isNumber "$__MEMBER_NUMBER") == "false" ]; then
         __MEMBER_NUMBER=""
      fi
   fi
   echo $__MEMBER_NUMBER
}

#
# Returns the member name of the specified VM host (address).
# @required POD     Pod name.
# @required VM_USER VM ssh user name
# @optional VM_KEY  VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host    VM host name or address. If not specified then the current VM's host name is applied.
#
function getVmMemberName
{
   __HOST="$1"
   if [ "$__HOST" == "" ]; then
      __HOSTNAME=`hostname`
   else
      __HOSTNAME=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "hostname"`
      __HOSTNAME=$__HOSTNAME
   fi
   if [ "$__HOSTNAME" == "" ]; then
      echo ""
   elif [ "$POD" != "local" ]; then
      echo "${CLUSTER}-${__HOSTNAME}"
   else
      echo "${CLUSTER}-${__HOSTNAME}-01"
   fi
}

#
# Returns the member PID if it is running. Empty value otherwise.
# @required NODE_LOCAL       Node name with the local extension. For remote call only.
# @optional POD              Pod type. Default: local
# @optional REMOTE_SPECIFIED true if remote node, false if local node. Default: false
# @param    memberName       Unique member name
# @param    workspaceName    Workspace name
# @param    rweName          Optional RWE name. This parameter is optional in order to make
#                            it version backward compatible (v0.9.19). It will be mandatory
#                            in the future.
#
function getMemberPid
{
   local __MEMBER=$1
   local __WORKSPACE=$2
   local __RWE=$3

   __IS_GUEST_OS_NODE=`isGuestOs $NODE_LOCAL`
   if [ "$__IS_GUEST_OS_NODE" == "true" ] && [ "$POD" != "local" ] && [ "$REMOTE_SPECIFIED" == "false" ]; then
     if [ "$ __RWE" == "" ]; then
        members=`ssh -q -n $SSH_USER@$NODE_LOCAL -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -wweo pid,comm,args | grep java | grep pado.vm.id=$__MEMBER | grep padogrid.workspace=$__WORKSPACE | grep -v grep" | awk '{print $1}'`
     else
        members=`ssh -q -n $SSH_USER@$NODE_LOCAL -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -wweo pid,comm,args | grep java | grep pado.vm.id=$__MEMBER | grep padogrid.workspace=$__WORKSPACE | grep padogrid.rwe=$__RWE | grep -v grep" | awk '{print $1}'`
     fi
   else
      if [ "$ __RWE" == "" ]; then
         if [[ "$OS_NAME" == "CYGWIN"* ]]; then
            local members="$(WMIC path win32_process get Caption,Processid,Commandline | grep java | grep pado.vm.id=$__MEMBER | grep "padogrid.workspace=$__WORKSPACE | grep -v grep" | awk '{print $(NF-1)}')"
         else
            local members="$(ps -wweo pid,comm,args | grep java | grep pado.vm.id=$__MEMBER | grep padogrid.workspace=$__WORKSPACE | grep -v grep | awk '{print $1}')"
         fi
      else
         if [[ "$OS_NAME" == "CYGWIN"* ]]; then
            local members="$(WMIC path win32_process get Caption,Processid,Commandline | grep java | grep pado.vm.id=$__MEMBER | grep padogrid.workspace=$__WORKSPACE | grep padogrid.rwe=$__RWE | awk '{print $(NF-1)}')"
         else
            local members="$(ps -wweo pid,comm,args | grep java | grep pado.vm.id=$__MEMBER | grep padogrid.workspace=$__WORKSPACE | grep padogrid.rwe=$__RWE | grep -v grep | awk '{print $1}')"
         fi
      fi
   fi
   spids=""
   for j in $members; do
      spids="$j $spids"
   done
   spids=`trimString "$spids"`
   echo $spids
}

#
# Returns the member PID of VM if it is running. Empty value otherwise.
# This function is for clusters running on VMs whereas the getMemberPid
# is for pods running on the same machine.
# @required VM_USER        VM ssh user name
# @optional VM_KEY         VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host           VM host name or address
# @param    memberName     Unique member name
# @param    workspaceName  Workspace name
# @param    rweName        Optional RWE name. This parameter is optional in order to make
#                          it version backward compatible (v0.9.19). It will be mandatory
#                          in the future.
#
function getVmMemberPid
{
   local __HOST=$1
   local __MEMBER=$2
   local __WORKSPACE=$3
   local __RWE=$4

   if [ "$ __RWE" == "" ]; then
      members=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -wweo pid,comm,args | grep java | grep pado.vm.id=$__MEMBER | grep padogrid.workspace=$__WORKSPACE | grep -v grep" | awk '{print $1}'`
   else
      members=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -wweo pid,comm,args | grep java | grep pado.vm.id=$__MEMBER | grep padogrid.workspace=$__WORKSPACE | grep padogrid.rwe=$__RWE | grep -v grep " | awk '{print $1}'`
   fi
   spids=""
   for j in $members; do
      spids="$j $spids"
   done
   spids=`trimString "$spids"`
   echo $spids
}

#
# Returns a string list with all duplicate words removed from the specified string list.
# @param stringList String list of words separated by spaces
#
function unique_words
{
   local __words=$1
   local  __resultvar=$2
   local __visited
   local __unique_words
   local __i
   local __j

   # remove all repeating hosts
   for __i in $__words; do
      __visited=false
      for __j in $__unique_words; do
         if [ "$__i" == "$__j" ]; then
            __visited=true
         fi
      done
      if [ "$__visited" == "false" ]; then
         __unique_words="$__unique_words $__i"
      fi
   done

   if [[ "$__resultvar" ]]; then
      eval $__resultvar="'$__unique_words'"
      #echo `trimString "$__resultvar"`
   else
     echo `trimString "$__unique_words"`
   fi
}

#
# Sets all the properties read from the specified properties file to the
# specified array. The array must be declared before invoking this function,
# otherwise, it will fail with an error.
#
# Example:
#    declare -a propArray
#    getPropertiesArray "$ETC_DIR/cluster.properties" propArray
#    let len=${#propArray[@]}
#    if [ $len -gt 0 ]; then
#       let last_index=len-1
#       for i in $(seq 0 $last_index); do
#          echo "[$i] ${propArray[$i]}"
#       done
#    fi
#    
# @param propFilePath  Properties file path 
# @param propArray     Sets this index array containing properties in the form of
#                      "key=value". It must be declared before invoking this
#                      function, e.g., declear -a propArray.
#
function getPropertiesArray
{
   local __PROPERTIES_FILE=$1
   local array=$2
   declare -a | grep -q "declare -a ${array}" || echo -e >&2 "${CError}ERROR:${CNone} getPropertiesArray - no ${array} index array declared"
   local index=0
   if [ -f $__PROPERTIES_FILE ]; then
      while IFS= read -r line; do
         local line=`trimString "$line"`
         if [ "$line" != "" ] && [[ $line != "#"* ]]; then
            local key=${line%%=*}
            local value=${line#*=}
            eval "${array}[\"${index}\"]=${key}=${value}"
            let index=index+1
         fi
      done < "$__PROPERTIES_FILE"
      unset IFS
   fi
}

#
# Returns the property value found in the specified file.
# @param propertiesFilePath  Properties file path.
# @param propertyName        Property name.
# @param defaultValue        If the specified property is not found then this default value is returned.
#
function getProperty
{
   local __PROPERTIES_FILE=$1
   local __VALUE=""
local IFS="
"
   if [ -f $__PROPERTIES_FILE ]; then
      for line in `grep $2 ${__PROPERTIES_FILE}`; do
         line=`trimString "$line"`
         if [[ $line == $2=* ]]; then
            __VALUE=${line#$2=}
            break;
         fi
      done
   fi
   if [ "$__VALUE" == "" ]; then
      __VALUE="$3"
   fi
   if [[ $__VALUE =~ .*\${.*}.* ]]; then
      local __ENV_VAR=$(echo "$__VALUE" | sed -e 's/^.*${//' -e 's/}.*//')
      local __ENV_VAR_VALUE=$(eval "echo \${$__ENV_VAR}")
      # Replace '/' to avoid expansion for the subsequent use.
      __ENV_VAR_VALUE=$(echo $__ENV_VAR_VALUE | sed 's/\//\\\//g')
      __VALUE=$(echo $__VALUE | sed -e "s/\${$__ENV_VAR}/$__ENV_VAR_VALUE/")
   fi
   echo "$__VALUE"
   unset IFS
}

#
# Returns the property value found in the specified file.
# @param propertiesFilePath  Properties file path.
# @param propertyName        Property name.
# @param defaultValue        If the specified property is not found then this default value is returned.
#
function getProperty2
{
   local __PROPERTIES_FILE=$1
   if [ -f $__PROPERTIES_FILE ]; then
      while IFS= read -r line; do
         line=`trimString "$line"`
         if [[ $line == $2=* ]]; then
            __VALUE=${line#$2=}
            break;
         fi
      done < "$__PROPERTIES_FILE"
      unset IFS
      if [ -z "$__VALUE" ]; then
         echo $3
      else
         echo "$__VALUE"
      fi
   else
      echo "$__VALUE"
   fi
}

#
# Returns the property value found in the $PODS_DIR/$POD/etc/pod.properties file.
# @required  POD           Pod name.
# @param     propertyName  Property name.
# @param     defaultValue  If the specified property is not found then this default value is returned.
#
function getPodProperty
{
   __PROPERTIES_FILE="$PODS_DIR/$POD/etc/pod.properties"
   echo `getProperty $__PROPERTIES_FILE $1 $2`
}

#
# Returns the property value found in the $CLUSTERS_DIR/$CLUSTER/cluster.properties file.
# @required  CLUSTERS_DIR  Cluster directory path.
# @required  CLUSTER       Cluster name.
# @param     propertyName  Property name.
# @param     defaultValue  If the specified property is not found then this default value is returned.
#
function getClusterProperty
{
   __PROPERTIES_FILE="$CLUSTERS_DIR/$CLUSTER/etc/cluster.properties"
   echo `getProperty $__PROPERTIES_FILE $1 $2`
}

#
# Returns the specified workspace's cluster property value. It returns an empty string if
# any of the following conditions is met.
#   - workspaceName or clusterName is not specified
#   - workspaceName or clusterName do not exist
#
# @param workspaceName Workspace name.
#                      it assumes the current workspace.
# @param clusterName   Cluster name.
# @param propertyName  Property name.
# @param defaultValue  If the specified property is not found then this default value is returned.
#
function getWorkspaceClusterProperty
{
   local __WORKSPACE=$1
   local __CLUSTER=$2
   if [ "$__WORKSPACE" == "" ]; then
      echo ""
      return
   fi
   if [ "$__CLUSTER" == "" ]; then
      echo ""
      return
   fi
   __PROPERTIES_FILE="$__WORKSPACE/$__CLUSTER/etc/cluster.properties"
   if [ -f "$__PROPERTIES_FILE" ]; then
      echo `getProperty $__PROPERTIES_FILE $3 $4`
   else
      echo ""
   fi
}

# 
# Sets the specified property in the the properties file.
# @param propertiesFilePath Properties file path.
# @param propertyName       Property name.
# @param propertyValue      Property value.
#
function setProperty
{
   local __PROPERTIES_FILE="$1"
   local __PROPERTY="$2"
   local __VALUE=$(echo "$3" | sed -e 's/ /\\ /g')
   local __LINE_NUM=0
   local __SED_BACKUP
   if [ -f "$__PROPERTIES_FILE" ]; then
      local __found="false"
      while IFS= read -r line; do
         let __LINE_NUM=__LINE_NUM+1
         line=`trimString "$line"`
         if [[ $line == ${__PROPERTY}=* ]]; then
            __found="true"
            break;
         fi
      done < "$__PROPERTIES_FILE"
      unset IFS
      if [ "$__found" == "true" ]; then
         # SED backup prefix
         if [[ ${OS_NAME} == DARWIN* ]]; then
            # Mac - space required
            __SED_BACKUP=" 0"
         else
            __SED_BACKUP="0"
         fi
         sed -i${__SED_BACKUP} -e "${__LINE_NUM}s/.*/${__PROPERTY}=${__VALUE}/" "$__PROPERTIES_FILE"
      else
         echo "$2=$3" >> "$__PROPERTIES_FILE"
      fi
   fi
}

#
# Sets the specified pod property in the $PODS_DIR/$POD/etc/pod.properties file. 
# @required  PODS_DIR      Pods directory path
# @required  POD           Pod name.
# @param     propertyName  Property name.
# @param     propertyValue Property value.
#
function setPodProperty
{
   __PROPERTIES_FILE="$PODS_DIR/$POD/etc/pod.properties"
   `setProperty "$__PROPERTIES_FILE" $1 $2`
}

# 
# Sets the cluster property in the $ETC_DIR/cluster.properties file.
# @required  CLUSTER Cluster name.
# @param     propertyName  Property name.
# @param     propertyValue Property value.
#
function setClusterProperty
{
   __PROPERTIES_FILE="$CLUSTERS_DIR/$CLUSTER/etc/cluster.properties"
   `setProperty "$__PROPERTIES_FILE" $1 $2`
}

#
# Returns a list of all member directory names.
# @required RUN_DIR        Cluster run directory.
# @required MEMBER_PREFIX  Member name prefix
#
function getMemberDirNameList
{
   pushd $RUN_DIR > /dev/null 2>&1
   __COUNT=0
   __MEMBERS=""
   for i in ${MEMBER_PREFIX}*; do
      let __COUNT=__COUNT+1
      if [ $__COUNT -eq 1 ]; then
        __MEMBERS="$i"
      else
         __MEMBERS="$__MEMBERS $i"
      fi
   done
   popd > /dev/null 2>&1
   echo $__MEMBERS
}

#
# Returns the total number of members added.
# @required RUN_DIR        Cluster run directory.
# @required MEMBER_PREFIX  Member name prefix
#
function getMemberCount
{
   pushd $RUN_DIR > /dev/null 2>&1
   __COUNT=0
   for i in ${MEMBER_PREFIX}*; do
      if [ -d "$i" ]; then
         let __COUNT=__COUNT+1
      fi
   done
   popd > /dev/null 2>&1
   echo $__COUNT
}

#
# Returns a list of all member numbers including leading zero.
# @required RUN_DIR        Cluster run directory.
# @required MEMBER_PREFIX  Member name prefix
#
function getMemberNumList
{
   pushd $RUN_DIR > /dev/null 2>&1
   __COUNT=0
   __MEMBERS=""
   for i in ${MEMBER_PREFIX}*; do
      let __COUNT=__COUNT+1
      __NUM=${i:(-2)}
      if [ $__COUNT -eq 1 ]; then
        __MEMBERS="$__NUM"
      else
         __MEMBERS="$__MEMBERS $__NUM"
      fi
   done
   popd > /dev/null 2>&1
   echo $__MEMBERS
}

#
# Returns VirtualBox adapter private IP addresses.
# @required PADOGRID_WORKSPACE
#
function getPrivateNetworkAddresses
{
   if [ "$PADOGRID_WORKSPACE" != "" ] && [ -d $PADOGRID_WORKSPACE ]; then
      __TMP_DIR=$PADOGRID_WORKSPACE/tmp
   else
      __TMP_DIR=/tmp
   fi
   if [ ! -d "$__TMP_DIR" ]; then
      mkdir -p $__TMP_DIR
   fi
   __TMP_FILE=$__TMP_DIR/padogrid-tmp.txt
   
   __PRIVATE_IP_ADDRESSES=""
   vb_found="false"
   if [[ "$OS_NAME" == "CYGWIN"* ]]; then
      ipconfig > $__TMP_FILE
      while IFS= read -r line; do
         if [[ $line == *"VirtualBox Host-Only Network"* ]]; then
            vb_found="true"
         elif [ $vb_found == "true" ]; then
            if [[ $line == *"IPv4 Address"* ]]; then
               ip_address=${line#*:}
               __PRIVATE_IP_ADDRESSES="$__PRIVATE_IP_ADDRESSES $ip_address"
               vb_found="false"
            fi
         fi  
      done < "$__TMP_FILE"
      unset IFS
      if [ -f $__TMP_FILE ]; then
         rm $__TMP_FILE
      fi
   else
      ifconfig > $__TMP_FILE
      while IFS= read -r line; do
         if [[ $line == *"vboxnet"* ]]; then
            vb_found="true"
         elif [ $vb_found == "true" ]; then
            if [[ $line == *"inet"* ]]; then
               ip_address=`echo $line | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p'`
               __PRIVATE_IP_ADDRESSES="$__PRIVATE_IP_ADDRESSES $ip_address"
               vb_found="false"
            fi
         fi  done < "$__TMP_FILE"
      unset IFS
   fi
   if [ -f $__TMP_FILE ]; then
      rm $__TMP_FILE
   fi
   echo $__PRIVATE_IP_ADDRESSES
}

#
# Updates the RWE workspaces envionment variables with the current values
# in the $HOME/.padogrid/workspaces/<rwe>/rweenv.sh file.
# @param rwePath RWE path. If not specified then PADOGRID_WORKSPACES_HOME is assigned.
# @required PADOGRID_WORKSPACE
#
function updateRweEnvFile
{
   local __RWE_PATH="$1"
   if [ "$__RWE_PATH" == "" ]; then
      __RWE_PATH="$PADOGRID_WORKSPACES_HOME"
   fi
   local RWE=$(basename "$__RWE_PATH")
   local WORKSPACE=$(basename "$PADOGRID_WORKSPACE")
   local HOME_RWE_DIR="$HOME/.padogrid/workspaces/$RWE"
   local HOME_RWEENV_FILE="$HOME_RWE_DIR/rweenv.sh"

   if [ ! -d "$HOME_RWE_DIR" ]; then
      mkdir -p "$HOME_RWE_DIR"
   fi
   local HOME_RWEENV_FILE="$HOME_RWE_DIR/rweenv.sh"
   echo "WORKSPACE=\"$WORKSPACE\"" > $HOME_RWEENV_FILE
}

#
# Retrieves the RWE environment variables set in the $HOME/.padogrid/workspaces/<rwe>/rweenv.sh file.
# @param rwePath RWE path. If not specified then PADOGRID_WORKSPACES_HOME is assigned.
#
function retrieveRweEnvFile
{
   local __RWE_PATH="$1"
   if [ "$__RWE_PATH" == "" ]; then
      __RWE_PATH="$PADOGRID_WORKSPACES_HOME"
   fi
   local RWE=$(basename "$__RWE_PATH")
   local WORKSPACE=$(basename "$PADOGRID_WORKSPACE")
   local HOME_RWE_DIR="$HOME/.padogrid/workspaces/$RWE"
   local HOME_RWEENV_FILE="$HOME_RWE_DIR/rweenv.sh"

   # For backward compatibility
   if [ ! -d "$HOME_RWE_DIR" ]; then
      if [ -f "$__RWE_PATH/rweenv.sh" ]; then
         mkdir -p "$HOME_RWE_DIR"
         cp "$__RWE_PATH/.rwe/rweenv.sh" "$HOME_RWE_DIR/"
      fi
   fi

   if [ -f "$HOME_RWEENV_FILE" ]; then
      . "$HOME_RWEENV_FILE"
      if [ "$WORKSPACE" != "" ]; then
         PADOGRID_WORKSPACE="$PADOGRID_WORKSPACES_HOME/$WORKSPACE"
      else
         PADOGRID_WORKSPACE=""
      fi
   else
      PADOGRID_WORKSPACE=""
   fi
   # If the workspace does not exist then pick the first workspace in the RWE dir
   if [ "$PADOGRID_WORKSPACE" == "" ] || [ ! -d "$PADOGRID_WORKSPACE" ]; then
      local __WORKSPACES=$(list_workspaces -rwe $RWE)
      for i in $__WORKSPACES; do
         __WORKSPACE=$i
         PADOGRID_WORKSPACE="$PADOGRID_WORKSPACES_HOME/$__WORKSPACE"
         updateRweEnvFile
         break;
      done
   fi
}

#
# Updates the workspace envionment variables with the current values
# in the $HOME/.padogrid/workspaces/<rwe>/<workspace>/workspaceenv.sh file.
# @param workspacePath Workspace path. If not specified then PADOGRID_WORKSPACE is assigned.
# @required CLUSTER
# @required POD
#
function updateWorkspaceEnvFile
{
   local __WORKSPACE_PATH="$1"
   if [ "$__WORKSPACE_PATH" == "" ]; then
      __WORKSPACE_PATH="$PADOGRID_WORKSPACE"
   fi
   if [ ! -d "$__WORKSPACE_PATH" ]; then
      echo >&2 "Workspace does not exist: [$__WORKSPACE_PATH]."
      return 1
   fi

   local RWE=$(basename $(dirname "$__WORKSPACE_PATH"))
   local WORKSPACE=$(basename "$__WORKSPACE_PATH")
   local HOME_WORKSPACE_DIR="$HOME/.padogrid/workspaces/$RWE/$WORKSPACE"
   local HOME_WORKSPACEENV_FILE="$HOME_WORKSPACE_DIR/workspaceenv.sh"

   if [ ! -d "$HOME_WORKSPACE_DIR" ]; then
      mkdir -p "$HOME_WORKSPACE_DIR"
   fi
   
   echo "CLUSTER=$CLUSTER" > "$HOME_WORKSPACEENV_FILE"
   echo "POD=$POD" >> "$HOME_WORKSPACEENV_FILE"
}

#
# Retrieves the workspace environment variables set in the
# $HOME/.padogrid/workspaces/<rwe>/<workspace>/workspaceenv.sh file.
# @param workspacePath Workspace path. If not specified then PADOGRID_WORKSPACE is assigned.
#
function retrieveWorkspaceEnvFile
{
   local __WORKSPACE_PATH="$1"
   if [ "$__WORKSPACE_PATH" == "" ]; then
      __WORKSPACE_PATH="$PADOGRID_WORKSPACE"
   fi

   local RWE=$(basename $(dirname "$__WORKSPACE_PATH"))
   local WORKSPACE=$(basename "$__WORKSPACE_PATH")
   local HOME_WORKSPACE_DIR="$HOME/.padogrid/workspaces/$RWE/$WORKSPACE"
   local HOME_WORKSPACEENV_FILE="$HOME_WORKSPACE_DIR/workspaceenv.sh"

   # For backward compatibility
   if [ ! -d "$HOME_WORKSPACE_DIR" ]; then
      if [ -f "$__WORKSPACE_PATH/.workspace/workspaceenv.sh" ]; then
         mkdir -p "$HOME_WORKSPACE_DIR"
         cp "$__WORKSPACE_PATH/.workspace/workspaceenv.sh" "$HOME_WORKSPACE_DIR/"
      fi
   fi

   if [ -f "$HOME_WORKSPACEENV_FILE" ]; then
      . "$HOME_WORKSPACEENV_FILE"
   fi

   if [ -d "$__WORKSPACE_PATH/clusters" ]; then
      if [ "$CLUSTER" == "" ] || [ ! -d "$__WORKSPACE_PATH/clusters/$CLUSTER" ]; then
         local __CLUSTERS=$(ls $__WORKSPACE_PATH/clusters)
         local __CLUSTER=""
         for i in $__CLUSTERS; do
            __CLUSTER=$i
            break;
         done
         CLUSTER=$__CLUSTER
         updateWorkspaceEnvFile "$__WORKSPACE_PATH"
      fi
   fi
   if [ -d "$__WORKSPACE_PATH/pods" ]; then
      if [ "$POD" == "" ] || [ ! -d "$__WORKSPACE_PATH/pods/$POD" ]; then
         local __PODS=$(ls $__WORKSPACE_PATH/pods)
         local __POD=""
         for i in $__PODS; do
            __POD=$i
            break;
         done
         POD=$__POD
         updateWorkspaceEnvFile "$__WORKSPACE_PATH"
      fi
   fi
}

#
# Updates the product envionment variables with the current values
# in the $HOME/.padogrid/<rwe>/<workspace>/clusters/<cluster>/clusterenv.sh file.
# @param clusterPath Cluster path. If not specified then $PADOGRID_WORKSPACE/clusters/$CLUSTER is assigned.
# @required PRODUCT
# @required CLUSTER_TYPE
#
function updateClusterEnvFile
{
   local __CLUSTER_PATH="$1"
   if [ "$__CLUSTER_PATH" == "" ]; then
      __CLUSTER_PATH="$PADOGRID_WORKSPACE/clusters/$CLUSTER"
   fi
   if [ ! -d "$__CLUSTER_PATH" ]; then
      echo >&2 "Cluster does not exist: [$__CLUSTER_PATH]."
      return 1
   fi

   # Upgrade to the new release directory struture (0.9.6)
   if [ ! -d "$__CLUSTER_PATH/.cluster" ]; then
      if [ -f "$__CLUSTER_PATH/.cluster" ]; then
         rm -f "$__CLUSTER_PATH/.cluster"
      fi
   fi
   if [ ! -d "$__CLUSTER_PATH/.cluster" ]; then
      mkdir "$__CLUSTER_PATH/.cluster"
   fi

   local __CLUSTER=$(basename "$__CLUSTER_PATH")
   local WORKSPACE_DIR=$(dirname $(dirname "$__CLUSTER_PATH"))
   local WORKSPACE=$(basename "$WORKSPACE_DIR")
   local RWE=$(basename $(dirname "$WORKSPACE_DIR"))
   local HOME_WORKSPACE_DIR="$HOME/.padogrid/workspaces/$RWE/$WORKSPACE"
   local HOME_CLUSTER_DIR="$HOME_WORKSPACE_DIR/clusters/$__CLUSTER"
   local HOME_CLUSTERENV_FILE="$HOME_CLUSTER_DIR/clusterenv.sh"

   if [ ! -d "$HOME_CLUSTER_DIR" ]; then
      mkdir -p "$HOME_CLUSTER_DIR"
   fi

   # Override "gemfire" with "geode". Both products share resources under the name "geode".
   # Override "jet" with "hazelcast". Both products share resources under the name "hazelcast".
   local PRODUCT_TYPE="$PRODUCT"
   if [ "$PRODUCT" == "gemfire" ]; then
      echo "PRODUCT=geode" > "$HOME_CLUSTERENV_FILE"
      CLUSTER_TYPE="gemfire"
   elif [ "$PRODUCT" == "jet" ]; then
      echo "PRODUCT=hazelcast" > "$HOME_CLUSTERENV_FILE"
   elif [ "$PRODUCT" == "confluent" ]; then
      echo "PRODUCT=kafka" > "$HOME_CLUSTERENV_FILE"
      CLUSTER_TYPE="confluent"
   else
      echo "PRODUCT=$PRODUCT" > "$HOME_CLUSTERENV_FILE"
   fi
   echo "CLUSTER_TYPE=$CLUSTER_TYPE" >> "$HOME_CLUSTERENV_FILE"
}

#
# Retrieves the cluster environment variables set in the
# $HOME/.padogrid/<rwe>/<workspace>/clusters/<cluster>/clusterenv.sh file.
# @param clusterPath Cluster path. If not specified then the current cluster path is assigned.
#
function retrieveClusterEnvFile
{
   local __CLUSTER_PATH="$1"
   if [ "$__CLUSTER_PATH" == "" ]; then
      if [ "$CLUSTER" == "" ]; then
          PRODUCT="none"
          CLUSTER_TYPE="none"
          return
      else
         __CLUSTER_PATH="$PADOGRID_WORKSPACE/clusters/$CLUSTER"
      fi
   fi

   local __CLUSTER=$(basename "$__CLUSTER_PATH")
   local WORKSPACE_DIR=$(dirname $(dirname "$__CLUSTER_PATH"))
   local WORKSPACE=$(basename "$WORKSPACE_DIR")
   local RWE=$(basename $(dirname "$WORKSPACE_DIR"))
   local HOME_WORKSPACE_DIR="$HOME/.padogrid/workspaces/$RWE/$WORKSPACE"
   local HOME_CLUSTERENV_FILE="$HOME_WORKSPACE_DIR/clusters/$__CLUSTER/clusterenv.sh"

   # For backward compatibility
   if [ ! -d "$HOME_WORKSPACE_DIR/clusters/$__CLUSTER" ]; then
      if [ -f "$__CLUSTER_PATH/.cluster/clusterenv.sh" ]; then
         mkdir -p "$HOME_WORKSPACE_DIR/clusters/$__CLUSTER"
         cp "$__CLUSTER_PATH/.cluster/clusterenv.sh" "$HOME_WORKSPACE_DIR/clusters/$__CLUSTER/"
      fi
   fi

   if [ -f "$HOME_CLUSTERENV_FILE" ]; then
      . "$HOME_CLUSTERENV_FILE"
   else
      if [ -f "$CLUSTER_DIR/etc/gemfire.properties" ]; then
         # Without the clusterenv.sh file, we cannot determine whether geode or gemfire.
         # Set it to geode for now.
         PRODUCT="geode"
         CLUSTER_TYPE=$PRODUCT
      elif [ -f "$CLUSTER_DIR/etc/hazelcast-jet.xml" ]; then
         PRODUCT="hazelcast"
         CLUSTER_TYPE="jet"
      elif [ -f "$CLUSTER_DIR/etc/hazelcast.xml" ]; then
         PRODUCT="hazelcast"
         CLUSTER_TYPE="imdg"
      elif [ -f "$CLUSTER_DIR/etc/gemfirexd.properties" ]; then
         PRODUCT="snappydata"
         CLUSTER_TYPE=$PRODUCT
      elif [ -f "$CLUSTER_DIR/etc/tangosol-coherence-override.xml" ]; then
         PRODUCT="coherence"
         CLUSTER_TYPE=$PRODUCT
      elif [ -f "$CLUSTER_DIR/etc/redis.conf" ]; then
         PRODUCT="redis"
         CLUSTER_TYPE=$PRODUCT
      elif [ -f "$CLUSTER_DIR/etc/spark-env.sh" ]; then
         PRODUCT="spark"
         CLUSTER_TYPE="standalone"
      elif [ -f "$CLUSTER_DIR/etc/server.properties" ]; then
         # Without the clusterenv.sh file, we cannot determine whether kafka or confluent.
         # Set it to kafka for now.
         PRODUCT="kafka"
         CLUSTER_TYPE=$PRODUCT
      elif [ -d "$CLUSTER_DIR/etc/pseudo" ]; then
         PRODUCT="hadoop"
         CLUSTER_TYPE="pseudo"
      else
         PRODUCT="none"
         CLUSTER_TYPE="none"
      fi
   fi
   # Override "gemfire" with "geode". Both products share resources under the name "geode".
   # Override "jet" with "hazelcast". Both products share resources under the name "hazelcast".
   # Override "confluent" with "kafka". Both products share resources under the name "kafka".
   if [ "$PRODUCT" == "gemfire" ]; then
      PRODUCT="geode"
   elif [ "$PRODUCT" == "jet" ]; then
      PRODUCT="hazelcast"
   elif [ "$PRODUCT" == "confluent" ]; then
      PRODUCT="kafka"
   elif [ "$PRODUCT" == "" ]; then
      PRODUCT="none"
      CLUSTER_TYPE="none"
   fi
}

#
# Switches to the specified root workspaces environment. This function is provided
# to be executed in the shell along with other padogrid commands. It
# sets the environment variables in the parent shell.
#
# @required PADOGRID_WORKSPACES_HOME Workspaces directory path.
# @param    rweName   Optional RWE name.
#
function switch_rwe
{
   EXECUTABLE=switch_rwe
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Switch to the specified root workspaces environment"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [rwe_name [workspace_name[/directory_name/...]]] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Switches to the specified rwe (root workspaces environment) and changes directory"
      echo "   to the specified directory. To specify the nested directory names, use the tab"
      echo "   key to drill down the directory structure. If the specified nested directory"
      echo "   is a workspace or workspace component, then it also automatically switches to"
      echo "   their respective context."
      echo ""
      echo "   If the workspace directory is not specified and the current workspace name exists in"
      echo "   the target RWE, then it switches into that workspace. Otherwise, it switches to"
      echo "   the target RWE's default workspace."
      echo ""
      echo "OPTIONS"
      echo "   rwe_name"
      echo "             Name of the root workspaces environment. If not specified, then switches"
      echo "             to the current rwe (root workspaces environment)."
      echo ""
      echo "   workspace_name"
      echo "             Workspace to switch to. If not specified, then switches to the default"
      echo "             workspace of the specified RWE."
      echo ""
      echo "   /directory_name/..."
      echo "             Directory path relative to the rwe directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name."
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE"
      echo ""
      echo "EXAMPLES"
      echo "   - Switch RWE to 'myrwe', switch workspace to 'myws', switch cluster to 'mycluster', and"
      echo "     change directory to that cluster's 'etc' directory."
      echo ""
      echo "        switch_rwe myrwe/myws/clusters/mycluster/etc/"
      echo ""
      echo "   - Switch RWE to 'myrwe', wwitch workspace to 'myws', change directory to the perf_test's"
      echo "     'bin_sh' directory."
      echo ""
      echo "        switch_rwe myrwe/myws/apps/ perf_test/ bin_sh/"
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*rwe*" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi

   # Reset Pado home path
   export PADO_HOME=""

   local NEW_RWE_DIR
   local NEW_WORKSPACE_DIR

   if [ "$1" == "" ]; then
      if [ ! -d "$PADOGRID_WORKSPACES_HOME/clusters/$CLUSTER" ]; then
         export CLUSTER=""
      fi
      . $PADOGRID_WORKSPACES_HOME/initenv.sh -quiet

      # Source in the last switched cluster
      retrieveClusterEnvFile
      cd_rwe $@
   else
      local __PATH=""
      for i in "$@"; do
        if [ "$__PATH" == "" ] || [[ "$__PATH" == */ ]]; then
           __PATH="${__PATH}$i"
        else
           __PATH="${__PATH}/$i"
        fi
      done
      local PARENT_DIR="$(dirname "$PADOGRID_WORKSPACES_HOME")"
      local tokens=$(echo "$__PATH" | sed 's/\// /g')
      local __INDEX=0
      local __WORKSPACE=""
      local __COMPONENT_DIR_NAME=""
      local __COMPONENT_NAME=""
      for i in $tokens; do
        let __INDEX=__INDEX+1
        if [ $__INDEX -eq 1 ]; then
            __RWE=$i
        elif [ $__INDEX -eq 2 ]; then
            __WORKSPACE=$i
        elif [ $__INDEX -eq 3 ]; then
            __COMPONENT_DIR_NAME=$i
        elif [ $__INDEX -eq 4 ]; then
            __COMPONENT_NAME=$i
        fi
      done
      NEW_RWE_DIR="$PARENT_DIR/$__RWE"
      if [ ! -d "$PARENT_DIR/$__RWE" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid RWE name: [$__RWE]. RWE does not exist. Command aborted."
         return 1
      elif [ ! -r "$PARENT_DIR/$__RWE" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid RWE: [$__RWE]. Permission denied. Command aborted."
         return 1
      elif [ "$__WORKSPACE" != "" ]; then
         NEW_WORKSPACE_DIR="$NEW_RWE_DIR/$__WORKSPACE"
         if [ ! -r "$NEW_RWE_DIR/initenv.sh" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} RWE access denied: [$__RWE]. You do not have RWE access permissions. Command aborted."
            return 1
         elif [ ! -d "$NEW_WORKSPACE_DIR" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid workspace name: [$__WORKSPACE]. Workspace does not exist. Command aborted."
            return 1
         elif [ ! -r "$NEW_WORKSPACE_DIR" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Workspace access denied: [$__WORKSPACE]. You do not have workspace access permissions. Command aborted."
            return 1
         fi

         # Set PADOGRID_WORKSPACES_HOME here. It's a new RWE.
         export PADOGRID_WORKSPACES_HOME="$NEW_RWE_DIR"

         # Retreive last switched cluster/pod
         retrieveWorkspaceEnvFile "$NEW_WORKSPACE_DIR"
         export CLUSTER
         export POD

         if [ "$__COMPONENT_DIR_NAME" == "clusters" ] && [ "$__COMPONENT_NAME" != "" ]; then
             if [ -d "$NEW_WORKSPACE_DIR/clusters/$__COMPONENT_NAME" ]; then
                export CLUSTER="$__COMPONENT_NAME"
                updateWorkspaceEnvFile
             fi
         elif [ "$__COMPONENT_DIR_NAME" == "pods" ] && [ "$__COMPONENT_NAME" != "" ]; then
             if [ -d "$NEW_WORKSPACE_DIR/pods/$__COMPONENT_NAME" ]; then
                export POD="$__COMPONENT_NAME"
                updateWorkspaceEnvFile
             fi
         fi

         # Source in the last switched cluster (pod has no env file)
         retrieveClusterEnvFile "$NEW_WORKSPACE_DIR/clusters/$CLUSTER"
         export PRODUCT
         export CLUSTER_TYPE

         # Initialze workspace
         . "$NEW_WORKSPACE_DIR/initenv.sh" -quiet

         local __SHIFTED="${__PATH#*\/}"
         cd_workspace $__SHIFTED

      else

         if [ ! -r "$NEW_RWE_DIR/initenv.sh" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid RWE: [$__RWE]. Permission denied. Command aborted."
            return 1
         fi

         . "$NEW_RWE_DIR/initenv.sh" -quiet

         # Retrieve the last switched workspace
         retrieveRweEnvFile

         # PADOGRID_WORKSPACE is retrieved by the above call, retrieveRweEnvFile
         NEW_WORKSPACE_DIR=$PADOGRID_WORKSPACE

         if [ "$NEW_WORKSPACE_DIR" != "" ] && [ -r "$NEW_WORKSPACE_DIR/initenv.sh" ]; then
            # Retreive last switched cluster/pod
            retrieveWorkspaceEnvFile

            # Source in the last switched cluster (pod has no env file)
            retrieveClusterEnvFile "$NEW_WORKSPACE_DIR/clusters/$CLUSTER"

            export PADOGRID_WORKSPACES_HOME
            export CLUSTER
            export POD
            export PRODUCT
            export CLUSTER_TYPE
            . "$NEW_WORKSPACE_DIR/initenv.sh" -quiet
         fi

         cd_rwe $__RWE
      fi
   fi
}

# 
# Switches the workspace to the specified workspace. This function is provided
# to be executed in the shell along with other padogrid commands. It
# sets the environment variables in the parent shell.
#
# @required PADOGRID_WORKSPACES_HOME Workspaces directory path.
# @param    workspaceName         Optional workspace name in the 
#                                 $PADOGRID_WORKSPACES_HOME directory.
#                                 If not specified, then switches to the   
#                                 current workspace.
#
function switch_workspace
{
   EXECUTABLE=switch_workspace
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Switch to the specified workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [workspace_name[/directory_name/...]] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Switches to the specified workspace and changes directory to the specified"
      echo "   directory. To specify the nested directory names, use the tab key to drill down"
      echo "   the directory structure. This command automatically switches component context"
      echo "   based on the specified directory path. For example, if the directory path includes"
      echo "   a cluster, then it switches into that cluster."
      echo ""
      echo "OPTIONS"
      echo "   workspace_name"
      echo "             Workspace to switch to. If not specified, then switches to the current"
      echo "             workspace."
      echo ""
      echo "   /directory_name/..."
      echo "             Directory path relative to the workspace directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name."
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE"
      echo ""
      echo "EXAMPLES"
      echo "   - Switch workspace to 'myws', switch cluster to 'mycluster', and change directory"
      echo "     to that cluster's 'etc' directory."
      echo ""
      echo "        switch_workspace myws/clusters/mycluster/etc/"
      echo ""
      echo "   - Switch workspace to 'myws', change directory to the perf_test's 'bin_sh' directory."
      echo ""
      echo "        switch_workspace myws/apps/perf_test/bin_sh/"
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*workspace*" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi

   # Reset Pado home path
   export PADO_HOME=""

   if [ "$1" == "" ]; then

      if [ "$PADOGRID_WORKSPACE" == "" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid workspace: [$1]. Workspace undefined. Command aborted."
         return 1
      elif [ ! -r "$PADOGRID_WORKSPACE" ]; then
         local __WORKSPACE="$(basename $PADOGRID_WORKSPACE)"
         echo -e >&2 "${CError}ERROR:${CNone} Invalid workspace: [$__WORKSPACE]. Permission denied. Command aborted."
         return 1
      elif [ ! -d "$PADOGRID_WORKSPACE" ]; then
         # If the current workspace does not exist then retrieve it from the rweenv file.
         retrieveRweEnvFile
         export PADOGRID_WORKSPACE
      fi

      # Retreive last switched cluster/pod

      retrieveWorkspaceEnvFile

      # Source in the last switched cluster (pod has no env)
      retrieveClusterEnvFile

      # Intialize workspace
      . "$PADOGRID_WORKSPACE/initenv.sh" -quiet

      export CLUSTER
      export POD
      export PRODUCT
      export CLUSTER_TYPE

   else
      if [ ! -d "$PADOGRID_WORKSPACES_HOME/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid workspace: [$1]. Workspace does not exist. Command aborted."
         return 1
      elif [ ! -r "$PADOGRID_WORKSPACES_HOME/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid workspace: [$1]. Permission denied. Command aborted."
         return 1
      fi
      local __PATH=""
      for i in "$@"; do
        if [ "$__PATH" == "" ] || [[ "$__PATH" == */ ]]; then
           __PATH="${__PATH}$i"
        else
           __PATH="${__PATH}/$i"
        fi
      done
      local tokens=$(echo "$__PATH" | sed 's/\// /g')
      local __INDEX=0
      local __WORKSPACE=""
      local __COMPONENT_DIR_NAME=""
      local __COMPONENT_NAME=""
      for i in $tokens; do
        let __INDEX=__INDEX+1
        if [ $__INDEX -eq 1 ]; then
            __WORKSPACE=$i
        elif [ $__INDEX -eq 2 ]; then
            __COMPONENT_DIR_NAME=$i
        elif [ $__INDEX -eq 3 ]; then
            __COMPONENT_NAME=$i
        fi
      done

      NEW_WORKSPACE_DIR="$PADOGRID_WORKSPACES_HOME/$__WORKSPACE"

      # Retreive last switched cluster/pod
      retrieveWorkspaceEnvFile "$NEW_WORKSPACE_DIR"

      if [ "$__COMPONENT_DIR_NAME" == "clusters" ] && [ "$__COMPONENT_NAME" != "" ]; then
          if [ -d "$NEW_WORKSPACE_DIR/clusters/$__COMPONENT_NAME" ]; then
             export CLUSTER="$__COMPONENT_NAME"
          fi
      fi
      if [ "$__COMPONENT_DIR_NAME" == "pods" ] && [ "$__COMPONENT_NAME" != "" ]; then
          if [ -d "$NEW_WORKSPACE_DIR/pods/$__COMPONENT_NAME" ]; then
             export POD="$__COMPONENT_NAME"
          fi
      fi

      # Source in the last switched cluster (pod has no env)
      retrieveClusterEnvFile "$NEW_WORKSPACE_DIR/clusters/$CLUSTER"

      # Intialize workspace
      . "$NEW_WORKSPACE_DIR/initenv.sh" -quiet

      # Export workspace
      export PADOGRID_WORKSPACE="$NEW_WORKSPACE_DIR"

      # Update the rweenv.sh file with the new workspace
      updateRweEnvFile

      __switch_cluster $CLUSTER
      __switch_pod $POD
   fi
   cd_workspace $@
}

# 
# Switches the group to the specified group. This function is provided
# to be executed in the shell along with other padogrid commands. It
# sets the environment variables in the parent shell.
#
# @required PADOGRID_WORKSPACE  Workspace path.
# @param    groupName           Optional group in the
#                               $PADOGRID_WORKSPACE/groups directory.
#                               If not specified, then switches to the   
#                               current group.
#
function switch_group
{
   EXECUTABLE=switch_group
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Switch to the specified group in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [group_name[/directory_name/...]] [-?]"
      echo ""
      echo "   Switches to the specified group and chagnes directory to the specified nested."
      echo "   directory. To specify the nested directory names, use the tab key to drill down"
      echo "   the directory structure."
      echo ""
      echo "OPTIONS"
      echo "   group_name"
      echo "             Group to switch to. If not specified, then switches to the current group."
      echo ""
      echo "   /directory_name/..."
      echo "             One or names of nested directories. The $EXECUTABLE command constructs"
      echo "             the leaf directory path using the specified directory names and then"
      echo "             changes directory to that directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name."
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE"
      echo ""
      echo "EXAMPLES"
      echo "   - Switch group to 'mygroup', and change directory to that group's 'etc' directory."
      echo ""
      echo "        switch mygroup/etc/"
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*group* list_groups" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi
   if [ -z $CLUSTER ]; then
      retrieveWorkspaceEnvFile
   fi
   __switch_group $@
   cd_group $@
}

#
# Returns the current cluster group. It returns an empty string if the current cluster
# does not belong to any group.
#
# @required PADOGRID_WORKSPACES_HOME RWE path.
# @param workspace Optional workspace name. If not specified, then the current workspace name is assumed.
# @param cluster   Optional cluster name. If not specified, then the current cluster name is assumed.
#
function getCurrentClusterGroup
{
   local WORKSPACE="$1"
   local CLUSTER_NAME="$2"
   if [ "$CLUSTER_NAME" == "" ]; then
      CLUSTER_NAME=$CLUSTER
   fi
   local GROUPS_DIR
   local GROUP_NAMES
   if [ "$WORKSPACE" == "" ]; then
      GROUPS_DIR="$PADOGRID_WORKSPACE/groups"
      GROUP_NAMES=$(list_groups)
   else
      GROUPS_DIR="$PADOGRID_WORKSPACES_HOME/$WORKSPACE/groups"
      GROUP_NAMES=$(list_groups -workspace $WORKSPACE)
   fi
   local GROUP=""
   for __GROUP in $GROUP_NAMES; do
      GROUP_FILE="$GROUPS_DIR/$__GROUP/etc/group.properties"
      local CLUSTER_NAMES_COMMAS=$(getProperty "$GROUP_FILE" "group.cluster.names")
      local CLUSTER_NAMES=$(echo $CLUSTER_NAMES_COMMAS | sed 's/,/ /g')
      for i in $CLUSTER_NAMES; do
         if [ "$i" == "$CLUSTER_NAME" ]; then
            GROUP=$__GROUP
            break;
         fi
      done
   done
   echo "$GROUP"
}

# 
# Switches the group to the specified group but does not change directory.
#
# @required PADOGRID_WORKSPACE  Workspace path.
# @param    groupName           Optional group in the
#                               $PADOGRID_WORKSPACE/groups directory.
#                               If not specified, then switches to the   
#                               current group.
#
function __switch_group
{
   local __GROUP="$1"
   if [ "$__GROUP" == "" ]; then
      return 0
   fi
   local GROUP_FILE="$PADOGRID_WORKSPACE/groups/$__GROUP/etc/group.properties"
   if [ ! -f "$GROUP_FILE" ]; then
      return 0
   fi
   local CLUSTER_NAMES_COMMAS=$(getProperty "$GROUP_FILE" "group.cluster.names")
   if [ "$CLUSTER_NAMES_COMMAS" == "" ]; then
      return 0
   fi
   CLUSTER_NAMES=$(echo $CLUSTER_NAMES_COMMAS | sed 's/,/ /g')
   FIRST_CLUSTER=""
   for i in $CLUSTER_NAMES; do
      if [ "$FIRST_CLUSTER" == "" ]; then
         FIRST_CLUSTER=$i
      fi
      # If the cluster found then set the group and return
      if [ "$i" == "$CLUSTER" ]; then
         export GROUP=$__GROUP
         return 0
      fi
   done
   if [ "$FIRST_CLUSTER" == "" ]; then
      return 0
   fi
   switch_cluster $FIRST_CLUSTER
   export GROUP=$__GROUP
}

# 
# Switches the cluster to the specified cluster. This function is provided
# to be executed in the shell along with other padogrid commands. It
# sets the environment variables in the parent shell.
#
# @required PADOGRID_WORKSPACE Workspace path.
# @param    clusterName         Optional cluster in the
#                               $PADOGRID_WORKSPACE/clusters directory.
#                               If not specified, then switches to the   
#                               current cluster.
#
function switch_cluster
{
   EXECUTABLE=switch_cluster
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Switch to the specified cluster in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [cluster_name[/directory_name/...]] [-?]"
      echo ""
      echo "   Switches to the specified cluster and chagnes directory to the specified nested."
      echo "   directory. To specify the nested directory names, use the tab key to drill down"
      echo "   the directory structure."
      echo ""
      echo "OPTIONS"
      echo "   cluster_name"
      echo "             Cluster to switch to. If not specified, then switches"
      echo "             to the current cluster."
      echo ""
      echo "   /directory_name/..."
      echo "             One or names of nested directories. The $EXECUTABLE command constructs"
      echo "             the leaf directory path using the specified directory names and then"
      echo "             changes directory to that directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name."
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE"
      echo ""
      echo "EXAMPLES"
      echo "   - Switch cluster to 'mycluster', and change directory to that cluster's 'etc' directory."
      echo ""
      echo "        switch mycluster/etc/"
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*cluster* list_clusters" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi
   if [ -z $CLUSTER ]; then
      retrieveWorkspaceEnvFile
   fi
   __switch_cluster $@
   cd_cluster $@
   if [ "$PRODUCT" == "hadoop" ]; then
      export HADOOP_CONF_DIR="$(pwd)/etc/pseudo"
   fi
 }

# 
# Switches the cluster to the specified cluster but does not change directory.
#
# @required PADOGRID_WORKSPACE Workspace path.
# @param    clusterName         Optional cluster in the
#                               $PADOGRID_WORKSPACE/clusters directory.
#                               If not specified, then switches to the   
#                               current cluster.
#
function __switch_cluster
{
   if [ "$1" != "" ]; then
      local __PATH=""
      for i in "$@"; do
        if [ "$__PATH" == "" ] || [[ "$__PATH" == */ ]]; then
           __PATH="${__PATH}$i"
        else
           __PATH="${__PATH}/$i"
        fi
      done
      local tokens=$(echo "$__PATH" | sed 's/\// /g')
      local __INDEX=0
      local __COMPONENT_NAME=""
      for i in $tokens; do
        let __INDEX=__INDEX+1
        if [ $__INDEX -eq 1 ]; then
            __COMPONENT_NAME=$i
            break;
        fi
      done
      export CLUSTER=$__COMPONENT_NAME

      local RWE=$(basename $(dirname "$PADOGRID_WORKSPACE"))
      local WORKSPACE=$(basename "$PADOGRID_WORKSPACE")
      local HOME_WORKSPACE_DIR="$HOME/.padogrid/workspaces/$RWE/$WORKSPACE"
      local HOME_WORKSPACEENV_FILE="$HOME_WORKSPACE_DIR/workspaceenv.sh"
      if [ -f "$HOME_WORKSPACEENV_FILE"  ]; then
         sed -i${__SED_BACKUP} '/CLUSTER=/d' "$HOME_WORKSPACEENV_FILE"
         echo "CLUSTER=$CLUSTER" >> "$HOME_WORKSPACEENV_FILE"
      fi
      determineClusterProduct
      local __PRODUCT
      if [ "$PRODUCT" == "geode" ]; then
         if [ "$CLUSTER_TYPE" == "gemfire" ]; then
            export PRODUCT_HOME=$GEMFIRE_HOME
         else
            export PRODUCT_HOME=$GEODE_HOME
         fi
         __PRODUCT="geode"
      elif [ "$PRODUCT" == "gemfire" ]; then
         export PRODUCT_HOME=$GEMFIRE_HOME
         __PRODUCT="geode"
      elif [ "$PRODUCT" == "hazelcast" ]; then
         if [ "CLUSTER_TYPE" == "jet" ]; then
            export PRODUCT_HOME=$JET_HOME
         else
            export PRODUCT_HOME=$HAZELCAST_HOME
         fi
         __PRODUCT="hazelcast"
      elif [ "$PRODUCT" == "jet" ]; then
         export PRODUCT_HOME=$JET_HOME
         __PRODUCT="hazelcast"
      elif [ "$PRODUCT" == "redis" ]; then
         export PRODUCT_HOME=$REDIS_HOME
         __PRODUCT="snappydata"
      elif [ "$PRODUCT" == "snappydata" ]; then
         export PRODUCT_HOME=$SNAPPYDATA_HOME
         __PRODUCT="snappydata"
      elif [ "$PRODUCT" == "spark" ]; then
         export PRODUCT_HOME=$SPARK_HOME
         __PRODUCT="spark"
      elif [ "$PRODUCT" == "coherence" ]; then
         export PRODUCT_HOME=$COHERENCE_HOME
         __PRODUCT="coherence"
      elif [ "$PRODUCT" == "redis" ]; then
         export PRODUCT_HOME=$REDIS_HOME
         __PRODUCT="redis"
      elif [ "$PRODUCT" == "kafka" ]; then
         if [ "$CLUSTER_TYPE" == "confluent" ]; then
            export PRODUCT_HOME=$CONFLUENT_HOME
         else
            export PRODUCT_HOME=$KAFKA_HOME
         fi
         __PRODUCT="kafka"
      elif [ "$PRODUCT" == "hadoop" ]; then
         export PRODUCT_HOME=$HADOOP_HOME
         __PRODUCT="hadoop"
      fi
      local NEW_PRODUCT=$PRODUCT
      local NEW_PRODUCT_HOME=$PRODUCT_HOME
      retrieveClusterEnvFile "$CLUSTERS_DIR/$CLUSTER"
      export CLUSTER
      export CLUSTER_TYPE
      . $PADOGRID_HOME/$__PRODUCT/bin_sh/.${__PRODUCT}_completion.bash
      # Must set the new product values again to overwrite the values set by completion
      export PRODUCT=$NEW_PRODUCT
      export PRODUCT_HOME=$NEW_PRODUCT_HOME
   fi
}

# 
# Switches the pod to the specified pod. This function is provided
# to be executed in the shell along with other padogrid commands. It
# sets the environment variables in the parent shell.
#
# @required PADOGRID_WORKSPACE Workspace path.
# @param    podName         Optional pod in the
#                           $PADOGRID_WORKSPACE/pods directory.
#                           If not specified, then switches to the   
#                           current pod and changes directory into 
#                           that pod. Note that if pod is 'local' then 
#                           it changes directory to $PADOGRID_WORKSPACE/pods.
#
function switch_pod
{
   EXECUTABLE=switch_pod
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Switch to the specified pod in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [pod_name[/directory_name/...]] [-?]"
      echo ""
      echo "   Switches to the specified pod and changes directory to the specified directory."
      echo "   To specify the nested directory names, use the tab key to drill down the directory"
      echo "   structure."
      echo ""
      echo "OPTIONS"
      echo "   pod_name"
      echo "             Pod to switch to. If not specified, then switches to the current pod."
      echo ""
      echo "   /directory_name/..."
      echo "             Directory path relative to the pod directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name."
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE"
      echo ""
      echo "EXAMPLES"
      echo "   - Switch pod to 'mypod', and change directory to that pod's 'etc' directory."
      echo ""
      echo "        switch mypod/etc/"
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*pod*" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi
   __switch_pod $@
   cd_pod $@
}

# 
# Switches the pod to the specified pod but does not change directory.
#
# @required PADOGRID_WORKSPACE Workspace path.
# @param    podName         Optional pod in the
#                           $PADOGRID_WORKSPACE/pods directory.
#                           If not specified, then switches to the   
#                           current pod and changes directory into 
#                           that pod. Note that if pod is 'local' then 
#                           it changes directory to $PADOGRID_WORKSPACE/pods.
#
function __switch_pod
{
   if [ "$1" != "" ]; then
      local __PATH=""
      for i in "$@"; do
        if [ "$__PATH" == "" ] || [[ "$__PATH" == */ ]]; then
           __PATH="${__PATH}$i"
        else
           __PATH="${__PATH}/$i"
        fi
      done
      local tokens=$(echo "$__PATH" | sed 's/\// /g')
      local __INDEX=0
      local __COMPONENT_NAME=""
      for i in $tokens; do
        let __INDEX=__INDEX+1
        if [ $__INDEX -eq 1 ]; then
            __COMPONENT_NAME=$i
            break;
        fi
      done
      export POD=$__COMPONENT_NAME

      local RWE=$(basename $(dirname "$PADOGRID_WORKSPACE"))
      local WORKSPACE=$(basename "$PADOGRID_WORKSPACE")
      local HOME_WORKSPACE_DIR="$HOME/.padogrid/workspaces/$RWE/$WORKSPACE"
      local HOME_WORKSPACEENV_FILE="$HOME_WORKSPACE_DIR/workspaceenv.sh"
      if [ -f "$HOME_WORKSPACEENV_FILE"  ]; then
         sed -i${__SED_BACKUP} '/POD=/d' "$HOME_WORKSPACEENV_FILE"
         echo "POD=$POD" >> "$HOME_WORKSPACEENV_FILE"
      fi
   fi
}

#
# Changes directory to the specified RWE directory. This function is provided
# to be executed in the shell along with other padogrid commands. It changes
# directory in the parent shell.
#
# @required PADOGRID_WORKSPACES_HOME Workspaces directory path.
# @param    rweName   Optional RWE name.
#
function cd_rwe
{
   #__ctrl_c
   EXECUTABLE=cd_rwe
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified root workspaces environment"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [rwe_name [workspace_name[/directory_name/...]]] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Changes directory to the specified nested directory in the specified RWE"
      echo "   environment. To specify the nested directory names, use the tab key to drill"
      echo "   down the directory structure."
      echo ""
      echo "OPTIONS"
      echo "   rwe_name"
      echo "             Root environment name. If not specified then changes to the"
      echo "             current root workspaces environment directory."
      echo ""
      echo "   workspace_name"
      echo "             Workspace name. If not specified then changes to the"
      echo "             default workspace directory of the specified RWE."
      echo ""
      echo "   /directory_name/..."
      echo "             One or names of nested directories. The $EXECUTABLE command constructs"
      echo "             the leaf directory path using the specified directory names and then"
      echo "             changes directory to that directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name."
      echo ""
      echo "EXAMPLES"
      echo "   - Change directory to 'myrwe/myws/clusters/mycluster/etc'."
      echo ""
      echo "        cd_rwe myrwe/myws/clusters/mycluster/etc/"
      echo "DEFAULT"
      echo "   $EXECUTABLE"
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*rwe*" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi
   if [ "$1" == "" ]; then
      cd $PADOGRID_WORKSPACES_HOME
   else
      local PARENT_DIR="$(dirname "$PADOGRID_WORKSPACES_HOME")"
      if [ ! -d "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid RWE name: [$1]. RWE does not exist. Command aborted."
         return 1
      elif [ ! -r "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid RWE: [$1]. Permission denied. Command aborted."
         return 1
      else
         local DIR=""
         for i in "$@"; do
            DIR="$DIR"/"$i"
         done
         DIR="${PARENT_DIR}${DIR}"
         if [ ! -d "$DIR" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid directory: [$DIR]. Directory does not exist. Command aborted."
            return 1
         fi
         cd "$DIR"
      fi
   fi
   pwd
}

#
# Changes directory to the specified workspace directory. This function is provided
# to be executed in the shell along with other padogrid commands. It changes
# directory in the parent shell.
#
# @required PADOGRID_WORKSPACES_HOME Workspaces directory path.
# @param    workspaceName             Workspace name in the 
#                                     $PADOGRID_WORKSPACES_HOME directory.
#
function cd_workspace
{
   #__ctrl_c
   EXECUTABLE=cd_workspace
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [workspace_name [/directory_name/...]] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Chagnes directory to the specified workspace's nested directory. To specify"
      echo "   the nested directory names, use the tab key to drill down the directory"
      echo "   structure."
      echo ""
      echo "OPTIONS"
      echo "   workspace_name"
      echo "             Workspace name. If not specified then changes to the"
      echo "             current workspace directory."
      echo ""
      echo "   /directory_name/..."
      echo "             One or names of nested directories. The $EXECUTABLE command constructs"
      echo "             the leaf directory path using the specified directory names and then"
      echo "             changes directory to that directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name."
      echo ""
      echo "EXAMPLES"
      echo "   - Change directory to 'myws/clusters/mycluster/etc'."
      echo ""
      echo "        cd_workspace myws/clusters/mycluster/etc/"
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE"
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*workspace*" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi

   if [ "$1" == "" ]; then
      if [ "$PADOGRID_WORKSPACE" == "" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Workspace undefined. The current workspace is undefined. Command aborted."
         return 1
      elif [ ! -r "$PADOGRID_WORKSPACE" ]; then
         local __WORKSPACE="$(basename $PADOGRID_WORKSPACE)"
         echo -e >&2 "${CError}ERROR:${CNone} Invalid workspace: [$__WORKSPACE]. Permission denied. Command aborted."
         return 1
      else
         cd $PADOGRID_WORKSPACE
      fi
   else
      local PARENT_DIR="$PADOGRID_WORKSPACES_HOME"
      if [ ! -d "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid workspace name: [$1]. Workspace does not exist. Command aborted."
         return 1
      elif [ ! -r "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid workspace: [$1]. Permission denied. Command aborted."
         return 1
      else
         local DIR=""
         for i in "$@"; do
            DIR="$DIR"/"$i"
         done
         DIR="${PARENT_DIR}${DIR}"
         if [ ! -d "$DIR" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid directory: [$DIR]. Directory does not exist. Command aborted."
            return 1
         fi
         cd "$DIR"
      fi
   fi
   pwd
}

#
# Changes directory to the specified pod directory. This function is provided
# to be executed in the shell along with other padogrid commands. It changes
# directory in the parent shell.
#
# @required PADOGRID_WORKSPACE Workspace path.
# @param    clusterName         Optional cluster in the
#                               $PADOGRID_WORKSPACE/clusters directory.
#                               If not specified, then switches to the   
#                               current pod.
#
function cd_pod
{
   #__ctrl_c
   EXECUTABLE=cd_pod
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified padogrid pod in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [pad_name][/directory_name/...]] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Chagnes directory to the specified pod's nested directory. To specify"
      echo "   the nested directory names, use the tab key to drill down the directory"
      echo "   structure."
      echo ""
      echo "   Note that the 'local' pod is reserved for the host OS and does not have"
      echo "   a directory assigned. 'cd_pod local' will change directory to the parent"
      echo "   directory instead."
      echo ""
      echo "OPTIONS"
      echo "   pod_name" 
      echo "             Pod name. If not specified then changes to the current pod directory."
      echo ""
      echo "   /directory_name/..."
      echo "             One or names of nested directories. The $EXECUTABLE command constructs"
      echo "             the leaf directory path using the specified directory names and then"
      echo "             changes directory to that directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name." echo ""
      echo "EXAMPLES"
      echo "   - Change directory to 'mypod/bin_sh'"
      echo ""
      echo "        cd_pod mypod/bin_sh/"
      if [ "$MAN_SPECIFIED" == "false" ]; then
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE $POD"
      fi
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*pod*" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi

   local tokens=$(echo "$1" | sed 's/\// /g')
   local __INDEX=0
   local __COMPONENT_NAME=""
   for i in $tokens; do
     let __INDEX=__INDEX+1
     if [ $__INDEX -eq 1 ]; then
         __COMPONENT_NAME=$i
     fi
   done

   if [ "$__COMPONENT_NAME" == "" ]; then
      if [ ! -d "$PADOGRID_WORKSPACE/pods/$POD" ]; then
         cd $PADOGRID_WORKSPACE/pods
      else
         cd $PADOGRID_WORKSPACE/pods/$POD
      fi
   else
      local PARENT_DIR="$PADOGRID_WORKSPACE/pods"
      if [ "$__COMPONENT_NAME" != "local" ]; then
         if [ ! -d "$PARENT_DIR/$__COMPONENT_NAME" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid pod name: [$__COMPONENT_NAME]. Pod does not exist. Command aborted."
            return 1
         elif [ ! -r "$PARENT_DIR/$__COMPONENT_NAME" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid pod: [$__COMPONENT_NAME]. Permission denied. Command aborted."
            return 1
         fi
      else
         local DIR=""
         for i in "$@"; do
            DIR="$DIR"/"$i"
         done
         DIR="${PARENT_DIR}${DIR}"
         if [ ! -d "$DIR" ]; then
            if [ "$__COMPONENT_NAME" == "local" ] && [ "$__INDEX" -eq 1 ]; then
               cd $PADOGRID_WORKSPACE/pods
            else
               echo -e >&2 "${CError}ERROR:${CNone} Invalid directory: [$DIR]. Directory does not exist. Command aborted."
               return 1
            fi
         elif [ ! -r "$DIR" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid directory: [$DIR]. Permission denied. Command aborted."
            return 1
         else
            cd "$DIR"
         fi
      fi
   fi
   pwd
}

#
# Returns a list of relevant commands for the specified filter.
#
# @required SCRIPT_DIR    Script directory path in which the specified filter is to be applied.
# @param    commandFilter Commands to filter in the script directory.
#
function getSeeAlsoList
{
   local FILTER=$1
   local COMMANDS=`ls $SCRIPT_DIR/$FILTER`
   echo $COMMANDS
}

#
# Changes directory to the specified group directory. This function is provided
# to be executed in the shell along with other padogrid commands. It changes
# directory in the parent shell.
#
# @required PADOGRID_WORKSPACE Workspace path.
# @param    groupName Optional group in the
#                     $PADOGRID_WORKSPACE/groups directory.
#                     If not specified, then changes directory to the current group.
#
function cd_group
{
   #__ctrl_c
   EXECUTABLE=cd_group
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified padogrid group in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [group_name[/directory_name/...]] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Chagnes directory to the specified group's nested directory. To specify"
      echo "   the nested directory names, use the tab key to drill down the directory"
      echo "   structure."
      echo ""
      echo "OPTIONS"
      echo "   group_name" 
      echo "             Group name. If not specified then changes to the current group directory."
      echo ""
      echo "   /directory_name/..."
      echo "             One or names of nested directories. The $EXECUTABLE command constructs"
      echo "             the leaf directory path using the specified directory names and then"
      echo "             changes directory to that directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name."
      echo ""
      echo "EXAMPLES"
      echo "   - Change directory to 'mygroup/etc'"
      echo ""
      echo "        cd_group mygroup/etc/"
      if [ "$MAN_SPECIFIED" == "false" ]; then
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE $CLUSTER"
      fi
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*group*" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi

   if [ "$1" == "" ]; then
      if [ -z $GROUP ]; then
         retrieveWorkspaceEnvFile
      fi
      cd $PADOGRID_WORKSPACE/groups/$GROUP
   else
      local PARENT_DIR="$PADOGRID_WORKSPACE/groups"
      if [ ! -d "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid group name: [$1]. Group does not exist. Command aborted."
         return 1
      elif [ ! -r "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid group: [$1]. Permission denied. Command aborted."
         return 1
      else
         local DIR=""
         for i in "$@"; do
            DIR="$DIR"/"$i"
         done
         DIR="${PARENT_DIR}${DIR}"
         if [ ! -d "$DIR" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid directory: [$DIR]. Directory does not exist. Command aborted."
            return 1
         fi
         cd "$DIR"
      fi
   fi
   pwd
}

#
# Changes directory to the specified cluster directory. This function is provided
# to be executed in the shell along with other padogrid commands. It changes
# directory in the parent shell.
#
# @required PADOGRID_WORKSPACE Workspace path.
# @param    clusterName         Optional cluster in the
#                               $PADOGRID_WORKSPACE/clusters directory.
#                               If not specified, then switches to the   
#                               current cluster.
#
function cd_cluster
{
   #__ctrl_c
   EXECUTABLE=cd_cluster
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified padogrid cluster in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [cluster_name[/directory_name/...]] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Chagnes directory to the specified cluster's nested directory. To specify"
      echo "   the nested directory names, use the tab key to drill down the directory"
      echo "   structure."
      echo ""
      echo "OPTIONS"
      echo "   cluster_name" 
      echo "             Cluster name. If not specified then changes to the"
      echo "             current cluster directory."
      echo ""
      echo "   /directory_name/..."
      echo "             One or names of nested directories. The $EXECUTABLE command constructs"
      echo "             the leaf directory path using the specified directory names and then"
      echo "             changes directory to that directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name."
      echo ""
      echo "EXAMPLES"
      echo "   - Change directory to 'mycluster/bin_sh'"
      echo ""
      echo "        cd_cluster mycluster/bin_sh/"
      if [ "$MAN_SPECIFIED" == "false" ]; then
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE $CLUSTER"
      fi
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*cluster*" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi

   if [ "$1" == "" ]; then
      if [ -z $CLUSTER ]; then
         retrieveWorkspaceEnvFile
      fi

      if [ ! -d "$PADOGRID_WORKSPACE/clusters/$CLUSTER" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid cluster: [$CLUSTER]. Cluster does not exit. Command aborted."
         return 1
      elif [ ! -r "$PADOGRID_WORKSPACE/clusters/$CLUSTER" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid cluster: [$CLUSTER]. Permission denied. Command aborted."
         return 1
      fi

      cd $PADOGRID_WORKSPACE/clusters/$CLUSTER
   else
      local PARENT_DIR="$PADOGRID_WORKSPACE/clusters"
      if [ ! -d "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid cluster name: [$1]. Cluster does not exist. Command aborted."
         return 1
      elif [ ! -r "$PARENT_DIR/" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid cluster: [$CLUSTER]. Permission denied. Command aborted."
         return 1
      else
         local DIR=""
         for i in "$@"; do
            DIR="$DIR"/"$i"
         done
         DIR="${PARENT_DIR}${DIR}"
         if [ ! -d "$DIR" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid directory: [$DIR]. Directory does not exist. Command aborted."
            return 1
         elif [ ! -r "$DIR" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid cluster: [$CLUSTER]. Permission denied. Command aborted."
            return 1
         fi
         cd "$DIR"
      fi
   fi
   pwd
}

#
# Changes directory to the specified Kubernetes cluster directory. This function is provided
# to be executed in the shell along with other padogrid commands. It changes
# directory in the parent shell.
#
# @required PADOGRID_WORKSPACE Workspace path.
# @param    clusterName         Optional cluster in the
#                               $PADOGRID_WORKSPACE/k8s directory.
#                               If not specified, then switches to the
#                               current Kubernetes cluster directory.
#
function cd_k8s
{
   #__ctrl_c
   EXECUTABLE=cd_k8s
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified padogrid Kubernetes cluster directory in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [cluster_name][/directory_name/...]] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Chagnes directory to the specified Kubernetes cluster directory. To specify the"
      echo "   nested directory names, use the tab key to drill down the directory structure."
      echo ""
      echo "OPTIONS"
      echo "   cluster_name"
      echo "             Kubernetes cluster name. If not specified then changes to the current"
      echo "             Kubernetes cluster directory."
      echo ""
      echo "   /directory_name/..."
      echo "             Directory path relative to the Kubernetes cluster directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name."
      echo ""
      echo "EXAMPLES"
      echo "   - Change directory to 'myk8s/bin_sh'"
      echo ""
      echo "        cd_k8s myk8s/bin_sh/"
      if [ "$MAN_SPECIFIED" == "false" ]; then
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE $CLUSTER"
      fi
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*cluster*" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi

   if [ "$1" == "" ]; then
      cd $PADOGRID_WORKSPACE/k8s/$K8S
   else
      local PARENT_DIR="$PADOGRID_WORKSPACE/k8s"
      if [ ! -d "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid k8s name: [$1]. K8s cluster does not exist. Command aborted."
         return 1
      elif [ ! -r "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid k8s: [$1]. Permission denied. Command aborted."
         return 1
      else
         local DIR=""
         for i in "$@"; do
            DIR="$DIR"/"$i"
         done
         DIR="${PARENT_DIR}${DIR}"
         if [ ! -d "$DIR" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid directory: [$DIR]. Directory does not exist. Command aborted."
            return 1
         fi
         cd "$DIR"
      fi
   fi
   pwd
}

#
# Changes directory to the specified Docker cluster directory. This function is provided
# to be executed in the shell along with other padogrid commands. It changes
# directory in the parent shell.
#
# @required PADOGRID_WORKSPACE Workspace path.
# @param    clusterName         Optional cluster in the
#                               $PADOGRID_WORKSPACE/docker directory.
#                               If not specified, then switches to the
#                               current Docker cluster directory.
#
function cd_docker
{
   #__ctrl_c
   EXECUTABLE=cd_docker
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified padogrid Docker cluster directory in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [cluster_name][/directory_name/...]] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Chagnes directory to the specified Docker cluster directory. To specify the"
      echo "   nested directory names, use the tab key to drill down the directory structure."
      echo ""
      echo "OPTIONS"
      echo "   cluster_name"
      echo "             Docker cluster name. If not specified then changes to the current Docker"
      echo "             cluster directory."
      echo ""
      echo "   /directory_name/..."
      echo "             Directory path relative to the Docker cluster directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name."
      echo ""
      echo "EXAMPLES"
      echo "   - Change directory to 'mydocker/bin_sh'"
      echo ""
      echo "        cd_docker mydocker/bin_sh/"
      if [ "$MAN_SPECIFIED" == "false" ]; then
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE $CLUSTER"
      fi
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*cluster*" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi

   if [ "$1" == "" ]; then
      cd $PADOGRID_WORKSPACE/docker/$DOCKER
   else
      local PARENT_DIR="$PADOGRID_WORKSPACE/docker"
      if [ ! -d "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR: Invalid docker name: [$1]. Docker cluster does not exist. Command aborted."
         return 1
      elif [ ! -r "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR: Invalid docker: [$1]. Permission denied. Command aborted."
         return 1
      else
         local DIR=""
         for i in "$@"; do
            DIR="$DIR"/"$i"
         done
         DIR="${PARENT_DIR}${DIR}"
         if [ ! -d "$DIR" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid directory: [$DIR]. Directory does not exist. Command aborted."
            return 1
         fi
         cd "$DIR"
      fi
   fi
   pwd
}

#
# Changes directory to the specified app directory. This function is provided
# to be executed in the shell along with other padogrid commands. It changes
# directory in the parent shell.
#
# @required PADOGRID_WORKSPACE Workspace path.
# @param    appName         Optional cluster in the
#                           $PADOGRID_WORKSPACE/apps directory.
#                           If not specified, then switches to the   
#                           current app.
#
function cd_app
{
   #__ctrl_c
   EXECUTABLE=cd_app
   if [ "$1" == "-?" ]; then
      echo ""
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified app in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [app_name[/directory_name/...]] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Changes directory to the specified app."
      echo ""
      echo "OPTIONS"
      echo "   app_name"
      echo "             App name. If not specified then changes to the current app directory."
      echo ""
      echo "   /directory_name/..."
      echo "             Directory path relative to the app directory."
      echo ""
      echo "             HINT: Use the tab key to get the next nested directory name."
      echo ""
      echo "EXAMPLES"
      echo "   - Change directory to 'myapp/bin_sh'"
      echo ""
      echo "        cd_app myapp/bin_sh/"

      if [ "$MAN_SPECIFIED" == "false" ]; then
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE $APP"
      fi
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*app*" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi

   if [ "$1" == "" ]; then
      cd $PADOGRID_WORKSPACE/apps/$APP
   else
      local PARENT_DIR="$PADOGRID_WORKSPACE/apps"
      if [ ! -d "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid app name: [$1]. App does not exist. Command aborted."
         return 1
      elif [ ! -d "$PARENT_DIR/$1" ]; then
         echo -e >&2 "${CError}ERROR:${CNone} Invalid app: [$1]. Permission denied. Command aborted."
         return 1
      else 
         local DIR=""
         for i in "$@"; do
            DIR="$DIR"/"$i"
         done
         DIR="${PARENT_DIR}${DIR}"
         if [ ! -d "$DIR" ]; then
            echo -e >&2 "${CError}ERROR:${CNone} Invalid directory: [$DIR]. Directory does not exist. Command aborted."
            return 1
         fi
         cd "$DIR"
      fi
   fi
   pwd
}

#
# Executes the specified padogrid command.
#
# @param command  Command to execute
# @param ...args  Command argument list
#
function padogrid
{
   EXECUTABLE=padogrid
   # Use the first arg instead of $HELP. This ensures
   # the -? to be passed to the subsequent command if specified.
   if [ "$1" == "-?" ]; then
      COMMANDS=`ls $SCRIPT_DIR`
      echo ""
      echo "WORKSPACE"
      echo "   $PADOGRID_WORKSPACE"
      echo ""
      echo "NAME"
      echo "   $EXECUTABLE - Execute the specified padogrid command"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [-product|-rwe|-version] [padogrid_command command] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Executes the specified padogrid command. If no options are specified then it displays"
      echo "   the entire RWEs in a tree view."
      echo ""
      echo "   Note that the product displayed in a workspace node is the default product configured"
      echo "   for that workspace and does not necessarily represent the how the components in the"
      echo "   workspace are configured. For example, a workspace may contain a cluster configured"
      echo "   with the default product and yet another cluster with another product."
      echo ""
      echo "OPTIONS"
      echo "   -rwe"
      echo "             If specified, then displays only RWEs in tree view. To display a space sparated"
      echo "             list of RWEs, run 'list_rwes' instead."
      echo ""
      echo "   -product"
      echo "             If specified, then displays the current workspace product version."
      echo ""
      echo "   -version"
      echo "             If specified, then displays the current workspace padogrid version."
      echo ""
      echo "   padogrid_command"
      echo "             One of the PadoGrid commands listed below."
      echo ""
      echo "COMMANDS"
      ls $SCRIPT_DIR
      help_padogrid
      return
   fi

   if [ "$1" == "cp_sub" ] || [ "$1" == "tools" ]; then
      local COMMAND=$2
      local SHIFT_NUM=2
   elif [ "$1" == "-rwe" ]; then
      local COMMAND=""
      local RWE_SPECIFIED="true"
   elif [ "$1" == "-product" ]; then
      echo "$PRODUCT"
      return 0
   elif [ "$1" == "-version" ]; then
      echo "$PADOGRID_VERSION"
      return 0
   elif [[ "$1" == *"-"* ]]; then
      echo >&2 -e "${CError}ERROR:${CNone} Invalid option: [$1]. Command aborted."
      return 1
   else
      local COMMAND=$1
      local SHIFT_NUM=1
   fi

   if [ "$COMMAND" == "" ]; then
cat <<EOF
.______      ___       _______   ______     _______ .______       __   _______ ™
|   _  \    /   \     |       \ /  __  \   /  _____||   _  \     |  | |       \ 
|  |_)  |  /  ^  \    |  .--.  |  |  |  | |  |  __  |  |_)  |    |  | |  .--.  |
|   ___/  /  /_\  \   |  |  |  |  |  |  | |  | |_ | |      /     |  | |  |  |  |
|  |     /  _____  \  |  '--'  |  '--'  | |  |__| | |  |\  \----.|  | |  '--'  |
| _|    /__/     \__\ |_______/ \______/   \______| | _| '._____||__| |_______/ 
Copyright 2020-2022 Netcrest Technologies, LLC. All rights reserved.
EOF
echo -e "Version: v$PADOGRID_VERSION"
echo -e " Manual: ${CUrl}https://github.com/padogrid/padogrid/wiki${CNone}"
echo -e "Bundles: ${CUrl}https://github.com/padogrid/catalog-bundles/blob/master/all-catalog.md${CNone}"
echo ""

      RWE_HOME="$(dirname "$PADOGRID_WORKSPACES_HOME")"
      echo "Root Workspaces Environments (RWEs)"
      echo "-----------------------------------"
      local CURRENT_RWE="$(basename "$PADOGRID_WORKSPACES_HOME")"
      local CURRENT_WORKSPACE="$(basename "$PADOGRID_WORKSPACE")"
      local ROOTS="$(getRweList)"
      echo "$RWE_HOME"
      local RWES=( $ROOTS )
      let RWES_LAST_INDEX=${#RWES[@]}-1
      for ((i = 0; i < ${#RWES[@]}; i++)); do
         RWE=${RWES[$i]}
         if [ $i -lt $RWES_LAST_INDEX ]; then
            if [ "$RWE" == "$CURRENT_RWE" ]; then
               echo -e "$TTee ${CLightGreen}$RWE${CNone}"
            else
               echo "$TTee $RWE"
            fi
            LEADING_BAR="$TBar   "
         else
            if [ "$RWE" == "$CURRENT_RWE" ]; then
               echo -e "$TLel ${CLightGreen}$RWE${CNone}"
            else
               echo "$TLel $RWE"
            fi
            LEADING_BAR="    "
         fi
         if [ "$RWE_SPECIFIED" == "true" ]; then
            continue;
         fi
         local WORKSPACES=`ls $RWE_HOME/$RWE`
         WORKSPACES=$(removeTokens "$WORKSPACES" "initenv.sh setenv.sh")
         WORKSPACES=( $WORKSPACES )
         let WORKSPACES_LAST_INDEX=${#WORKSPACES[@]}-1
         for ((j = 0; j < ${#WORKSPACES[@]}; j++)); do
            local WORKSPACE=${WORKSPACES[$j]}
            if [ ! -f $RWE_HOME/$RWE/$WORKSPACE/.addonenv.sh ]; then
               continue;
            fi
            local WORKSPACE_INFO=$(getWorkspaceInfoList "$WORKSPACE" "$RWE_HOME/$RWE")
            if [ $j -lt $WORKSPACES_LAST_INDEX ]; then
               if [ "$RWE" == "$CURRENT_RWE" ] && [ "$WORKSPACE" == "$CURRENT_WORKSPACE" ]; then
                  echo -e "${LEADING_BAR}$TTee ${CLightGreen}$WORKSPACE [$WORKSPACE_INFO]${CNone}"
               else
                  echo "${LEADING_BAR}$TTee $WORKSPACE [$WORKSPACE_INFO]"
            fi
            else
               if [ "$RWE" == "$CURRENT_RWE" ] && [ "$WORKSPACE" == "$CURRENT_WORKSPACE" ]; then
                  echo -e "${LEADING_BAR}$TLel ${CLightGreen}$WORKSPACE [$WORKSPACE_INFO]${CNone}"
               else
                  echo "${LEADING_BAR}$TLel $WORKSPACE [$WORKSPACE_INFO]"
               fi
            fi
         done
      done
      echo ""
      echo "Current Workspace"
      echo "-----------------"
      echo "           PRODUCT: $PRODUCT"
      echo "   Product Version: $PRODUCT_VERSION"
      echo "PADOGRID_WORKSPACE: $PADOGRID_WORKSPACE"
      echo "           CLUSTER: $CLUSTER"
      echo "      CLUSTER_TYPE: $CLUSTER_TYPE"
      echo "               POD: $POD"
      echo "          POD_TYPE: $POD_TYPE"
      return 0
   fi

   shift $SHIFT_NUM
   $COMMAND $* 
}

#
# Returns a comma separated list of the specified workspace info.
#
# @required PADOGRID_WORKSPACES_HOME
# @param workspaceName Workspace name.
# @param rwePath       RWE path. If not specified then PADOGRID_WORKSPACES_HOME is assumed.
#
function getWorkspaceInfoList
{
   local WORKSPACE="$1"
   local RWE_PATH="$2"
   if [ "$WORKSPACE" == "" ]; then
      echo ""
      return 0
   fi
   if [ "$RWE_PATH" == "" ]; then
      RWE_PATH="$PADOGRID_WORKSPACES_HOME"
   fi
   local WORKSPACE_PATH="$RWE_PATH/$WORKSPACE"
   if [ ! -d "$WORKSPACE_PATH" ]; then
      echo ""
      return 0
   fi

   # Remove blank lines from grep results. Pattern includes space and tab.
   # If multi-tenant workspace then the user might not have read access.
   if [ -r "$WORKSPACE_PATH/setenv.sh" ]; then
      local __JAVA_HOME=$(grep "export JAVA_HOME=" "$WORKSPACE_PATH/setenv.sh" | sed -e 's/#.*$//' -e '/^[ 	]*$/d' -e 's/.*=//' -e 's/"//g')
   else
      local __JAVA_HOME=""
   fi
   if [ "$__JAVA_HOME" == "" ]; then
      # Get the RWE's JAVA_HOME
      if [ -r "$WORKSPACE_PATH/../setenv.sh" ]; then
         __JAVA_HOME=$(grep "export JAVA_HOME=" "$WORKSPACE_PATH/../setenv.sh" | sed -e 's/#.*$//' -e '/^[ 	]*$/d' -e 's/.*=//' -e 's/"//g')
      else
         if [ "$JAVA_HOME" != "" ]; then
            __JAVA_HOME=$JAVA_HOME
         else
            local JAVA_EXEC_PATH=$(which java)
            if [ "$JAVA_EXEC_PATH" != "" ]; then
               __JAVA_HOME=$(dirname $(dirname $JAVA_EXEC_PATH))
            fi
         fi
      fi
   fi
   local JAVA_VERSION=""
   local JAVA_INFO=""
   if [ "$__JAVA_HOME" != "" ] && [ -f "$__JAVA_HOME/bin/java" ]; then
      # Use eval to handle commands with spaces
      local __COMMAND="\"$__JAVA_HOME/bin/java\" -version 2>&1 | grep version "
      JAVA_VERSION=$(eval $__COMMAND)
      JAVA_VERSION=$(echo $JAVA_VERSION |  sed -e 's/.*version//' -e 's/"//g' -e 's/ //g')
      JAVA_INFO="java_$JAVA_VERSION, ";
   fi

   local VM_ENABLED=$(isWorkspaceVmEnabled "$WORKSPACE" "$RWE_PATH")
   if [ "$VM_ENABLED" == "true" ]; then
      VM_WORKSPACE="vm, ";
   else
      VM_WORKSPACE="";
   fi
   # Remove blank lines from grep results. Pattern includes space and tab.
   # If multi-tenant workspace then the user might not have read access.
   if [ -r "$WORKSPACE_PATH/setenv.sh" ]; then
      local PADOGRID_VERSION=$(grep "export PADOGRID_HOME=" "$WORKSPACE_PATH/setenv.sh" | sed -e 's/#.*$//' -e '/^[ 	]*$/d' -e 's/.*=//' -e 's/"//g')
   else
      PADOGRID_VERSION=""
   fi
   if [ "$PADOGRID_VERSION" == "" ]; then
      # Get the RWE's PADOGRID_HOME
      PADOGRID_VERSION=$(grep "export PADOGRID_HOME=" "$WORKSPACE_PATH/../setenv.sh" | sed -e 's/#.*$//' -e '/^[ 	]*$/d' -e 's/.*=//' -e 's/"//g')
   fi
   PADOGRID_VERSION=$(echo "$PADOGRID_VERSION" | sed -e 's/#.*$//' -e '/^[ 	]*$/d' -e 's/^.*padogrid_//' -e 's/"//')

   # TODO: For some reason, Cygwin does not print the beginning string...
   echo "${VM_WORKSPACE}${JAVA_INFO}padogrid_$PADOGRID_VERSION"
}

#
# Returns "true" if the specified workspace name is VM enabled in the sepcified RWE.
#
# @param workspaceName Workspace name in the current RWE.
# @param rwePath       RWE path. If not specified then PADOGRID_WORKSPACES_HOME is assumed.
#
function isWorkspaceVmEnabled
{
   local WORKSPACE="$1"
   local RWE_PATH="$2"
   local VM_ENABLED
   if [ "$WORKSPACE" == "" ]; then
      VM_ENABLED="false"
   else
      if [ "$RWE_PATH" == "" ]; then
         RWE_PATH="$PADOGRID_WORKSPACES_HOME"
      fi
      local WORKSPACE_PATH="$RWE_PATH/$WORKSPACE"
      if [ ! -d "$WORKSPACE_PATH" ]; then
         VM_ENABLED="false"
      else
         # If multi-tenant workspace then the user might not have read access.
         if [ -r "$WORKSPACE_PATH/setenv.sh" ]; then
            local VM_ENABLED=$(grep "VM_ENABLED=" "$WORKSPACE_PATH/setenv.sh")
         else
            local VM_ENABLED="false"
         fi
         VM_ENABLED=$(echo "$VM_ENABLED" | sed -e 's/^.*VM_ENABLED=//' -e 's/"//g')
      fi
   fi
   echo $VM_ENABLED
}

#
# Pretty-prints the specified CLASSPATH
#
# @required OS_NAME  OS name
# @param    classPath Class path
#
function printClassPath()
{
   # '()' for subshell to localize IFS
   (
   local IFS
   if [[ ${OS_NAME} == CYGWIN* ]]; then
      IFS=';';
   else
      IFS=':';
   fi
   for token in $__CLASSPATH; do
      if [[ $token != *v3 ]] && [[ $token != *v4 ]] && [[ $token != *v5 ]]; then
         echo "$token"
      fi
   done
   unset IFS
   )
}

#
# Removes the specified tokens from the specified string value
# @param removeFromValue  String value
# @param tokens           Space separated tokens
# @returns String value with the tokens values removed
#
# Example: removeTokens "$VALUE" "$TOKENS"
#
function removeTokens()
{
   local __VALUE=$1
   local __TOKENS=$2

   for i in $__TOKENS; do
      __VALUE=${__VALUE/$i/}
   done
   echo $__VALUE
}

#
# Removes the specified tokens from the specified string value
# @param removeFromValue  String value
# @param tokensArray      Tokens in array. See example for passing in array.
# @returns String value with the tokens values removed
#
# Example: removeTokensArray "$VALUE" "${TOKENS_ARRAY[@]}"
#
function removeTokensArray()
{
   local __VALUE="$1"
   shift
   local  __TOKENS=("$@")

   for ((i = 1; i < ${#__TOKENS[@]}; i++)); do
       __VALUE=${__VALUE/${__TOKENS[$i]}/}
   done 
   echo $__VALUE
}

#
# Prints the SEE ALSO list by applying the specified filter and exclusion command
# @param filter            Filter must be in double quotes with wild card
# @param exclusionCommand  Command to exclude from the list
# @returns SEE ALSO list
#
# Example: printSeeAlsoList "*cluster*" remove_cluster
#
function printSeeAlsoList
{
   local FILTER=$1
   local EXCLUDE=$2
   pushd $SCRIPT_DIR > /dev/null 2>&1
   local COMMANDS=`ls $FILTER 2> /dev/null`
   popd > /dev/null 2>&1
   local LINE=""
   COMMANDS=$(unique_words "$COMMANDS")
   COMMANDS=($COMMANDS)
   local len=${#COMMANDS[@]}
   local last_index
   let last_index=len-1
   local count=0
   for ((i = 0; i < $len; i++)); do
      if [ "${COMMANDS[$i]}" == "$EXCLUDE" ]; then
         continue;
      fi
      if [ $(( $count % 5 )) == 0 ]; then
         if [ "$LINE" != "" ]; then
            if [ $i -lt $last_index ]; then
               echo "$LINE,"
            else
               echo "$LINE"
            fi
         fi
         LINE="   ${COMMANDS[$i]}(1)"
      else
         LINE="$LINE, ${COMMANDS[$i]}(1)"
      fi
      let count=count+1
   done
   if [ "$LINE" != "" ]; then
      echo "$LINE"
   fi
   echo ""
}

#
# Displays a tree view of the specified list
# @param list          Space separated list
# @param highlightItem Optional. If sepecified, then the matching item is highlighted in green.
#
function showTree
{
   local LIST=($1)
   local HIGHLIGHT_ITEM="$2"
   local len=${#LIST[@]}
   local last_index
   let last_index=len-1
   for ((i = 0; i < $len; i++)); do
      if [ $i -lt $last_index ]; then
         if [ "${LIST[$i]}" == "$HIGHLIGHT_ITEM" ]; then
            echo -e "$TTee ${CLightGreen}${LIST[$i]}${CNone}"
         else
            echo "$TTee ${LIST[$i]}"
         fi
      else
         if [ "${LIST[$i]}" == "$HIGHLIGHT_ITEM" ]; then
            echo -e "$TLel ${CLightGreen}${LIST[$i]}${CNone}"
         else
            echo "$TLel ${LIST[$i]}"
         fi
      fi
   done
}

#
# Returns a list of host IPv4 addresses
#
function getHostIPv4List
{
   local HOST_IPS=""
   if [[ ${OS_NAME} == DARWIN* ]]; then
      IP_LIST=$(ifconfig -u |grep "inet " |awk '{print $2}')
      for i in $IP_LIST; do
         if [[ "$i" != *".1" ]]; then
            if [ "$HOST_IPS" == "" ]; then
               HOST_IPS="$i"
            else
               HOST_IPS="$HOST_IPS $i"
            fi
         fi
      done
      # Determine the IP address using the router to the google DNS server
      if [ "$HOST_IPS" == "" ]; then
         HOST_IPS=$(ipconfig getifaddr $(route get 8.8.8.8 | awk '/interface: / {print $2; }'))
      fi
   else
      for i in $(hostname -i); do
         if [[ $i != 127* ]] && [[ $i != *::* ]]; then
            if [ "$HOST_IPS" == "" ]; then
               HOST_IPS="$i"
            else
               HOST_IPS="$HOST_IPS $i"
            fi
         fi
      done
   fi
   echo "$HOST_IPS"
}

#
# Returns the sorted list of the specified list that contains product versions
# @param versionList
#
function sortVersionList
{
   local VERSION_LIST="$1"
   local TMP_FILE=/tmp/$EXECUTABLE-$(date "+%m%d%y%H%M%S").txt
   echo 
   echo "" > $TMP_FILE
   if [ -f $TMP_FILE ]; then
      rm $TMP_FILE
   fi
   touch $TMP_FILE
   for i in $VERSION_LIST; do
      echo "$i" >> $TMP_FILE
   done
   SORTED_VERSIONS=$(sort -rV $TMP_FILE)
   if [ -f $TMP_FILE ]; then
      rm $TMP_FILE
   fi
   echo $SORTED_VERSIONS
}

#
# Determines the versions of all installed products by scanning the products base directory.
# This function sets the following arrays.
#    PADOGRID_VERSIONS
#    PADO_VERSIONS
#    COHERENCE_VERSIONS
#    GEMFIRE_VERSIONS
#    GEODE_VERSIONS
#    GRAFANA_ENTERPRISE_VERSIONS
#    GRAFANA_OSS_VERSIONS
#    HADOOP_VERSIONS
#    HAZELCAST_DESKTOP_VERSIONS
#    HAZELCAST_ENTERPRISE_VERSIONS
#    HAZELCAST_OSS_VERSIONS
#    HAZELCAST_MANAGEMENT_CENTER_VERSIONS
#    JAVA_VERSIONS
#    JET_ENTERPRISE_VERSIONS
#    JET_OSS_VERSIONS
#    JET_MANAGEMENT_CENTER_VERSIONS
#    KAFKA_VERSIONS
#    PROMETHEUS_VERSIONS
#    REDIS_VERSIONS
#    SNAPPYDATA_VERSIONS
#    SPARK_VERSIONS
#
# @required PADOGRID_ENV_BASE_PATH 
# @param workspaceName Workspace name. If not specified then the current workspace is assumed. Optional.
# @param rwePath       RWE path. If not specified then PADOGRID_WORKSPACES_HOME is assumed. Optional.
#
function determineInstalledProductVersions
{
   local WORKSPACE="$1"
   local RWE_PATH="$2"
   if [ "$WORKSPACE" == "" ]; then
      WORKSPACE=$(basename $PADOGRID_WORKSPACE)
      if [ "$WORKSPACE" == "" ]; then
         return 0
      fi
   fi
   if [ "$RWE_PATH" == "" ]; then
      RWE_PATH="$PADOGRID_WORKSPACES_HOME"
   fi
   local WORKSPACE_PATH="$RWE_PATH/$WORKSPACE"
   if [ ! -d "$WORKSPACE_PATH" ]; then
      echo ""
      return 0
   fi

   PADOGRID_VERSIONS=""
   PADO_VERSIONS=""
   PADODEKSTOP_VERSIONS=""
   PADOWEB_VERSIONS=""
   COHERENCE_VERSIONS=""
   GEMFIRE_VERSIONS=""
   GEODE_VERSIONS=""
   GRAFANA_VERSIONS=""
   HADOOP_VERSIONS=""
   HAZELCAST_ENTERPRISE_VERSIONS=""
   HAZELCAST_MANAGEMENT_CENTER_VERSIONS=""
   HAZELCAST_DESKTOP_VERSIONS=""
   JAVA_VERSIONS=""
   JET_ENTERPRISE_VERSIONS=""
   JET_OSS_VERSIONS=""
   HAZELCAST_OSS_VERSIONS=""
   JET_MANAGEMENT_CENTER_VERSIONS=""
   KAFKA_VERSIONS=""
   CONFLUENT_VERSIONS=""
   PROMETHEUS_VERSIONS=""
   REDIS_VERSIONS=""
   SNAPPYDATA_VERSIONS=""
   SPARK_VERSIONS=""
   DERBY_VERSIONS=""

   if [ -d "$PADOGRID_ENV_BASE_PATH/products" ]; then
      pushd $PADOGRID_ENV_BASE_PATH/products > /dev/null 2>&1

      # To prevent wildcard not expanding in a for-loop if files do not exist
      shopt -s nullglob

      local __versions
      local henterv hmanv hdesktopv jenterv jmanv jossv hossv

      # PadoGrid
      __versions=""
      for i in padogrid_*; do
         __version=${i#padogrid_}
         __versions="$__versions $__version "
      done
      PADOGRID_VERSIONS=$(sortVersionList "$__versions")

      # Pado
      __versions=""
      for i in pado_*; do
         __version=${i#pado_}
         __versions="$__versions $__version "
      done
      PADO_VERSIONS=$(sortVersionList "$__versions")

      # PadoDesktop
      __versions=""
      for i in pado-desktop_*; do
         __version=${i#pado-desktop_}
         __versions="$__versions $__version "
      done
      PADODEKSTOP_VERSIONS=$(sortVersionList "$__versions")

      # PadoWeb
      __versions=""
      for i in padoweb_*; do
         __version=${i#padoweb_}
         __versions="$__versions $__version "
      done
      PADOWEB_VERSIONS=$(sortVersionList "$__versions")

      # Coherence - Set the current version. Determining Coherence versions requires setting up a custom environment.
      if [ -f "$COHERENCE_HOME/product.xml" ]; then
         COHERENCE_VERSIONS=$(grep "version value" "$COHERENCE_HOME/product.xml" | sed -e 's/^.*="//' -e 's/".*//')
      fi

      # Confluent
      __versions=""
      for i in confluent-*; do
         __version=${i#confluent-}
         __versions="$__versions $__version "
      done
      CONFLUENT_VERSIONS=$(sortVersionList "$__versions")

      # Derby
      __versions=""
      for i in db-derby-*; do
         __version=${i#db-derby-}
         __versions="$__versions $__version "
      done
      DERBY_VERSIONS=$(sortVersionList "$__versions")

      # GemFire
      __versions=""
      for i in pivotal-gemfire-*; do
         __version=${i#pivotal-gemfire-}
         __versions="$__versions $__version "
      done
      GEMFIRE_VERSIONS=$(sortVersionList "$__versions")

      # Geode
      __versions=""
      for i in apache-geode-*; do
         __version=${i#apache-geode-}
         __versions="$__versions $__version "
      done
      GEODE_VERSIONS=$(sortVersionList "$__versions")

      __versions=""
      for i in grafana-*; do
         __version=${i#grafana-}
         __versions="$__versions $__version "
      done
      GRAFANA_VERSIONS=$(sortVersionList "$__versions")

      # Hadoop
      __versions=""
      for i in hadoop-*; do
         __version=${i#hadoop-}
         __versions="$__versions $__version "
      done
      HADOOP_VERSIONS=$(sortVersionList "$__versions")

      # Hazelcast OSS, Enterprise, Hazelcast Management Center, Jet OSS, Jet Enterprise, Jet Management Center
      local hossv henterv hmanv jossv jenterv jmanv
      for i in hazelcast-*; do
         if [[ "$i" == "hazelcast-enterprise-"** ]]; then
            __version=${i#hazelcast-enterprise-}
            henterv="$henterv $__version"
            # Get man center version included in the hazelcast distribution
            for j in $i/management-center/*.jar; do
               if [[ "$j" == *"hazelcast-management-center"* ]]; then
                  local mcv=${j#*hazelcast-management-center-}
                  hmanv="$hmanv ${mcv%.jar}"
                  break;
               fi
             done
         elif [[ "$i" == "hazelcast-management-center-"** ]]; then
            __version=${i#hazelcast-management-center-}
            hmanv="$hmanv $__version"
         elif [[ "$i" == "hazelcast-desktop_"** ]]; then
            __version=${i#hazelcast-desktop_}
            hdesktopv="$hdesktopv $__version"
         elif [[ "$i" == "hazelcast-jet-enterprise-"** ]]; then
            __version=${i#hazelcast-jet-enterprise-}
            jenterv="$jenterv $__version"
         elif [[ "$i" == "hazelcast-jet-management-center-"** ]]; then
            __version=${i#hazelcast-jet-management-center-}
            jmanv="$jmanv $__version"
         elif [[ "$i" == "hazelcast-jet-"** ]]; then
            __version=${i#hazelcast-jet-}
            jossv="$jossv $__version"
         elif [[ "$i" == "hazelcast-"** ]]; then
            __version=${i#hazelcast-}
            hossv="$hossv $__version"
            # Get man center version included in the hazelcast distribution
            for j in $i/*.jar; do
               if [[ "$j" == *"hazelcast-management-center"* ]]; then
                  local mcv=${j#*hazelcast-management-center-}
                  hmanv="$hmanv ${mcv%.jar}"
                  break;
               fi
             done
         fi
      done

      hmanv=$(unique_words "$hmanv")
      HAZELCAST_ENTERPRISE_VERSIONS=$(sortVersionList "$henterv")
      HAZELCAST_MANAGEMENT_CENTER_VERSIONS=$(sortVersionList "$hmanv")
      HAZELCAST_DESKTOP_VERSIONS=$(sortVersionList "$hdesktopv")
      JET_ENTERPRISE_VERSIONS=$(sortVersionList "$jenterv")
      JET_OSS_VERSIONS=$(sortVersionList "$jossv")
      HAZELCAST_OSS_VERSIONS=$(sortVersionList "$hossv")

      # Hazelcast/Jet  management center merged starting 4.2021.02
      for i in ${HAZELCAST_MANAGEMENT_CENTER_VERSIONS[@]}; do
         if [[ "$i" == "4.2021"* ]]; then
            jmanv="$i $jmanv"
         fi
      done
      JET_MANAGEMENT_CENTER_VERSIONS=$(sortVersionList "$jmanv")

      # Java - only the one that is set in the workspace
      # Remove blank lines from grep results. Pattern includes space and tab.
      # If multi-tenant workspace then the user might not have read access.
      if [ -r "$WORKSPACE_PATH/setenv.sh" ]; then
         local __JAVA_HOME=$(grep "export JAVA_HOME=" "$WORKSPACE_PATH/setenv.sh" | sed -e 's/#.*$//' -e '/^[ 	]*$/d' -e 's/.*=//' -e 's/"//g')
      else
         local __JAVA_HOME=""
      fi
      if [ "$__JAVA_HOME" == "" ]; then
         # Get the RWE's JAVA_HOME
         if [ -r "$WORKSPACE_PATH/../setenv.sh" ]; then
            __JAVA_HOME=$(grep "export JAVA_HOME=" "$WORKSPACE_PATH/../setenv.sh" | sed -e 's/#.*$//' -e '/^[ 	]*$/d' -e 's/.*=//' -e 's/"//g')
         else
            if [ "$JAVA_HOME" != "" ]; then
               __JAVA_HOME=$JAVA_HOME
            else
               local JAVA_EXEC_PATH=$(which java)
               if [ "$JAVA_EXEC_PATH" != "" ]; then
                  __JAVA_HOME=$(dirname $(dirname $JAVA_EXEC_PATH))
               fi
            fi
         fi
      fi
      if [ "$__JAVA_HOME" != "" ] && [ -f "$__JAVA_HOME/bin/java" ]; then
         # Use eval to handle commands with spaces
         local __COMMAND="\"$__JAVA_HOME/bin/java\" -version 2>&1 | grep version "
         JAVA_VERSIONS=$(eval $__COMMAND)
         JAVA_VERSIONS=$(echo $JAVA_VERSIONS |  sed -e 's/.*version//' -e 's/"//g' -e 's/ //g')
      fi

      # Kafka
      __versions=""
      for i in kafka_*; do
         __version=${i#kafka_}
         __versions="$__versions $__version "
      done
      KAFKA_VERSIONS=$(sortVersionList "$__versions")

      # Prometheus
      __versions=""
      for i in prometheus-*; do
         __version=${i#prometheus-}
         __versions="$__versions $__version "
      done
      PROMETHEUS_VERSIONS=$(sortVersionList "$__versions")

      # Redis
      __versions=""
      for i in redis-*; do
         __version=${i#redis-}
         __versions="$__versions $__version "
      done
      REDIS_VERSIONS=$(sortVersionList "$__versions")

      # SnappyData
      __versions=""
      for i in snappydata-*; do
         __version=${i#snappydata-}
         #__version=${__version%-bin}
         __versions="$__versions $__version "
      done
      SNAPPYDATA_VERSIONS=$(sortVersionList "$__versions")

      # Spark
      __versions=""
      for i in spark-*; do
         __version=${i#spark-}
         __version=${__version%-bin}
         __versions="$__versions $__version "
      done
      SPARK_VERSIONS=$(sortVersionList "$__versions")

      popd > /dev/null 2>&1
            
   fi
}

#
# Determines the product based on the product home path value of PRODUCT_HOME.
# The following environment variables are set after invoking this function.
#   PRODUCT         geode, gemfire, hazelcast, jet, snappydata, coherence, redis, hadoop, kafka, spark
#   CLUSTER_TYPE    Set to imdg or jet if PRODUCT is hazelcast,
#                   Set to geode or gemfire if PRODUCT is geode or gemfire,
#                   set to standalone if PRODUCT is spark,
#                   set to kafka or confluent if PRODUCT is kafka or confluent,
#                   set to pseudo if PRODUCT is hadoop,
#                   set to PRODUCT for all others.
#   CLUSTER         Set to the default cluster name, i.e., mygeode, mygemfire, myhz, myjet, mysnappy, myspark
#                   only if CLUSTER is not set.
#   GEODE_HOME      Set to PRODUCT_HOME if PRODUCT is geode and CLSUTER_TYPE is geode.
#   GEMFIRE_HOME    Set to PRODUCT_HOME if PRODUCT is geode and CLUSTER_TYPE is gemfire.
#   HAZELCAST_HOME  Set to PRODUCT_HOME if PRODUCT is hazelcast and CLUSTER_TYPE is imdg.
#   JET_HOME        Set to PRODUCT_HOME if PRODUCT is hazelcast and CLUSTER_TYPE is jet.
#   REDIS_HOME      Set to PRODUCT_HOME if PRODUCT is redis.
#   SNAPPYDATA_HOME Set to PRODUCT_HOME if PRODUCT is snappydata.
#   SPARK_HOME      Set to PRODUCT_HOME if PRODUCT is spark.
#   KAFKA_HOME      Set to PRODUCT_HOME if PRODUCT is kafka or confluent.
#   CONFLUENT_HOME  Set to PRODUCT_HOME if PRODUCT is confluent.
#   HADOOP_HOME     Set to PRODUCT_HOME if PRODUCT is hadoop.
#
# @required PRODUCT_HOME Product home path (installation path)
#
function determineProduct
{
   if [[ "$PRODUCT_HOME" == *"coherence"* ]]; then
      PRODUCT="coherence"
      COHERENCE_HOME="$PRODUCT_HOME"
      CLUSTER_TYPE="coherence"
      CLUSTER=$DEFAULT_COHERENCE_CLUSTER
   elif [[ "$PRODUCT_HOME" == *"geode"* ]] ||  [[ "$PRODUCT_HOME" == *"gemfire"* ]]; then
      PRODUCT="geode"
      if [[ "$PRODUCT_HOME" == *"geode"* ]]; then
         CLUSTER_TYPE="geode"
         if [ "$CLUSTER" == "" ]; then
            CLUSTER=$DEFAULT_GEODE_CLUSTER
         fi
      else
         CLUSTER_TYPE="gemfire"
         if [ "$CLUSTER" == "" ]; then
            CLUSTER=$DEFAULT_GEMFIRE_CLUSTER
         fi
      fi
      GEODE_HOME="$PRODUCT_HOME"
   elif [[ "$PRODUCT_HOME" == *"hadoop"* ]]; then
      PRODUCT="hadoop"
      HADOOP_HOME="$PRODUCT_HOME"
      CLUSTER_TYPE="pseudo"
      CLUSTER=$DEFAULT_HADOOP_CLUSTER
   elif [[ "$PRODUCT_HOME" == *"hazelcast"* ]]; then
      PRODUCT="hazelcast"
      if [[ "$PRODUCT_HOME" == *"hazelcast-jet"* ]]; then
         CLUSTER_TYPE="jet"
         if [ "$CLUSTER" == "" ]; then
            CLUSTER=$DEFAULT_JET_CLUSTER
         fi
         JET_HOME="$PRODUCT_HOME"
      else
         CLUSTER_TYPE="imdg"
         if [ "$CLUSTER" == "" ]; then
            CLUSTER=$DEFAULT_HAZELCAST_CLUSTER
         fi
         HAZELCAST_HOME="$PRODUCT_HOME"
      fi
   elif [[ "$PRODUCT_HOME" == *"kafka"* ]] || [[ "$PRODUCT_HOME" == *"confluent"* ]]; then
      PRODUCT="kafka"
      if [[ "$PRODUCT_HOME" == *"kafka"* ]]; then
         CONFLUENT_HOME=""
         CLUSTER_TYPE="kafka"
         if [ "$CLUSTER" == "" ]; then
            CLUSTER=$DEFAULT_KAFKA_CLUSTER
         fi
      else
         CONFLUENT_HOME="$PRODUCT_HOME"
         CLUSTER_TYPE="confluent"
         if [ "$CLUSTER" == "" ]; then
            CLUSTER=$DEFAULT_CONFLUENT_CLUSTER
         fi
      fi
      KAFKA_HOME="$PRODUCT_HOME"
   elif [[ "$PRODUCT_HOME" == *"redis"* ]]; then
      PRODUCT="redis"
      REDIS_HOME="$PRODUCT_HOME"
      CLUSTER_TYPE="redis"
      CLUSTER=$DEFAULT_REDIS_CLUSTER
   elif [[ "$PRODUCT_HOME" == *"snappydata"* ]]; then
      PRODUCT="snappydata"
      SNAPPYDATA_HOME="$PRODUCT_HOME"
      CLUSTER_TYPE="snappydata"
      CLUSTER=$DEFAULT_SNAPPYDATA_CLUSTER
   elif [[ "$PRODUCT_HOME" == *"spark"* ]]; then
      PRODUCT="spark"
      SPARK_HOME="$PRODUCT_HOME"
      CLUSTER_TYPE="standalone"
      CLUSTER=$DEFAULT_SPARK_CLUSTER
   else
      PRODUCT="none"
      CLUSTER_TYPE="none"
   fi
}

#
# Determines the product by examining cluster files. The following environment variables
# are set after invoking this function.
#   PRODUCT         geode, hazelcast, snappydata, coherence, redis, spark, hadoop
#   CLUSTER_TYPE    Set to imdg or jet if PRODUCT is hazelcast,
#                   set to standalone if PRODUCT is spark,
#                   set to PRODUCT for all others.
#
# @param clusterName    Cluster name. If unspecified, then defaults to $CLUSTER.
#
function determineClusterProduct
{
   local __CLUSTER=$1
   if [ "$__CLUSTER" == "" ]; then
      __CLUSTER=$CLUSTER
   fi
   local CLUSTER_DIR=$CLUSTERS_DIR/$__CLUSTER
   retrieveClusterEnvFile "$CLUSTER_DIR"
}

#
# Returns space separated list of installed products in the specified
# workspace.
#
# @param workspaceName Workspace name. If unspecified, then the current workspace is used.
#
function getInstalledProducts
{
  local __WORKSPACE_DIR
  if [ "$1" == "" ]; then
     __WORKSPACE_DIR="$PADOGRID_WORKSPACE"
  else
     __WORKSPACE_DIR="$PADOGRID_WORKSPACES_HOME/$1"
  fi

  local THIS_PRODUCT=$PRODUCT
  local THIS_PRODUCT_HOME=$PRODUCT_HOME

  . "$__WORKSPACE_DIR/setenv.sh"

  # Must reinstate the product values of the current cluster
  export PRODUCT=$THIS_PRODUCT
  export PRODUCT_HOME=$THIS_PRODUCT_HOME

  local PRODUCTS=""
  if [ "$GEODE_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS geode"
  fi
  if [ "$GEMFIRE_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS gemfire"
  fi
  if [ "$HAZELCAST_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS hazelcast"
  fi
  if [ "$JET_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS jet"
  fi
  if [ "$REDIS_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS redis"
  fi
  if [ "$SNAPPYDATA_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS snappydata"
  fi
  if [ "$SPARK_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS spark"
  fi
  if [ "$COHERENCE_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS coherence"
  fi
  if [ "$REDIS_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS redis"
  fi
  if [ "$KAFKA_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS kafka"
  fi
  if [ "$CONFLUENT_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS confluent"
  fi
  if [ "$HADOOP_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS hadoop"
  fi
  if [ "$DERBY_HOME" != "" ]; then
     PRODUCTS="$PRODUCTS derby"
  fi
  echo "$PRODUCTS"
}

#
# Creates the product env file, i.e., .geodeenv.sh, .hazelcastenv.sh, .snappydataenv.sh,
# .coherenceenv.sh, or .sparkenv.sh in the specified RWE directory if it does not exist.
#
# @optional PADOGRID_WORKSPACES_HOME
# @param productName      Valid value are 'geode', 'hazelcast', 'snappydata', 'coherence', 'redis', 'spark', 'hadoop'.
# @param workspacesHome   RWE directory path. If not specified then it creates .geodeenv.sh, 
#                         .hazelcastenv.sh, .snappydataenv.sh, .coherenceenv.sh, or .sparkenv.sh in
#                         PADOGRID_WORKSPACES_HOME.
#
function createProductEnvFile
{
   local PRODUCT_NAME="$1"
   local WORKSPACES_HOME="$2"
   if [ "$WORKSPACES_HOME" == "" ]; then
      WORKSPACES_HOME="$PADOGRID_WORKSPACES_HOME"
   fi
   if [ "$PRODUCT_NAME" == "geode" ] || [ "$PRODUCT_NAME" == "gemfire" ]; then
      if [ "$WORKSPACES_HOME" != "" ] && [ ! -f $WORKSPACES_HOME/.geodeenv.sh ]; then
         echo "#" > $WORKSPACES_HOME/.geodeenv.sh
         echo "# Enter Geode/GemFire product specific environment variables and initialization" >> $WORKSPACES_HOME/.geodeenv.sh
         echo "# routines here. This file is sourced in by setenv.sh." >> $WORKSPACES_HOME/.geodeenv.sh
         echo "#" >> $WORKSPACES_HOME/.geodeenv.sh
      fi
   elif [ "$PRODUCT_NAME" == "hazelcast" ] || [ "$PRODUCT_NAME" == "jet" ]; then
      if [ "$WORKSPACES_HOME" != "" ] && [ ! -f $WORKSPACES_HOME/.hazelcastenv.sh ]; then
         echo "#" > $WORKSPACES_HOME/.hazelcastenv.sh
         echo "# Enter Hazelcast product specific environment variables and initialization" >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "# routines here. This file is sourced in by setenv.sh." >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "#" >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "" >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "#" >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "# Set IMDG and/or Jet license keys. Note that you can create multiple workspaces" >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "# but each workspace can be configured with only one (1) cluster type, IMDG or Jet." >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "#" >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "IMDG_LICENSE_KEY=" >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "JET_LICENSE_KEY=" >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "" >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "#" >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "# Set Management Center license key. If this key is not set then the enterprise"  >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "# license key is used instead." >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "#" >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "MC_LICENSE_KEY=" >> $WORKSPACES_HOME/.hazelcastenv.sh
      fi
   elif [ "$PRODUCT_NAME" == "snappydata" ]; then
      if [ "$WORKSPACES_HOME" != "" ] && [ ! -f $WORKSPACES_HOME/.snappydataenv.sh ]; then
         echo "#" > $WORKSPACES_HOME/.snappydataenv.sh
         echo "# Enter SnappyData product specific environment variables and initialization" >> $WORKSPACES_HOME/.snappydataenv.sh
         echo "# routines here. This file is sourced in by setenv.sh." >> $WORKSPACES_HOME/.snappydataenv.sh
         echo "#" >> $WORKSPACES_HOME/.snappydataenv.sh
      fi
   elif [ "$PRODUCT_NAME" == "coherence" ]; then
      if [ "$WORKSPACES_HOME" != "" ] && [ ! -f $WORKSPACES_HOME/.coherenceenv.sh ]; then
         echo "#" > $WORKSPACES_HOME/.coherenceenv.sh
         echo "# Enter Coherence product specific environment variables and initialization" >> $WORKSPACES_HOME/.coherenceenv.sh
         echo "# routines here. This file is sourced in by setenv.sh." >> $WORKSPACES_HOME/.coherenceenv.sh
         echo "#" >> $WORKSPACES_HOME/.coherenceenv.sh
      fi
   elif [ "$PRODUCT_NAME" == "redis" ]; then
      if [ "$WORKSPACES_HOME" != "" ] && [ ! -f $WORKSPACES_HOME/.redisenv.sh ]; then
         echo "#" > $WORKSPACES_HOME/.redisenv.sh
         echo "# Enter Redis product specific environment variables and initialization" >> $WORKSPACES_HOME/.redisenv.sh
         echo "# routines here. This file is sourced in by setenv.sh." >> $WORKSPACES_HOME/.redisenv.sh
         echo "#" >> $WORKSPACES_HOME/.redisenv.sh
      fi
   elif [ "$PRODUCT_NAME" == "spark" ]; then
      if [ "$WORKSPACES_HOME" != "" ] && [ ! -f $WORKSPACES_HOME/.sparkenv.sh ]; then
         echo "#" > $WORKSPACES_HOME/.sparkenv.sh
         echo "# Enter Spark product specific environment variables and initialization" >> $WORKSPACES_HOME/.sparkenv.sh
         echo "# routines here. This file is sourced in by setenv.sh." >> $WORKSPACES_HOME/.sparkenv.sh
         echo "#" >> $WORKSPACES_HOME/.sparkenv.sh
      fi
   elif [ "$PRODUCT_NAME" == "hadoop" ]; then
      if [ "$WORKSPACES_HOME" != "" ] && [ ! -f $WORKSPACES_HOME/.hadoopenv.sh ]; then
         echo "#" > $WORKSPACES_HOME/.hadoopenv.sh
         echo "# Enter Hadoop product specific environment variables and initialization" >> $WORKSPACES_HOME/.hadoopenv.sh
         echo "# routines here. This file is sourced in by setenv.sh." >> $WORKSPACES_HOME/.hadoopenv.sh
         echo "#" >> $WORKSPACES_HOME/.hadoopenv.sh
      fi
   fi
}

#
# Removes all the source duplicate options from the specified target option list.
# and returns the new target option list. The option lists must be in the form of
# "opt1=value1 opt2=value2 ..."
#
# @param sourceOpts Source options list.
# @param targetOpts Target options list
#
function removeEqualToOpts
{
   local __SOURCE_OPTS=$1
   local __TARGET_OPTS=$2
   local __NEW_OPTS=""
   for i in $__TARGET_OPTS; do
      local __OPT=${i/=*/}
      if [[ "$__SOURCE_OPTS" != *"$__OPT="* ]]; then
         __NEW_OPTS="$__NEW_OPTS $i"
      fi
   done
   echo "$__NEW_OPTS"
}

#
# Returns the value of the specified option found in the specified option list.
# If not found returns an empty string. The options list must be in the form of
# "opt1=value1 opt2=value2 ...".
#
# @param opt        Option name without the '=' character. Include any preceeding characters
#                   such as '-' or '--'.
# @param sourceOpts Option list.
#
function getOptValue
{
   local __OPT_TO_FIND=$1
   local __SOURCE_OPTS=$2
   local __VALUE=""
   for i in $__SOURCE_OPTS; do
      if [[ "$i=" == "$__OPT_TO_FIND="* ]]; then
         __VALUE=${i#$__OPT_TO_FIND=}
         break;
      fi
   done
   echo "$__VALUE"
}

#
# Returns the default start port number of the specified product.
# @param product  Product name in lower case, i.e., geode, gemfire, hazelcast, jet, snappydata, coherence, redis, spark, kafka.
#
function getDefaultStartPortNumber
{
   local __PRODUCT=$1
   if [ "$__PRODUCT" == "geode" ] || [ "$__PRODUCT" == "gemfire" ]; then
      echo "10334"
   elif [ "$__PRODUCT" == "hazelcast" ] || [ "$__PRODUCT" == "jet" ]; then
      echo "5701"
   elif [ "$__PRODUCT" == "snappydata" ]; then
      echo "10334"
   elif [ "$__PRODUCT" == "coherence" ]; then
      echo "9000"
   elif [ "$__PRODUCT" == "redis" ]; then
      echo "6379"
   elif [ "$__PRODUCT" == "spark" ]; then
      echo "7077"
   elif [ "$__PRODUCT" == "kafka" ]; then
      echo "9092"
   else
      # DEFAULT_LOCATOR_START_PORT for geode/gemfire/snappydata
      echo "$DEFAULT_MEMBRER_START_PORT"
   fi
}

#
# Returns "true" if the specified cluster is a Pado cluster; "false", otherwise.
#
# @required PADOGRID_WORKSPACE Current PadoGrid workspace path.
# @param    clusterName        Optional cluster name. If not specified, then it
#                              assumes the current cluster.
#
function isPadoCluster
{
   local __CLUSTER="$1"
   if [ "$__CLUSTER" == "" ]; then
      __CLUSTER=$CLUSTER
   fi
   local __CLUSTER_DIR=$PADOGRID_WORKSPACE/clusters/$__CLUSTER
   if [ -f "$__CLUSTER_DIR/bin_sh/import_csv" ]; then
      echo "true" 
   else
      echo "false" 
   fi
}

#
# Returns "true" if the specified cluster is a Hazelcast cluster; "false", otherwise.
#
# @required PADOGRID_WORKSPACE Current PadoGrid workspace path.
# @param    clusterName        Optional cluster name. If not specified, then it
#                              assumes the current cluster.
#
function isHazelcastCluster
{
   local __CLUSTER="$1"
   if [ "$__CLUSTER" == "" ]; then
      __CLUSTER=$CLUSTER
   fi
   local __CLUSTER_DIR=$PADOGRID_WORKSPACE/clusters/$__CLUSTER
   if [ -f "$__CLUSTER_DIR/etc/hazelcast.xml" ]; then
      echo "true" 
   else
      echo "false" 
   fi
}

#
# @param timestamp  Timestamp in the format, 'yyyy/mm/dd hh:mm:ss'.
#
function get_time_in_seconds
{
   local timestamp="$@"
   local time_in_seconds
   if [ "$timestamp" == "" ]; then
      time_in_seconds="0"
   else
      local date=$(echo $timestamp |  awk '{print $1} ')
      local time=$(echo $timestamp |  awk '{print $2}')
      date=$(echo $date | sed 's/-/\//g')
      # remove milliseconds
      time=$(echo $time | sed 's/\..*//')
      if [ "$OS_NAME" == "DARWIN" ]; then
         local time_in_seconds=$(date -jf "%Y/%m/%d %H:%M:%S" "$date $time" +%s)
      else
         local time_in_seconds=$(date -d "$date $time" +"%s")
      fi
      if [ "$time_in_seconds" == "" ]; then
         time_in_seconds="0"
      fi
   fi
   echo $time_in_seconds
}

#
# Removes all generated temporary files that have the following name pattern.
#
#   /tmp/padogrid-$EXECUTABLE-*$POSTFIX*"
#
# where EXECUTABLE is the executable name,
#       POSTFIX is any postfix file name part that comes after $EXECUTABLE, e.g., timestamp.
#
# This function is for cleaning up files upon receiving Ctrl-C. To trap Ctrl-C, add
# the following lines in your script.
#
#    LAST_TIMESTAMP_TRAP=0
#    trap 'cleanExit "$EXECUTABLE" "$POSTFIX" "false"' INT
#
# If the executable is executing other scripts that also generate temporary files then the first
# argument must be a space separated list of those script names including the executable name
# itself.
#
# You can also exit without Ctrl-C by passing in "true" as described below.
#
# @param    executable_list  Space separted executable names in double quotes. Required.
# @param    postfix  Postfix file name part comes after the executable name, e.g., timestamp. Required.
# @param    isExit "true" to exit, others to exit if the current time is less than LAST_TIMESTAMP_TRAP. Required.
#
function cleanExit
{
   local EXECUTABLES="$1"
   local POSTFIX="$2"
   local IS_EXIT="$3"
   if [ "$EXECUTABLES" != "" ]; then
      if  [ "$IS_EXIT" == "true" ] || [ $(date +%s) -lt $(( $LAST_TIMESTAMP_TRAP + 1 )) ]; then
         for i in $EXECUTABLES; do
            rm /tmp/padogrid-$i-*$POSTFIX* > /dev/null 2>&1
         done
         exit
      elif [ "$IS_EXIT" != "true" ]; then
         LAST_TIMESTAMP_TRAP=$(date +%s)
      fi
   fi
}

#
# Returns the specified product's HOME env variable name.
#
# @param product  Product name, i.e., geode, gemfire, hazelcast, etc. If not specified, then
#                 the value of the PRODUCT env var is used.
#
function getProductHome
{
   local PRODUCT_TYPE="$1"
   if [ "$PRODUCT_TYPE" == "" ]; then
      PRODUCT_TYPE=$PRODUCT
   fi
   if [ "$CLUSTER_TYPE" == "gemfire" ]; then
      PRODUCT_TYPE="gemfire"
   elif [ "$CLUSTER_TYPE" == "confluent" ]; then
      PRODUCT_TYPE="confluent"
   elif [ "$PRODUCT_TYPE" == "hazelcast-enterprise" ]; then
      PRODUCT_TYPE="hazelcast"
   fi
   # Convert to uppper case and replace '-' with '_'
   local __VM_PRODUCT_HOME="$(echo ${PRODUCT_TYPE^^} | sed 's/-/_/')_HOME"
   echo $__VM_PRODUCT_HOME
}

#
# Returns the product name of the specified product directory.
#
# @param productDir  Product, product directory name or directory path.
# @param clusterType Optional cluster type. The product name of products such as gemfire
#                    is determined by th cluster type due to the shared name, 'geode'.
#
function getProductName
{
   local __PRODUCT_DIR="$1"
   local __CLUSTER_TYPE="$2"
   local __PRODUCT

   if [ "$CLUSTER_TYPE" == "gemfire" ]; then
      __PRODUCT="gemfire"
   elif [ "$CLUSTER_TYPE" == "confluent" ]; then
      __PRODUCT="confluent"
   elif [[ "$__PRODUCT_DIR" == **"padogrid"** ]]; then
      __PRODUCT="padogrid"
   elif [[ "$__PRODUCT_DIR" == **"padodesktop"** ]] || [[ "$__PRODUCT_DIR" == **"pado-desktop"** ]]; then
      __PRODUCT="padodesktop"
   elif [[ "$__PRODUCT_DIR" == **"padoweb"** ]]; then
      __PRODUCT="padoweb"
   elif [[ "$__PRODUCT_DIR" == **"pado"** ]]; then
      __PRODUCT="pado"
   elif [[ "$__PRODUCT_DIR" == **"geode"** ]]; then
      __PRODUCT="geode"
   elif [[ "$__PRODUCT_DIR" == **"hazelcast-enterprise"** ]]; then
      __PRODUCT="hazelcast-enterprise"
   elif [[ "$__PRODUCT_DIR" == **"hazelcast-management-center"** ]]; then
      __PRODUCT="hazelcast-mc"
   elif [[ "$__PRODUCT_DIR" == **"hazelcast"** ]]; then
      __PRODUCT="hazelcast"
   elif [[ "$__PRODUCT_DIR" == **"redis"** ]]; then
      __PRODUCT="redis"
   elif [[ "$__PRODUCT_DIR" == **"snappydata"** ]]; then
      __PRODUCT="snappydata"
   elif [[ "$__PRODUCT_DIR" == **"spark"** ]]; then
      __PRODUCT="spark"
   elif [[ "$__PRODUCT_DIR" == **"kafka"** ]]; then
      __PRODUCT="kafka"
   elif [[ "$__PRODUCT_DIR" == **"confluent"** ]]; then
      __PRODUCT="kafka"
   elif [[ "$__PRODUCT_DIR" == **"gemfire"** ]]; then
      __PRODUCT="gemfire"
   elif [[ "$__PRODUCT_DIR" == **"coherence"** ]]; then
      __PRODUCT="coherence"
   elif [[ "$__PRODUCT_DIR" == **"jdk"** ]]; then
      __PRODUCT="java"
   fi

   echo $__PRODUCT
}

#
# Sets the specified associative array with all the products found in the specified VM host.
# The array must be declared before invoking this function, otherwise, it will fail with an error.
#
#
# Example:
#    VM_HOST="myvm"
#    declare -A vmProductArray
#    declare -A vmProductHomeArray
#    getVmProductArray $VM_HOST vmProductArray vmProductHomeArray
#    for product in vmProductArray; do
#       echo "$product ${vmProductArray[$product]}"
#    done
#
# @required VM_PADOGRID_ENV_BASE_PATH
# @required VM_PADOGRID_WORKSPACES_HOME
# @required VM_KEY
# @required VM_USER
# @required SSH_CONNECT_TIMEOUT
#
# @param vmHost         VM host address.
# @param vmProductArray Set this associative array with VM product info in the form
#                      of vmProductArray[product]=product_dir_name. It must be declared
#                      before invoking this function, e.g., declear -A vmProductArray.
# @param vmProductHomeArray Set this associative array with VM product home info in the form
#                      of vmProductArray[PRODUCT_HOME]=product_dir_name. It must be declared
#                      before invoking this function, e.g., declear -A vmProductArray.
#
function getVmProductArray
{
   local VM_HOST=$1
   if [ "$VM_HOST" == "" ]; then
       echo -e >&2 "${CError}ERROR:${CNone} getVmProductArray - VM host not specified"
       return
   fi
   local array=$2
   local array2=$3
   declare -A | grep -q "declare -A ${array}" || echo -e >&2 "${CError}ERROR:${CNone} getVmProductArray - no ${array} associative array declared"
   declare -A | grep -q "declare -A ${array2}" || echo -e >&2 "${CError}ERROR:${CNone} getVmProductArray - no ${array2} associative array declared"
   local VM_PADOGRID_ENV_BASE_PATH=$(dirname $(dirname $VM_PADOGRID_WORKSPACES_HOME))
   local VM_PADOGRID_PRODUCTS_PATH="$VM_PADOGRID_ENV_BASE_PATH/products"
   local VM_INSTALLED_PRODUCTS=""
   local PRODUCT_HOME_VAR
   local PRODUCT_DIR_NAME_LIST=$(ssh -q -n $VM_KEY $VM_USER@$VM_HOST -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ls $VM_PADOGRID_PRODUCTS_PATH")
   for i in $PRODUCT_DIR_NAME_LIST; do
      local VM_PRODUCT=$(getProductName $i)
      local PRODUCT_HOME_VAR=$(getProductHome $VM_PRODUCT)
      if [ "$VM_PRODUCT" != "" ]; then
         eval "$array[\"\$VM_PRODUCT\"]=${i}"
         eval "$array2[\"\$PRODUCT_HOME_VAR\"]=${i}"
      fi
   done
}

#
# Returns a space-sparated list of this host's IP addresses
#
function getHostIpAddresses
{
   echo $(ifconfig | grep inet | grep netmask | awk '{print $2}' | sort)
}

#
# Returns a space-sparated list of active Jupyter server port numbers
#
function getActiveJupyterPorts
{
   echo $(ps -wwef |grep jupyter | grep "\-\-port" | grep -v grep | sed 's/^.*\-\-port=//' | awk '{print $1}')
}

#
# Returns the JupyterLab URL for the specified RWE.
#
# @param workspaceType     "default" to return the default URL that does not include the
#                          workspace path, otherwise, returns the workspace URL that
#                          includes the workspace path. Optional.
# @param ipAddress         IP address. If not specified, then defaults to "0.0.0.0". Optional.
# @param portNumber        Port number. If not specified, then defaults to "8888". Optional.
# @param rweName RWE name. If not specified, then defaults to the current RWE. Optional.
#
function getJupyterUrl
{
   local WORKSPACE_TYPE="$1"
   local IP_ADDRESS="$2"
   local PORT_NUMBER="$3"
   local RWE_NAME="$4"

   if [ "$IP_ADDRESS" == "" ]; then
      IP_ADDRESS="0.0.0.0"
   fi
   if [ "$PORT_NUMBER" == "" ]; then
      PORT_NUMBER="8888"
   fi
   if [ "$RWE_NAME" == "" ]; then
      RWE_NAME=$(basename $PADOGRID_WORKSPACES_HOME)
   fi
   local JUPYTER_LOG_FILE=$HOME/.padogrid/workspaces/$RWE_NAME/jupyterlab-$PORT_NUMBER.log
   local header=$(grep "https" $JUPYTER_LOG_FILE | grep $PORT_NUMBER)
   local URL_PROTOCOL
   if [ "$header" != "" ]; then
      URL_PROTOCOL="https"
   else
      URL_PROTOCOL="http"
   fi
   local token
   if [ "$(grep "http" $JUPYTER_LOG_FILE | grep $PORT_NUMBER | grep "?token=")" != "" ]; then
      token=$(grep "http" $JUPYTER_LOG_FILE | grep $PORT_NUMBER | sed 's/^.*?token=/?token=/' | uniq)
   else
      token=""
   fi
   local URL="$URL_PROTOCOL://$IP_ADDRESS:$PORT_NUMBER"
   local DEFAULT_URL="$URL$token"
   local RWE_URL="$URL/lab/workspaces/$RWE_NAME$token"

   if [ "$WORKSPACE_TYPE" == "default" ]; then
      echo "$DEFAULT_URL"
   else
      echo "$RWE_URL"
   fi
}

#
# Returns downloadable product versions extracted from the cache provided by install_padogrid.
#
# @required PADOGRID_ENV_BASE_PATH 
# @param product Downloadable product name
#
function getDownloadableProductVersions
{
   local PRODUCT="$1"
   local cache_file="$PADOGRID_ENV_BASE_PATH/downloads/padogrid_download_versions.sh"
   if [ "$PRODUCT" == "" ] || [ ! -f "$cache_file" ]; then
      echo ""
   else
      . "$cache_file"
      case $PRODUCT in
         confluent) echo "$CONFLUENT_DOWNLOAD_VERSIONS" ;;
         derby) echo "$DERBY_DOWNLOAD_VERSIONS" ;;
         hadoop) echo "$HADOOP_DOWNLOAD_VERSIONS" ;;
         hazelcast-desktop) echo "$HAZELCAST_DESKTOP_DOWNLOAD_VERSIONS" ;;
         hazelcast-enterprise) echo "$HAZELCAST_ENTERPRISE_DOWNLOAD_VERSIONS" ;;
         hazelcast-mc ) echo "$HAZELCAST_MANAGEMENT_CENTER_DOWNLOAD_VERSIONS" ;;
         hazelcast-oss) echo "$HAZELCAST_OSS_DOWNLOAD_VERSIONS" ;;
         geode ) echo "$GEODE_DOWNLOAD_VERSIONS" ;;
         grafana-enterprise ) echo "$GRAFANA_DOWNLOAD_VERSIONS" ;;
         grafana-oss ) echo "$GRAFANA_DOWNLOAD_VERSIONS" ;;
         kafka ) echo "$KAFKA_DOWNLOAD_VERSIONS" ;;
         pado ) echo "$PADO_DOWNLOAD_VERSIONS" ;;
         padodesktop ) echo "$PADODESKTOP_DOWNLOAD_VERSIONS" ;;
         padoeclipse ) echo "$PADOECLIPSE_DOWNLOAD_VERSIONS" ;;
         padogrid ) echo "$PADOGRID_DOWNLOAD_VERSIONS" ;;
         padoweb ) echo "$PADOWEB_DOWNLOAD_VERSIONS" ;;
         prometheus ) echo "$PROMETHEUS_DOWNLOAD_VERSIONS" ;;
         redis-oss ) echo "$REDIS_DOWNLOAD_VERSIONS" ;;
         snappydata ) echo "$SNAPPYDATA_DOWNLOAD_VERSIONS" ;;
         spark ) echo "$SPARK_DOWNLOAD_VERSIONS" ;;
         *) echo "" ;;
      esac
   fi
}
