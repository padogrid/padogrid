#!/usr/bin/env bash

# ========================================================================
# Copyright (c) 2020 Netcrest Technologies, LLC. All rights reserved.
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
fi
. $SCRIPT_DIR/.addonenv.sh -script_dir $SCRIPT_DIR
. $SCRIPT_DIR/.utilenv.sh

__get_pod()
{
   local __found=false
   for i in "${COMP_WORDS[@]}"; do
      if [ "$__found" == "true" ]; then
         POD=$i
         break;
      elif [ "$i" == "-pod" ]; then
         __found="true"
      fi
   done  
   echo "$POD"
}

__get_cluster()
{
   local __CLUSTER
   local __found=false 
   for i in "${COMP_WORDS[@]}"; do
      if [ "$__found" == "true" ]; then
         __CLUSTER=$i
         break;
      elif [ "$i" == "-cluster" ]; then
         __found="true"
      fi
   done  
   echo "$__CLUSTER"
}

__padogrid_complete()
{
   local second_word cur_word prev_word type_list commands len

   # COMP_WORDS is an array of words in the current command line.
   # COMP_CWORD is the index of the current word (the one the cursor is
   # in). So COMP_WORDS[COMP_CWORD] is the current word.
   second_word="${COMP_WORDS[1]}"
   third_word="${COMP_WORDS[2]}"
   cur_word="${COMP_WORDS[COMP_CWORD]}"
   prev_word="${COMP_WORDS[COMP_CWORD-1]}"
   len=${#COMP_WORDS[@]}
      
   type_list=""

   case "$prev_word" in
   -?)
      type_list=""
      ;;
      
   -pod)
      if [ "$command" != "find_padogrid" ]; then
         type_list=`getPods`
      fi
      ;;

   -count)
      type_list="1 2 3 4 5 6 7 8 9"
      ;;
   
   -app)
      if [ "$second_word" == "create_app" ]; then
         type_list=`getAddonApps $CLUSTER_TYPE`
      elif [ "$second_word" != "find_padogrid" ]; then
         type_list=`getApps`
      fi
      ;;

   -port)
      if [ "$second_word" == "create_cluster" ] || [ "$second_word" == "create_docker" ] || [ "$second_word" == "create_grid" ]; then
         type_list="$DEFAULT_MEMBER_START_PORT"
      fi
      ;;

   -cluster)
      if [ "$second_word" == "create_k8s" ] || [ "$second_word" == "remove_k8s" ]; then
         __ENV="k8s"
      elif [ "$second_word" == "create_docker" ] || [ "$second_word" == "remove_docker" ]; then
         __ENV="docker"
      else
         __ENV="clusters"
      fi
      if [ "$second_word" != "find_padogrid" ]; then
         type_list=`getClusters $__ENV`
      fi
      ;;

   -prefix)
      if [ "$second_word" == "create_grid" ]; then
         type_list="grid"
      fi
     ;;

   -type)
      if [ "$second_word" == "create_pod" ]; then
         type_list="local vagrant"
      elif [ "$second_word" == "create_cluster" ] || [ "$second_word" == "create_grid" ]; then
         type_list="default pado"
      fi
      ;;

   -k8s) 
      if [ "$second_word" == "create_k8s" ]; then
         type_list="minikube"
      elif [ "$second_word" != "find_padogrid" ]; then
         type_list=`getClusters k8s`
      fi
      ;;

   -docker) 
      if [ "$second_word" == "create_bundle" ]; then
         type_list=`getClusters docker`
      elif [ "$second_word" != "find_padogrid" ]; then
         type_list="compose"
      fi
      ;;

   -product)
      if [ "$sconde_word" == "show_bundle" ]; then
         type_list="$BUNDLE_PRODUCT_LIST"
      fi
      ;;

   -rwe)
      if [ "$second_word" != "find_padogrid" ]; then
         type_list=`getRweList`
      fi
      ;;
      
   -workspace)
      if [ "$second_word" == "install_bundle" ]; then
         type_list=`$second_word -options`
      elif [ "$second_word" != "find_padogrid" ]; then
         type_list=`getWorkspaces`
      fi
      ;;

   -host)
      if [ "$second_word" == "create_docker" ]; then
         type_list="$(getHostIPv4List) host.docker.internal"
      fi
      ;;

   -user)
      GITHUB_USERS=""
      if [ -f "$HOME/.padogrid/setenv.sh" ]; then
         . $HOME/.padogrid/setenv.sh
      fi
      type_list="padogrid $GITHUB_USERS"
      ;;

   -githost)
      type_list="github gitea"
      ;;

   -connect)
      type_list="https ssh"
      ;;

   -log)
      type_list="data gc diag mc"
      ;;
   -num)
      #   type_list="1 2 3 4 5 6 7 8 9"
