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
#   DEFAULT_FOLDER       The default folder name. Default: padogrid-perf_test
#   DEFAULT_DATASOURCE   The default data source name. Default: Prometheus
#   GRAFANA_USER_NAME    Grafana HTTP login user name. The user must have admin previledges. Default: admin
#   GRAFANA_PASSWORD     Grafana HTTP login password. The user must have admin previledges. Default: admin
#   GRAFANA_HOST         Grafana HTTP host name. Default: localhost
#   GRAFANA_PORT         Grafana HTTP port number. Default: 3000
#   PROMETHEUS_HOST      Prometheus HTTP host name. Default: localhost
#   PROMETHEUS_PORT      Prometheus HTTP port number. Default: 9090
#   EXPORT_DASHBOARD_DIR Directory to which the 'export_folder' command exports dashboards.
#   EXPORT_TEMPLATE_DIR  Directory in which the 'export_to_template' command converts the exported dashboards.
#

#
# Enter Grafana uer name and password
#
GRAFANA_USER_NAME=admin
GRAFANA_PASSWORD=admin

#
# Enter Grafana host and port number (HTTP)
GRAFANA_HOST=localhost
GRAFANA_PORT=3000

#
# Enter Prometheus host and port number (HTTP)
#
PROMETHEUS_HOST=localhost
PROMETHEUS_PORT=9090

#
# Enter the directory to which the `export_folder` command exports dashboards.
#
EXPORT_DASHBOARD_DIR=$APP_DIR/export

#
# Enter the template directory where the exported files to be converted.
#
EXPORT_TEMPLATE_DIR=$APP_DIR/templates

#
# Default folder name
#
DEFAULT_FOLDER="padogrid-perf_test"

#
# Default data source name
#
DEFAULT_DATASOURCE="Prometheus"

#
# Source in app specifics
#
. $APP_DIR/bin_sh/setenv.sh

# -------------------------------------------------------------------------------

PROMETHEUS_URL=$PROMETHEUS_HOST:$PROMETHEUS_PORT
GRAFANA_URL=http://$GRAFANA_USER_NAME:$GRAFANA_PASSWORD@$GRAFANA_HOST:$GRAFANA_PORT

DASHBOARDS_DIR=$APP_ETC_DIR/dashboards
TMP_DIR=$APP_DIR/tmp

if [ ! -d $TMP_DIR ]; then
   mkdir -p $TMP_DIR
fi

LOG_DIR="$APP_DIR/log"
if [ ! -d "$LOG_DIR" ]; then
   mkdir -p "$LOG_DIR"
fi
ETC_DIR="$APP_DIR/etc"

#
# Prometheus bootstrap settings
#
PROMETHEUS_LOG_FILE="$LOG_DIR/prometheus.log"
PROMETHEUS_CONFIG_FILE="$APP_DIR/etc/prometheus.yml"
if [[ ${OS_NAME} == CYGWIN* ]]; then
   PROMETHEUS_CONFIG_FILE="$(cygpath -wp "$PROMETHEUS_CONFIG_FILE")"
fi
PROMETHEUS_OPTS="--web.listen-address="$PROMETHEUS_HOST:$PROMETHEUS_PORT" --config.file=$PROMETHEUS_CONFIG_FILE"

#
# Grafana bootstrap settings
#
GRAFANA_LOG_FILE="$LOG_DIR/grafana.log"
GRAFANA_CONFIG_FILE="$APP_DIR/etc/grafana.ini"
if [ ! -f "$GRAFANA_CONFIG_FILE" ]; then
   if [ -f "$GRAFANA_HOME/conf/defaults.ini" ]; then
      cp "$GRAFANA_HOME/conf/defaults.ini" "$GRAFANA_CONFIG_FILE"
      GRAFANA_OPTS="-config $GRAFANA_CONFIG_FILE"
   fi
fi
if [[ ${OS_NAME} == CYGWIN* ]]; then
   GRAFANA_CONFIG_FILE="$(cygpath -wp "$GRAFANA_CONFIG_FILE")"
