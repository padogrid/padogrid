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
# Redis Utility Functions. Do NOT modify!
# -----------------------------------------------------

#
# Returns the member PID if it is running. Empty value otherwise.
# @required POD
# @required REMOTE_SPECIFIED
# @required NODE_LOCAL     Node name with the local extenstion. For remote call only.
# @param    port           Port number
#
function getRedisMemberPid
{
   local __MEMBER_PORT="$1"
   if [ "$__MEMBER_PORT" == "" ]; then
     echo ""
     return
   fi
   local __IS_GUEST_OS_NODE=`isGuestOs $NODE_LOCAL`

   if [ "$__IS_GUEST_OS_NODE" == "true" ] && [ "$POD" != "local" ] && [ "$REMOTE_SPECIFIED" == "false" ]; then
      pid=`ssh -q -n $SSH_USER@$NODE_LOCAL -o stricthostkeychecking=no "ps -opid,command |grep redis-server |grep $__MEMBER_PORT | grep -v grep | awk '{print $1}'"`
   else
      # Use eval to handle commands with spaces
      pid=$(ps -opid,command |grep redis-server |grep $__MEMBER_PORT | grep -v grep | awk '{print $1}')
   fi
   echo $pid
}

#
# Returns the member PID of VM if it is running. Empty value otherwise.
# This function is for clusters running on VMs whereas the getMemberPid
# is for pods running on the same machine.
# @required VM_USER        VM ssh user name
# @optional VM_KEY         VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host           VM host name or address
# @param    port           Port number
#
function getRedisVmMemberPid
{
   local __HOST=$1
   local __MEMBER_PORT="$2"
   members=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no "ps -opid,command |grep redis-server |grep $__MEMBER_PORT | grep -v grep | awk '{print $1}'"`
   echo $pid
}

#
# Returns the number of active (or running) members in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# This function works for both VM and non-VM workspaces.
# @required POD
# @required REMOTE_SPECIFIED
# @required NODE_LOCAL     Node name with the local extenstion. For remote call only.
# @param workspaceName Workspace name. Optional.
# @param clusterName   Cluster name. Optional.
#
function getRedisActiveMemberCount
{
   # Members
   local __WORKSPACE="$1"
   local __CLUSTER="$2"
   if [ "$__WORKSPACE" == "" ]; then
      __WORKSPACE="$PADOGRID_WORKSPACE"
   fi
   if [ "$__CLUSTER" == "" ]; then
      __CLUSTER="$CLUSTER"
   fi

   local MEMBER
   local MEMBER_PORT
   local MEMBER_NUM_NO_LEADING_ZERO
   local MEMBER_COUNT=0
   local MEMBER_RUNNING_COUNT=0
   local MEMBER_START_PORT=`getClusterProperty "tcp.startPort" $DEFAULT_MEMBER_START_PORT`
   local VM_ENABLED=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.enabled")
   if [ "$VM_ENABLED" == "true" ]; then
      local VM_HOSTS=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.hosts")
      let MEMBER_PORT=MEMBER_START_PORT
      for VM_HOST in ${VM_HOSTS}; do
         let MEMBER_COUNT=MEMBER_COUNT+1
         pid=`getRedisVmMemberPid $VM_HOST $MEMBER_PORT`
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
            MEMBER_NUM_NO_LEADING_ZERO=$((10#$MEMBER_NUM))
            let MEMBER_PORT=MEMBER_START_PORT+MEMBER_NUM_NO_LEADING_ZERO-1
            let MEMBER_COUNT=MEMBER_COUNT+1
            pid=`getRedisMemberPid $MEMBER_PORT`
            if [ "$pid" != "" ]; then
               let MEMBER_RUNNING_COUNT=MEMBER_RUNNING_COUNT+1
       fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $MEMBER_RUNNING_COUNT
}
