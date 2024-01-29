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

# -----------------------------------------------------
# Spark Utility Functions. Do NOT modify!
# -----------------------------------------------------

#
# Returns the master number that includes the leading zero.
# @param masterNumber
#
function getMasterNumWithLeadingZero
{
   if [ $1 -lt 10 ]; then
      echo "0$1"
   else
      echo "$1"
   fi
}

#
# Returns the master PID if it is running. Empty value otherwise.
# @required NODE_LOCAL       Node name with the local extenstion. For remote call only.
# @required REMOTE_SPECIFIED false to invoke remotely, true to invoke locally.
# @param    masterName      Unique master name
# @param    workspaceName   Workspace name
# @param    rweName         RWE name
#
function getMasterPid
{
   local __MASTER=$1
   local __WORKSPACE=$2
   local __RWE=$3

   local __IS_GUEST_OS_NODE=`isGuestOs $NODE_LOCAL`
   local masters
   if [ "$__IS_GUEST_OS_NODE" == "true" ] && [ "$POD" != "local" ] && [ "$REMOTE_SPECIFIED" == "false" ]; then
      if [ "$__RWE" == "" ]; then
         masters=`ssh -n $SSH_USER@$NODE_LOCAL -o LogLevel=error -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -wweo pid,comm,args | grep java | grep pado.vm.id=$__MASTER | grep padogrid.workspace=$__WORKSPACE | grep -v grep" | awk '{print $1}'`
      else
         masters=`ssh -n $SSH_USER@$NODE_LOCAL -o LogLevel=error -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -wweo pid,comm,args | grep java | grep pado.vm.id=$__MASTER | grep padogrid.workspace=$__WORKSPACE | grep padogrid.rwe=$__RWE | grep -v grep" | awk '{print $1}'`
      fi
   else
      if [ "$__RWE" == "" ]; then
         # Use eval to handle commands with spaces
         if [[ "$OS_NAME" == "CYGWIN"* ]]; then
            local masters="$(WMIC path win32_process get Caption,Processid,Commandline | grep java | grep pado.vm.id=$__MASTER | grep "padogrid.workspace=$__WORKSPACE" | grep -v grep | awk '{print $(NF-1)}')"
         else
            local masters="$(ps -wweo pid,comm,args | grep java | grep pado.vm.id=$__MASTER | grep padogrid.workspace=$__WORKSPACE | grep -v grep | awk '{print $1}')"
         fi
      else
         # Use eval to handle commands with spaces
         if [[ "$OS_NAME" == "CYGWIN"* ]]; then
            local masters="$(WMIC path win32_process get Caption,Processid,Commandline |grep java | grep pado.vm.id=$__MASTER | grep "padogrid.workspace=$__WORKSPACE" | grep "padogrid.rwe=$__RWE" | grep -v grep | awk '{print $(NF-1)}')"
         else
            local masters="$(ps -wweo pid,comm,args | grep java | grep pado.vm.id=$__MASTER | grep padogrid.workspace=$__WORKSPACE | grep padogrid.rwe=$__RWE | grep -v grep | awk '{print $1}')"
         fi
      fi
   fi
   spids=""
   for j in $masters; do
      spids="$j $spids"
   done
   spids=`trimString $spids`
   echo $spids
}

#
# Returns the master PID of VM if it is running. Empty value otherwise.
# This function is for clusters running on VMs whereas the getMasterPid
# is for pods running on the same machine.
# @required VM_USER        VM ssh user name
# @optional VM_KEY         VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host           VM host name or address
# @param    masterName     Unique master name
# @param    workspaceName  Workspace name
# @param    rweName        RWE name
#
function getVmMasterPid
{
   local __HOST=$1
   local __MEMBER=$2
   local __WORKSPACE=$3
   local __RWE=$4

   if [ "$__RWE" == "" ]; then
      local masters=`ssh -n $VM_KEY $VM_USER@$__HOST -o LogLevel=error -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -wweo pid,comm,args | grep java | grep pado.vm.id=$__MEMBER | grep padogrid.workspace=$__WORKSPACE | grep -v grep" | awk '{print $1}'`
   else
      local masters=`ssh -n $VM_KEY $VM_USER@$__HOST -o LogLevel=error -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -wweo pid,comm,args | grep java | grep pado.vm.id=$__MEMBER | grep padogrid.workspace=$__WORKSPACE | grep padogrid.rwe=$__RWE | grep -v grep" | awk '{print $1}'`
   fi
   spids=""
   for j in $masters; do
      spids="$j $spids"
   done
   spids=`trimString $spids`
   echo $spids
}

