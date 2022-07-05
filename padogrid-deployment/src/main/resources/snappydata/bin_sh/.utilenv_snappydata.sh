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
# Geode/GemFire Utility Functions. Do NOT modify!
# -----------------------------------------------------

#
# Returns the locator number that includes the leading zero.
# @param locatorNumber
#
function getLocatorNumWithLeadingZero
{
   if [ $1 -lt 10 ]; then
      echo "0$1"
   else
      echo "$1"
   fi
}

#
# Returns the locator PID if it is running. Empty value otherwise.
# @required NODE_LOCAL       Node name with the local extenstion. For remote call only.
# @required REMOTE_SPECIFIED false to invoke remotely, true to invoke locally.
# @param    locatorName      Unique locator name
# @param    workspaceName    Workspace name
#
function getLocatorPid
{
   local __LOCATOR=$1
   local __WORKSPACE=$2
   local __IS_GUEST_OS_NODE=`isGuestOs $NODE_LOCAL`
   local locators
   if [ "$__IS_GUEST_OS_NODE" == "true" ] && [ "$POD" != "local" ] && [ "$REMOTE_SPECIFIED" == "false" ]; then
      locators=`ssh -q -n $SSH_USER@$NODE_LOCAL -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -eo pid,comm,args | grep java | grep pado.vm.id=$__LOCATOR | grep padogrid.workspace=$__WORKSPACE" | awk '{print $1}'`
   else
      if [[ "$OS_NAME" == "CYGWIN"* ]]; then
         local locators="$(WMIC path win32_process get Caption,Processid,Commandline |grep java | grep pado.vm.id=$__LOCATOR | grep "padogrid.workspace=$__WORKSPACE" | awk '{print $(NF-1)}')"
      else
         local locators="$(ps -eo pid,comm,args | grep java | grep pado.vm.id=$__LOCATOR | grep padogrid.workspace=$__WORKSPACE | awk '{print $1}')"
      fi
   fi
   spids=""
   for j in $locators; do
      spids="$j $spids"
   done
   spids=`trimString $spids`
   echo $spids
}

#
# Returns the leader PID if it is running. Empty value otherwise.
# @required NODE_LOCAL     Node name with the local extenstion. For remote call only.
# @param    leaderName     Unique leader name
# @param    workspaceName  Workspace name
#
function getLeaderPid
{
   getLocatorPid $@
}

#
# Returns the locator PID of VM if it is running. Empty value otherwise.
# This function is for clusters running on VMs whereas the getLocatorPid
# is for pods running on the same machine.
# @required VM_USER        VM ssh user name
# @optional VM_KEY         VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host           VM host name or address
# @param    locatorName    Unique locator name
# @param    workspaceName  Workspace name
#
function getVmLocatorPid
{
   local __HOST=$1
   local __MEMBER=$2
   local __WORKSPACE=$3
   local locators=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -eo pid,comm,args | grep java | grep pado.vm.id=$__MEMBER | grep padogrid.workspace=$__WORKSPACE" | awk '{print $1}'`
   spids=""
   for j in $locators; do
      spids="$j $spids"
   done
   spids=`trimString $spids`
   echo $spids
}

#
# Returns the leader PID of VM if it is running. Empty value otherwise.
# This function is for clusters running on VMs whereas the getLeaderPid
# is for pods running on the same machine.
# @required VM_USER        VM ssh user name
# @optional VM_KEY         VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host           VM host name or address
# @param    leaderName     Unique leader name
# @param    workspaceName  Workspace name
#
function getVmLeaderPid
{
   local __HOST=$1
   local __MEMBER=$2
   local __WORKSPACE=$3
   local locators=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -eo pid,comm,args | grep java | grep pado.vm.id=$__MEMBER | grep padogrid.workspace=$__WORKSPACE" | awk '{print $1}'`
   spids=""
   for j in $locators; do
      spids="$j $spids"
   done
   spids=`trimString $spids`
   echo $spids
}

