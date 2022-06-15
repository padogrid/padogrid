#!/usr/bin/env bash

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

# ----------------------------------------------------------------------------------------------------
# IMPORTANT:
# ----------------------------------------------------------------------------------------------------
# This file contains a partial list of environment variables. For a complete list, see '.addonenv.sh'. 
# ----------------------------------------------------------------------------------------------------

# ----------------------------------------------------------------------------------------------------
# CORE ENVIRONMENT VARIABLES:
# ----------------------------------------------------------------------------------------------------
# The following describes the core environment variables that you would most likely 
# to set in this file to drive padogrid.
#
# Required:
# ---------
# COHERENCE_HOME         Coherence IMDG root directory path
#
# Optional:
# ---------
# JAVA_HOME          Java root directory path. If not specified then the default java executable
#                    in your PATH will be used.
# JAVA_OPTS          Any Java options such as standard and non-standard (-XX) options,
#                    system properties (-D), etc.
# CLASSPATH          Class paths that includes your server components such as data (domain) classes.
#                    This will be prepended to the padogrid class paths.
# CLUSTER            The default cluster name. The default cluster can be managed without
#                    specifying the '-cluster' command option. Default: mypadogrid
# MIN_HEAP_SIZE      Minimum heap size. Used initially when the cluster is created. Default: 1g
# MAX_HEAP_SIZE      Maximum heap size. Used initially when the cluster is created. Default: 1g
#                    The heap sizes can be changed in clusters/<cluster>/etc/cluster.properties.
# ----------------------------------------------------------------------------------------------------

#
# JAVA_HOME: If JAVA_HOME is not specified then the default java executable in
#            your PATH will be used.
#
#JAVA_HOME=/home/vagrant/products/jdk1.8.0_212

#
# JAVA_OPTS - Java options. Enter any Java options here.
#
#JAVA_OPTS=

#
# CLASSPATH - Set your class path here. List all jars and folders that contain server-side
# classes such as data (domain) classes.
#
CLASSPATH=""

# 
# Enter your Coherence home directory.
#
#COHERENCE_HOME=

# 
# Default Cluster - If the -cluster option is not specified in any of the commands, then
# the commands default to this cluster. You can also export CLUSTER instead of defining
# it in this file.
#
#CLUSTER=""

#
# Default heap min/max sizes. These values are initially set in $ETC_DIR/cluster.properties
# when a new cluster is created using the 'create_cluster" command. All members in 
# the cluster share the same sizes. You can change them later in the cluster.properties
# file.
#
#MIN_HEAP_SIZE=1g
#MAX_HEAP_SIZE=1g

# Vagrant box image. Search a Vagrant box from  https://app.vagrantup.com/boxes/search.
# The default box is "hashicorp/precise64".
#POD_BOX_IMAGE="hashicorp/precise64"
POD_BOX_IMAGE="ubuntu/trusty64"

#
# The pod type determines the node envirionment in which the cluster is formed. 
# The supported types are as follows:
#
#    "local"    Local environment. The cluster forms members running stricly only
#               in your physical machine. The is the default cluster type.
#
#    "vagrant"  Vagrant box environment. The cluster forms members running in
#               VMs managed by Vagrant. You must install vagrant before you can
#               use this cluster type.
#
#POD_TYPE="local"

#
# The "local" pod is local to the OS (either guest OS or host OS) and 
# the cluster runs in that OS only. For non-local, i.e., any pod names other than
# "local", the members in the cluster runs on one or more guest OS machines.
# You can also export CLUSTER instead of defining it in this file.
#
#POD="local"

#
# Pod settings. For default values, see .addonenv.sh
#

#
# Primary node name. The primary node is not a data node. It should be used to manage
# PadoGrid clusters and run client programs.
#
#NODE_NAME_PRIMARY="pnode"

#
# Data node name prefix. Each data node name begins with the prefix followed by
# a number assigned by the pod builder.
#
#NODE_NAME_PREFIX="node"

#
# The last octet of the primary node IP address. The pod buillder assigns incremented
# IP addresses to all nodes starting from this octect. The first octect is assigned to
# the primary node and it is incremented thereafter for all data nodes. For example,
# if your host OS private IP address is 192.168.56.1, the last octet is 10, and
# 3 nodes are added, then the pod builder assignes IP addresses as follows:
# 
# Node Type  Node      IP
# ---------  -------   -------------
# primary    pnode     192.168.56.10       
# data       node-01   192.168.56.11
# data       node-02   192.168.56.12
# data       node-03   192.168.56.13
#
#NODE_IP_LAST_OCTET=10

#
# The directory path where the required products are installed in the host OS. The default
# path is "<padogrid-dir>/products". This path is mounted as "~/products" in the
# guest OS.
#
#HOST_PRODUCTS_DIR=""