#break;
      # required: MEMBER_PREFIX, POD, CLUSTER, NODE_NAME_PREFIX
   #   POD=`__get_pod`
      POD=`getClusterProperty "pod.name" $POD`
      CLUSTER=`__get_cluster`
      NODE_NAME_PREFIX=`getPodProperty "node.name.prefix" $NODE_NAME_PREFIX`
      MEMBER_PREFIX=`getMemberPrefix`
      RUN_DIR=$CLUSTERS_DIR/$CLUSTER/run
      MEMBER=${MEMBER_PREFIX}${MEMBER_NUMBER}
      member_nums="`getMemberNumList`"
      case "$second_word" in add_member)
         type_list="1 2 3 4 5 6 7 8 9"
         ;;
      remove_member|show_log)
         type_list=""
         for MEMBER_NUM in "$member_nums"; do
            MEMBER=${MEMBER_PREFIX}${MEMBER_NUM}
            pid=`getMemberPid $MEMBER`
            if [ "$pid" == "" ]; then
               type_list="$type_list $MEMBER_NUM"
            fi
         done
         ;;
      start_member|stop_member)
         type_list=$member_nums
         ;;
      *)
         type_list=$member_nums
         ;;
      esac
      ;;
   -path | -datagrid | -java | -geode | -hazelcast | -jet | -vm-java | -vm-geode | -vm-hazelcast)
     ;;
   *)
      if [ "$second_word" == "cp_sub" ] || [ "$second_word" == "tools" ]; then
         if [ $len -gt 3 ]; then
            type_list=`$third_word -options`
         else
            type_list=`ls $PADOGRID_HOME/$PRODUCT/bin_sh/$second_word`
         fi
      elif [ "$second_word" == "switch_rwe" ] || [ "$second_word" == "cd_rwe" ]; then
            if [ $len -lt 4 ]; then
               type_list=`list_rwes`
            elif [ $len -lt 5 ]; then
               local RWE_HOME="$(dirname "$PADOGRID_WORKSPACES_HOME")"
               if [ ! -d "$RWE_HOME/$prev_word" ]; then
                  echo "No such RWE: $prev_word"
               else
                  type_list=`ls $RWE_HOME/$prev_word`
                  type_list=$(removeTokens "$type_list" "setenv.sh initenv.sh")
               fi
            fi
      elif [ "$second_word" == "switch_workspace" ] || [ "$second_word" == "cd_workspace" ]; then
            if [ $len -lt 4 ]; then
               type_list=`ls $PADOGRID_WORKSPACES_HOME`
               type_list=$(removeTokens "$type_list" "setenv.sh initenv.sh")
            fi
      elif [ "$second_word" == "switch_cluster" ] || [ "$second_word" == "cd_cluster" ]; then
            if [ $len -lt 4 ]; then
               type_list=`ls $PADOGRID_WORKSPACE/clusters`
            fi
      elif [ "$second_word" == "cd_pod" ]; then
            if [ $len -lt 4 ]; then
               type_list=`ls $PADOGRID_WORKSPACE/pods`
            fi
      elif [ "$second_word" == "cd_k8s" ]; then
            if [ $len -lt 4 ]; then
               type_list=`ls $PADOGRID_WORKSPACE/k8s`
            fi
      elif [ "$second_word" == "cd_docker" ]; then
            if [ $len -lt 4 ]; then
               type_list=`ls $PADOGRID_WORKSPACE/docker`
            fi
      elif [ "$second_word" == "cd_app" ]; then
            if [ $len -lt 4 ]; then
               type_list=`ls $PADOGRID_WORKSPACE/apps`
            fi
      else
         if [ "$second_word" == "-version" ]; then
            type_list=""
         elif [ "$second_word" == "-product" ]; then
            type_list=""
         elif [ $len -gt 2 ]; then
            type_list=`$second_word -options`
         else
            type_list=`ls $SCRIPT_DIR`
            type_list=$(removeTokens "$type_list" "setenv.sh")
            type_list="-product -rwe -version $type_list"
         fi
      fi
      ;;
   esac

   # Remove the help option if one or more options are already specified
   if [ $len -gt 3 ]; then
      type_list=${type_list/\-\?/}
   fi
   # Remove typed options from the list
   if [ "$prev_word" == "padogrid" ]; then
      type_list=${type_list/ padogrid/}
   else
      for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
         __WORD="${COMP_WORDS[$i]}"
         if [ "$__WORD" != "$cur_word" ]; then
            type_list=${type_list/$__WORD/}
         fi
      done
   fi


   COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   return 0
}

