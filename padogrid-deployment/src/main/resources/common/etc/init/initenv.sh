#!/usr/bin/env bash
SCRIPT_DIR="$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
. $SCRIPT_DIR/setenv.sh

#
# IMPORTANT: Do NOT modify this file.
#

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
