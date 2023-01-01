#!/usr/bin/env bash

# ========================================================================
# Copyright (c) 2013-2023 Netcrest Technologies, LLC. All rights reserved.
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

# Locators
if [ "$LOCATORS" == "" ]; then
   LOCATOR_START_PORT=$(getClusterProperty "locator.tcp.startPort")
   let LOCATOR_PORT=LOCATOR_START_PORT+LOCATOR_NUMBER-1
   let LOCATOR_END_PORT=LOCATOR_START_PORT+MAX_LOCATOR_COUNT-1
   LOCATOR_TCP_LIST=""

   if [ "$POD" == "local" ]; then
      HOST_NAME=`hostname`
      BIND_ADDRESS=`getClusterProperty "cluster.bindAddress" "$HOST_NAME"`
      HOSTNAME_FOR_CLIENTS=`getClusterProperty "cluster.hostnameForClients" "$HOST_NAME"`
      LOCATOR_PREFIX=`getLocatorPrefix`
      pushd $RUN_DIR > /dev/null 2>&1
      for i in ${LOCATOR_PREFIX}*; do
         if [ -d "$i" ]; then
            __LOCATOR=$i
            __LOCATOR_NUM=${__LOCATOR##$LOCATOR_PREFIX}
            __LOCATOR_NUM=$(trimLeadingZero $__LOCATOR_NUM)
            let __LOCATOR_PORT=LOCATOR_START_PORT+__LOCATOR_NUM-1
            if [ "$LOCATOR_TCP_LIST" == "" ]; then
               LOCATOR_TCP_LIST="$BIND_ADDRESS[$__LOCATOR_PORT]"
            else
               LOCATOR_TCP_LIST="$LOCATOR_TCP_LIST,$BIND_ADDRESS[$__LOCATOR_PORT]"
            fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   LOCATORS=$(echo $LOCATOR_TCP_LIST | sed -e "s/\[/:/g"  -e "s/\]//g")
   GEODE_LOCATORS=$LOCATOR_TCP_LIST
else
   GEODE_LOCATORS=$(echo $LOCATORS | sed -e "s/:/\[/g"  -e "s/,/\]/g" -e "s/$/\]/")
fi
SECURITY_DIR=$CLUSTER_DIR/security

if [ "$GEMFIRE_SECURITY_PROPERTY_FILE" == "" ]; then
   GEMFIRE_SECURITY_PROPERTY_FILE=$ETC_DIR/client/gfsecurity.properties
fi

#
# Check if security is enabled
#
GEMFIRE_SECURITY_PROPERTY_SYSTEM=
if [ -f $GEMFIRE_SECURITY_PROPERTY_FILE ]; then
   if [ "$SECURITY_ENABLED" == "true" ]; then
      GEMFIRE_SECURITY_PROPERTY_SYSTEM=-DgemfireSecurityPropertyFile=$GEMFIRE_SECURITY_PROPERTY_FILE
   fi
else
   if [ "$SECURITY_ENABLED" == "true" ]; then
      echo ""
      echo "Security is enabled but the following security file does not exist:"
      echo "   $GEMFIRE_SECURITY_PROPERTY_FILE"
      echo "start_server Aborted."
      echo ""
      exit
   fi
fi

if [ "$SECURITY_ENABLED" == "true" ]; then
   SECURITY_PROPERTIES=-Dpado.security.enabled=true
else
   SECURITY_PROPERTIES=-Dpado.security.enabled=false
fi

if [ "$SECURITY_ENABLED" == "true" ]; then
   GEMFIRE_SECURITY_PROPERTY_SYSTEM="$GEMFIRE_SECURITY_PROPERTY_SYSTEM -Dgemfire.security-client-auth-init=com.netcrest.pado.gemfire.security.PadoAuthInit.create"
fi
SECURITY_PROPERTIES="$SECURITY_PROPERTIES $GEMFIRE_SECURITY_PROPERTY_SYSTEM"

PADO_PROPERTIES="-Dpado.home.dir=$PADO_HOME -Dpado.server=false -Dpado.locators=$LOCATORS -Dpado.properties=$ETC_DIR/client/pado.properties -Dpado.command.jar.path=$BASE_DIR/lib/pado-tools.jar -Dpado.security.aes.userCertificate=$SECURITY_DIR/user.cer -Dpado.security.keystore.path=$SECURITY_DIR/client/client-user.keystore"
