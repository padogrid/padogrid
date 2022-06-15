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

if [ "$1" == "-script_dir" ]; then
   SCRIPT_DIR=$2
else
   SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
fi
. $SCRIPT_DIR/.utilenv.sh

# OS_NAME in uppercase
OS_NAME=`uname`
OS_NAME=`echo "$OS_NAME"|awk '{print toupper($0)}'`

#
# .argenv.sh parses input arguments of individual scripts
# and assign appropriate parameters.
#

#
# Determine arguments
#
PRODUCT_ARG=
ENV_ARG=
RWE_ARG=
RWE_SPECIFIED=false
WORKSPACE_ARG=
CHECKOUT_ARG=
WORKSPACE_SPECIFIED=false
CHECKOUT_SPECIFIED=false
JAVA_HOME_ARG=
PATH_ARG=
JAR_ARG=
CLASSPATH_ARG=
JET_ARG=
NAME_ARG=
PORT_ARG=
CREATE_SCRIPT=false
POD_SPECIFIED=false
AVAHI_SPECIFIED=false
POD_TYPE=
TYPE_ARG=
REFID=
K8S=
K8S_SPECIFIED=false
DOCKER=
DOCKER_SPECIFIED=false
HOST=
HOST_SPECIFIED=false
COUNT=
INIT_SPECIFIED=false
VERSION_SPECIFIED=false
VERSION_ARG=
FORCE_SPECIFIED=false
MAN_SPECIFIED=false
CLUSTER_SPECIFIED=false
CLUSTER_TYPE_SPECIFIED=false
FG_SPECIFIED=false
MEMBER_NUM=1
MEMBER_NUM_SPECIFIED=false
REMOTE=
REMOTE_SPECIFIED=false
PRODUCT_CLUSTER=
PRODUCT_CLUSTER_SPECIFIED=false
MIRROR_SPECIFIED=false
VM_SPECIFIED=false
VM_HOSTS_ARG=
VM_JAVA_HOME_ARG=
VM_PADOGRID_HOME_ARG=
VM_PADOGRID_WORKSPACES_HOME_ARG=
VM_USER_ARG=
VM_PRIVATE_KEY_FILE_ARG=
KEY=
APP=
APP_SPECIFIED=false
GRID_SPECIFIED=false
SITE_SPECIFIED=false
LOCATOR=
LOCATOR_SPECIFIED=false
PADOWEB=
PADOWEB_SPECIFIED=false
MC=
MC_SPECIFIED=false
MEMBER=
MEMBER_SPECIFIED=false
PASSWORD=
GROUP=
CLUSTER_GROUP=
CP_GROUP=
UUID==
START=false
LOG=
FULL=false
HELP=
OPTIONS=false
SIMULATE=false
PREVIEW=false
DOWNLOAD=false
CONSOLE=false
USER=
GITHOST=github
BRANCH=
CONNECT=https
LIST=false
HEADER=false
CATALOG=false
STANDALONE=false
TREE=false
OVERWRITE=false
ALL=false
OSS=false
RHEL=false
WAN=false
WAN_ARG=
PID=
PIDONLY=
BEGIN_NUM=1
END_NUM=
KILL=
DEBUG=
DIR=
CLEAN=
LOCAL=false
QUIET=false
SHORT=false
LONG=false
DATASOURCE=
FOLDER=
PRIMARY=
BOX=
DIR=
PREFIX=
OCTET=
PM=
NM=
PREV=

