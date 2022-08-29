#!/usr/bin/env bash

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
BIN_DIR="$(dirname "$SCRIPT_DIR")"
pushd $BIN_DIR > /dev/null 2>&1
. $BIN_DIR/.addonenv.sh -script_dir $BIN_DIR $*
popd > /dev/null 2>&1
