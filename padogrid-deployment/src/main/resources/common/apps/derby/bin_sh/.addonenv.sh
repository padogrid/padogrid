#!/bin/bash

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

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
APP_DIR="$(dirname "$SCRIPT_DIR")"
APPS_DIR="$(dirname "$APP_DIR")"
BASE_DIR=$PADOGRID_HOME/$PRODUCT
pushd  $BASE_DIR/bin_sh > /dev/null 2>&1
. $BASE_DIR/bin_sh/.addonenv.sh
popd > /dev/null 2>&1

APP_NAME="$(basename "$APP_DIR")"

APP_ETC_DIR=$APP_DIR/etc

#
# Variables in use. Override them in setenv.sh.
#   DERBY_USER_NAME    Derby login user name. The user must have admin previledges. Default: admin
#   DERBY_PASSWORD     Derby login password. The user must have admin previledges. Default: admin
#   DERBY_HOST         Derby host name. Default: localhost
#   DERBY_PORT         Derby port number. Default: 1527
#

#
# Enter Derby user name and password
#
DERBY_USER_NAME=""
DERBY_PASSWORD=""

#
# Enter Derby host and port number
DERBY_HOST=localhost
DERBY_PORT=1527

DERBY_LOG_FILE=$APP_DIR/log/derby.log

#
# Source in app specifics
#
. $APP_DIR/bin_sh/setenv.sh

# -------------------------------------------------------------------------------

LOG_DIR="$APP_DIR/log"
if [ ! -d "$LOG_DIR" ]; then
   mkdir -p "$LOG_DIR"
fi
#
# Derby bootstrap settings
#
RUN_DIR="$APP_DIR/run/derby"
DERBY_LOG_FILE="$LOG_DIR/derby.log"

JAVA_OPTS="-Dpadogrid.rwe=$RWE -Dpadogrid.workspace=$WORKSPACE -Dpadogrid.app.log=$DERBY_LOG_FILE"
CLASSPATH=""

#
# Returns the PID of the running process identified by the specified configuration
# file path. If the PID is not found then returns an emptry string.
#
# @required OS_NAME
#
# @param configFilePath Absolute path of Derby configuration file. If not specified
#                       then return an empty string.
#
function getDerbyPid
{
   local CONFIG_FILE="$1"
   local PID
   if [ "$CONFIG_FILE" == "" ]; then
      PID=""
   else
      if [[ "$OS_NAME" == "CYGWIN"* ]]; then
            PID="$(WMIC path win32_process get Caption,Processid,Commandline | grep derbyrun | grep "$LOG_FILE" | grep -v grep | awk '{print $(NF-1)}')"
      else
         PID=$(ps -wweo pid,comm,args | grep derbyrun | grep "$LOG_FILE" | grep -v grep | awk '{print $1}')
      fi
   fi
   echo $PID
}

#
# Returns a space separated list of PIDs of all Derby instances. Returns an
# emptry string if Derby instances are not found.
#
# @required OS_NAME
#
function getAllDerbyPids
{
   if [[ "$OS_NAME" == "CYGWIN"* ]]; then
      PIDs="$(WMIC path win32_process get Caption,Processid,Commandline | grep derbyrun | grep "\-homepath" | grep -v grep | awk '{print $(NF-1)}')"
   else
      PIDs=$(ps -wweo pid,comm,args | grep derbyrun | grep "padogrid.app.log" | grep -v grep | awk '{print $1}')
   fi
   echo "$PIDs"
}