for i in "$@"
do
   if [ "$PREV" == "-product" ]; then
      PRODUCT_ARG=$i
      PRODUCT_HOME_ARG=$i
   elif [ "$PREV" == "-product-cluster" ]; then
      PRODUCT_CLUSTER_ARG=$i
   elif [ "$PREV" == "-rwe" ]; then
      RWE_ARG=$i
   elif [ "$PREV" == "-env" ]; then
      ENV_ARG=$i
   elif [ "$PREV" == "-workspace" ]; then
      WORKSPACE_ARG=$i
   elif [ "$PREV" == "-checkout" ]; then
      CHECKOUT_ARG=$i
   elif [ "$PREV" == "-java" ]; then
      JAVA_HOME_ARG=$i
   elif [ "$PREV" == "-path" ]; then
      PATH_ARG=$i
   elif [ "$PREV" == "-jar" ]; then
      JAR_ARG=$i
   elif [ "$PREV" == "-classpath" ]; then
      CLASSPATH_ARG=$i
   elif [ "$PREV" == "-jet" ]; then
      JET_ARG=$i
   elif [ "$PREV" == "-name" ]; then
      NAME_ARG=$i
   elif [ "$PREV" == "-pod" ]; then
      POD=$i
   elif [ "$PREV" == "-port" ]; then
      PORT_ARG=$i
   elif [ "$PREV" == "-type" ]; then
      POD_TYPE=$i
      TYPE_ARG=$i
   elif [ "$PREV" == "-refid" ]; then
      REFID=$i
   elif [ "$PREV" == "-primary" ]; then
      PRIMARY=$i
   elif [ "$PREV" == "-box" ]; then
      BOX=$i
   elif [ "$PREV" == "-octet" ]; then
      OCTET=$i
   elif [ "$PREV" == "-pm" ]; then
      PM=$i
   elif [ "$PREV" == "-nm" ]; then
      NM=$i
   elif [ "$PREV" == "-dir" ]; then
      DIR=$i
   elif [ "$PREV" == "-count" ]; then
      COUNT=$i
   elif [ "$PREV" == "-cluster" ]; then
      CLUSTER=$i
   elif [ "$PREV" == "-cluster-type" ]; then
      CLUSTER_TYPE=$i
      CLUSTER_TYPE_SPECIFIED="true"
   elif [ "$PREV" == "-num" ]; then
      MEMBER_NUM=$i
   elif [ "$PREV" == "-password" ]; then
      PASSWORD=$i
   elif [ "$PREV" == "-k8s" ]; then
      K8S=$i
   elif [ "$PREV" == "-docker" ]; then
      DOCKER=$i
   elif [ "$PREV" == "-host" ]; then
      HOST=$i
   elif [ "$PREV" == "-group" ]; then
      GROUP=$i
   elif [ "$PREV" == "-clustergroup" ]; then
      CLUSTER_GROUP=$i
   elif [ "$PREV" == "-cpgroup" ]; then
      CP_GROUP=$i
   elif [ "$PREV" == "-uuid" ]; then
      UUID=$i
   elif [ "$PREV" == "-id" ]; then
      ID=$i
   elif [ "$PREV" == "-remote" ]; then
      REMOTE=$i
   elif [ "$PREV" == "-user" ]; then
      USER=$i
   elif [ "$PREV" == "-githost" ]; then
      GITHOST=$i
   elif [ "$PREV" == "-branch" ]; then
      BRANCH=$i
   elif [ "$PREV" == "-connect" ]; then
      CONNECT=$i
   elif [ "$PREV" == "-wan" ]; then
      WAN_ARG=$i
      WAN=true
   elif [ "$PREV" == "-vm" ]; then
      if [[ "$i" != "-"* ]]; then
         VM_HOSTS_ARG=$i
      fi
   elif [ "$PREV" == "-vm-java" ]; then
      VM_JAVA_HOME_ARG=$i
   elif [ "$PREV" == "-vm-product" ]; then
      VM_PRODUCT_HOME_ARG=$i
   elif [ "$PREV" == "-vm-padogrid" ]; then
      VM_PADOGRID_HOME_ARG=$i
   elif [ "$PREV" == "-vm-workspaces" ]; then
      VM_PADOGRID_WORKSPACES_HOME_ARG=$i
   elif [ "$PREV" == "-vm-user" ]; then
      VM_USER_ARG=$i
   elif [ "$PREV" == "-vm-key" ]; then
      VM_PRIVATE_KEY_FILE_ARG=$i
   elif [ "$PREV" == "-key" ]; then
      KEY=$i
   elif [ "$PREV" == "-app" ]; then
      APP=$i
   elif [ "$PREV" == "-grid" ]; then
      GRID=$i
   elif [ "$PREV" == "-site" ]; then
      SITE=$i
   elif [ "$PREV" == "-locator" ]; then
      LOCATOR=$i
   elif [ "$PREV" == "-padoweb" ]; then
      PADOWEB=$i
   elif [ "$PREV" == "-mc" ]; then
      MC=$i
   elif [ "$PREV" == "-member" ]; then
      MEMBER=$i
   elif [ "$PREV" == "-log" ]; then
      LOG=$i
   elif [ "$PREV" == "-begin" ]; then
      BEGIN_NUM=$i
   elif [ "$PREV" == "-end" ]; then
      END_NUM=$i
   elif [ "$PREV" == "-dir" ]; then
      DIR=$i
   elif [ "$PREV" == "-prefix" ]; then
      PREFIX=$i
   elif [ "$PREV" == "-folder" ]; then
      FOLDER=$i
   elif [ "$PREV" == "-datasource" ]; then
      DATASOURCE=$i
   elif [ "$PREV" == "-version" ]; then
      VERSION_ARG=$i

