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
# Mosquitto Utility Functions. Do NOT modify!
# -----------------------------------------------------

#
# Returns the member PID of VM if it is running. Empty value otherwise.
# This function is for clusters running on VMs whereas the getMemberPid
# is for pods running on the same machine.
# @required VM_USER        VM ssh user name
# @optional VM_KEY         VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host           VM host name or address
# @param    port           Port number
#
function getVmMosquittoMemberPid
{
   local __HOST="$1"
   local __MEMBER_PORT="$2"
   local CONF_FILE="$ETC_DIR/mosquitto.conf"
   local pid=`ssh -n $VM_KEY $VM_USER@$__HOST -o LogLevel=error -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -ewwwo pid,comm,args | grep mosquitto | grep \"\-p $__MEMBER_PORT\" | grep \"$CONF_FILE\" | grep -v grep | awk '{print \\$1}'"`
   echo $pid
}

#
# Returns the number of active (or running) members in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# This function works for both VM and non-VM workspaces.
# @required POD
# @required REMOTE_SPECIFIED
# @required NODE_LOCAL Node name with the local extenstion. For remote call only.
# @param workspaceName Workspace name. Optional.
# @param clusterName   Cluster name. Optional.
#
function getMosquittoActiveMemberCount
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
         pid=`getVmMosquittoMemberPid $VM_HOST $MEMBER_PORT`
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
            pid=`getMosquittoMemberPid $MEMBER_NUM`
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
# Returns the number of active (or running) members in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# This function works for both VM and non-VM workspaces.
# @param workspaceName Workspace name.
# @param clusterName   Cluster name.
#
function getActiveMemberCount
{
   getMosquittoActiveMemberCount "$@"
}

