#!/usr/bin/env bash

tools_SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"

BIN_DIR="$(dirname "$tools_SCRIPT_DIR")"
pushd $BIN_DIR > /dev/null 2>&1
. $BIN_DIR/.addonenv.sh -script_dir $BIN_DIR $*
popd > /dev/null 2>&1
SCRIPT_DIR=$tools_SCRIPT_DIR
if [ -f $SCRIPT_DIR/setenv.sh ]; then
   . $SCRIPT_DIR/setenv.sh
fi
