#!/usr/bin/env bash
SCRIPT_DIR="$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
. $SCRIPT_DIR/setenv.sh

#
# IMPORTANT: Do NOT modify this file.
#

#
# Remove the previous paths from PATH to prevent duplicates
#
if [ "$PADOGRID_PATH" == "" ]; then
   CLEANED_PATH=$PATH
else
   CLEANED_PATH=${PATH//$PADOGRID_PATH:/}
fi

#
# Set PATH by removing old paths first to prevent duplicates
#
unset __PATHS
declare -a __PATHS
let __INDEX=0
if [ "$JET_HOME" != "" ]; then
   __PATHS[$__INDEX]=$JET_HOME/bin
   let __INDEX=__INDEX+1
fi
if [ "$HAZELCAST_HOME" != "" ]; then
   __PATHS[$__INDEX]="$HAZELCAST_HOME/bin"
   let __INDEX=__INDEX+1
fi
if [ "$JAVA_HOME" != "" ]; then
   __PATHS[__INDEX]="$JAVA_HOME/bin"
   let __INDEX=__INDEX+1
fi
if [ "$PRODUCT" == "hazelcast" ]; then
   __PATHS[__INDEX]="$PADOGRID_HOME/$PRODUCT/bin_sh/cp_sub"
   let __INDEX=__INDEX+1
fi
__PATHS[__INDEX]="$PADOGRID_HOME/$PRODUCT/bin_sh"

for ((i = 0; i < ${#__PATHS[@]}; i++)); do
   __TOKEN="${__PATHS[$i]}"
   CLEANED_PATH=${CLEANED_PATH//$__TOKEN:/}
   CLEANED_PATH=${CLEANED_PATH//$__TOKEN/}
   CLEANED_PATH=${CLEANED_PATH//::/:}
done
PADOGRID_PATH=""
for ((i = 0; i < ${#__PATHS[@]}; i++)); do
   __TOKEN="${__PATHS[$i]}"
   if [ "$CLUSTER_TYPE" == "jet" ] && [ "${HAZELCAST_HOME}" != "" ] && [[ $__TOKEN == ${HAZELCAST_HOME}** ]]; then
      continue;
   elif [ "$CLUSTER_TYPE" == "imdg" ] && [ "${JET_HOME}" != "" ] && [[ $__TOKEN == ${JET_HOME}** ]]; then
      continue;
   fi
   PADOGRID_PATH=$__TOKEN:"$PADOGRID_PATH"
done
export PADOGRID_PATH=$(echo $PADOGRID_PATH | sed 's/.$//')
export PATH=$PADOGRID_PATH:$CLEANED_PATH

#
# Initialize auto completion
#
. $PADOGRID_HOME/$PRODUCT/bin_sh/.${PRODUCT}_completion.bash

#
# Display initialization info
#
if [ "$1" == "" ] || [ "$1" != "-quiet" ]; then
      echo ""
      echo "Workspaces Home:"
      echo "   PADOGRID_WORKSPACES_HOME=$PADOGRID_WORKSPACES_HOME"
      echo "Workspace:"
      echo "   PADOGRID_WORKSPACE=$PADOGRID_WORKSPACE"
      echo ""
      echo "All of your padogrid operations will be recorded in the workspace directory."
      echo ""
fi
