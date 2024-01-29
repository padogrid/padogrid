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

if [ "$1" == "-script_dir" ]; then
   SCRIPT_DIR=$2
else
   SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
fi

#
# .argenv.sh parses input arguments of individual scripts
# and assign appropriate parameters.
#

#
# Determine arguments
#
ALL_SPECIFIED=false
MAN_SPECIFIED=false
COHERENCE_SPECIFIED=false
DEBUG=false
PREV=

for i in "$@" -ignore
do
   if [ "$PREV" == "-all" ]; then
      ALL_SPECIFIED="true"
      MAN_SPECIFIED="true"
      COHERENCE_SPECIFIED="true"
   elif [ "$PREV" == "-man" ]; then
      MAN_SPECIFIED="true"
   elif [ "$PREV" == "-coherence" ]; then
      COHERENCE_SPECIFIED="true"
   elif [ "$PREV" == "-debug" ]; then
      DEBUG="true"

   # The following must be the last check
   elif [ "$PREV" == "-ignore" ]; then
      DEBUG=$i
   fi
   PREV=$i
done
