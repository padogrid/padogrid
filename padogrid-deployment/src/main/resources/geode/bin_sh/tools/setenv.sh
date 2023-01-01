#!/usr/bin/env bash

# ========================================================================
# Copyright (c) 2020-2023 Netcrest Technologies, LLC. All rights reserved.
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

LOCATOR_FILES="*locator*.log"
SERVER_FILES="$CLUSTER-node*.log"
ALL_FILES="*.log"
LOCATOR_START_PORT=`getClusterProperty "locator.tcp.startPort" $DEFAULT_LOCATOR_START_PORT`
VM_ENABLED=`getClusterProperty "vm.enabled" "false"`

# LOCATOR_HOST - Loator host used by revoke_all_missing_disk_stores
if [ "$VM_ENABLED" == "true" ]; then
   VM_LOCATOR_HOSTS=$(getClusterProperty "vm.locator.hosts")
   VM_LOCATORS=$(echo "$VM_LOCATOR_HOSTS" | sed "s/,/ /g")
   VM_HOSTS=$(getClusterProperty "vm.hosts")
   __ALL_NODES=$(echo "$VM_HOSTS" | sed "s/,/ /g")
   __ALL_NODES="$__ALL_NODES $VM_LOCATORS"

   for i in $VM_LOCATORS; do
      LOCATOR_HOST=$i
      break;
   done

else
   RUN_DIR=$CLUSTERS_DIR/$CLUSTER/run
   POD=`getClusterProperty "pod.name" "local"`
   POD_TYPE=`getClusterProperty "pod.type" $POD_TYPE`
   NODE_NAME_PREFIX=`getPodProperty "node.name.prefix" $DEFAULT_NODE_NAME_PREFIX`

   LOCATOR_HOSTNAME_FOR_CLIENTS=`getClusterProperty "cluster.hostnameForClients" "$NODE"`
   LOCATOR_HOST=$LOCATOR_HOSTNAME_FOR_CLIENTS
   LOCATOR_PREFIX=`getLocatorPrefix`
   pushd $RUN_DIR > /dev/null 2>&1
   LOCATORS=""
   for i in ${LOCATOR_PREFIX}*; do
      if [ -d "$i" ]; then
         LOCATORS="$LOCATORS $i"
      fi
   done
   MEMBER_PREFIX=`getMemberPrefix`
   __ALL_NODES="$LOCATORS"
   for i in ${MEMBER_PREFIX}*; do
      if [ -d "$i" ]; then
         MEMBER=$i
         __ALL_NODES="$__ALL_NODES $MEMBER"
      fi
   done
   popd > /dev/null 2>&1
fi

ALL_NODES=""
for i in $__ALL_NODES; do
   # Remove domain name if not IP address
   if [[ $i =~ [1-9].* ]]; then
      NODE=$i
   else
      NODE=$(echo $i | sed 's/\..*//g')
   fi
   ALL_NODES="$ALL_NODES $NODE"
done
ALL_NODES=$(trimString "$ALL_NODES")

# Locator port used by show_type and revoke_all_missing_disk_stores
LOCATOR_PORT=$LOCATOR_START_PORT

#echo debug: LOCATOR_HOST=$LOCATOR_HOST
#echo debug: ALL_NODES=$ALL_NODES
#echo debug: LOCATOR_PORT=$LOCATOR_PORT
