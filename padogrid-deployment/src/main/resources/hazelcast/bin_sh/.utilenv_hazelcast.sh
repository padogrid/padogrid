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
# Hazelcast Utility Functions. Do NOT modify!
# -----------------------------------------------------

#
# Returns the management center PID if it is running.
# @param mcName         Unique management center name
# @param workspaceName  Workspace name
# @param rweName        RWE name
#
function getMcPid
{
   __MC=$1
   __WORKSPACE=$2
   __RWE=$3

   if [ "$__RWE" == "" ]; then
      # Use eval to handle commands with spaces
      if [[ "$OS_NAME" == "CYGWIN"* ]]; then
         local mcs="$(WMIC path win32_process get Caption,Processid,Commandline | grep java | grep hazelcast.mc.name=$__MC | grep "padogrid.workspace=$__WORKSPACE " | grep -v grep | awk '{print $(NF-1)}')"
      else
         local mcs="$(ps -wweo pid,comm,args | grep java | grep hazelcast.mc.name=$__MC | grep padogrid.workspace=$__WORKSPACE | grep padogrid.rwe=$__RWE | grep -v grep | awk '{print $1}')"
      fi
   else
      # Use eval to handle commands with spaces
      if [[ "$OS_NAME" == "CYGWIN"* ]]; then
         local mcs="$(WMIC path win32_process get Caption,Processid,Commandline | grep java | grep hazelcast.mc.name=$__MC | grep padogrid.workspace=$__WORKSPACE | grep padogrid.rwe=$__RWE | grep -v grep | awk '{print $(NF-1)}')"
      else
         local mcs="$(ps -wweo pid,comm,args | grep java | grep hazelcast.mc.name=$__MC | grep padogrid.workspace=$__WORKSPACE | grep padogrid.rwe=$__RWE | grep -v grep | awk '{print $1}')"
      fi
   fi
   spids=""
   for j in $mcs; do
      spids="$j $spids"
   done
   spids=`trimString $spids`
   echo $spids
}

#
# Returns the mamangement center PID of VM if it is running. Empty value otherwise.
# This function is for clusters running on VMs whereas the getMcPid
# is for pods running on the same machine.
# @required VM_USER        VM ssh user name
# @optional VM_KEY         VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host           VM host name or address
# @param    mcName         Unique management center name
# @param    workspaceName  Workspace name
# @param    rweName        RWE name
#
function getVmMcPid
{
   local __HOST=$1
   local __MEMBER=$2
   local __WORKSPACE=$3
   local __RWE=$4

   if [ "$__RWE" == "" ]; then
      members=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -wweo pid,comm,args | grep java | grep hazelcast.mc.name=$__MC | grep padogrid.workspace=$__WORKSPACE | grep -v grep" | awk '{print $1}'`
   else
      members=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -wweo pid,comm,args | grep java | grep hazelcast.mc.name=$__MC | grep padogrid.workspace=$__WORKSPACE | grep padogrid.rwe=$__RWE | grep -v grep" | awk '{print $1}'`
   fi
   spids=""
   for j in $members; do
      spids="$j $spids"
   done
   spids=`trimString $spids`
   echo $spids
}

#
# Returns the number of active (or running) management centers in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# This function works for both VM and non-VM workspaces.
# @param clusterName   Cluster name.
# @param workspaceName Workspace name.
# @param rweName       RWE name
#
function getActiveMcCount
{
   # MC
   local __CLUSTER=$1
   local __WORKSPACE=$2
   local __RWE=$3

   if [ "$__WORKSPACE" == "" ] || [ "$__CLUSTER" == "" ]; then
      echo 0
   fi
   local MC
   local let MC_COUNT=0
   local let MC_RUNNING_COUNT=0
   local VM_ENABLED=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.enabled")
   if [ "$VM_ENABLED" == "true" ]; then
      local VM_HOSTS=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.locator.hosts")
      for VM_HOST in ${VM_HOSTS}; do
         let MC_COUNT=MC_COUNT+1
         MC=$(getMcName)
         pid=`getVmMcPid $VM_HOST $MC $__WORKSPACE $__RWE`
         if [ "$pid" != "" ]; then
             let MC_RUNNING_COUNT=MC_RUNNING_COUNT+1
         fi
      done
   else
      local RUN_DIR=$PADOGRID_WORKSPACES_HOME/$__WORKSPACE/clusters/$__CLUSTER/run
      pushd $RUN_DIR > /dev/null 2>&1
      MC_NAME=$(getMcName)
      for i in ${MC_NAME}*; do
         if [ -d "$i" ]; then
            MC=$i
            MC_NUM=${MC##$MC_NAME}
            let MC_COUNT=MC_COUNT+1
            pid=`getMcPid $MC $WORKSPACE $__RWE`
            if [ "$pid" != "" ]; then
               let MC_RUNNING_COUNT=MC_RUNNING_COUNT+1
	    fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $MC_RUNNING_COUNT
}

#
# Returns the management center name.
# @required CLUSTER           Cluster name.
#
function getMcName
{
   MC_HTTPS_ENABLED=`getClusterProperty "mc.https.enabled" "false"`
   if [ "$MC_HTTPS_ENABLED" == "true" ]; then
      MC_HTTPS_PORT=`getClusterProperty "mc.https.port" $DEFAULT_MC_HTTPS_PORT`
      MC_NAME=${CLUSTER}-mc-${MC_HTTPS_PORT}
   else
      MC_HTTP_PORT=`getClusterProperty "mc.http.port" $DEFAULT_MC_HTTP_PORT`
      MC_NAME=${CLUSTER}-mc-${MC_HTTP_PORT}
   fi
   echo $MC_NAME
}

# 
# Returns a complete list of apps found in PADOGRID_HOME/$PRODUCT/apps
# @required PADOGRID_HOME
# @required PRODUCT
# @param clusterType  "imdg" to return IMDG apps, "jet" to return Jet apps.
#                     If not specified or an invalid value then returns all apps.
#
function getAddonApps {
   if [ "$1" == "jet" ]; then
      __APPS="desktop jet_demo"
   else
      pushd $PADOGRID_HOME/${PRODUCT}/apps > /dev/null 2>&1
      __APPS=""
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
   if [ "$1" == "imdg" ]; then
      __APPS=${__APPS/jet_demo/}
   fi   
   echo $__APPS
}

#
# Pretty-prints the specified JAVA_OPTS
#
# @param javaOpts Java options
#
function printJavaOpts()
{
   __JAVA_OPTS=$1
   for token in $__JAVA_OPTS; do
      if [[ $token == -D* ]]; then
         echo "${token:2}"
      fi
   done
   for token in $__JAVA_OPTS; do
      if [[ $token != -D* ]]; then
         echo "$token"
      fi
   done
}
