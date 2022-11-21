#!/usr/bin/env bash

# ========================================================================
# Copyright (c) 2020-2022 Netcrest Technologies, LLC. All rights reserved.
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

if [ "$PADOGRID_HOME" != "" ]; then
   SCRIPT_DIR=$PADOGRID_HOME/$PRODUCT/bin_sh
else
   SCRIPT_DIR="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
   PRODUCT=$(basename $(dirname "$SCRIPT_DIR"))
fi
. $SCRIPT_DIR/.addonenv.sh -script_dir $SCRIPT_DIR
. $SCRIPT_DIR/.utilenv.sh
. $SCRIPT_DIR/.padogrid_completion.bash

#
# Echos Jet jobs
# @required COMP_WORDS
# @param jobStatus "RUNNING" for running jobs, "SUSPENDED" for suspended jobs, else for all jobs.
#                  Case sensitive. Must be all uppercase.
__get_jet_jobs()
{
   local jobStatus=$1
   local __ADDRESSES __PREV_WORD __WORD __PREV_WORD __ADDRESSES __JOBS
   __ADDRESSES=""
   __PREV_WORD=""
   for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
      __WORD="${COMP_WORDS[$i]}"
      if [ "$__PREV_WORD" == "-a" ]; then
         __ADDRESSES="-a $__WORD" 
      elif [ "$__PREV_WORD" == "--addresses" ]; then
         __ADDRESSES="--addresses $__WORD" 
      fi
      __PREV_WORD=$__WORD
   done
   if [ $HAZELCAST_MAJOR_VERSION_NUMBER -ge 5 ]; then
      __JOBS=$(hz-cli $__ADDRESSES list-jobs)
   elif [ $HAZELCAST_MAJOR_VERSION_NUMBER -eq 4 ]; then
      __JOBS=$(jet $__ADDRESSES list-jobs)
   else
      __JOBS=$(jet.sh $__ADDRESSES list-jobs)
   fi
   if [ "$jobStatus" == "RUNNING" ] || [ "$jobStatus" == "SUSPENDED" ]; then
      __JOBS=$(echo "$__JOBS" | sed -e 's/ID.*NAME//' | grep ${jobStatus} | sed -e 's/ .*$//')
   else
      __JOBS=$(echo "$__JOBS" | sed -e 's/ID.*NAME//' | sed -e 's/ .*$//')
   fi
   echo "$__JOBS"
}

#
# Echos Jet snapshots with spaces in snapshot names replaced with '|'
# @required COMP_WORDS
#
__get_jet_snapshots()
{
   local jobStatus=$1
   local __ADDRESSES __PREV_WORD __WORD __PREV_WORD __ADDRESSES __SNAPSHOTS __POS
   __ADDRESSES=""
   __PREV_WORD=""
   for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
      __WORD="${COMP_WORDS[$i]}"
      if [ "$__PREV_WORD" == "-a" ]; then
         __ADDRESSES="-a $__WORD" 
      elif [ "$__PREV_WORD" == "--addresses" ]; then
         __ADDRESSES="--addresses $__WORD" 
      fi
      __PREV_WORD=$__WORD
   done

   __TMP_FILE=/tmp/__dump.txt
   if [ $HAZELCAST_MAJOR_VERSION_NUMBER -ge 4 ]; then
      jet $__ADDRESSES list-snapshots > $__TMP_FILE
   else
      jet.sh $__ADDRESSES list-snapshots > $__TMP_FILE
   fi
   while IFS= read -r line; do
      line=$(echo "$line" | sed -e 's/ /|/g')
      if [[ $line == TIME** ]]; then
         __POS=$(__get_str_position "$line" "SNAPSHOT")
      else
         __SNAPSHOTS="$__SNAPSHOTS ${line:$__POS}"
      fi
   done < "$__TMP_FILE"
   echo $__SNAPSHOTS
}

