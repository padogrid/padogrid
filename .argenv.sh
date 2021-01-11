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