__rwe_complete()
{
   local len cur_word type_list
   len=${#COMP_WORDS[@]}
   prev_word=${COMP_WORDS[COMP_CWORD-1]}
   cur_word="${COMP_WORDS[COMP_CWORD]}"

   if [ $len -lt 3 ]; then
      type_list=`list_rwes`
   elif [ $len -lt 4 ]; then
      local RWE_HOME="$(dirname "$PADOGRID_WORKSPACES_HOME")"
      if [ ! -d "$RWE_HOME/$prev_word" ]; then
         echo "No such RWE: $prev_word"
      else
         type_list=`ls $RWE_HOME/$prev_word`
         type_list=$(removeTokens "$type_list" "setenv.sh initenv.sh")
      fi
   else
      type_list=""
   fi

   COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   return 0
}

__workspaces_complete()
{
   local len cur_word type_list
   len=${#COMP_WORDS[@]}
   cur_word="${COMP_WORDS[COMP_CWORD]}"
      
   if [ $len -ge 3 ]; then
     type_list=""
   else
      type_list=`ls $PADOGRID_WORKSPACES_HOME`
      type_list=$(removeTokens "$type_list" "setenv.sh initenv.sh")
   fi

   COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   return 0
}

__clusters_complete()
{
   local len cur_word type_list
   len=${#COMP_WORDS[@]}
   cur_word="${COMP_WORDS[COMP_CWORD]}"
      
   if [ $len -ge 3 ]; then
     type_list=""
   else
      type_list=`ls $PADOGRID_WORKSPACE/clusters`
   fi
   
   COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   return 0
}

__pods_complete()
{
   local len cur_word type_list
   len=${#COMP_WORDS[@]}
   cur_word="${COMP_WORDS[COMP_CWORD]}"

   if [ $len -ge 3 ]; then
     type_list=""
   else
      type_list=`ls $PADOGRID_WORKSPACE/pods`
   fi

   COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   return 0
}

__k8s_complete()
{
   local len cur_word type_list
   len=${#COMP_WORDS[@]}
   cur_word="${COMP_WORDS[COMP_CWORD]}"

   if [ $len -ge 3 ]; then
     type_list=""
   else
      type_list=`ls $PADOGRID_WORKSPACE/k8s`
   fi

   COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   return 0
}

__docker_complete()
{
   local len cur_word type_list
   len=${#COMP_WORDS[@]}
   cur_word="${COMP_WORDS[COMP_CWORD]}"

   if [ $len -ge 3 ]; then
     type_list=""
   else
      type_list=`ls $PADOGRID_WORKSPACE/docker`
   fi

   COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   return 0
}

__apps_complete()
{
   local len cur_word type_list
   len=${#COMP_WORDS[@]}
   cur_word="${COMP_WORDS[COMP_CWORD]}"
      
   if [ $len -ge 3 ]; then
     type_list=""
   else
     type_list=`ls $PADOGRID_WORKSPACE/apps`
   fi

   COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   return 0
}

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
   if [ $HAZELCAST_MAJOR_VERSION_NUMBER -ge 4 ]; then
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

__get_str_position()
{ 
  x="${1%%$2*}"
  [[ "$x" = "$1" ]] && echo -1 || echo "${#x}"
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
      if [ "$CLUSTER_TYPE" == "imdg" ]; then
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
      if [ "$CLUSTER_TYPE" == "imdg" ]; then
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

   COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
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

   COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   return 0
}

__command_complete()
{
   local command cur_word prev_word type_list

   # COMP_WORDS is an array of words in the current command line.
   # COMP_CWORD is the index of the current word (the one the cursor is
   # in). So COMP_WORDS[COMP_CWORD] is the current word.
   command="${COMP_WORDS[0]}"
   cur_word="${COMP_WORDS[COMP_CWORD]}"
   prev_word="${COMP_WORDS[COMP_CWORD-1]}"
   len=${#COMP_WORDS[@]}

   case $prev_word in
   -?)
      type_list=""
      ;;
   -pod)
      if [ "$command" != "find_padogrid" ]; then
         type_list=`getPods`
      fi
      ;;
   -count)
      type_list="1 2 3 4 5 6 7 8 9"
      ;;
   -app)
      if [ "$command" == "create_app" ]; then
         type_list=`getAddonApps $CLUSTER_TYPE`
      elif [ "$command" != "find_padogrid" ]; then
         type_list=`getApps`
      fi
      ;;
   -cluster)
      if [ "$command" == "create_k8s" ] || [ "$command" == "remove_k8s" ]; then
         __ENV="k8s"
      elif [ "$command" == "create_docker" ] || [ "$command" == "remove_docker" ]; then
         __ENV="docker"
      else
         __ENV="clusters"
      fi
      if [ "$command" != "find_padogrid" ]; then
         type_list=`getClusters $__ENV`
      fi
      ;;
   -prefix)
      if [ "$command" == "create_grid" ]; then
         type_list="grid"
      fi
      ;;
   -type)
      if [ "$command" == "create_pod" ]; then
         type_list="local vagrant"
      elif [ "$command" == "create_cluster" ] || [ "$command" == "create_grid" ]; then
         type_list="default pado"
      fi
      ;;
   -product)
      if [ "$command" == "show_bundle" ]; then
         type_list="$BUNDLE_PRODUCT_LIST"
      fi
      ;;
   -rwe)
      if [ "$command" != "find_padogrid" ]; then
         type_list=`getRweList`
      fi
      ;;
   -workspace)
      if [ "$command" == "install_bundle" ]; then
         type_list=`$command -options`
      elif [ "$command" != "find_padogrid" ]; then
         type_list=`getWorkspaces`
      fi
      ;;
   -k8s)
      if [ "$command" != "create_workspace" ]; then
         type_list="minikube gke minishift openshift"
      elif [ "$command" != "find_padogrid" ]; then
         type_list=`getClusters k8s`
      fi
      ;;
   -docker)
      if [ "$command" == "create_bundle" ]; then
         type_list=`getClusters docker`
      elif [ "$command" != "find_padogrid" ]; then
         type_list="compose"
      fi
      ;;
   -host)
      if [ "$command" == "create_docker" ]; then
         type_list="$(getHostIPv4List) host.docker.internal"
      fi
      ;;
   -user)
      GITHUB_USERS=""
      if [ -f "$HOME/.padogrid/setenv.sh" ]; then
         . $HOME/.padogrid/setenv.sh
      fi
      type_list="padogrid $GITHUB_USERS"
      ;;
   -githost)
      type_list="github gitea"
      ;;
   -connect)
      type_list="https ssh"
      ;;
   -log)
      type_list="data gc diag mc"
     ;;
   -num)
      type_list="1 2 3 4 5 6 7 8 9"
     ;;
   -port)
      if [ "$command" == "create_cluster" ] || [ "$command" == "create_docker" ] || [ "$command" == "create_grid" ]; then
         type_list="$DEFAULT_MEMBER_START_PORT"
      fi
     ;;
   -path | -datagrid | -java | -geode | -hazelcast | -jet | -vm-java | -vm-geode | -vm-hazelcast)
     ;;
   *)
      # Command options
      type_list=`$command -options`
     ;;
   esac

   # Remove the help option if one or more options are already specified
   if [ $len -gt 2 ]; then
      type_list=${type_list/\-\?/}
   fi
   # Remove typed options from the list
   for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
      __WORD="${COMP_WORDS[$i]}"
      if [ "$__WORD" != "$cur_word" ]; then
         type_list=${type_list/$__WORD/}
      fi
   done

   COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   return 0
}

