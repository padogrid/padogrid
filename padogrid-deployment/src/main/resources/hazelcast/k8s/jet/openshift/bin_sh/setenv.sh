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

#
# Enter app specifics in this file.
#

# Cluster level variables:
# ------------------------
# BASE_DIR - padogrid base dir
# ETC_DIR - Cluster etc dir

# App level variables:
# --------------------
# APPS_DIR - <padogrid>/apps dir
# APP_DIR - App base dir
# APP_ETC_DIR - App etc dir

#
# Default OpenShift project name
#
export PROJECT_NAME="$APP_NAME"

#
# Default service names
#
SERVICE_NAME="hazelcast-jet-service"
MANAGEMENT_CENTER_SERVICE_NAME="hazelcast-jet-management-center-service"

#
# Number of members
#
MEMBER_COUNT=3

#
# Gap between the first load balancer port and the first node port. This numer is used
# to separate the load balancer ports from node ports. That means only up to this number
# of node ports (or services) can be created.
#
NODE_PORT_GAP=10

#
# Hazelcast Jet starting serivce port number. All service port numbers are incremented starting
# from this port number as follows.
#  - This port number is assigned to the headless cluster IP for load balancing Hazelcast pods.
#  - All load-balancer ports are incremented from this number.
#  - $NODE_PORT_GAP is added to the load-balancer ports for the counterpart node ports
#
START_SERVICE_PORT=30200