#
# Returns any running Mosquitto server that matches the specified member number's port number.
# The returned PID does not necessarily represent the cluster's member PID. It might be
# another cluster's member PID.
#
# @param memberNumber Member number. If unspecified, then 1 is assigned. Leading zero (0) allowed.
#
function getMosquittoServerPortPid
{
   local MEMBER_NUM=$1
   if [ "$MEMBER_NUM" == "" ]; then
      MEMBER_NUM="1"
   fi
   local MEMBER_NUM_NO_LEADING_ZERO=$((10#$MEMBER_NUM))
   local MEMBER_START_PORT=`getClusterProperty "tcp.startPort" $DEFAULT_MEMBER_START_PORT`
   local MEMBER_PORT
   let MEMBER_PORT=MEMBER_START_PORT+MEMBER_NUM_NO_LEADING_ZERO-1
   local pid=$(ps -ewwwo pid,comm,args | grep mosquitto | grep "\-p $MEMBER_PORT" | grep -v grep | awk '{print $1}')
   echo $pid
}

#
# Returns the current cluster's member PID if it is running. Empty value otherwise.
# @required NODE_LOCAL       Node name with the local extension. For remote call only.
# @optional POD              Pod type. Default: local
# @optional REMOTE_SPECIFIED true if remote node, false if local node. Default: false
# @param    memberNumber     Member number. If not specified then the first member, i.e.,
#                            1, is assigned. Optional.
# @param    workspaceName    Workspace name. If not specified, then the current workspace
#                            is assumed. This parameter is currently used for remote calls.
#                            Optional.
#
function getMosquittoMemberPid
{
   local __MEMBER_NUM=$1
   local __WORKSPACE="$2"
   if [ "$__MEMBER_NUM" == "" ]; then
      __MEMBER_NUM="1"
   fi
   # Remove leading zero
   __MEMBER_NUM=$((10#$__MEMBER_NUM))
   if [ "$__WORKSPACE" == "" ]; then
      __WORKSPACE="$PADOGRID_WORKSPACE"
   fi
   local __MEMBER=`getMemberName $__MEMBER_NUM`
   local MEMBER_DIR=$RUN_DIR/$__MEMBER
   local __MEMBER_START_PORT=`getClusterProperty "tcp.startPort" $DEFAULT_MEMBER_START_PORT`
   local __MEMBER_PORT
   let __MEMBER_PORT=__MEMBER_START_PORT+__MEMBER_NUM-1
   local __IS_GUEST_OS_NODE=`isGuestOs $NODE_LOCAL`
   local CONF_FILE="$ETC_DIR/mosquitto.conf"

   if [ "$__IS_GUEST_OS_NODE" == "true" ] && [ "$POD" != "local" ] && [ "$REMOTE_SPECIFIED" == "false" ]; then
     __MEMBER_PORT=$__MEMBER_START_PORT
     pid=$(ssh -n $SSH_USER@$NODE_LOCAL -o LogLevel=error -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -ewwwo pid,comm,args | grep mosquitto | grep \"\-p $__MEMBER_PORT\" | grep \"$CONF_FILE\" | grep -v grep")
      pid=$(echo $pid | awk '{print $1}')
   else
      if [ "$POD" != "local" ]; then
         __MEMBER_PORT=$__MEMBER_START_PORT
      fi
      pid=$(ps -ewwwo pid,comm,args | grep mosquitto | grep "\-p $__MEMBER_PORT" | grep "$CONF_FILE" | grep -v grep | awk '{print $1}')
   fi
   echo $pid
}

#
# Returns the current cluster's member PID if it is running. Empty value otherwise.
# @required NODE_LOCAL       Node name with the local extension. For remote call only.
# @optional POD              Pod type. Default: local
# @optional REMOTE_SPECIFIED true if remote node, false if local node. Default: false
# @param    memberName       Unique member name
# @param    workspaceName    Workspace name. If not specified, then the current workspace
#                            is assumed. This parameter is currently used for remote calls.
#                            Optional.
#
function getMemberPid
{
  local MEMBER_NUMBER=$(getMemberNumber "$1")
  if [ "$MEMBER_NUMBER" != "" ]; then
     getMosquittoMemberPid "$MEMBER_NUMBER" "$2"
  else
     echo ""
  fi
}

#
# Returns the first live Mosquitto node (host:port) in the current cluster.
#
# @required CLUSTER
# @required RUN_DIR
# @required NODE_LOCAL       Node name with the local extension. For remote call only.
#
function getMosquittoFirstLiveNode
{
   local NODE=""
   local MEMBER_START_PORT=`getClusterProperty "tcp.startPort" $DEFAULT_MEMBER_START_PORT`
   local MEMBER_PREFIX=`getMemberPrefix`
   local MEMBER_PREFIX_LEN=${#MEMBER_PREFIX}
   local HOST_NAME=`hostname`
   local BIND_ADDRESS=`getClusterProperty "cluster.bindAddress" "$HOST_NAME"`
   local __IS_GUEST_OS_NODE=`isGuestOs $NODE_LOCAL`
   pushd ${RUN_DIR} > /dev/null 2>&1
   for i in ${MEMBER_PREFIX}*; do
      if [ -d "$i" ]; then
         let COUNT=COUNT+1
         local MEMBER_NUM=${i:$MEMBER_PREFIX_LEN}
         local PID=`getMosquittoMemberPid $MEMBER_NUM`
         if [ "$PID" != "" ]; then
            if [ "$__IS_GUEST_OS_NODE" == "true" ] && [ "$POD" != "local" ]; then
               MEMBER_PORT=$MEMBER_START_PORT
               NODE=$NODE_LOCAL:$MEMBER_PORT
            else
               local MEMBER_NUM_NO_LEADING_ZERO=$((10#$MEMBER_NUM))
               let MEMBER_PORT=MEMBER_START_PORT+MEMBER_NUM_NO_LEADING_ZERO-1
               NODE=$BIND_ADDRESS:$MEMBER_PORT
            fi
            break; 
         fi
      fi
   done 
   popd > /dev/null 2>&1
   echo "$NODE"
}
