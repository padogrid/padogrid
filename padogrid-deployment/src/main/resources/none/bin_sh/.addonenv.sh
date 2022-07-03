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
   SCRIPT_DIR="$2"
else
   SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
fi
BASE_DIR="$(dirname "$SCRIPT_DIR")"

# ----------------------------------------------------------------------------------------------------
# CORE ENVIRONMENT VARIABLES:
# ----------------------------------------------------------------------------------------------------
# The following describes the core environment variables that are typically overriden in the
# 'setenv.sh' file by the user.
#
# Required:
# ---------
#
# Optional:
# ---------
# JAVA_OPTS              Any Java options such as standard and non-standard (--J=-XX) options,
#                        system properties (--J=-D), etc.
# CLASSPATH              Class paths that includes your server components such as data (domain) classes.
#                        This will be prepended to the padogrid class paths.
# DEFAULT_CLUSTER        The default cluster name. The default cluster can be managed without
#                        specifying the '-cluster' command option. Default: mynone  
# ----------------------------------------------------------------------------------------------------

# 
# Default Cluster - If the -cluster option is not specified in any of the commands, then
# the commands default to this cluster.
#
DEFAULT_CLUSTER="$DEFAULT_NONE_CLUSTER"

# -------------------------------------------------------------------------------
# Source in .argenv.sh to set all default variables. This call is required.
# IMPORTANT: Do NOT remove this call.
# -------------------------------------------------------------------------------
. $SCRIPT_DIR/.argenv.sh "$@"
. $SCRIPT_DIR/.utilenv_none.sh "$@"

# -----------------------------------------------------
# IMPORTANT: Do NOT modify below this line
# -----------------------------------------------------

#
# Source in setenv.sh that contains common variables
#
if [ -f "$SCRIPT_DIR/setenv.sh" ]; then
   # CLUSTER and POD options override setenv.sh
   __CLUSTER=$CLUSTER
   __POD=$POD

   . $SCRIPT_DIR/setenv.sh

   if [ "$CLUSTER_SPECIFIED" == "true" ]; then
      CLUSTER=$__CLUSTER
   fi
   if [ "$POD_SPECIFIED" == "true" ]; then
      POD=$__POD
   fi
fi

#
# log4j2 logging
#
#if [[ ${OS_NAME} == CYGWIN* ]]; then
#   __ETC_DIR="$(cygpath -wp "$ETC_DIR")"
#else
#   __ETC_DIR=$ETC_DIR
#fi
LOG4J_FILE="$ETC_DIR/log4j2.properties"
if [[ ${OS_NAME} == CYGWIN* ]]; then
   LOG4J_FILE="$(cygpath -wp "$LOG4J_FILE")"
fi
LOG_PROPERTIES="--J=-Dlog4j.configurationFile=$LOG4J_FILE"

# PATH Depends on PRODUCT_HOME due to switch_workspace which does not have cluster info.
# We need to change that accordingly here.
export PRODUCT="none"
export CLUSTER_TYPE="none"
export PATH="$SCRIPT_DIR:$PADOGRID_HOME/bin_sh:$PATH"

#
# CLASSPATH
#
__CLASSPATH=""
if [ "$CLASSPATH" != "" ]; then
   __CLASSPATH="$CLASSPATH"
fi
if [ "$__CLASSPATH" == "" ]; then
   __CLASSPATH="$CLUSTER_DIR/plugins/*:$CLUSTER_DIR/lib/*"
else
   __CLASSPATH="$__CLASSPATH:$CLUSTER_DIR/plugins/*:$CLUSTER_DIR/lib/*"
fi
if [ "$PADOGRID_WORKSPACE" != "" ] && [ "$PADOGRID_WORKSPACE" != "$BASE_DIR" ]; then
   __CLASSPATH="$__CLASSPATH:$PADOGRID_WORKSPACE/plugins/*:$PADOGRID_WORKSPACE/lib/*"
fi
__CLASSPATH="$__CLASSPATH:$BASE_DIR/plugins/*:$BASE_DIR/lib/*"
__CLASSPATH="$__CLASSPATH:$PADOGRID_HOME/lib/*"
export CLASSPATH="$__CLASSPATH"

#
# Source in cluster specific setenv.sh
#
RUN_SCRIPT=
if [ -f "$CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh" ] && [ "$1" != "-options" ]; then
   . "$CLUSTERS_DIR/$CLUSTER/bin_sh/setenv.sh"
fi
