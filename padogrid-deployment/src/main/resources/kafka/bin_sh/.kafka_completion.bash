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

if [ "$PADOGRID_HOME" != "" ]; then
   SCRIPT_DIR=$PADOGRID_HOME/$PRODUCT/bin_sh
else
   SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
   PRODUCT=$(basename $(dirname "$SCRIPT_DIR"))
fi
. $SCRIPT_DIR/.addonenv.sh -script_dir $SCRIPT_DIR
. $SCRIPT_DIR/.utilenv.sh
. $SCRIPT_DIR/.padogrid_completion.bash