#
# Returns the number of active (or running) locators in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# This function works for both VM and non-VM workspaces.
# @param workspaceName Workspace name.
# @param clusterName   Cluster name.
#
function getActiveLocatorCount
{
   # Locators
   local __WORKSPACE=$1
   local __CLUSTER=$2
   if [ "$__WORKSPACE" == "" ] || [ "$__CLUSTER" == "" ]; then
      echo 0
   fi
   local LOCATOR
   local let LOCATOR_COUNT=0
   local let LOCATOR_RUNNING_COUNT=0
   local VM_ENABLED=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.enabled")
   if [ "$VM_ENABLED" == "truen" ]; then
      local VM_HOSTS=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.locator.hosts")
      for VM_HOST in ${VM_HOSTS}; do
         let LOCATOR_COUNT=LOCATOR_COUNT+1
         LOCATOR=`getVmLocatorName $VM_HOST`
         pid=`getVmLocatorPid $VM_HOST $LOCATOR $__WORKSPACE`
         if [ "$pid" != "" ]; then
             let LOCATOR_RUNNING_COUNT=LOCATOR_RUNNING_COUNT+1
         fi
      done
   else
      local RUN_DIR=$PADOGRID_WORKSPACES_HOME/$__WORKSPACE/clusters/$__CLUSTER/run
      pushd $RUN_DIR > /dev/null 2>&1
      LOCATOR_PREFIX=$(getLocatorPrefix)
      for i in ${LOCATOR_PREFIX}*; do
         if [ -d "$i" ]; then
            LOCATOR=$i
            LOCATOR_NUM=${LOCATOR##$LOCATOR_PREFIX}
            let LOCATOR_COUNT=LOCATOR_COUNT+1
            pid=`getLocatorPid $LOCATOR $WORKSPACE`
            if [ "$pid" != "" ]; then
               let LOCATOR_RUNNING_COUNT=LOCATOR_RUNNING_COUNT+1
	    fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $LOCATOR_RUNNING_COUNT
}

#
# Returns the number of active (or running) leaders in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# This function works for both VM and non-VM workspaces.
# @param workspaceName Workspace name.
# @param clusterName   Cluster name.
#
function getActiveLeaderCount
{
   # Locators
   local __WORKSPACE=$1
   local __CLUSTER=$2
   if [ "$__WORKSPACE" == "" ] || [ "$__CLUSTER" == "" ]; then
      echo 0
   fi
   local LEADER
   local let LEADER_COUNT=0
   local let LEADER_RUNNING_COUNT=0
   local VM_ENABLED=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.enabled")
   if [ "$VM_ENABLED" == "truen" ]; then
      local VM_HOSTS=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.locator.hosts")
      for VM_HOST in ${VM_HOSTS}; do
         let LEADER_COUNT=LEADER_COUNT+1
         LEADER=`getVmLeaderName $VM_HOST`
         pid=`getVmLeaderPid $VM_HOST $LEADER $__WORKSPACE`
         if [ "$pid" != "" ]; then
             let LEADER_RUNNING_COUNT=LEADER_RUNNING_COUNT+1
         fi
      done
   else
      local RUN_DIR=$PADOGRID_WORKSPACES_HOME/$__WORKSPACE/clusters/$__CLUSTER/run
      pushd $RUN_DIR > /dev/null 2>&1
      LEADER_PREFIX=$(getLeaderPrefix)
      for i in ${LEADER_PREFIX}*; do
         if [ -d "$i" ]; then
            LEADER=$i
            LEADER_NUM=${LEADER##$LEADER_PREFIX}
            let LEADER_COUNT=LEADER_COUNT+1
            pid=`getLeaderPid $LEADER $WORKSPACE`
            if [ "$pid" != "" ]; then
               let LEADER_RUNNING_COUNT=LEADER_RUNNING_COUNT+1
	    fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $LEADER_RUNNING_COUNT
}

#
# Returns the number of active (or running) locators in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# @param workspaceName Workspace name.
# @param clusterName   Cluster name.
#
function getVmActiveLocatorCount
{
   # Locators
   local __WORKSPACE=$1
   local __CLUSTER=$2
   if [ "$__WORKSPACE" == "" ] || [ "$__CLUSTER" == "" ]; then
      return 0
   fi
   local LOCATOR
   local LOCATOR_COUNT=0
   local LOCATOR_RUNNING_COUNT=0
   local VM_HOSTS=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.locator.hosts")
   for VM_HOST in ${VM_HOSTS}; do
      let LOCATOR_COUNT=LOCATOR_COUNT+1
      LOCATOR=`getVmLocatorName $VM_HOST`
      pid=`getVmLocatorPid $VM_HOST $LOCATOR $__WORKSPACE`
      if [ "$pid" != "" ]; then
          let LOCATOR_RUNNING_COUNT=LOCATOR_RUNNING_COUNT+1
      fi
   done
   return $LOCATOR_RUNNING_COUNT
}

#
# Returns the number of active (or running) members in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# @param workspaceName Workspace name.
# @param clusterName   Cluster name.
#
function getVmActiveMemberCount
{
   # Members
   local __WORKSPACE=$1
   local __CLUSTER=$2
   if [ "$__WORKSPACE" == "" ] || [ "$__CLUSTER" == "" ]; then
      return 0
   fi
   local MEMBER
   local MEMBER_COUNT=0
   local MEMBER_RUNNING_COUNT=0
   local VM_HOSTS=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.hosts")
   for VM_HOST in ${VM_HOSTS}; do
      let MEMBER_COUNT=MEMBER_COUNT+1
      MEMBER=`getVmMemberName $VM_HOST`
      pid=`getVmMemberPid $VM_HOST $MEMBER $__WORKSPACE`
      if [ "$pid" != "" ]; then
          let MEMBER_RUNNING_COUNT=MEMBER_RUNNING_COUNT+1
      fi
   done
   return $MEMBER_RUNNING_COUNT
}

#
# Returns the locator name prefix that is used in constructing the unique locator
# name for a given locator number. See getLocatorName.
# @required POD               Pod name.
# @required NODE_NAME_PREFIX  Node name prefix.
# @required CLUSTER           Cluster name.
#
function getLocatorPrefix
{
   if [ "$POD" != "local" ]; then
      echo "${CLUSTER}-locator-${NODE_NAME_PREFIX}-"
   else
      echo "${CLUSTER}-locator-`hostname`-"
   fi
}

#
# Returns the leader name prefix that is used in constructing the unique leader
# name for a given leader number. See getLeaderName.
# @required POD               Pod name.
# @required NODE_NAME_PREFIX  Node name prefix.
# @required CLUSTER           Cluster name.
#
function getLeaderPrefix
{
   if [ "$POD" != "local" ]; then
      echo "${CLUSTER}-leader-${NODE_NAME_PREFIX}-"
   else
      echo "${CLUSTER}-leader-`hostname`-"
   fi
}

#
# Returns the unique locator name (ID) for the specified locator number.
# @required POD               Pod name.
# @required NODE_NAME_PREFIX  Node name prefix.
# @required CLUSTER           Cluster name.
# @param locatorNumber
#
function getLocatorName
{
   local __LOCATOR_NUM=`trimString $1`
   len=${#__LOCATOR_NUM}
   if [ $len == 1 ]; then
      __LOCATOR_NUM=0$__LOCATOR_NUM
   else
      __LOCATOR_NUM=$__LOCATOR_NUM
   fi
   echo "`getLocatorPrefix`$__LOCATOR_NUM"
}

#
# Returns the unique leader name (ID) for the specified leader number.
# @param leaderNumber
#
function getLeaderName
{
   local __LEADER_NUM=`trimString $1`
   len=${#__LEADER_NUM}
   if [ $len == 1 ]; then
      __LEADER_NUM=0$__LEADER_NUM
   else
      __LEADER_NUM=$__LEADER_NUM
   fi
   echo "`getLeaderPrefix`$__LEADER_NUM"
}

#
# Returns the locator name of the specified VM host (address).
# @required VM_USER VM ssh user name
# @optional VM_KEY  VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host    VM host name or address
#
function getVmLocatorName
{
   local __HOST=$1
   local __HOSTNAME=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "hostname"`
   if [ "$__HOSTNAME" == "" ]; then
      echo ""
   else
      echo "${CLUSTER}-locator-${__HOSTNAME}-01"
   fi
}

#
# Returns the leader name of the specified VM host (address).
# @required VM_USER VM ssh user name
# @optional VM_KEY  VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host    VM host name or address
#
function getVmLeaderName
{
   __HOST=$1
   __HOSTNAME=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "hostname"`
   if [ "$__HOSTNAME" == "" ]; then
      echo ""
   else
      echo "${CLUSTER}-leader-${__HOSTNAME}-01"
   fi
}

#
# Returns merged comma-separated list of VM locator and member hosts
# @required  CLUSTERS_DIR  Cluster directory path.
# @required  CLUSTER       Cluster name.
#
function getAllMergedVmHosts
{
   local VM_LOCATOR_HOSTS=$(getClusterProperty "vm.locator.hosts")
   local VM_HOSTS=$(getClusterProperty "vm.hosts")
   if [ "$VM_LOCATOR_HOSTS" != "" ]; then
      # Replace , with space
      __VM_LOCATOR_HOSTS=$(echo "$VM_LOCATOR_HOSTS" | sed "s/,/ /g")
      __VM_HOSTS=$(echo "$VM_HOSTS" | sed "s/,/ /g")
      for i in $__VM_LOCATOR_HOSTS; do
         found=false
         for j in $__VM_HOSTS; do
            if [ "$i" == "$j" ]; then
               found=true
            fi
	 done
	 if [ "$found" == "false" ]; then
            VM_HOSTS="$VM_HOSTS,$i"
         fi
      done
   fi
   echo $VM_HOSTS
}

#
# Returns a list of all locator directory names.
# @required RUN_DIR        Cluster run directory.
# @required LOCATOR_PREFIX  Locator name prefix
#
function getLocatorDirNameList
{
   pushd $RUN_DIR > /dev/null 2>&1
   local __COUNT=0
   local __LOCATORS=""
   for i in ${LOCATOR_PREFIX}*; do
      let __COUNT=__COUNT+1
      if [ $__COUNT -eq 1 ]; then
        __LOCATORS="$i"
      else
         __LOCATORS="$__LOCATORS $i"
      fi
   done
   popd > /dev/null 2>&1
   echo $__LOCATORS
}

#
# Returns the total number of locators added.
# @required RUN_DIR        Cluster run directory.
# @required LOCATOR_PREFIX  Locator name prefix
#
function getLocatorCount
{
   pushd $RUN_DIR > /dev/null 2>&1
   local __COUNT=0
   for i in ${LOCATOR_PREFIX}*; do
      if [ -d "$i" ]; then
         let __COUNT=__COUNT+1
      fi
   done
   popd > /dev/null 2>&1
   echo $__COUNT
}

#
# Returns a list of all locator numbers including leading zero.
# @required RUN_DIR        Cluster run directory.
# @required MEMBER_PREFIX  Locator name prefix
#
function getLocatorNumList
{
   pushd $RUN_DIR > /dev/null 2>&1
   local __COUNT=0
   local __LOCATORS=""
   for i in ${LOCATOR_PREFIX}*; do
      let __COUNT=__COUNT+1
      __NUM=${i:(-2)}
      if [ $__COUNT -eq 1 ]; then
        __LOCATORS="$__NUM"
      else
         __LOCATORS="$__LOCATORS $__NUM"
      fi
   done
   popd > /dev/null 2>&1
   echo $__LOCATORS
}

#
# Returns the total number of leaders added.
# @required RUN_DIR        Cluster run directory.
# @required LEADER_PREFIX  Leader name prefix
#
function getLeaderCount
{
   pushd $RUN_DIR > /dev/null 2>&1
   local __COUNT=0
   for i in ${LEADER_PREFIX}*; do
      if [ -d "$i" ]; then
         let __COUNT=__COUNT+1
      fi
   done
   popd > /dev/null 2>&1
   echo $__COUNT
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
