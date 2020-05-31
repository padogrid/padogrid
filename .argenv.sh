#!/usr/bin/env bash

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
SKIP_MAN=false
COHERENCE=false
DEBUG=false
PREV=

for i in "$@" -ignore
do
   if [ "$PREV" == "-skipMan" ]; then
      SKIP_MAN="true"
   elif [ "$PREV" == "-coherence" ]; then
      COHERENCE="true"
   elif [ "$PREV" == "-debug" ]; then
      DEBUG="true"

   # The following must be the last check
   elif [ "$PREV" == "-ignore" ]; then
      DEBUG=$i
   fi
   PREV=$i
done