# options with no value
   elif [ "$i" == "-init" ]; then
      INIT_SPECIFIED=true
   elif [ "$i" == "-version" ]; then
      VERSION_SPECIFIED=true
   elif [ "$i" == "-force" ]; then
      FORCE_SPECIFIED=true
   elif [ "$i" == "-man" ]; then
      MAN_SPECIFIED=true
   elif [ "$i" == "-fg" ]; then
      FG_SPECIFIED=true
   elif [ "$i" == "-simulate" ]; then
      SIMULATE=true
   elif [ "$i" == "-preview" ]; then
      PREVIEW=true
   elif [ "$i" == "-download" ]; then
      DOWNLOAD=true
   elif [ "$i" == "-workspace" ]; then
      WORKSPACE_SPECIFIED=true
   elif [ "$i" == "-checkout" ]; then
      CHECKOUT_SPECIFIED=true
   elif [ "$i" == "-list" ]; then
      LIST=true
   elif [ "$i" == "-header" ]; then
      HEADER=true
   elif [ "$i" == "-catalog" ]; then
      CATALOG=true
   elif [ "$i" == "-console" ]; then
      CONSOLE=true
   elif [ "$i" == "-create-script" ]; then
      CREATE_SCRIPT=true
   elif [ "$i" == "-full" ]; then
      FULL=true
   elif [ "$i" == "-start" ]; then
      START=true
   elif [ "$i" == "-?" ]; then
      HELP=true
   elif [ "$i" == "-options" ]; then
      OPTIONS=true
   elif [ "$i" == "-all" ]; then
      ALL=true
   elif [ "$i" == "-oss" ]; then
      OSS=true
   elif [ "$i" == "-rhel" ]; then
      RHEL=true
   elif [ "$i" == "-wan" ]; then
      WAN=true
   elif [ "$i" == "-kill" ]; then
      KILL=true
   elif [ "$i" == "-debug" ]; then
      DEBUG=true
   elif [ "$i" == "-pid" ]; then
      PID=true
   elif [ "$i" == "-pidonly" ]; then
      PIDONLY=true
   elif [ "$i" == "-clean" ]; then
      CLEAN=true
   elif [ "$i" == "-local" ]; then
      LOCAL=true
   elif [ "$i" == "-quiet" ]; then
      QUIET=true
   elif [ "$i" == "-short" ]; then
      SHORT=true
   elif [ "$i" == "-long" ]; then
      LONG=true
   elif [ "$i" == "-num" ]; then
      MEMBER_NUM_SPECIFIED=true
   elif [ "$i" == "-pod" ]; then
      POD_SPECIFIED=true
   elif [ "$i" == "-avahi" ]; then
      AVAHI_SPECIFIED=true
   elif [ "$i" == "-cluster" ]; then
      CLUSTER_SPECIFIED=true
   elif [ "$i" == "-k8s" ]; then
      K8S_SPECIFIED=true
   elif [ "$i" == "-docker" ]; then
      DOCKER_SPECIFIED=true
   elif [ "$i" == "-app" ]; then
      APP_SPECIFIED=true
   elif [ "$i" == "-host" ]; then
      HOST_SPECIFIED="true"      
   elif [ "$i" == "-grid" ]; then
      GRID_SPECIFIED=true
   elif [ "$i" == "-site" ]; then
      SITE_SPECIFIED=true
   elif [ "$i" == "-padoweb" ]; then
      PADOWEB_SPECIFIED=true
   elif [ "$i" == "-mc" ]; then
      MC_SPECIFIED=true
   elif [ "$i" == "-member" ]; then
      MEMBER_SPECIFIED=true
   elif [ "$i" == "-locator" ]; then
      LOCATOR_SPECIFIED=true
   elif [ "$i" == "-vm" ]; then
      VM_SPECIFIED=true
   elif [ "$i" == "-rwe" ]; then
      RWE_SPECIFIED=true
   elif [ "$i" == "-mirror" ]; then
      MIRROR_SPECIFIED=true
   elif [ "$i" == "-remote" ]; then
      REMOTE_SPECIFIED=true
   elif [ "$i" == "-product-cluster" ]; then
      PRODUCT_CLUSTER_SPECIFIED=true
   elif [ "$i" == "-standalone" ]; then
      STANDALONE=true
   elif [ "$i" == "-tree" ]; then
      TREE=true
   elif [ "$i" == "-overwrite" ]; then
      OVERWRITE=true
   # this must be the last check
   elif [ "$PREV" == "-gateway" ]; then
      GATEWAY_XML_FILE=$i
   fi
   PREV=$i
done

# Set MEMBER_NUM_NO_LEADING_ZERO
MEMBER_NUM_NO_LEADING_ZERO=$MEMBER_NUM
while [[ $MEMBER_NUM_NO_LEADING_ZERO == 0* ]]; do
   MEMBER_NUM_NO_LEADING_ZERO=${MEMBER_NUM_NO_LEADING_ZERO:1};
done
MEMBER_NUM=$MEMBER_NUM_NO_LEADING_ZERO
LOCATOR_NUM_NO_LEADING_ZERO=$MEMBER_NUM_NO_LEADING_ZERO
MASTER_NUM_NO_LEADING_ZERO=$MEMBER_NUM_NO_LEADING_ZERO
NAMENODE_NUM_NO_LEADING_ZERO=$MEMBER_NUM_NO_LEADING_ZERO

