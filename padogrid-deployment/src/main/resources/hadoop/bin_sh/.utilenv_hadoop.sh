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
# Spark Utility Functions. Do NOT modify!
# -----------------------------------------------------

#
# Returns the namenode number that includes the leading zero.
# @param namenodeNumber
#
function getNameNodeNumWithLeadingZero
{
   if [ $1 -lt 10 ]; then
      echo "0$1"
   else
      echo "$1"
   fi
}

#
# Returns the namenode PID if it is running. Empty value otherwise.
# @required NODE_LOCAL       Node name with the local extenstion. For remote call only.
# @required REMOTE_SPECIFIED false to invoke remotely, true to invoke locally.
# @param    namenodeName   Unique namenode name
# @param    workspaceName  Workspace name
#
function getNameNodePid
{
   local __NAMENODE=$1
   local __WORKSPACE=$2
   local __IS_GUEST_OS_NODE=`isGuestOs $NODE_LOCAL`
   local namenodes
   if [ "$__IS_GUEST_OS_NODE" == "true" ] && [ "$POD" != "local" ] && [ "$REMOTE_SPECIFIED" == "false" ]; then
      namenodes=`ssh -q -n $SSH_USER@$NODE_LOCAL -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -eo pid,comm,args | grep java | grep pado.vm.id=$__NAMENODE | grep padogrid.workspace=$__WORKSPACE" | awk '{print $1}'`
   else
      # Use eval to handle commands with spaces
      if [[ "$OS_NAME" == "CYGWIN"* ]]; then
         local namenodes="$(WMIC path win32_process get Caption,Processid,Commandline |grep java | grep pado.vm.id=$__NAMENODE | grep "padogrid.workspace=$__WORKSPACE" | awk '{print $(NF-1)}')"
      else
         local namenodes="$(ps -eo pid,comm,args | grep java | grep pado.vm.id=$__NAMENODE | grep padogrid.workspace=$__WORKSPACE | awk '{print $1}')"
      fi
   fi
   spids=""
   for j in $namenodes; do
      spids="$j $spids"
   done
   spids=`trimString $spids`
   echo $spids
}

#
# Returns the namenode PID of VM if it is running. Empty value otherwise.
# This function is for clusters running on VMs whereas the getNameNodePid
# is for pods running on the same machine.
# @required VM_USER        VM ssh user name
# @optional VM_KEY         VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host           VM host name or address
# @param    namenodeName   Unique namenode name
# @param    workspaceName  Workspace name
#
function getVmNameNodePid
{
   local __HOST=$1
   local __MEMBER=$2
   local __WORKSPACE=$3
   local namenodes=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "ps -eo pid,comm,args | grep java | grep pado.vm.id=$__MEMBER | grep padogrid.workspace=$__WORKSPACE" | awk '{print $1}'`
   spids=""
   for j in $namenodes; do
      spids="$j $spids"
   done
   spids=`trimString $spids`
   echo $spids
}

#
# Returns the number of active (or running) namenodes in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# This function works for both VM and non-VM workspaces.
# @param workspaceName Workspace name.
# @param clusterName   Cluster name.
#
function getActiveNameNodeCount
{
   # NameNodes
   local __WORKSPACE=$1
   local __CLUSTER=$2
   if [ "$__WORKSPACE" == "" ] || [ "$__CLUSTER" == "" ]; then
      echo 0
   fi
   local NAMENODE
   local let NAMENODE_COUNT=0
   local let NAMENODE_RUNNING_COUNT=0
   local VM_ENABLED=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.enabled")
   if [ "$VM_ENABLED" == "truen" ]; then
      local VM_HOSTS=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.namenode.hosts")
      for VM_HOST in ${VM_HOSTS}; do
         let NAMENODE_COUNT=NAMENODE_COUNT+1
         NAMENODE=`getVmNameNodeName $VM_HOST`
         pid=`getVmNameNodePid $VM_HOST $NAMENODE $__WORKSPACE`
         if [ "$pid" != "" ]; then
             let NAMENODE_RUNNING_COUNT=NAMENODE_RUNNING_COUNT+1
         fi
      done
   else
      local RUN_DIR=$PADOGRID_WORKSPACES_HOME/$__WORKSPACE/clusters/$__CLUSTER/run
      pushd $RUN_DIR > /dev/null 2>&1
      NAMENODE_PREFIX=$(getNameNodePrefix)
      for i in ${NAMENODE_PREFIX}*; do
         if [ -d "$i" ]; then
            NAMENODE=$i
            NAMENODE_NUM=${NAMENODE##$NAMENODE_PREFIX}
            let NAMENODE_COUNT=NAMENODE_COUNT+1
            pid=`getNameNodePid $NAMENODE $WORKSPACE`
            if [ "$pid" != "" ]; then
               let NAMENODE_RUNNING_COUNT=NAMENODE_RUNNING_COUNT+1
	    fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   echo $NAMENODE_RUNNING_COUNT
}

#
# Returns the number of active (or running) namenodes in the specified cluster.
# Returns 0 if the workspace name or cluster name is unspecified or invalid.
# @param workspaceName Workspace name.
# @param clusterName   Cluster name.
#
function getVmActiveNameNodeCount
{
   # NameNodes
   local __WORKSPACE=$1
   local __CLUSTER=$2
   if [ "$__WORKSPACE" == "" ] || [ "$__CLUSTER" == "" ]; then
      return 0
   fi
   local NAMENODE
   local NAMENODE_COUNT=0
   local NAMENODE_RUNNING_COUNT=0
   local VM_HOSTS=$(getWorkspaceClusterProperty $__WORKSPACE $__CLUSTER "vm.namenode.hosts")
   for VM_HOST in ${VM_HOSTS}; do
      let NAMENODE_COUNT=NAMENODE_COUNT+1
      NAMENODE=`getVmNameNodeName $VM_HOST`
      pid=`getVmNameNodePid $VM_HOST $NAMENODE $__WORKSPACE`
      if [ "$pid" != "" ]; then
          let NAMENODE_RUNNING_COUNT=NAMENODE_RUNNING_COUNT+1
      fi
   done
   return $NAMENODE_RUNNING_COUNT
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
# Returns Hadoop name prefix that is used in constructing a unique Hadoop component name.
# @param nodeName Node name. Valid values are namenode, datanode, .
# @required POD               Pod name.
# @required NODE_NAME_PREFIX  Node name prefix.
# @required CLUSTER           Cluster name.
#
function getHadoopPrefix
{
   local name=$1
   if [ "$POD" != "local" ]; then
      echo "${CLUSTER}-$name-${NODE_NAME_PREFIX}-"
   else
      echo "${CLUSTER}-$name-`hostname`-"
   fi
}

# Returns the namenode name prefix that is used in constructing the unique namenode
# name for a given namenode number. See getNameNodeName.
# @required POD               Pod name.
# @required NODE_NAME_PREFIX  Node name prefix.
# @required CLUSTER           Cluster name.
#
function getNameNodePrefix
{
   echo $(getHadoopPrefix namenode)
}

#
# Returns the unique namenode name (ID) for the specified namenode number.
# @required POD               Pod name.
# @required NODE_NAME_PREFIX  Node name prefix.
# @required CLUSTER           Cluster name.
# @param namenodeNumber
#
function getNameNodeName
{
   local __NAMENODE_NUM=`trimString $1`
   len=${#__NAMENODE_NUM}
   if [ $len == 1 ]; then
      __NAMENODE_NUM=0$__NAMENODE_NUM
   else
      __NAMENODE_NUM=$__NAMENODE_NUM
   fi
   echo $(getHadoopPrefix namenode)$__NAMENODE_NUM
}

#
# Returns the namenode name of the specified VM host (address).
# @required VM_USER VM ssh user name
# @optional VM_KEY  VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host    VM host name or address
#
function getVmNameNodeName
{
   local __HOST=$1
   local __HOSTNAME=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no -o connecttimeout=$SSH_CONNECT_TIMEOUT "hostname"`
   if [ "$__HOSTNAME" == "" ]; then
      echo ""
   else
      echo "${CLUSTER}-namenode-${__HOSTNAME}-01"
   fi
}

