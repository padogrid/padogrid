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

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
POD_DIR="$(dirname "$SCRIPT_DIR")"
PODS_DIR="$(dirname "$POD_DIR")"
BASE_DIR="$PADOGRID_HOME/$PRODUCT"
pushd  $BASE_DIR/bin_sh > /dev/null 2>&1
. $BASE_DIR/bin_sh/.addonenv.sh
popd > /dev/null 2>&1

# Need to reset SCRIPT_DIR. It has a different value due to the above calls. 
SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"

POD="$(basename "$POD_DIR")"
PODS_DIR="$(dirname "$POD_DIR")"
ETC_DIR=$POD_DIR/etc

#
# Source in app specifics
#
. $POD_DIR/bin_sh/setenv.sh

# Client node memory size.
#if [ "$CLIENT_MEMORY_SIZE" ]; then
#   let CLIENT_MEMORY_SIZE=HEAP_MAX+10
#fi

# Data node memory size. All data nodes are set to this memory size.
#NODE_MEMORY_SIZE="2048"