__hz_complete()
{
   local word cur_word prev_word type_list commands len

   # COMP_WORDS is an array of words in the current command line.
   # COMP_CWORD is the index of the current word (the one the cursor is
   # in). So COMP_WORDS[COMP_CWORD] is the current word.
   second_word="${COMP_WORDS[1]}"
   third_word="${COMP_WORDS[2]}"
   cur_word="${COMP_WORDS[COMP_CWORD]}"
   prev_word="${COMP_WORDS[COMP_CWORD-1]}"
   len=${#COMP_WORDS[@]}
   if [ $len -gt 2 ]; then
      before_prev_word="${COMP_WORDS[COMP_CWORD-2]}"
   fi
   local is_path="false"

   case "$second_word" in
      start)
      type_list="-c --config -p --port -i --interface"
      for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
         __WORD="${COMP_WORDS[$i]}"
         if [ "$__WORD" != "$cur_word" ]; then
            type_list=${type_list/$__WORD/}
            if [ "$__WORD" == "-c" ]; then
               type_list=${type_list/--config/}
            elif [ "$__WORD" == "--config" ]; then
               type_list=${type_list/-c/}
            elif [ "$__WORD" == "-p" ]; then
               type_list=${type_list/--port/}
            elif [ "$__WORD" == "--port" ]; then
               type_list=${type_list/-p/}
            elif [ "$__WORD" == "-i" ]; then
              type_list=${type_list/--interface/}
            elif [ "$__WORD" == "--interface" ]; then
               type_list=${type_list/-i/}
            fi
         fi
      done
      ;;

   *)
      type_list="-V --version -h --help start"
      for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
         __WORD="${COMP_WORDS[$i]}"
         if [ "$__WORD" != "$cur_word" ]; then
            type_list=${type_list/$__WORD/}
            if [ "$__WORD" == "-V" ]; then
               type_list=${type_list/--version/}
            elif [ "$__WORD" == "--version" ]; then
               type_list=${type_list/-v/}
            elif [ "$__WORD" == "-h" ]; then
               type_list=${type_list/--help/}
            elif [ "$__WORD" == "--help" ]; then
               type_list=${type_list/-h/}
            elif [ "$__WORD" == "-c" ]; then
               type_list=${type_list/--config/}
            elif [ "$__WORD" == "--config" ]; then
               type_list=${type_list/-c/}
            fi
         fi
      done
      ;;
   esac

   case "$prev_word" in
   -V|--version)
      type_list=""
      ;;
   -h|--help)
      type_list=""
      ;;
   -c|--config)
      is_path="true"
      ;;
   -p|--port)
      type_list="5701"
      ;;
   -i|--interface)
      type_list="0.0.0.0 $(getHostIpAddresses)"
      ;;
   *)
      ;;
   esac

   # If -c or --config then default the next word to file name
   __getArrayElementIndex "-c" "${COMP_WORDS[@]}"
   local index=$?
   if [ $index -eq 255 ]; then
      __getArrayElementIndex "--config" "${COMP_WORDS[@]}"
      index=$?
   fi
   if [ $index -ne 255 ]; then
      let last_index=len-2
      if [ $index -eq $last_index ]; then
         is_path="true"
      fi
      if [ "$is_path" == "true" ]; then
         COMPREPLY=( $( compgen -f -- "$cur_word" ))
         return 0
      fi
   fi

   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__hz_healthcheck_complete()
{
   local cur_word prev_word type_list commands len

   # COMP_WORDS is an array of words in the current command line.
   # COMP_CWORD is the index of the current word (the one the cursor is
   # in). So COMP_WORDS[COMP_CWORD] is the current word.
   second_word="${COMP_WORDS[1]}"
   third_word="${COMP_WORDS[2]}"
   cur_word="${COMP_WORDS[COMP_CWORD]}"
   prev_word="${COMP_WORDS[COMP_CWORD-1]}"
   len=${#COMP_WORDS[@]}
   if [ $len -gt 2 ]; then
      before_prev_word="${COMP_WORDS[COMP_CWORD-2]}"
   fi
   local is_path="false"

   case "$before_prev_word" in
   *)
      type_list="-o --operation -a --address -p --port -d --debug --https --cacert --cert --key --insecure -h --help"

      for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
         __WORD="${COMP_WORDS[$i]}"
         if [ "$__WORD" != "$cur_word" ]; then
            type_list=${type_list/$__WORD/}
            if [ "$__WORD" == "-o" ]; then
               type_list=${type_list/--operation/}
            elif [ "$__WORD" == "--operation" ]; then
               type_list=${type_list/-o/}
            elif [ "$__WORD" == "-a" ]; then
               type_list=${type_list/--address/}
            elif [ "$__WORD" == "--address" ]; then
               type_list=${type_list/-a/}
            elif [ "$__WORD" == "-p" ]; then
               type_list=${type_list/--port/}
            elif [ "$__WORD" == "--port" ]; then
               type_list=${type_list/-p/}
            elif [ "$__WORD" == "-d" ]; then
               type_list=${type_list/--debug/}
            elif [ "$__WORD" == "--debug" ]; then
               type_list=${type_list/-d/}
            elif [ "$__WORD" == "-d" ]; then
               type_list=${type_list/--debug/}
            elif [ "$__WORD" == "--debug" ]; then
               type_list=${type_list/-d/}
            elif [ "$__WORD" == "-h" ]; then
               type_list=${type_list/--help/}
            elif [ "$__WORD" == "--help" ]; then
               type_list=${type_list/-h/}
            fi
         fi
      done
      ;;
   esac

   case "$prev_word" in
   -o|--operation)
      type_list="all node-state cluster-state cluster-safe migration-queue-size cluster-size"
      ;;
   -a|--address)
      type_list="127.0.0.1"
      ;;
   -p|--port)
      type_list="5701"
      ;;
   -d|--debug)
      type_list=""
      ;;
   -h|--help)
      type_list=""
      ;;
   --cacert|--cert|--key)
      is_path="true"
      ;;
   *)
      ;;
   esac

   if [ "$is_path" == "true" ]; then
      COMPREPLY=( $( compgen -f -- "$cur_word" ))
   elif [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__hz_cluster_cp_admin_complete()
{
   local cur_word prev_word type_list commands len

   # COMP_WORDS is an array of words in the current command line.
   # COMP_CWORD is the index of the current word (the one the cursor is
   # in). So COMP_WORDS[COMP_CWORD] is the current word.
   second_word="${COMP_WORDS[1]}"
   third_word="${COMP_WORDS[2]}"
   cur_word="${COMP_WORDS[COMP_CWORD]}"
   prev_word="${COMP_WORDS[COMP_CWORD-1]}"
   len=${#COMP_WORDS[@]}
   if [ $len -gt 2 ]; then
      before_prev_word="${COMP_WORDS[COMP_CWORD-2]}"
   fi
   local is_path="false"

   case "$before_prev_word" in
   *)
      type_list="-o --operation -g --group -m --member -s --session-id -a --address -p --port -c --clustername -d --debug --https --cacert --cert --key --insecure -h --help"

      for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
         __WORD="${COMP_WORDS[$i]}"
         if [ "$__WORD" != "$cur_word" ]; then
            type_list=${type_list/$__WORD/}
            if [ "$__WORD" == "-o" ]; then
               type_list=${type_list/--operation/}
            elif [ "$__WORD" == "--operation" ]; then
               type_list=${type_list/-o/}
            elif [ "$__WORD" == "-g" ]; then
               type_list=${type_list/--group/}
            elif [ "$__WORD" == "--group" ]; then
               type_list=${type_list/-g/}
            elif [ "$__WORD" == "-m" ]; then
               type_list=${type_list/--member/}
            elif [ "$__WORD" == "--member" ]; then
               type_list=${type_list/-m/}
            elif [ "$__WORD" == "-s" ]; then
               type_list=${type_list/--session/}
            elif [ "$__WORD" == "--session" ]; then
               type_list=${type_list/-s/}
            elif [ "$__WORD" == "-a" ]; then
               type_list=${type_list/--address/}
            elif [ "$__WORD" == "--address" ]; then
               type_list=${type_list/-a/}
            elif [ "$__WORD" == "-p" ]; then
               type_list=${type_list/--port/}
            elif [ "$__WORD" == "--port" ]; then
               type_list=${type_list/-p/}
            elif [ "$__WORD" == "-c" ]; then
               type_list=${type_list/--clustername/}
            elif [ "$__WORD" == "--clustername" ]; then
               type_list=${type_list/-c/}
            elif [ "$__WORD" == "-P" ]; then
               type_list=${type_list/--password/}
            elif [ "$__WORD" == "--password" ]; then
               type_list=${type_list/-P/}
            elif [ "$__WORD" == "-d" ]; then
               type_list=${type_list/--debug/}
            elif [ "$__WORD" == "--debug" ]; then
               type_list=${type_list/-d/}
            elif [ "$__WORD" == "-d" ]; then
               type_list=${type_list/--debug/}
            elif [ "$__WORD" == "--debug" ]; then
               type_list=${type_list/-d/}
            elif [ "$__WORD" == "-h" ]; then
               type_list=${type_list/--help/}
            elif [ "$__WORD" == "--help" ]; then
               type_list=${type_list/-h/}
            fi
         fi
      done
      ;;
   esac

   case "$prev_word" in
   -o|--operation)
      type_list="get-local-member get-groups get-group force-destroy-group get-members remove-member promote-member get-sessions force-close-session reset"
      ;;
   -g|--group)
      type_list="group"
      ;;
   -m|--member)
      type_list="uuid"
      ;;
   -s|--session-id)
      type_list="session-id"
      ;;
   -a|--address)
      type_list="127.0.0.1"
      ;;
   -p|--port)
      type_list="5701"
      ;;
   -c|--clustername)
      type_list="dev"
      ;;
   -P|--password)
      type_list="dev-pass"
      ;;
   -d|--debug)
      type_list=""
      ;;
   -h|--help|--https|--insecure)
      type_list=""
      ;;
   --cacert|--cert|--key)
      is_path="true"
      ;;
   *)
      ;;
   esac

   if [ "$is_path" == "true" ]; then
      COMPREPLY=( $( compgen -f -- "$cur_word" ))
   elif [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__jet_complete()
{
   local cur_word prev_word type_list commands len before_prev_word

   # COMP_WORDS is an array of words in the current command line.
   # COMP_CWORD is the index of the current word (the one the cursor is
   # in). So COMP_WORDS[COMP_CWORD] is the current word.
   len=${#COMP_WORDS[@]}
   second_word="${COMP_WORDS[1]}"
   third_word="${COMP_WORDS[2]}"
   cur_word="${COMP_WORDS[COMP_CWORD]}"
   prev_word="${COMP_WORDS[COMP_CWORD-1]}"
   local is_path="false"

   # If submit then default the next word to file name
   __getArrayElementIndex "submit" "${COMP_WORDS[@]}"
   local index=$?
   if [ $index -ne 255 ]; then
      let last_index=len-2
      if [ $index -eq $last_index ]; then
         is_path="true"
      fi
      if [ "$is_path" == "true" ]; then
         COMPREPLY=( $( compgen -f -- "$cur_word" ))
         return 0
      fi
      return 0
   fi

   if [ $len -gt 2 ]; then
      before_prev_word="${COMP_WORDS[COMP_CWORD-2]}"
   fi
   case "$before_prev_word" in
      help|cancel|cluster|delete-snapshot|list-jobs|list-snapshots|restart|resume|save-snapshot|submit|suspend)
      type_list=""
      ;;
   *)
      if [ $HAZELCAST_MAJOR_VERSION_NUMBER -ge 4 ]; then
         type_list="-t --targets -h --help -V --version -f --config -v --verbosity -a --addresses -n --cluster-name help cancel cluster delete-snapshot list-jobs list-snapshots restart resume submit suspend"
      else
         type_list="-h --help -V --version -f --config -v --verbosity -a --addresses -g --group help cancel cluster delete-snapshot list-jobs list-snapshots restart resume submit suspend"
      fi

      for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
         __WORD="${COMP_WORDS[$i]}"
         if [ "$__WORD" != "$cur_word" ]; then
            type_list=${type_list/$__WORD/}
            if [ "$__WORD" == "-g" ]; then
               type_list=${type_list/--group/}
            elif [ "$__WORD" == "--group" ]; then
               type_list=${type_list/-g/}
            elif [ "$__WORD" == "-a" ]; then
               type_list=${type_list/--addresses/}
            elif [ "$__WORD" == "--addresses" ]; then
               type_list=${type_list/-a/}
            elif [ "$__WORD" == "-n" ]; then
               type_list=${type_list/--cluster-name/}
            elif [ "$__WORD" == "--cluster-name" ]; then
               type_list=${type_list/-n/}
            elif [ "$__WORD" == "-v" ]; then
               type_list=${type_list/--version/}
            elif [ "$__WORD" == "--version" ]; then
               type_list=${type_list/-v/}
            elif [ "$__WORD" == "-f" ]; then
               type_list=${type_list/--config/}
            elif [ "$__WORD" == "--config" ]; then
               type_list=${type_list/-f/}
            elif [ "$__WORD" == "-h" ]; then
               type_list=${type_list/--help/}
            elif [ "$__WORD" == "--help" ]; then
               type_list=${type_list/-h/}
            fi
         fi
      done
      ;;
   esac

   case "$prev_word" in
   -a|--addresses)
      type_list="localhost:5701"
      ;;
   -n|--cluster-name)
      type_list="jet"
      ;;
   -g|--group)
      type_list="dev"
      ;;
   -f|--config)
      type_list=""
      ;;
   -h|--help)
      type_list=""
      ;;
   help)
      type_list=""
      ;;
   cancel)
      type_list=$(__get_jet_jobs)
      ;;
   restart|suspend)
      type_list=$(__get_jet_jobs "RUNNING")
      ;;
   resume)
      type_list=$(__get_jet_jobs "SUSPENDED")
      ;;
   cluster)
      type_list=""
      ;;
   save-snapshot)
      type_list=""
      ;;
   delete-snapshot)
      type_list="$(__get_jet_snapshots)"
      for iter in $type_list; do
         # only reply with completions
         if [[ $iter =~ ^$cur ]]; then
             # swap back our escaped spaces
             COMPREPLY+=( "${iter//|/ }" )
         fi
      done
      return 0
      ;;
   *)
      ;;
   esac

   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__hz-cli_complete()
{
   local cur_word prev_word type_list commands len before_prev_word

   # COMP_WORDS is an array of words in the current command line.
   # COMP_CWORD is the index of the current word (the one the cursor is
   # in). So COMP_WORDS[COMP_CWORD] is the current word.
   len=${#COMP_WORDS[@]}
   second_word="${COMP_WORDS[1]}"
   third_word="${COMP_WORDS[2]}"
   cur_word="${COMP_WORDS[COMP_CWORD]}"
   prev_word="${COMP_WORDS[COMP_CWORD-1]}"
   local is_path="false"

   # If submit then default the next word to file name
   __getArrayElementIndex "submit" "${COMP_WORDS[@]}"
   local index=$?
   if [ $index -ne 255 ]; then
      let last_index=len-2
      if [ $index -eq $last_index ]; then
         is_path="true"
      fi
      if [ "$is_path" == "true" ]; then
         COMPREPLY=( $( compgen -f -- "$cur_word" ))
         return 0
      fi
      return 0
   fi

   if [ $len -gt 2 ]; then
      before_prev_word="${COMP_WORDS[COMP_CWORD-2]}"
   fi
   case "$before_prev_word" in
      help|cancel|cluster|delete-snapshot|list-jobs|list-snapshots|restart|resume|save-snapshot|submit|suspend)
      type_list=""
      ;;
   *)
      type_list="--ignore-version-mismatch -t --targets -h --help -V --version -f --config -v --verbosity -n -c --class -s --snapshot help cancel cluster console delete-snapshot list-jobs list-snapshots restart resume save-snapshot sql submit suspend"

      for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
         __WORD="${COMP_WORDS[$i]}"
         if [ "$__WORD" != "$cur_word" ]; then
            type_list=${type_list/$__WORD/}
            if [ "$__WORD" == "--ignore-version-mismatch" ]; then
               type_list=${type_list/--ignore-version-mismatch/}
            elif [ "$__WORD" == "-g" ]; then
               type_list=${type_list/--group/}
            elif [ "$__WORD" == "--group" ]; then
               type_list=${type_list/-g/}
            elif [ "$__WORD" == "-n" ]; then
               type_list=${type_list/--cluster-name/}
            elif [ "$__WORD" == "-v" ]; then
               type_list=${type_list/--version/}
            elif [ "$__WORD" == "--version" ]; then
               type_list=${type_list/-v/}
            elif [ "$__WORD" == "-f" ]; then
               type_list=${type_list/--config/}
            elif [ "$__WORD" == "--config" ]; then
               type_list=${type_list/-f/}
            elif [ "$__WORD" == "-c" ]; then
               type_list=${type_list/--class/}
            elif [ "$__WORD" == "--class" ]; then
               type_list=${type_list/-c/}
            elif [ "$__WORD" == "-s" ]; then
               type_list=${type_list/--snapshot/}
            elif [ "$__WORD" == "--snapshot" ]; then
               type_list=${type_list/-s/}
            elif [ "$__WORD" == "-v" ]; then
               type_list=${type_list/--verbosity/}
            elif [ "$__WORD" == "--verbosity" ]; then
               type_list=${type_list/-v/}
            elif [ "$__WORD" == "-b" ]; then
               type_list=${type_list/--help/}
            elif [ "$__WORD" == "--help" ]; then
               type_list=${type_list/-h/}
            fi
         fi
      done
      ;;
   esac

   case "$prev_word" in
   --ignore-version-mismatch)
      type_list=""
      ;;
   -t|--targets)
     type_list="dev@localhost:5701"
     ;;
   -f|--config)
      type_list=""
      ;;
   -h|--help)
      type_list=""
      ;;
   help)
      type_list="help cancel cluster console delete-snapshot list-jobs list-snapshots restart resume save-snapshot sql submit suspend"
      ;;
   cancel)
      type_list=$(__get_jet_jobs)
      ;;
   restart|suspend)
      type_list=$(__get_jet_jobs "RUNNING")
      ;;
   resume)
      type_list=$(__get_jet_jobs "SUSPENDED")
      ;;
   cluster)
      type_list=""
      ;;
   save-snapshot)
      type_list="" ;;
   delete-snapshot)
      type_list="$(__get_jet_snapshots)"
      for iter in $type_list; do
         # only reply with completions
         if [[ $iter =~ ^$cur ]]; then
             # swap back our escaped spaces
             COMPREPLY+=( "${iter//|/ }" )
         fi
      done
      return 0
      ;;
   *)
      ;;
   esac

   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__cluster_complete()
{
   local cur_word prev_word type_list commands len

   # COMP_WORDS is an array of words in the current command line.
   # COMP_CWORD is the index of the current word (the one the cursor is
   # in). So COMP_WORDS[COMP_CWORD] is the current word.
   second_word="${COMP_WORDS[1]}"
   third_word="${COMP_WORDS[2]}"
   cur_word="${COMP_WORDS[COMP_CWORD]}"
   prev_word="${COMP_WORDS[COMP_CWORD-1]}"
   len=${#COMP_WORDS[@]}
   if [ $len -gt 2 ]; then
      before_prev_word="${COMP_WORDS[COMP_CWORD-2]}"
   fi

   case "$before_prev_word" in
   *)
      type_list="-o --operation -s --state -a --address -p --port -g --groupname -P --password -v --version -h --help"
      if [ "$CLUSTER_TYPE" == "imdg" ] || [ $HAZELCAST_MAJOR_VERSION_NUMBER -ge 5 ]; then
         type_list="$type_list -d --debug --https --cacert --cert --key --insecure"
      fi

      for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
         __WORD="${COMP_WORDS[$i]}"
         if [ "$__WORD" != "$cur_word" ]; then
            type_list=${type_list/$__WORD/}
            if [ "$__WORD" == "-o" ]; then
               type_list=${type_list/--operation/}
            elif [ "$__WORD" == "--operation" ]; then
               type_list=${type_list/-o/}
            elif [ "$__WORD" == "-s" ]; then
               type_list=${type_list/--state/}
            elif [ "$__WORD" == "--state" ]; then
               type_list=${type_list/-s/}
            elif [ "$__WORD" == "-a" ]; then
               type_list=${type_list/--address/}
            elif [ "$__WORD" == "--address" ]; then
               type_list=${type_list/-a/}
            elif [ "$__WORD" == "-p" ]; then
               type_list=${type_list/--port/}
            elif [ "$__WORD" == "--port" ]; then
               type_list=${type_list/-p/}
            elif [ "$__WORD" == "-g" ]; then
               type_list=${type_list/--groupname/}
            elif [ "$__WORD" == "--groupname" ]; then
               type_list=${type_list/-g/}
            elif [ "$__WORD" == "-P" ]; then
               type_list=${type_list/--password/}
            elif [ "$__WORD" == "--password" ]; then
               type_list=${type_list/-P/}
            elif [ "$__WORD" == "-v" ]; then
               type_list=${type_list/--version/}
            elif [ "$__WORD" == "--version" ]; then
               type_list=${type_list/-v/}
            elif [ "$__WORD" == "-d" ]; then
               type_list=${type_list/--debug/}
            elif [ "$__WORD" == "--debug" ]; then
               type_list=${type_list/-d/}
            elif [ "$__WORD" == "-h" ]; then
               type_list=${type_list/--help/}
            elif [ "$__WORD" == "--help" ]; then
               type_list=${type_list/-h/}
            fi
         fi
      done
      ;;
   esac

   case "$prev_word" in
   -o|--operation)
      type_list="get-state change-state shutdown force-start get-cluster-version change-cluster-version"
      if [ "$CLUSTER_TYPE" == "imdg" ] || [ $HAZELCAST_MAJOR_VERSION_NUMBER -ge 5 ]; then
         type_list="$type_list partial-start"
      fi
      ;;
   -s|--state)
      type_list="active frozen passive no_migration"
      ;;
   -a|--address)
      type_list="127.0.0.1"
      ;;
   -p|--port)
      type_list="5701"
      ;;
   -g|--groupname)
      type_list="dev"
      ;;
   -P|--password)
      type_list="dev-pass"
      ;;
   -v|--version)
      type_list=""
      ;;
   -d|--debug)
      type_list=""
      ;;
   -h|--help)
      type_list=""
      ;;
   *)
      ;;
   esac

   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__jet_complete()
{
   local cur_word prev_word type_list commands len before_prev_word

   # COMP_WORDS is an array of words in the current command line.
   # COMP_CWORD is the index of the current word (the one the cursor is
   # in). So COMP_WORDS[COMP_CWORD] is the current word.
   len=${#COMP_WORDS[@]}
   second_word="${COMP_WORDS[1]}"
   third_word="${COMP_WORDS[2]}"
   cur_word="${COMP_WORDS[COMP_CWORD]}"
   prev_word="${COMP_WORDS[COMP_CWORD-1]}"
   local is_path="false"

   # If submit then default the next word to file name
   __getArrayElementIndex "submit" "${COMP_WORDS[@]}"
   local index=$?
   if [ $index -ne 255 ]; then
      let last_index=len-2
      if [ $index -eq $last_index ]; then
         is_path="true"
      fi
      if [ "$is_path" == "true" ]; then
         COMPREPLY=( $( compgen -f -- "$cur_word" ))
         return 0
      fi
      return 0
   fi

   if [ $len -gt 2 ]; then
      before_prev_word="${COMP_WORDS[COMP_CWORD-2]}"
   fi
   case "$before_prev_word" in
      help|cancel|cluster|delete-snapshot|list-jobs|list-snapshots|restart|resume|save-snapshot|submit|suspend)
      type_list=""
      ;;
   *)
      if [ $HAZELCAST_MAJOR_VERSION_NUMBER -ge 4 ]; then
         type_list="-t --targets -h --help -V --version -f --config -v --verbosity -a --addresses -n --cluster-name help cancel cluster delete-snapshot list-jobs list-snapshots restart resume submit suspend"
      else
         type_list="-h --help -V --version -f --config -v --verbosity -a --addresses -g --group help cancel cluster delete-snapshot list-jobs list-snapshots restart resume submit suspend"
      fi

      for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
         __WORD="${COMP_WORDS[$i]}"
         if [ "$__WORD" != "$cur_word" ]; then
            type_list=${type_list/$__WORD/}
            if [ "$__WORD" == "-g" ]; then
               type_list=${type_list/--group/}
            elif [ "$__WORD" == "--group" ]; then
               type_list=${type_list/-g/}
            elif [ "$__WORD" == "-a" ]; then
               type_list=${type_list/--addresses/}
            elif [ "$__WORD" == "--addresses" ]; then
               type_list=${type_list/-a/}
            elif [ "$__WORD" == "-n" ]; then
               type_list=${type_list/--cluster-name/}
            elif [ "$__WORD" == "--cluster-name" ]; then
               type_list=${type_list/-n/}
            elif [ "$__WORD" == "-v" ]; then
               type_list=${type_list/--version/}
            elif [ "$__WORD" == "--version" ]; then
               type_list=${type_list/-v/}
            elif [ "$__WORD" == "-f" ]; then
               type_list=${type_list/--config/}
            elif [ "$__WORD" == "--config" ]; then
               type_list=${type_list/-f/}
            elif [ "$__WORD" == "-h" ]; then
               type_list=${type_list/--help/}
            elif [ "$__WORD" == "--help" ]; then
               type_list=${type_list/-h/}
            fi
         fi
      done
      ;;
   esac

   case "$prev_word" in
   -a|--addresses)
      type_list="localhost:5701"
      ;;
   -n|--cluster-name)
      type_list="jet"
      ;;
   -g|--group)
      type_list="dev"
      ;;
   -f|--config)
      type_list=""
      ;;
   -h|--help)
      type_list=""
      ;;
   help)
      type_list=""
      ;;
   cancel)
      type_list=$(__get_jet_jobs)
      ;;
   restart|suspend)
      type_list=$(__get_jet_jobs "RUNNING")
      ;;
   resume)
      type_list=$(__get_jet_jobs "SUSPENDED")
      ;;
   cluster)
      type_list=""
      ;;
   save-snapshot)
      type_list="" ;;
   delete-snapshot)
      type_list="$(__get_jet_snapshots)"
      for iter in $type_list; do
         # only reply with completions
         if [[ $iter =~ ^$cur ]]; then
             # swap back our escaped spaces
             COMPREPLY+=( "${iter//|/ }" )
         fi
      done
      return 0
      ;;
   *)
      ;;
   esac

   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__hz-cli_complete()
{
   local cur_word prev_word type_list commands len before_prev_word

   # COMP_WORDS is an array of words in the current command line.
   # COMP_CWORD is the index of the current word (the one the cursor is
   # in). So COMP_WORDS[COMP_CWORD] is the current word.
   len=${#COMP_WORDS[@]}
   second_word="${COMP_WORDS[1]}"
   third_word="${COMP_WORDS[2]}"
   cur_word="${COMP_WORDS[COMP_CWORD]}"
   prev_word="${COMP_WORDS[COMP_CWORD-1]}"
   local is_path="false"

   # If submit then default the next word to file name
   __getArrayElementIndex "submit" "${COMP_WORDS[@]}"
   local index=$?
   if [ $index -ne 255 ]; then
      let last_index=len-2
      if [ $index -eq $last_index ]; then
         is_path="true"
      fi
      if [ "$is_path" == "true" ]; then
         COMPREPLY=( $( compgen -f -- "$cur_word" ))
         return 0
      fi
      return 0
   fi

   if [ $len -gt 2 ]; then
      before_prev_word="${COMP_WORDS[COMP_CWORD-2]}"
   fi
   case "$before_prev_word" in
      help|cancel|cluster|delete-snapshot|list-jobs|list-snapshots|restart|resume|save-snapshot|submit|suspend)
      type_list=""
      ;;
   *)
      type_list="-t --targets -h --help -V --version -f --config -v --verbosity -n -c --class -s --snapshot help cancel cluster console delete-snapshot list-jobs list-snapshots restart resume save-snapshot sql submit suspend"

      for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
         __WORD="${COMP_WORDS[$i]}"
         if [ "$__WORD" != "$cur_word" ]; then
            type_list=${type_list/$__WORD/}
            if [ "$__WORD" == "-g" ]; then
               type_list=${type_list/--group/}
            elif [ "$__WORD" == "--group" ]; then
               type_list=${type_list/-g/}
            elif [ "$__WORD" == "-n" ]; then
               type_list=${type_list/--cluster-name/}
            elif [ "$__WORD" == "-v" ]; then
               type_list=${type_list/--version/}
            elif [ "$__WORD" == "--version" ]; then
               type_list=${type_list/-v/}
            elif [ "$__WORD" == "-f" ]; then
               type_list=${type_list/--config/}
            elif [ "$__WORD" == "--config" ]; then
               type_list=${type_list/-f/}
            elif [ "$__WORD" == "-c" ]; then
               type_list=${type_list/--class/}
            elif [ "$__WORD" == "--class" ]; then
               type_list=${type_list/-c/}
            elif [ "$__WORD" == "-s" ]; then
               type_list=${type_list/--snapshot/}
            elif [ "$__WORD" == "--snapshot" ]; then
               type_list=${type_list/-s/}
            elif [ "$__WORD" == "-v" ]; then
               type_list=${type_list/--verbosity/}
            elif [ "$__WORD" == "--verbosity" ]; then
               type_list=${type_list/-v/}
            elif [ "$__WORD" == "-b" ]; then
               type_list=${type_list/--help/}
            elif [ "$__WORD" == "--help" ]; then
               type_list=${type_list/-h/}
            fi
         fi
      done
      ;;
   esac

   case "$prev_word" in
   -t|--targets)
     type_list="dev@localhost:5701"
     ;;
   -f|--config)
      type_list=""
      ;;
   -h|--help)
      type_list=""
      ;;
   help)
      type_list="help cancel cluster console delete-snapshot list-jobs list-snapshots restart resume save-snapshot sql submit suspend"
      ;;
   cancel)
      type_list=$(__get_jet_jobs)
      ;;
   restart|suspend)
      type_list=$(__get_jet_jobs "RUNNING")
      ;;
   resume)
      type_list=$(__get_jet_jobs "SUSPENDED")
      ;;
   cluster)
      type_list=""
      ;;
   save-snapshot)
      type_list="" ;;
   delete-snapshot)
      type_list="$(__get_jet_snapshots)"
      for iter in $type_list; do
         # only reply with completions
         if [[ $iter =~ ^$cur ]]; then
             # swap back our escaped spaces
             COMPREPLY+=( "${iter//|/ }" )
         fi
      done
      return 0
      ;;
   *)
      ;;
   esac
   #
   # Remove typed options from the list
   for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
      __WORD="${COMP_WORDS[$i]}"
      if [[ "$__WORD" == "-"* ]] && [ "$__WORD" != "$cur_word" ]; then
         type_list=${type_list/$__WORD/}
      fi
   done

   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

# Register hz
complete -F __hz_complete -o bashdefault hz

# Register cluster.sh, hz-cluster-admin
complete -F __cluster_complete -o bashdefault cluster.sh hz-cluster-admin

# Register hz-healthcheck
complete -F __hz_healthcheck_complete -o bashdefault hz-healthcheck

# Register hz-cluster-cp-admin
complete -F __hz_cluster_cp_admin_complete -o bashdefault hz-cluster-cp-admin

# Register jet.sh
complete -F __jet_complete -o bashdefault jet.sh
complete -F __jet_complete -o bashdefault jet
complete -F __hz-cli_complete -o bashdefault hz-cli