#
# Returns merged comma-separated list of VM namenode and member hosts
# @required  CLUSTERS_DIR  Cluster directory path.
# @required  CLUSTER       Cluster name.
#
function getAllMergedVmHosts
{
   local VM_NAMENODE_HOSTS=$(getClusterProperty "vm.namenode.hosts")
   local VM_HOSTS=$(getClusterProperty "vm.hosts")
   if [ "$VM_NAMENODE_HOSTS" != "" ]; then
      # Replace , with space
      __VM_NAMENODE_HOSTS=$(echo "$VM_NAMENODE_HOSTS" | sed "s/,/ /g")
      __VM_HOSTS=$(echo "$VM_HOSTS" | sed "s/,/ /g")
      for i in $__VM_NAMENODE_HOSTS; do
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
# Returns a list of all namenode directory names.
# @required RUN_DIR        Cluster run directory.
# @required NAMENODE_PREFIX NameNode name prefix
#
function getNameNodeDirNameList
{
   pushd $RUN_DIR > /dev/null 2>&1
   local __COUNT=0
   local __NAMENODES=""
   for i in ${NAMENODE_PREFIX}*; do
      let __COUNT=__COUNT+1
      if [ $__COUNT -eq 1 ]; then
        __NAMENODES="$i"
      else
         __NAMENODES="$__NAMENODES $i"
      fi
   done
   popd > /dev/null 2>&1
   echo $__NAMENODES
}

#
# Returns the total number of namenodes added.
# @required RUN_DIR        Cluster run directory.
# @required NAMENODE_PREFIX  NameNode name prefix
#
function getNameNodeCount
{
   pushd $RUN_DIR > /dev/null 2>&1
   local __COUNT=0
   for i in ${NAMENODE_PREFIX}*; do
      if [ -d "$i" ]; then
         let __COUNT=__COUNT+1
      fi
   done
   popd > /dev/null 2>&1
   echo $__COUNT
}

#
# Returns a list of all namenode numbers including leading zero.
# @required RUN_DIR        Cluster run directory.
# @required MEMBER_PREFIX  NameNode name prefix
#
function getNameNodeNumList
{
   pushd $RUN_DIR > /dev/null 2>&1
   local __COUNT=0
   local __NAMENODES=""
   for i in ${NAMENODE_PREFIX}*; do
      let __COUNT=__COUNT+1
      __NUM=${i:(-2)}
      if [ $__COUNT -eq 1 ]; then
        __NAMENODES="$__NUM"
      else
         __NAMENODES="$__NAMENODES $__NUM"
      fi
   done
   popd > /dev/null 2>&1
   echo $__NAMENODES
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