fi
GRAFANA_OPTS=""
if [ -f "$GRAFANA_CONFIG_FILE" ]; then
   GRAFANA_OPTS="-config $GRAFANA_CONFIG_FILE"
fi

#
# Returns the PID of the running process identified by the specified configuration
# file path. If the PID is not found then returns an emptry string.
#
# @required OS_NAME
#
# @param configFilePath Absolute path of Prometheus configuration file. If not specified
#                       then return an empty string.
#
function getPrometheusPid
{
   local CONFIG_FILE="$1"
   local PID
   if [ "$CONFIG_FILE" == "" ]; then
      PID=""
   else
      if [[ "$OS_NAME" == "CYGWIN"* ]]; then
         PID="$(WMIC path win32_process get Caption,Processid,Commandline | grep prometheus | grep "$CONFIG_FILE" | grep -v grep | awk '{print $(NF-1)}')"
      else
         PID=$(ps -wweo pid,comm,args | grep prometheus | grep "$CONFIG_FILE" | grep -v grep | awk '{print $1}')
      fi
      echo $PID
   fi
}

#
# Returns a space separated list of PIDs of all Prometheus instances. Returns an
# emptry string if Prometheus instances are not found.
#
# @required OS_NAME
#
function getAllPrometheusPids
{
   if [[ "$OS_NAME" == "CYGWIN"* ]]; then
      PIDs="$(WMIC path win32_process get Caption,Processid,Commandline | ps -wweo pid,comm,args | grep prometheus | grep "\-\-config.file" | grep -v grep | awk '{print $(NF-1)}')"
   else
      PIDs=$(ps -wweo pid,comm,args | ps -wweo pid,comm,args | grep prometheus | grep "\-\-config.file" | grep -v grep | awk '{print $1}')
   fi
   echo "$PIDs"
}   

#
# Returns the PID of the running process identified by the specified configuration
# file path. If the PID is not found then returns an emptry string.
#
# @required OS_NAME
#
# @param configFilePath Absolute path of Grafana configuration file. If not specified
#                       then return an empty string.
#
function getGrafanaPid
{
   local CONFIG_FILE="$1"
   local PID
   if [ "$CONFIG_FILE" == "" ]; then
      PID=""
   else
      if [[ "$OS_NAME" == "CYGWIN"* ]]; then
         if [ -f "$CONFIG_FILE" ]; then
            PID="$(WMIC path win32_process get Caption,Processid,Commandline | grep "grafana[ -]server" | grep "$CONFIG_FILE" | grep -v grep | awk '{print $(NF-1)}')"
         else
            PID="$(WMIC path win32_process get Caption,Processid,Commandline | grep "grafana[ -]server" | grep "\-homepath" | grep -v grep | awk '{print $(NF-1)}')"
         fi
      else
         if [ -f "$CONFIG_FILE" ]; then
            PID=$(ps -wweo pid,comm,args | grep "grafana[ -]server" | grep "$CONFIG_FILE" | grep -v grep | awk '{print $1}')
         else
            PID=$(ps -wweo pid,comm,args | grep "grafana[ -]server" | grep "\-homepath" | grep -v grep | awk '{print $1}')
         fi
      fi
   fi
   echo $PID
}

#
# Returns a space separated list of PIDs of all Prometheus instances. Returns an
# emptry string if Prometheus instances are not found.
#
# @required OS_NAME
#
function getAllGrafanaPids
{
   if [[ "$OS_NAME" == "CYGWIN"* ]]; then
      PIDs="$(WMIC path win32_process get Caption,Processid,Commandline | grep "grafana[ -]server" | grep "\-homepath" | grep -v grep | awk '{print $(NF-1)}')"
   else
      PIDs=$(ps -wweo pid,comm,args | grep "grafana[ -]server" | grep "\-homepath" | grep -v grep | awk '{print $1}')
   fi
   echo "$PIDs"
}
