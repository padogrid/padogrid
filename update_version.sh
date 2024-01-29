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

EXECUTABLE="`basename $0`"

if [ "$1" == "-?" ]; then
cat <<EOF
NAME
   $EXECUTABLE - Update pom.xml files with the specified PadoGrid version

SYNOPSIS
   $EXECUTABLE new_version_number

DESCRIPTION
   Updates pom.xml files with the specified PadoGrid version.

EOF
exit
fi

FROM_VERSION=$(grep "<version>" pom.xml | head -n 1 | sed -e 's/^.*<version>//' -e 's/<\/version>//')

if [ "$1" == "" ]; then
   echo >&2 "ERROR: new version not specified. Current version in pom.xml: [$FROM_VERSION]. Command aborted."
   exit 1
fi

TO_VERSION=$1

read -p "Change version from $FROM_VERSION to $TO_VERSION? Enter 'continue' to confirm: " INPUT
if [ "$INPUT" != "continue" ]; then
   echo "Command aborted."
   exit
fi

POM_FILES=`find . -name pom.xml`
POM_FILES="$POM_FILES $(find . -name pom-geode.xml)"
for i in $POM_FILES; do
  echo sed -i '' "s/$FROM_VERSION/$TO_VERSION/" $i
  sed -i '' "s/$FROM_VERSION/$TO_VERSION/" $i
done