let LAST_LOCATOR_NUM=MAX_LOCATOR_COUNT-1
let LAST_MASTER_NUM=MAX_LOCATOR_COUNT-1
let LAST_NAMENODE_NUM=MAX_LOCATOR_COUNT-1
let LAST_MEMBER_NUM=MAX_MEMBER_COUNT-1

# Determine the member number
re='^[0-9]+$'
if ! [[ $MEMBER_NUM =~ $re ]] ; then
   echo "ERROR: Member number must be a positive number the range [1, 99]: $MEMBER_NUM. Command aborted." >&2; exit 1
fi
if [ $MEMBER_NUM -lt 1 ]; then
   echo "ERROR: Member number must be greater than 0: $MEMBER_NUM. Command aborated." >&2; exit 1
fi
if [ $MEMBER_NUM -gt 99 ]; then
   echo "ERROR: Member number must be less than 99: $MEMBER_NUM. Command aborated." >&2; exit 1
fi
if [ $CONNECT != "https" ] && [ "$CONNECT" != "ssh" ]; then
   echo "ERROR: Invalid -connect type: [$CONNECT]. Valid values are https or ssh. Command aborted." >&2; exit 1
fi
if [ $MEMBER_NUM -lt 10 ]; then
   MEMBER_NUM=0$MEMBER_NUM
fi

# If the end member number is not defined then
# assign it to the beginning member number.
if [ "$END_NUM" == "" ]; then
   END_NUM=$BEGIN_NUM
fi

# Set the grid options to display in the command usage.
GRID_DEFAULT=
GRIDS_OPT=
ALL_SITES=
for i in $GRIDS
do
   if [ "$GRIDS_OPT" == "" ]; then
      GRIDS_OPT=$i
      GRID_DEFAULT=$i
   else
      GRIDS_OPT=${GRIDS_OPT}"|"$i
   fi
   . grids/$i/grid_env.sh
   ALL_SITES="$ALL_SITES $SITES"
done

# Set all sites found in all grids
unique_words "$ALL_SITES" SITES

# Set the site options to display in the command usage.
SITE_DEFAULT=
SITES_OPT=
for i in $SITES
do
   if [ "$SITES_OPT" == "" ]; then
      SITES_OPT=$i
      SITE_DEFAULT=$i
   else
      SITES_OPT=${SITES_OPT}"|"$i
   fi
done

# Set the site to the default site if undefined.
if [ "$SITE" == "" ]; then
   SITE=$SITE_DEFAULT
fi

if [ "$SSH_USER" == "" ]; then
#   SSH_USER=`id -un`
   SSH_USER=vagrant
fi
if [ "$REMOTE_BASE_DIR" == "" ]; then
   if [ "$BASE_DIR" != "" ]; then
      REMOTE_BASE_DIR="$PADOGRID_HOME"
   fi
fi

# SED backup prefix
if [[ ${OS_NAME} == DARWIN* ]]; then
   # Mac - space required
   __SED_BACKUP=" 0"
else
   __SED_BACKUP="0"
fi

# If -env file present, then source it in.
if [ "$ENV_ARG" != "" ]; then
   . $ENV_ARG
fi

# 
# Determine the PadoGrid environment base path. Default is "$HOME/Padogrid".
#
if [ "$PADOGRID_ENV_BASE_PATH" == "" ]; then
   if [ "$PADOGRID_HOME" == "" ]; then
      export PADOGRID_ENV_BASE_PATH="$HOME/Padogrid"
   else
      export PADOGRID_ENV_BASE_PATH="$(dirname $(dirname $PADOGRID_WORKSPACES_HOME))"
   fi
fi      

DOWNLOADABLE_PRODUCTS="padogrid pado padodesktop padoweb geode hazelcast-enterprise hazelcast-oss hazelcast-mc hazelcast-desktop jet-enterprise jet-oss snappydata spark kafka hadoop"

# Bash color code
CNone='\033[0m' # No Color
CBlack='\033[0;30m'
CDarkGray='\033[1;30m'
CRed='\033[0;31m'
CLightRed='\033[1;31m'
CGreen='\033[0;32m'
CLightGreen='\033[1;32m'
CBrownOrange='\033[0;33m'
CYellow='\033[1;33m'
CBlue='\033[0;34m'
CLightBlue='\033[1;34m'
CPurple='\033[0;35m'
CLightPurple='\033[1;35m'
CCyan='\033[0;36m'
CLightCyan='\033[1;36m'
CLightGray='\033[0;37m'
CWhite='\033[1;37m'
CUnderline='\033[4m'
CUrl=$CBlue$CUnderline