# Register __command_complete to provide completion for all commands
commands=`ls $SCRIPT_DIR`
for i in $commands; do
   if [ "$i" != "setenv.sh" ]; then
      if [ "$i" == "cp_sub" ] || [ "$i" == "tools" ]; then
         sub_commands=`ls $PADOGRID_HOME/$PRODUCT/bin_sh/$i`
         for j in $sub_commands; do
            complete -F __command_complete -o bashdefault -o default $j
         done
         complete -F __command_complete -o bashdefault -o default $i
      else
         complete -F __command_complete -o bashdefault -o default $i
      fi
   fi
done

# Register padogrid
complete -F __padogrid_complete -o bashdefault -o default padogrid

# Register switch_rwe, cd_rwe
complete -F __rwe_complete -o bashdefault -o default switch_rwe
complete -F __rwe_complete -o bashdefault -o default cd_rwe

# Register switch_workspace, cd_workspace
complete -F __workspaces_complete -o bashdefault -o default switch_workspace
complete -F __workspaces_complete -o bashdefault -o default cd_workspace

# Register switch_cluster, cd_cluster
complete -F __clusters_complete -o bashdefault -o default switch_cluster
complete -F __clusters_complete -o bashdefault -o default cd_cluster

# Register cd_pod
complete -F __pods_complete -o bashdefault -o default cd_pod

# Register cd_k8s
complete -F __k8s_complete -o bashdefault -o default cd_k8s

# Register cd_docker
complete -F __docker_complete -o bashdefault -o default cd_docker

# Register cd_app
complete -F __apps_complete -o bashdefault -o default cd_app

# Register cluster.sh
complete -F __cluster_complete -o bashdefault -o default cluster.sh

# Register jet.sh
complete -F __jet_complete -o bashdefault -o default jet.sh
complete -F __jet_complete -o bashdefault -o default jet
