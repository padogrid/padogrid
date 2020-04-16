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
# Returns the member PID if it is running. Empty value otherwise.
# @required NODE_LOCAL     Node name with the local extenstion. For remote call only.
# @param    memberName     Unique member name
# @param    workspaceName  Workspace name
#
function getMemberPid
{
   __MEMBER=$1
   __WORKSPACE=$2
   __IS_GUEST_OS_NODE=`isGuestOs $NODE_LOCAL`
   if [ "$__IS_GUEST_OS_NODE" == "true" ] && [ "$POD" != "local" ] && [ "$REMOTE_SPECIFIED" == "false" ]; then
     members=`ssh -q -n $SSH_USER@$NODE_LOCAL -o stricthostkeychecking=no "$JAVA_HOME/bin/jps -v | grep hazelcast.instance.name=$__MEMBER | grep padogrid.workspace=$__WORKSPACE" | awk '{print $1}'`
   else
     members=`"$JAVA_HOME/bin/jps" -v | grep "hazelcast.instance.name=$__MEMBER" | grep "padogrid.workspace=$__WORKSPACE" | awk '{print $1}'`
   fi
   spids=""
   for j in $members; do
      spids="$j $spids"
   done
   spids=`trimString $spids`
   echo $spids
}

#
# Returns the member PID of VM if it is running. Empty value otherwise.
# This function is for clusters runnin on VMs whereas the getMemberPid
# is for pods runnin on the same machine.
# @required VM_USER        VM ssh user name
# @optional VM_KEY         VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host           VM host name or address
# @param    memberName     Unique member name
# @param    workspaceName  Workspace name
#
function getVmMemberPid
{
   __HOST=$1
   __MEMBER=$2
   __WORKSPACE=$3
   members=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no "$VM_JAVA_HOME/bin/jps -v | grep hazelcast.instance.name=$__MEMBER | grep padogrid.workspace=$__WORKSPACE" | awk '{print $1}'`
   spids=""
   for j in $members; do
      spids="$j $spids"
   done
   spids=`trimString $spids`
   echo $spids
}

#
# Returns the management center PID if it is running.
# @param mcName         Unique management center name
# @param workspaceName  Workspace name
#
function getMcPid
{
   __MC=$1
   __WORKSPACE=$2
   mcs=`jps -v | grep "hazelcast.mc.name=$__MC" | grep "padogrid.workspace=$__WORKSPACE" | awk '{print $1}'`
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
#
function getVmMcPid
{
   __HOST=$1
   __MEMBER=$2
   __WORKSPACE=$3
      members=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no "$VM_JAVA_HOME/bin/jps -v | grep hazelcast.mc.name=$__MC | grep padogrid.workspace=$__WORKSPACE" | awk '{print $1}'`
   spids=""
   for j in $members; do
      spids="$j $spids"
   done
   spids=`trimString $spids`
   echo $spids
}

#
# Returns the member name prefix that is used in constructing the unique member
# name for a given member number. See getMemberName.
# @required POD               Pod name.
# @required NODE_NAME_PREFIX  Node name prefix.
# @required CLUSTER           Cluster name.
#
function getMemberPrefix
{
   if [ "$POD" != "local" ]; then
      echo "${CLUSTER}-${NODE_NAME_PREFIX}-"
   else
      echo "${CLUSTER}-`hostname`-"
   fi
}

#
# Returns the member name of the specified VM host (address).
# @required VM_USER VM ssh user name
# @optional VM_KEY  VM private key file path with -i prefix, e.g., "-i file.pem"
# @param    host    VM host name or address
#
function getVmMemberName
{
   __HOST=$1
   __HOSTNAME=`ssh -q -n $VM_KEY $VM_USER@$__HOST -o stricthostkeychecking=no "hostname"`
   echo "${CLUSTER}-${__HOSTNAME}-01"
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
      __APPS="jet_demo"
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
