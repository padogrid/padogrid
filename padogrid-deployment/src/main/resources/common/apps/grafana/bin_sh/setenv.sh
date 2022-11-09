#!/usr/bin/env bash

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

#
# All of the variables set with the default values shown below. If you need to override any of them
# set them in this file.
#   DEFAULT_FOLDER       The default folder name. Default: padogrid-perf_test
#   DEFAULT_DATASOURCE   The default data source name. Default: padogrid-perf_test
#   GRAFANA_USER_NAME    Grafana HTTP login user name. The user must have admin previledges. Default: admin
#   GRAFANA_PASSWORD     Grafana HTTP login password. The user must have admin previledges. Default: admin
#   GRAFANA_HOST         Grafana HTTP host name. Default: localhost
#   GRAFANA_PORT         Grafana HTTP port number. Default: 3000
#   PROMETHEUS_HOST      Prometheus HTTP host name. Default: localhost
#   PROMETHEUS_PORT      Prometheus HTTP port number. Default: 9090
#   EXPORT_DASHBOARD_DIR Directory to which the 'export_folder' command exports dashboards. Default: export
#   EXPORT_TEMPLATE_DIR  Directory in which the 'export_to_template' command converts the exported dashboards.
#

#
# Default folder name
#
DEFAULT_FOLDER="padogrid-perf_test"

#
# Default data source name
#
#DEFAULT_DATASOURCE="Prometheus"

#
# Enter Grafana uer name and password
#
#GRAFANA_USER_NAME=admin
#GRAFANA_PASSWORD=admin

#
# Enter Grafana host and port number (HTTP)
#
#GRAFANA_HOST=localhost
#GRAFANA_PORT=3000

#
# Enter Prometheus host and port number (HTTP)
#
#PROMETHEUS_HOST=localhost
#PROMETHEUS_PORT=9090

#
# Enter the directory to which the `export_folder` command exports dashboards.
#
#EXPORT_DASHBOARD_DIR=$APP_DIR/export

#
# Enter the template directory where the exported files to be converted.
#
#EXPORT_TEMPLATE_DIR=$APP_DIR/templates
