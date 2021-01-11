#!/usr/bin/env bash

# ========================================================================
# Copyright (c) 2020 Netcrest Technologies, LLC. All rights reserved.
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
         echo "hg"   # host (or unknonw) viewd from guest
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
# Returns a complete list of workspaces found in the specified workspace path.
# If the workspaces path is not specified then it returns the workspaces in 
# PADOGRID_WORKSPACES_HOME.
# @required PADOGRID_WORKSPACES_HOME
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
#                     Valid values: "clusters", "pods", "k8s", "docker", and "apps".
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
            __TMP_DIR=$BASE_DIR/tmp
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
                  __VB_ID_PROCESS=`ps -ef |grep $__VB_ID | grep -v grep`
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
# @param workspaceName Workspace name.
# @param clusterName   Cluster name.
#
function getActiveMemberCount
{
   # Members
   local __WORKSPACE=$1
   local __CLUSTER=$2
   if [ "$__WORKSPACE" == "" ] || [ "$__CLUSTER" == "" ]; then
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
         pid=`getVmMemberPid $VM_HOST $MEMBER $__WORKSPACE`
         if [ "$pid" != "" ]; then
             let MEMBER_RUNNING_COUNT=MEMBER_RUNNING_COUNT+1
         fi
      done
   else
      local RUN_DIR=$PADOGRID_WORKSPACES_HOME/$__WORKSPACE/clusters/$__CLUSTER/run
      pushd $RUN_DIR > /dev/null 2>&1
      MEMBER_PREFIX=$(getMemberPrefix)
      for i in ${MEMBER_PREFIX}*; do
         if [ -d "$i" ]; then
            MEMBER=$i
            MEMBER_NUM=${MEMBER##$LOCATOR_PREFIX}
            let MEMBER_COUNT=MEMBER_COUNT+1
            pid=`getMemberPid $MEMBER $WORKSPACE`
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
# Returns the unique member name (ID) for the specified member number.
# @param memberNumber
#
function getMemberName
{
   __MEMBER_NUM=`trimString $1`
   len=${#__MEMBER_NUM}
   if [ $len == 1 ]; then
      __MEMBER_NUM=0$__MEMBER_NUM
   else
      __MEMBER_NUM=$__MEMBER_NUM
   fi
   echo "`getMemberPrefix`$__MEMBER_NUM"
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
# Returns the property value found in the $PODS_DIR/$POD/etc/pod.properties file.
# @param propertiesFilePath  Properties file path.
# @param propertyName        Property name.
# @param defaultValue        If the specified property is not found then this default value is returned.
#
function getProperty
{
   __PROPERTIES_FILE=$1
   if [ -f $__PROPERTIES_FILE ]; then
      for line in `grep $2 ${__PROPERTIES_FILE}`; do
         line=`trimString $line`
         if [[ $line == $2=* ]]; then
            __VALUE=${line#$2=}
            break;
         fi
      done
      if [ "$__VALUE" == "" ]; then
         echo "$3"
      else
         echo "$__VALUE"
      fi
   else
      echo "$3"
   fi
}

#
# Returns the property value found in the $PODS_DIR/$POD/etc/pod.properties file.
# @param propertiesFilePath  Properties file path.
# @param propertyName        Property name.
# @param defaultValue        If the specified property is not found then this default value is returned.
#
function getProperty2
{
   __PROPERTIES_FILE=$1
   if [ -f $__PROPERTIES_FILE ]; then
      while IFS= read -r line; do
         line=`trimString $line`
         if [[ $line == $2=* ]]; then
            __VALUE=${line#$2=}
            break;
         fi
      done < "$__PROPERTIES_FILE"
      if [ -z $__VALUE ]; then
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
# @parma     propertyName  Property name.
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
# @parma     propertyName  Property name.
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
# @parma propertyName  Property name.
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
# @param propertiesFilePath  Properties file path.
# @parma propertyName       Property name.
# @param propertyValue      Property value.
#
function setProperty
{
   local __LINE_NUM=0
   local __SED_BACKUP
   if [ -f $__PROPERTIES_FILE ]; then
      local __found="false"
      while IFS= read -r line; do
         let __LINE_NUM=__LINE_NUM+1
         line=`trimString $line`
         if [[ $line == $2=* ]]; then
            __found="true"
            break;
         fi
      done < "$__PROPERTIES_FILE"
      if [ "$__found" == "true" ]; then
         # SED backup prefix
         if [[ ${OS_NAME} == DARWIN* ]]; then
            # Mac - space required
            __SED_BACKUP=" 0"
         else
            __SED_BACKUP="0"
         fi
         sed -i${__SED_BACKUP} ''$__LINE_NUM's/'$line'/'$2'='$3'/g' "$__PROPERTIES_FILE"
      else
         echo "$2=$3" >> "$__PROPERTIES_FILE"
      fi
   fi
}

#
# Sets the specified pod property in the $PODS_DIR/$POD/etc/pod.properties file. 
# @required  PODS_DIR      Pods directory path
# @required  POD           Pod name.
# @parma     propertyName  Property name.
# @param     propertyValue Property value.
#
function setPodProperty
{
   __PROPERTIES_FILE="$PODS_DIR/$POD/etc/pod.properties"
   `setProperty $__PROPERTIES_FILE $1 $2`
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
   `setProperty $__PROPERTIES_FILE $1 $2`
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
# @required BASE_DIR
#
function getPrivateNetworkAddresses
{
   __TMP_DIR=$BASE_DIR/tmp
   if [ ! -d "$__TMP_DIR" ]; then
      mkdir -p $__TMP_DIR
   fi
   __TMP_FILE=$__TMP_DIR/tmp.txt
   
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
      rm $__TMP_FILE
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
         fi  
      done < "$__TMP_FILE"
   fi
   rm -f $__TMP_FILE
   echo $__PRIVATE_IP_ADDRESSES
}

#
# Updates the default workspaces envionment variables with the current values
# in the .rwe/defaultenv.sh file.
# @required PRODUCT
# @required PADOGRID_WORKSPACE
#
function updateDefaultEnv
{
   local RWE_DIR="$PADOGRID_WORKSPACES_HOME/.rwe"
   local DEFAULTENV_FILE="$RWE_DIR/defaultenv.sh"
   if [ ! -d "$RWE_DIR" ]; then
      mkdir "$RWE_DIR"
   fi
   local WORKSPACE=${PADOGRID_WORKSPACE##*/}
   echo "export PRODUCT=\"$PRODUCT\"" > $DEFAULTENV_FILE
   echo "export PADOGRID_WORKSPACE=\"\$PADOGRID_WORKSPACES_HOME/$WORKSPACE\"" >> $DEFAULTENV_FILE
}

#
# Creates a temporary defaultenv.sh file containing the specified parameters.
# @param product       Product name
# @param workspacePath Workspace path
#
function createTmpDefaultEnv
{
   local __PRODUCT="$1"
   local __WORKSPACE_PATH="$2"
   local DEFAULTENV_FILE="/tmp/defaultenv.sh"
   local WORKSPACE=${PADOGRID_WORKSPACE##*/}
   echo "export PRODUCT=\"$__PRODUCT\"" > $DEFAULTENV_FILE
   echo "export PADOGRID_WORKSPACE=\"\$PADOGRID_WORKSPACES_HOME/$WORKSPACE\"" >> $DEFAULTENV_FILE
}

#
# Removes the temporary defaultenv.sh file create by the 'createTmpDefaultEnv function
# if exists.
#
function removeTmpDefaultEnv
{
   if [ -f "/tmp/defaultenv.sh" ]; then
      rm "/tmp/defaultenv.sh"
   fi
}

#
# Retrieves the default environment variables set in the .rwe/defaultenv.sh file.
# @required PADOGRID_WORKSPACES_HOME
#
function retrieveDefaultEnv
{
   local RWE_DIR="$PADOGRID_WORKSPACES_HOME/.rwe"
   local DEFAULTENV_FILE="$RWE_DIR/defaultenv.sh"
   if [ -f "$DEFAULTENV_FILE" ]; then
      . "$DEFAULTENV_FILE"
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
      echo "   $EXECUTABLE [rwe_name [workspace_name]] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Switches to the specified root workspaces environment."
      echo ""
      echo "OPTIONS"
      echo "   rwe_name"
      echo "             Name of the root workspaces environment. If not specified, then switches"
      echo "             to the current root workspaces environment."
      echo ""
      echo "   workspace_name"
      echo "             Workspace to switch to. If not specified, then switches"
      echo "             to the default workspace of the specified RWE."
      echo ""
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

   # Reset Pado home path
   export PADO_HOME=""

   if [ "$1" == "" ]; then
      if [ ! -d "$PADOGRID_WORKSPACES_HOME/clusters/$CLUSTER" ]; then
         export CLUSTER=""
      fi
      . $PADOGRID_WORKSPACES_HOME/initenv.sh -quiet
   else
      local PARENT_DIR="$(dirname "$PADOGRID_WORKSPACES_HOME")"
      if [ ! -d "$PARENT_DIR/$1" ]; then
         echo >&2 "ERROR: Invalid RWE name. RWE name does not exist. Command aborted."
         return 1
      elif [ "$2" != "" ]; then
         if [ ! -d "$PARENT_DIR/$1/$2" ]; then
            echo >&2 "ERROR: Invalid workspace name. Workspace name does not exist. Command aborted."
            return 1
         fi
      fi
      if [ ! -d "$PARENT_DIR/$1/clusters/$CLUSTER" ]; then
         export CLUSTER=""
      fi
      . $PARENT_DIR/$1/initenv.sh -quiet
   fi
   if [ "$2" != "" ]; then
      switch_workspace $2
   else
      cd_rwe $1
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
      echo "   $EXECUTABLE - Switch to the specified padogrid workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [workspace_name] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Switches to the specified workspace."
      echo ""
      echo "OPTIONS"
      echo "   workspace_name"
      echo "             Workspace to switch to. If not specified, then switches"
      echo "             to the current workspace."
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

   # Reset Pado home path
   export PADO_HOME=""

   if [ "$1" == "" ]; then
      if [ ! -d "$PADOGRID_WORKSPACE" ]; then
         retrieveDefaultEnv
      fi
      if [ ! -d "$PADOGRID_WORKSPACE" ]; then
         __WORKSPACES=$(list_workspaces)
    for i in $__WORKSPACES; do
            __WORKSPACE=$i
       break;
         done
    if [ "$__WORKSPACE" == "" ]; then
            echo >&2 "ERROR: Workspace does not exist. Command aborted."
       return 1
    fi
         PADOGRID_WORKSPACE="$PADOGRID_WORKSPACES_HOME/$__WORKSPACE"
         updateDefaultEnv
      fi
      if [ ! -d "$PADOGRID_WORKSPACE/clusters/$CLUSTER" ]; then
         export CLUSTER=""
      fi
      . $PADOGRID_WORKSPACE/initenv.sh -quiet
   else
      if [ ! -d "$PADOGRID_WORKSPACES_HOME/$1" ]; then
         echo >&2 "ERROR: Invalid workspace. Workspace does not exist. Command aborted."
         return 1
      fi
      if [ ! -d "$PADOGRID_WORKSPACES_HOME/$1/clusters/$CLUSTER" ]; then
         export CLUSTER=""
      fi
      . $PADOGRID_WORKSPACES_HOME/$1/initenv.sh -quiet
   fi
   cd_workspace $1
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
      echo "   $EXECUTABLE - Switch to the specified cluster in the current"
      echo "                 padogrid workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [cluster_name] [-?]"
      echo ""
      echo "  Switches to the specified cluster."
      echo ""
      echo "OPTIONS"
      echo "   cluster_name"
      echo "             Cluster to switch to. If not specified, then switches"
      echo "             to the current cluster."
      echo ""
      echo "DEFAULT"
      echo "   $EXECUTABLE"
      echo ""
      echo "SEE ALSO"
      printSeeAlsoList "*cluster*" $EXECUTABLE
      return
   elif [ "$1" == "-options" ]; then
      echo "-?"
      return
   fi

   if [ "$1" != "" ]; then
      export CLUSTER=$1
   fi
   cd_cluster $CLUSTER
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
   EXECUTABLE=cd_rwe
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified root workspaces environment"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [rwe_name [workspace_name]] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Changes directory to the specified root workspaces environment."
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
         echo >&2 "ERROR: Invalid RWE name. RWE name does not exist. Command aborted."
         return 1
      elif [ "$2" != "" ]; then
         if [ ! -d "$PARENT_DIR/$1/$2" ]; then
            echo >&2 "ERROR: Invalid workspace name. Workspace name does not exist. Command aborted."
            return 1
         fi
         cd $PARENT_DIR/$1/$2
      else
         cd $PARENT_DIR/$1
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
   EXECUTABLE=cd_workspace
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified padogrid workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [workspace_name] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Changes directory to the specified workspace."
      echo ""
      echo "OPTIONS"
      echo "   workspace_name"
      echo "             Workspace name. If not specified then changes to the"
      echo "             current workspace directory."
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
      cd $PADOGRID_WORKSPACE
   else
      cd $PADOGRID_WORKSPACES_HOME/$1
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
   EXECUTABLE=cd_pod
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified padogrid pod"
      echo "                 in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [pod_name] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Changes directory to the specified pod."
      echo ""
      echo "OPTIONS"
      echo "   pod_name" 
      echo "             Pod name. If not specified then changes to the"
      echo "             current pod directory."
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

   if [ "$1" == "" ]; then
      if [ -d $PADOGRID_WORKSPACE/pods/$POD ]; then
         cd $PADOGRID_WORKSPACE/pods/$POD
         pwd
      else
         echo >&2 "ERROR: Pod does not exist [$POD]. Command aborted."
      fi
   else
      if [ -d $PADOGRID_WORKSPACE/pods/$1 ]; then
         cd $PADOGRID_WORKSPACE/pods/$1
         pwd
      else
         echo >&2 "ERROR: Pod does not exist [$1]. Command aborted."
      fi
   fi
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
   EXECUTABLE=cd_cluster
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified padogrid cluster"
      echo "                 in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [cluster_name] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Changes directory to the specified cluster."
      echo ""
      echo "OPTIONS"
      echo "   cluster_name" 
      echo "             Cluster name. If not specified then changes to the"
      echo "             current cluster directory."
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
      cd $PADOGRID_WORKSPACE/clusters/$CLUSTER
   else
      cd $PADOGRID_WORKSPACE/clusters/$1
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
   EXECUTABLE=cd_k8s
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified padogrid Kubernetes cluster directory"
      echo "                 in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [cluster_name] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Changes directory to the specified Kubernetes directory."
      echo ""
      echo "OPTIONS"
      echo "   cluster_name"
      echo "             Kubernetes cluster name. If not specified then changes to the"
      echo "             current Kubernetes cluster directory."
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
      cd $PADOGRID_WORKSPACE/k8s/$1
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
   EXECUTABLE=cd_docker
   if [ "$1" == "-?" ]; then
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified padogrid Docker cluster directory"
      echo "                 in the current workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [cluster_name] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Changes directory to the specified Docker cluster."
      echo ""
      echo "OPTIONS"
      echo "   cluster_name"
      echo "             Docker cluster name. If not specified then changes to the"
      echo "             current Docker cluster directory."
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
      cd $PADOGRID_WORKSPACE/docker/$1
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
   EXECUTABLE=cd_app
   if [ "$1" == "-?" ]; then
      echo ""
      echo "NAME"
      echo "   $EXECUTABLE - Change directory to the specified app in the current"
      echo "                 padogrid workspace"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [app_name] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Changes directory to the specified app."
      echo ""
      echo "OPTIONS"
      echo "   app_name"
      echo "             App name. If not specified then changes to the"
      echo "             current app directory."
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
      cd $PADOGRID_WORKSPACE/apps/$1
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
      echo "WORKSPACE"
      echo "   $PADOGRID_WORKSPACE"
      echo ""
      echo "NAME"
      echo "   $EXECUTABLE - Execute the specified padogrid command"
      echo ""
      echo "SYNOPSIS"
      echo "   $EXECUTABLE [<padogrid-command>] [-version] [-?]"
      echo ""
      echo "DESCRIPTION"
      echo "   Executes the specified padogrid command. If no options are specified then it displays"
      echo "   the current workspace information."
      echo ""
      echo "OPTIONS"
      echo "   padogrid_command"
      echo "             Padogrid command to execute."
      echo ""
      echo "   -version"
      echo "             If specified, then displays the current workspace padogrid version."
      echo ""
      echo "   -product"
      echo "             If specified, then displays the current workspace product version."
      echo ""
      echo "COMMANDS"
      ls $SCRIPT_DIR
      echo ""
      return
   fi

   if [ "$1" == "cp_sub" ] || [ "$1" == "tools" ]; then
      COMMAND=$2
      SHIFT_NUM=2
   elif [ "$1" == "-version" ]; then
      echo "$PADOGRID_VERSION"
      return 0
   elif [ "$1" == "-product" ]; then
      echo "$PRODUCT"
      return 0
   else
      COMMAND=$1
      SHIFT_NUM=1
   fi


   if [ "$COMMAND" == "" ]; then
cat <<EOF
.______      ___       _______   ______     _______ .______       __   _______ ™
|   _  \    /   \     |       \ /  __  \   /  _____||   _  \     |  | |       \ 
|  |_)  |  /  ^  \    |  .--.  |  |  |  | |  |  __  |  |_)  |    |  | |  .--.  |
|   ___/  /  /_\  \   |  |  |  |  |  |  | |  | |_ | |      /     |  | |  |  |  |
|  |     /  _____  \  |  '--'  |  '--'  | |  |__| | |  |\  \----.|  | |  '--'  |
| _|    /__/     \__\ |_______/ \______/   \______| | _| '._____||__| |_______/ 
Copyright 2020 Netcrest Technologies, LLC. All rights reserved.
v$PADOGRID_VERSION
Manual: https://github.com/padogrid/padogrid/wiki

EOF

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
               echo -e "├── ${CLightGreen}$RWE${CNone}"
            else
               echo "├── $RWE"
	    fi
            LEADING_BAR="│   "
         else
            if [ "$RWE" == "$CURRENT_RWE" ]; then
               echo -e "└── ${CLightGreen}$RWE${CNone}"
            else
               echo "└── $RWE"
	    fi
            LEADING_BAR="    "
         fi
         local WORKSPACES=`ls $RWE_HOME/$RWE`
         WORKSPACES=$(removeTokens "$WORKSPACES" "initenv.sh setenv.sh")
         WORKSPACES=( $WORKSPACES )
         let WORKSPACES_LAST_INDEX=${#WORKSPACES[@]}-1
         for ((j = 0; j < ${#WORKSPACES[@]}; j++)); do
            local WORKSPACE=${WORKSPACES[$j]}
            local WORKSPACE_INFO=$(getWorkspaceInfoList "$WORKSPACE" "$RWE_HOME/$RWE")
            if [ $j -lt $WORKSPACES_LAST_INDEX ]; then
               if [ "$RWE" == "$CURRENT_RWE" ] && [ "$WORKSPACE" == "$CURRENT_WORKSPACE" ]; then
                  echo -e "${LEADING_BAR}├── ${CLightGreen}$WORKSPACE [$WORKSPACE_INFO]${CNone}"
               else
                  echo "${LEADING_BAR}├── $WORKSPACE [$WORKSPACE_INFO]"
	       fi
            else
               if [ "$RWE" == "$CURRENT_RWE" ] && [ "$WORKSPACE" == "$CURRENT_WORKSPACE" ]; then
                  echo -e "${LEADING_BAR}└── ${CLightGreen}$WORKSPACE [$WORKSPACE_INFO]${CNone}"
               else
                  echo "${LEADING_BAR}└── $WORKSPACE [$WORKSPACE_INFO]"
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

   local CLUSTER_TYPE=$(grep "CLUSTER_TYPE" $WORKSPACE_PATH/.addonenv.sh)
   CLUSTER_TYPE=$(echo "$CLUSTER_TYPE" | sed 's/^.*=//')
   local __PRODUCT_HOME=$(grep "export PRODUCT_HOME=" "$WORKSPACE_PATH/setenv.sh")
   local PRODUCT_VERSION
   if [ "$CLUSTER_TYPE" == "jet" ]; then
      PRODUCT_VERSION=$(echo "$__PRODUCT_HOME" | sed -e 's/^.*jet-enterprise-//')
      if [ "$PRODUCT_VERSION" == "$__PRODUCT_HOME" ]; then
         PRODUCT_VERSION=$(echo "$__PRODUCT_HOME" | sed -e 's/^.*jet-//' -e 's/"//')
      else
         PRODUCT_VERSION=$(echo "$PRODUCT_VERSION" | sed -e 's/"//')
      fi
   elif [ "$CLUSTER_TYPE" == "imdg" ]; then
      PRODUCT_VERSION=$(echo "$__PRODUCT_HOME" | sed -e 's/^.*hazelcast-enterprise-//' -e 's/"//')
      PRODUCT_VERSION=$(echo "$__PRODUCT_HOME" | sed -e 's/^.*hazelcast-enterprise-//')
      if [ "$PRODUCT_VERSION" == "$__PRODUCT_HOME" ]; then
         PRODUCT_VERSION=$(echo "$__PRODUCT_HOME" | sed -e 's/^.*hazelcast-//' -e 's/"//')
      else
         PRODUCT_VERSION=$(echo "$PRODUCT_VERSION" | sed -e 's/"//')
      fi
   elif [[ "$__PRODUCT_HOME" == *"gemfire"* ]]; then
      PRODUCT_VERSION=$(echo "$__PRODUCT_HOME" | sed -e 's/^.*pivotal-gemfire-//' -e 's/"//')
      CLUSTER_TYPE="gemfire"
   elif [[ "$__PRODUCT_HOME" == *"geode"* ]]; then
      PRODUCT_VERSION=$(echo "$__PRODUCT_HOME" | sed -e 's/^.*apache-geode-//' -e 's/"//')
      CLUSTER_TYPE="geode"
   elif [[ "$__PRODUCT_HOME" == *"snappydata"* ]]; then
      PRODUCT_VERSION=$(echo "$__PRODUCT_HOME" | sed -e 's/^.*snappydata-//' -e 's/"//')
      PRODUCT_VERSION=${PRODUCT_VERSION%-bin}
      CLUSTER_TYPE="snappydata"
   elif [[ "$__PRODUCT_HOME" == *"coherence"* ]]; then
      __PRODUCT_HOME=$(echo $__PRODUCT_HOME | sed -e 's/.*=//' -e 's/"//g')
      if [ -f "$__PRODUCT_HOME/product.xml" ]; then
         PRODUCT_VERSION=$(grep "version value" "$__PRODUCT_HOME/product.xml" | sed -e 's/^.*="//' -e 's/".*//')
      fi
      CLUSTER_TYPE="coherence"
   fi

   local VM_ENABLED=$(isWorkspaceVmEnabled "$WORKSPACE" "$RWE_PATH")
   if [ "$VM_ENABLED" == "true" ]; then
      VM_WORKSPACE="vm, "
   else
      VM_WORKSPACE=""
   fi
   PADOGRID_VERSION=$(grep "export PADOGRID_HOME=" "$WORKSPACE_PATH/setenv.sh")
   PADOGRID_VERSION=$(echo "$PADOGRID_VERSION" | sed -e 's/^.*padogrid_//' -e 's/"//')
   echo "${VM_WORKSPACE}${CLUSTER_TYPE}_${PRODUCT_VERSION}, padogrid_$PADOGRID_VERSION"
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
         local VM_ENABLED=$(grep "VM_ENABLED=" "$WORKSPACE_PATH/setenv.sh")
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
   local COMMANDS=`ls $FILTER`
   popd > /dev/null 2>&1
   local LINE=""
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
            echo -e "├── ${CLightGreen}${LIST[$i]}${CNone}"
         else
            echo "├── ${LIST[$i]}"
         fi
      else
         if [ "${LIST[$i]}" == "$HIGHLIGHT_ITEM" ]; then
            echo -e "└── ${CLightGreen}${LIST[$i]}${CNone}"
         else
            echo "└── ${LIST[$i]}"
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
# Determines the product based on the product home path value of PRODUCT_HOME.
# The following environment variables are set after invoking this function.
#   PRODUCT         geode, hazelcast, or snappydata
#   CLUSTER_TYPE    This is set to imdg or jet only if PRODUCT is hazelcast.
#   CLUSTER         Set to the default cluster name, i.e., mygeode, mygemfire, myhz, myjet, mysnappy
#                   only if CLUSTER is not set.
#   GEODE_HOME      Set to PRODUCT_HOME if PRODUCT is geode.
#   HAZELCAST_HOME  Set to PRODUCT_HOME if PRODUCT is hazelcast.
#   JET_HOME        Set to PRODUCT_HOME if PRODUCT is hazelcast.
#   SNAPPYDATA_HOME Set to PRODUCT_HOME if PRODUCT is snappydata.
# @required PRODUCT_HOME Product home path (installation path)
#
function determineProduct
{
   if [[ "$PRODUCT_HOME" == *"hazelcast"* ]]; then
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
   elif [[ "$PRODUCT_HOME" == *"snappydata"* ]]; then
      PRODUCT="snappydata"
      SNAPPYDATA_HOME="$PRODUCT_HOME"
      CLUSTER_TYPE="snappydata"
      CLUSTER=$DEFAULT_SNAPPYDATA_CLUSTER
   elif [[ "$PRODUCT_HOME" == *"coherence"* ]]; then
      PRODUCT="coherence"
      COHERENCE_HOME="$PRODUCT_HOME"
      CLUSTER_TYPE="coherence"
      CLUSTER=$DEFAULT_COHERENCE_CLUSTER
   else
      PRODUCT=""
   fi
}

#
# Creates the product env file, i.e., .geodeenv.sh or .hazelcastenv.sh, in the specified
# RWE directory if it does not exist.
# @optional PADOGRID_WORKSPACES_HOME
# @param productName      Valid value are 'geode' or 'hazelcast'.
# @param workspacesHome   RWE directory path. If not specified then it creates .hazelcastenv.sh in
#                         PADOGRID_WORKSPACES_HOME.
#
function createProductEnvFile
{
   local PRODUCT_NAME="$1"
   local WORKSPACES_HOME="$2"
   if [ "$WORKSPACES_HOME" == "" ]; then
      WORKSPACES_HOME="$PADOGRID_WORKSPACES_HOME"
   fi
   if [ "$PRODUCT_NAME" == "geode" ]; then
      if [ "$WORKSPACES_HOME" != "" ] && [ ! -f $WORKSPACES_HOME/.geodeenv.sh ]; then
         echo "#" > $WORKSPACES_HOME/.geodeenv.sh
         echo "# Enter Geode/GemFire product specific environment variables and initialization" >> $WORKSPACES_HOME/.geodeenv.sh
         echo "# routines here. This file is source in by setenv.sh." >> $WORKSPACES_HOME/.geodeenv.sh
         echo "#" >> $WORKSPACES_HOME/.geodeenv.sh
      fi
   elif [ "$PRODUCT_NAME" == "hazelcast" ]; then
      if [ "$WORKSPACES_HOME" != "" ] && [ ! -f $WORKSPACES_HOME/.hazelcastenv.sh ]; then
         echo "#" > $WORKSPACES_HOME/.hazelcastenv.sh
         echo "# Enter Hazelcast product specific environment variables and initialization" >> $WORKSPACES_HOME/.hazelcastenv.sh
         echo "# routines here. This file is source in by setenv.sh." >> $WORKSPACES_HOME/.hazelcastenv.sh
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
         echo "#" > $WORKSPACES_HOME/.geodeenv.sh
         echo "# Enter SnappyData product specific environment variables and initialization" >> $WORKSPACES_HOME/.geodeenv.sh
         echo "# routines here. This file is source in by setenv.sh." >> $WORKSPACES_HOME/.geodeenv.sh
         echo "#" >> $WORKSPACES_HOME/.geodeenv.sh
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
