#!/usr/bin/env bash

SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
BIN_DIR="$(dirname "$SCRIPT_DIR")"
pushd $BIN_DIR > /dev/null 2>&1
. $BIN_DIR/.addonenv.sh -script_dir $BIN_DIR $*
popd > /dev/null 2>&1

# Add log4j which was taken out by the main .addonenv.sh to prevent jar conflicts
# for running clusters. The commands in tools do not have cluster dependencies.
for i in $PADOGRID_HOME/lib/*; do
  if [[ "$i" == *"slf4j"* ]] || [[ "$i" == *"log4j"* ]]; then
     CLASSPATH="$CLASSPATH:$i"
  fi
done

