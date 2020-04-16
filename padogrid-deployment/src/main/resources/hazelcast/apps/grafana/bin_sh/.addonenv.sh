#!/bin/bash

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

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
APP_DIR="$(dirname "$SCRIPT_DIR")"
APPS_DIR="$(dirname "$APP_DIR")"
BASE_DIR=$PADOGRID_HOME/$PRODUCT
pushd  $BASE_DIR/bin_sh > /dev/null 2>&1
. $BASE_DIR/bin_sh/.addonenv.sh
popd > /dev/null 2>&1

APP_ETC_DIR=$APP_DIR/etc

#
# Variables in use. Override them in setenv.sh.
#   DEFAULT_FOLDER       The default folder name. Default: hazelcast-addon-perf_test
#   DEFAULT_DATASOURCE   The default data source name. Default: hazelcast-addon-perf_test
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
DEFAULT_FOLDER="hazelcast-addon-perf_test"

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