#
# Returns the number of active (or running) masters in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# This function works for both VM and non-VM workspaces.
# @param clusterName   Cluster name.
# @param workspaceName Workspace name.
# @param rweName       RWE name
#
function getActiveMasterCount
{
   # Masters
   local __CLUSTER=$1
   local __WORKSPACE=$2
   local __RWE=$3

   if [ "$__CLUSTER" == "" ] || [ "$__WORKSPACE" == "" ] || [ "$__RWE" == "" ]; then
      echo 0
   fi
   local MASTER
   local let MASTER_COUNT=0
   local let MASTER_RUNNING_COUNT=0
   local VM_ENABLED=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.enabled")
   if [ "$VM_ENABLED" == "true" ]; then
      local VM_HOSTS=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.master.hosts")
      for VM_HOST in ${VM_HOSTS}; do
         let MASTER_COUNT=MASTER_COUNT+1
         MASTER=`getVmMasterName $VM_HOST`
         pid=`getVmMasterPid $VM_HOST $MASTER $__WORKSPACE $__RWE`
         if [ "$pid" != "" ]; then
             let MASTER_RUNNING_COUNT=MASTER_RUNNING_COUNT+1
         fi
      done
   else
      local RUN_DIR=$PADOGRID_WORKSPACES_HOME/$__WORKSPACE/clusters/$__CLUSTER/run
      pushd $RUN_DIR > /dev/null 2>&1
      MASTER_PREFIX=$(getMasterPrefix)
      for i in ${MASTER_PREFIX}*; do
         if [ -d "$i" ]; then
            MASTER=$i
            MASTER_NUM=${MASTER##$MASTER_PREFIX}
            let MASTER_COUNT=MASTER_COUNT+1
            pid=`getMasterPid $MASTER $__WORKSPACE $__RWE`
            if [ "$pid" != "" ]; then
               let MASTER_RUNNING_COUNT=MASTER_RUNNING_COUNT+1
	    fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $MASTER_RUNNING_COUNT
}

#
# Returns the number of active (or running) masters in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# @param clusterName   Cluster name.
# @param workspaceName Workspace name.
# @param rweName       RWE name.
#
function getVmActiveMasterCount
{
   # Masters
   local __CLUSTER=$1
   local __WORKSPACE=$2
   local __RWE=$3

   if  [ "$__CLUSTER" == "" ] || [ "$__WORKSPACE" == "" ] || [ "$__RWE" == "" ]; then
      return 0
   fi
   local MASTER
   local MASTER_COUNT=0
   local MASTER_RUNNING_COUNT=0
   local VM_HOSTS=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.master.hosts")
   for VM_HOST in ${VM_HOSTS}; do
      let MASTER_COUNT=MASTER_COUNT+1
      MASTER=`getVmMasterName $VM_HOST`
      pid=`getVmMasterPid $VM_HOST $MASTER $__WORKSPACE $__RWE`
      if [ "$pid" != "" ]; then
          let MASTER_RUNNING_COUNT=MASTER_RUNNING_COUNT+1
      fi
   done
   return $MASTER_RUNNING_COUNT
}

#
# Returns the number of active (or running) members in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# @param workspaceName Workspace name.
# @param clusterName   Cluster name.
# @param rweName       RWE name
#
function getVmActiveMemberCount
{
   # Members
   local __CLUSTER=$1
   local __WORKSPACE=$2
   local __RWE=$3

   if  [ "$__CLUSTER" == "" ] || [ "$__WORKSPACE" == "" ] || [ "$__RWE" == "" ]; then
      return 0
   fi
   local MEMBER
   local MEMBER_COUNT=0
   local MEMBER_RUNNING_COUNT=0
   local VM_HOSTS=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.hosts")
   for VM_HOST in ${VM_HOSTS}; do
      let MEMBER_COUNT=MEMBER_COUNT+1
      MEMBER=`getVmMemberName $VM_HOST`
      pid=`getVmMemberPid $VM_HOST $MEMBER $__WORKSPACE $__RWE`
      if [ "$pid" != "" ]; then
          let MEMBER_RUNNING_COUNT=MEMBER_RUNNING_COUNT+1
      fi
   done
   return $MEMBER_RUNNING_COUNT
}

#
# Returns the master name prefix that is used in constructing the unique master
# name for a given master number. See getMasterName.
# @required POD               Pod name.
# @required NODE_NAME_PREFIX  Node name prefix.
# @required CLUSTER           Cluster name.
#
function getMasterPrefix
{
   if [ "$POD" != "local" ]; then
      echo "${CLUSTER}-master-${NODE_NAME_PREFIX}-"
   else
      echo "${CLUSTER}-master-`hostname`-"
   fi
}

#
# Returns the unique master name (ID) for the specified master number.
# @required POD               Pod name.
# @required NODE_NAME_PREFIX  Node name prefix.
# @required CLUSTER           Cluster name.
# @param masterNumber
#
function getMasterName
{
   local __MASTER_NUM=`trimString $1`
   len=${#__MASTER_NUM}
   if [ $len == 1 ]; then
      __MASTER_NUM=0$__MASTER_NUM
   else
      __MASTER_NUM=$__MASTER_NUM
   fi
   echo "`getMasterPrefix`$__MASTER_NUM"
}

#
# Returns the master name of the specified VM host (address).
# @required VM_USER VM ssh user name
# @optional VM_KEY  VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host    VM host name or address
#
function getVmMasterName
{
   local __HOST=$1
   local __HOSTNAME=`ssh -n $VM_KEY $VM_USER@$__HOST -o LogLevel=error -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "hostname"`
   if [ "$__HOSTNAME" == "" ]; then
      echo ""
   else
      echo "${CLUSTER}-master-${__HOSTNAME}-01"
   fi
}

#
# Returns a list of all master directory names.
# @required RUN_DIR        Cluster run directory.
# @required MASTER_PREFIX Master name prefix
#
function getMasterDirNameList
{
   pushd $RUN_DIR > /dev/null 2>&1
   local __COUNT=0
   local __MASTERS=""
   for i in ${MASTER_PREFIX}*; do
      let __COUNT=__COUNT+1
      if [ $__COUNT -eq 1 ]; then
        __MASTERS="$i"
      else
         __MASTERS="$__MASTERS $i"
      fi
   done
   popd > /dev/null 2>&1
   echo $__MASTERS
}

#
# Returns the total number of masters added.
# @required RUN_DIR        Cluster run directory.
# @required MASTER_PREFIX  Master name prefix
#
function getMasterCount
{
   pushd $RUN_DIR > /dev/null 2>&1
   local __COUNT=0
   for i in ${MASTER_PREFIX}*; do
      if [ -d "$i" ]; then
         let __COUNT=__COUNT+1
      fi
   done
   popd > /dev/null 2>&1
   echo $__COUNT
}

#
# Returns a list of all master numbers including leading zero.
# @required RUN_DIR        Cluster run directory.
# @required MEMBER_PREFIX  Master name prefix
#
function getMasterNumList
{
   pushd $RUN_DIR > /dev/null 2>&1
   local __COUNT=0
   local __MASTERS=""
   for i in ${MASTER_PREFIX}*; do
      let __COUNT=__COUNT+1
      __NUM=${i:(-2)}
      if [ $__COUNT -eq 1 ]; then
        __MASTERS="$__NUM"
      else
         __MASTERS="$__MASTERS $__NUM"
      fi
   done
   popd > /dev/null 2>&1
   echo $__MASTERS
}

# 
# Returns a complete list of apps found in PADOGRID_HOME/$PRODUCT/apps
# @required PADOGRID_HOME
# @required PRODUCT
#
function getAddonApps {
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
      echo "$token"
   done
}
