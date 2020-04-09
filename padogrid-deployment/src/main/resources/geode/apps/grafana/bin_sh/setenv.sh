#!/usr/bin/env bash

#
# All of the variables set with the default values shown below. If you need to override any of them
# set them in this file.
#   DEFAULT_FOLDER       The default folder name. Default: geode-addon-perf_test
#   DEFAULT_DATASOURCE   The default data source name. Default: geode-addon-perf_test
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
DEFAULT_FOLDER="geode-addon-perf_test"

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
