#!/usr/bin/env bash
SCRIPT_DIR="$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
. $SCRIPT_DIR/setenv.sh

#
# IMPORTANT: Do NOT modify this file.
#

#
#  Set the default environment variable for this RWE.
#
RWE=$(basename "$PADOGRID_WORKSPACES_HOME")
RWEENV_FILE="$HOME/.padogrid/workspaces/$RWE/rweenv.sh"
if [ -f "$RWEENV_FILE" ]; then 
   . "$RWEENV_FILE"
fi
if [ "$WORKSPACE" == "" ] || [ ! -f "$PADOGRID_WORKSPACES_HOME/$WORKSPACE/initenv.sh" ]; then 
   # Find the first workspace directory
   for i in $PADOGRID_WORKSPACES_HOME/*; do
      if [ -f "$i/initenv.sh" ]; then 
         WORKSPACE=$(basename "$i")
         break;  
      fi      
   done
fi
if [ "$WORKSPACE" != "" ]; then 
   . "$PADOGRID_WORKSPACES_HOME/$WORKSPACE/initenv.sh" -quiet
fi

#
# Remove the previous paths from PATH to prevent duplicates
#
CLEANED_PATH=""
__IFS=$IFS
IFS=":"
PATH_ARRAY=($PATH)
for i in "${PATH_ARRAY[@]}"; do
   if [ "$JAVA_HOME" != "" ] && [ "$i" == "$JAVA_HOME/bin" ]; then
      continue;
   elif [[ "$i" == **"padogrid_"** ]] && [[ "$i" == **"bin_sh"** ]]; then
      continue;
   elif [ "$PRODUCT_HOME" != "" ] && [[ "$i" == "$PRODUCT_HOME"** ]]; then
      continue;
   fi
   if [ "$CLEANED_PATH" == "" ]; then
      CLEANED_PATH="$i"
   else
      CLEANED_PATH="$CLEANED_PATH:$i"
   fi
done
IFS=$__IFS

# Export cleaned PATH
export PATH="$CLEANED_PATH"

#
# Initialize auto completion
#
if [ -f "$PADOGRID_HOME/$PRODUCT/bin_sh/.${PRODUCT}_completion.bash" ]; then
   . "$PADOGRID_HOME/$PRODUCT/bin_sh/.${PRODUCT}_completion.bash"
fi

#
# Need to reset here for the direct call, i.e., from .bashrc
#
if [ -f "$PADOGRID_WORKSPACE/.workspace/workspaceenv.sh" ]; then
   . "$PADOGRID_WORKSPACE/.workspace/workspaceenv.sh"
fi
if [ -f "$PADOGRID_WORKSPACE/clusters/$CLUSTER/.cluster/clusterenv.sh" ]; then
   . "$PADOGRID_WORKSPACE/clusters/$CLUSTER/.cluster/clusterenv.sh"
fi
export CLUSTER
export CLUSTER_TYPE
export POD
export PRODUCT

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
