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

# Unset IFS in case it is not reset by some of the commands executed.
# Without this, command completion may not properly parse options.
unset IFS

# Unset product specific functions.
# Hazelcast
unset -f __cluster_complete
unset -f __jet_complete
complete -r cluster.sh > /dev/null 2>&1
complete -r jet.sh > /dev/null 2>&1
complete -r jet > /dev/null 2>&1

__arrayContainsElement ()
{
  local e match="$1"
  shift
  for e; do
    [[ "$e" == "$match" ]] && echo "true" && return 0
  done
  echo "false"
  return 1
}

#
# Returns the index number of the specified array if the specified element is found, otherwise,
# returns 255.
# Example:
#    __getArrayElementIndex "submit" "${COMP_WORDS[@]}"
#    index=$?
#
# @param element - Element value to search
# @param array - Array
#
__getArrayElementIndex ()
{
  local e match="$1"
  shift
  local index=0
  for e; do
    [[ "$e" == "$match" ]] && return $index
    let index=index+1
  done
  return 255
}

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
   local second_word="${COMP_WORDS[1]}"
   local third_word="${COMP_WORDS[2]}"
   local cur_word="${COMP_WORDS[COMP_CWORD]}"
   local prev_word="${COMP_WORDS[COMP_CWORD-1]}"
   local len=${#COMP_WORDS[@]}
   local command=$second_word
   local command2=$third_word
   local is_product="false"
   local is_path="false"
      
   local type_list=""
   case "$prev_word" in
   -?)
      type_list=""
      ;;

   -name)
      if [ "$command" == "create_workspace" ]; then
         type_list=`getWorkspaces`
      fi
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
         # If -product specified then get the product's app options
         __getArrayElementIndex "-product" "${COMP_WORDS[@]}"
         local index=$?
         local product_name=""
         if [ $index -ne 255 ]; then
             product_name="${COMP_WORDS[$index+1]}"
         fi
         type_list=$(getAppOptions $product_name)
      elif [ "$command" != "find_padogrid" ]; then
         type_list=`getApps`
      fi
      ;;

   -port)
      if [ "$command" == "make_cluster" ] || [ "$command" == "create_cluster" ] || [ "$command" == "create_docker" ] || [ "$command" == "create_group" ]; then
         # If -product specified then get the product's app options
         __getArrayElementIndex "-product" "${COMP_WORDS[@]}"
         local index=$?
         local product_name=""
         if [ $index -ne 255 ]; then
             product_name="${COMP_WORDS[$index+1]}"
         fi
         if [ "$product_name" != "" ] && [[ "$product_name" != "-"** ]]; then
            prodcut_name=$product_name 
         else
            prodcut_name=$PRODUCT
         fi
         type_list=$(getClusterPortOptions $product_name)

      elif [ "$command" == "open_jupyter" ] || [ "$command" == "start_jupyter" ]; then
         type_list="8888"
      elif [ "$command" == "show_jupyter" ] || [ "$command" == "stop_jupyter" ]; then
         type_list=$(getActiveJupyterPorts)
      fi
      ;;

   -id)
      case "$PRODUCT" in
      geode|snappydata|spark|coherence)
         if [ "$command" == "create_cluster" ]; then
            type_list="-1 1 2 3 4 5 6 7 8 9"
         fi
         ;;
      esac
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
      if [ "$command" == "create_group" ]; then
         type_list="grid"
      fi
     ;;

   -diag)
      if [ "$command2" == "t_show_type" ]; then
         type_list="gfsh true false"
      fi
      ;;

   -type)
      if [ "$command" == "create_pod" ]; then
         type_list="local vagrant"
      elif [ "$command" == "create_cluster" ]; then
         case "$PRODUCT" in
         geode)
            type_list="default pado padolite";;
         *)
            type_list="default";;
         esac
      elif [ "$command" == "make_cluster" ] || [ "$command" == "create_group" ]; then
         type_list="default"
         local product=""
         for i in $(seq 1 $len); do
            if [ "${COMP_WORDS[i]}" == "-product" ]; then
               product="${COMP_WORDS[i+1]}"
               break;
            fi
         done
         if  [ "$product" == "geode" ] || [ "$product" == "gemfire" ]; then
            type_list="$type_list pado padolite"
         fi
      elif [ "$command2" == "t_show_cluster_views" ]; then
         type_list="received sending"
      elif [ "$command2" == "t_show_recovery_steps" ]; then
         type_list="0 1 2 3 4 5"
      fi
      ;;

   -k8s) 
      if [ "$command" == "create_k8s" ]; then
         # If -product specified then get the product's k8s options
         __getArrayElementIndex "-product" "${COMP_WORDS[@]}"
         local index=$?
         local product_name=""
         if [ $index -ne 255 ]; then
             product_name="${COMP_WORDS[$index+1]}"
         fi
         if [ "$product_name" != "" ] && [[ "$product_name" != "-"** ]]; then
            type_list=$(getK8sOptions $product_name)
         else
            type_list="minikube"
         fi
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

   -product)
      if [ "$command" == "show_bundle" ]; then
         type_list="$BUNDLE_PRODUCT_LIST"
      elif [ "$command" == "make_cluster" ] || [ "$command" == "add_cluster" ] || [ "$command" == "create_group" ]; then
         type_list=$(getInstalledProducts)
      elif [ "$command" == "create_docker" ]; then
         type_list="$DOCKER_PRODUCT_LIST"
      elif [ "$command" == "create_k8s" ]; then
         type_list="$K8S_PRODUCT_LIST"
      elif [ "$command" == "create_app" ]; then
         type_list="$APP_PRODUCT_LIST"
      elif [ "$command" == "install_padogrid" ]; then
         type_list="$DOWNLOADABLE_PRODUCTS"
      elif [ "$command" == "uninstall_product" ]; then
         # Replace grafana-enterprise and granfana-oss with grafana
         local __DOWNLOADABLE_PRODUCTS=$(echo $DOWNLOADABLE_PRODUCTS | sed -e 's/grafana-enterprise//' -e 's/grafana-oss/grafana/')
         type_list="$__DOWNLOADABLE_PRODUCTS gemfire"
      elif [ "$command" == "update_products" ]; then
         # Replace grafana-enterprise and granfana-oss with grafana
         local __DOWNLOADABLE_PRODUCTS=$(echo $DOWNLOADABLE_PRODUCTS | sed -e 's/grafana-enterprise//' -e 's/grafana-oss/grafana/')
         type_list="$__DOWNLOADABLE_PRODUCTS coherence gemfire java jet-mc"
      elif [ $len -gt 3 ]; then
         is_path="true"
      fi
      ;;

   -rwe)
      if [ $len -eq 3 ]; then
         type_list=""
      elif [ "$command" != "find_padogrid" ]; then
         type_list=`getRweList`
      fi
      ;;

   -version)
      if [ "$command" == "install_padogrid" ]; then
         # If -product specified then get downlodable product versions
         __getArrayElementIndex "-product" "${COMP_WORDS[@]}"
         local index=$?
         local product_name=""
         if [ $index -ne 255 ]; then
             product_name="${COMP_WORDS[$index+1]}"
         fi
         type_list=$(getDownloadableProductVersions $product_name)
      fi
      ;;
      
   -workspace)
      if [ "$command" == "install_bundle" ]; then
         type_list="default "`getWorkspaces`
      elif [ "$command" == "open_vscode" ]  \
        || [ "$command" == "list_apps" ]  \
        || [ "$command" == "list_clusters" ]  \
        || [ "$command" == "list_docker" ]  \
        || [ "$command" == "list_k8s" ]; then
         # If -rwe specified then get the rwe's workspaces
         __getArrayElementIndex "-rwe" "${COMP_WORDS[@]}"
         local index=$?
         local rwe_name=""
         if [ $index -ne 255 ]; then
             rwe_name="${COMP_WORDS[$index+1]}"
         fi
         if [ "$rwe_name" != "" ] && [[ "$rwe_name" != "-"** ]]; then
            type_list=`getWorkspaces "$(dirname $PADOGRID_WORKSPACES_HOME)/$rwe_name"`
         else
            type_list=`getWorkspaces`
         fi
      elif [ "$command" != "find_padogrid" ]; then
         type_list=`getWorkspaces`
      fi
      ;;

   -group)
      if [[ "$command" == *"group" ]] || [ "$command" == "add_cluster" ]; then
         # If -workspace specified then get the workspace's groups
         __getArrayElementIndex "-workspace" "${COMP_WORDS[@]}"
         local index=$?
         local workspace_name=""
         if [ $index -ne 255 ]; then
             workspace_name="${COMP_WORDS[$index+1]}"
         fi
         type_list=$(getClusters groups $workspace_name)
      fi
      ;;

   -host)
      if [ "$command" == "create_docker" ]; then
         type_list="$(getHostIPv4List) host.docker.internal"
      fi
      ;;

   -ip)
      if [ "$command" == "open_jupyter" ] || [ "$command" == "start_jupyter" ]; then
         type_list="0.0.0.0 $(getHostIpAddresses)"
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

  -branch)
      type_list="master"
      ;;

   -connect)
      type_list="https ssh"
      ;;

   -log)
      local __product=$PRODUCT
      local __cluster_type=$CLUSTER_TYPE
      local cluster=$CLUSTER
      for i in $(seq 1 $len); do
         if [ "${COMP_WORDS[i]}" == "-cluster" ]; then
            cluster="${COMP_WORDS[i+1]}"
            break;
         fi
      done
      determineClusterProduct $cluster
      case "$PRODUCT" in
      geode)
         type_list="data gc locator";;
      hazelcast)
         type_list="data gc diag mc";;
      snappydata)
         type_list="data gc locator leader";;
      spark)
         type_list="data gc master";;
      coherence)
         type_list="data gc";;
      kafka)
         type_list="data gc controller";;
      hadoop)
         type_list="data gc namenode secondarynamenode nodemanager resourcemanager historyserver";;
      esac
      PRODUCT=$__product
      CLUSTER_TYPE=$__cluster_type
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
      case "$command" in
      add_member)
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

   -vm-user)
      type_list="$(whoami)"
      ;;

   -path | -java | -save | -load | -vm-java | -vm-product | -vm-padogrid | -vm-workspaces | -vm-key)
      is_path="true"
     ;;

   -scan)
      type_list=3
      ;;

   *)
      if [ "$command" == "cp_sub" ] || [ "$command" == "tools" ]; then
         if [ $len -gt 3 ]; then
            type_list=`$command2 -options`
         else
            type_list=`ls $PADOGRID_HOME/$PRODUCT/bin_sh/$command`
         fi
      elif [ "$command" == "switch_rwe" ] || [ "$command" == "cd_rwe" ]; then
            type_list=$(__rwe_complete_arg 2)
      elif [ "$command" == "switch_workspace" ] || [ "$command" == "cd_workspace" ]; then
            type_list=$(__workspace_complete_arg 2)
      elif [ "$command" == "switch_cluster" ] || [ "$command" == "cd_cluster" ]; then
            type_list=$(__cd_complete_arg "clusters" 2)
      elif [ "$command" == "switch_group" ] || [ "$command" == "cd_group" ]; then
            type_list=$(__cd_complete_arg "groups" 2)
      elif [ "$command" == "cd_pod" ]; then
            type_list=$(__cd_complete_arg "pods" 2)
      elif [ "$command" == "cd_docker" ]; then
            type_list=$(__cd_complete_arg "docker" 2)
      elif [ "$command" == "cd_app" ]; then
          type_list=$(__cd_complete_arg "apps" 2)
      elif [ "$command" == "cd_k8s" ]; then
          type_list=$(__cd_complete_arg "k8s" 2)
      elif [ "$command" == "vm_copy" ] && [[ "$cur_word" != "-"* ]]; then
         is_path="true"
      else
         if [ "$command" == "-version" ]; then
            type_list=""
         elif [ "$command" == "-product" ]; then
            type_list=""
         elif [ $len -gt 2 ]; then
            type_list=`$command -options`
         else
            type_list=`ls $SCRIPT_DIR`
            type_list=$(removeTokens "$type_list" "setenv.sh")
            type_list="-product -rwe -version -? $type_list"
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
         if [[ "$__WORD" == "-"* ]] && [ "$__WORD" != "$cur_word" ]; then
            type_list=${type_list/$__WORD/}
         fi
      done
   fi

   if [ "$is_path" == "true" ]; then
      COMPREPLY=( $( compgen -f -- "$cur_word" ))
   elif [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

# 
# Completes cd_rwe command.
# @param argStartIndex Argument start index. 1 for straight command, 2 for padogrid command.
# @return String value of type_list
#
__rwe_complete_arg()
{
   local start_index=$1
   if [ "$start_index" == "" ]; then
      start_index=1
   fi
   local len cur_word type_list
   local len=${#COMP_WORDS[@]}
   local prev_word=${COMP_WORDS[COMP_CWORD-1]}
   local cur_word="${COMP_WORDS[COMP_CWORD]}"
   local RWE_HOME="$(dirname "$PADOGRID_WORKSPACES_HOME")"

   let i1=start_index+2
   let i2=start_index+3
   let i3=start_index+4

   if [ $len -lt $i1 ]; then
      type_list=`list_rwes`
   elif [ $len -lt $i2 ]; then
      if [ ! -d "$RWE_HOME/$prev_word" ]; then
         echo "No such RWE: $prev_word"
      else
         local __type_list=`ls $RWE_HOME/$prev_word`
         local __type_list=$(removeTokens "$__type_list" "setenv.sh initenv.sh")
         type_list=""
         for i in $__type_list; do
            if [ -r "$RWE_HOME/$prev_word/$i" ]; then
               type_list="$type_list $i"
            fi
         done
      fi
   else
      local WORKSPACE_DIR="$RWE_HOME/${COMP_WORDS[start_index]}/${COMP_WORDS[start_index+1]}"
      local DIR=""
      local PARENT_DIR=""
      local count=0
      for i in ${COMP_WORDS[@]}; do
        let count=count+1
        if [ $count -gt $i1 ]; then
           DIR="$DIR/$i"
        fi
        if [ $count -lt $len ]; then
           PARENT_DIR=$DIR
        fi
      done
      if [ -d "${WORKSPACE_DIR}${DIR}" ]; then
         type_list=$(__get_dir_list "${WORKSPACE_DIR}${DIR}")
      elif [ -d "${WORKSPACE_DIR}${PARENT_DIR}" ]; then
         type_list=$(__get_dir_list "${WORKSPACE_DIR}${PARENT_DIR}")
      else
         type_list=""
      fi
   fi
   echo $type_list
}

__rwe_complete_space()
{
   cur_word="${COMP_WORDS[COMP_CWORD]}"
   type_list=$(__rwe_complete_arg 1)
   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__rwe_complete_nospace()
{
   local len=${#COMP_WORDS[@]}
   if [ $len -eq 2 ]; then
     if [ "$(dirname $PADOGRID_WORKSPACES_HOME)" != "$(pwd)" ]; then
       pushd $(dirname $PADOGRID_WORKSPACES_HOME) > /dev/null
      fi
   fi
   return 0
}

# 
# Completes cd_workspace and switch_workspace commands.
# @param argStartIndex Argument start index. 1 for straight command, 2 for padogrid command.
# @return String value of type_list
#
__workspace_complete_arg()
{
   local start_index=$1
   if [ "$start_index" == "" ]; then
      start_index=1
   fi
   local len cur_word type_list
   len=${#COMP_WORDS[@]}
   cur_word="${COMP_WORDS[COMP_CWORD]}"

   let i1=start_index
   let i2=start_index+1
   let i3=start_index+2
      
   if [ $len -lt $i3 ]; then
      type_list=`list_workspaces`
      type_list=$(removeTokens "$type_list" "setenv.sh initenv.sh")
   else
      local COMPONENT_DIR="$PADOGRID_WORKSPACES_HOME/${COMP_WORDS[start_index]}"
      local DIR=""
      local PARENT_DIR=""
      local count=0
      for i in ${COMP_WORDS[@]}; do
        let count=count+1
        if [ $count -gt $i2 ]; then
           DIR="$DIR/$i"
        fi
        if [ $count -lt $len ]; then
           PARENT_DIR=$DIR
        fi
      done
      if [ -d "${COMPONENT_DIR}${DIR}" ]; then
         type_list=$(__get_dir_list "${COMPONENT_DIR}${DIR}")
      elif [ -d "${COMPONENT_DIR}${PARENT_DIR}" ]; then
         type_list=$(__get_dir_list "${COMPONENT_DIR}${PARENT_DIR}")
      else
         type_list=""
      fi
   fi
   echo $type_list
}

__workspace_complete_space()
{
   cur_word="${COMP_WORDS[COMP_CWORD]}"
   local type_list=$(__workspace_complete_arg 1)
   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" ${cur_word}) )
   fi
   return 0
}

# trap ctrl-c and call __ctrl_c()
#trap __ctrl_c INT
#
#function __ctrl_c() {
#   pushd -0 > /dev/null
#   dirs -c
#}

__workspace_complete_nospace()
{
   local len=${#COMP_WORDS[@]}
   if [ $len -eq 2 ]; then
      if [ "$PADOGRID_WORKSPACES_HOME" != "$(pwd)" ]; then
         pushd $PADOGRID_WORKSPACES_HOME > /dev/null
      fi
   fi
   return 0
}

#
# Returns a list of sub-directories in the specified directory.
# @parm parentDir  Parent directory path. If not specified then the current directory
#                  is assigned.
#
__get_dir_list()
{
  local parent_dir=$1
  if [ "$parent_dir" == "" ]; then
     parent_dir="."
  fi
  local __dir_list=$(echo $parent_dir/*/)
  local __command="$parent_dir/*/"
  local dir_list=""
  # echo returns the same input string if sub-directories do not exist
  if [ "$__dir_list" != "$__command" ]; then
     for i in $__dir_list; do
        dir_list="$dir_list $(basename $i)"
     done
  fi
  echo $dir_list
}

# 
# Completes cd command.
# @parm dirName        Directory name, i.e., apps, docker, k8s, pods.
# @param argStartIndex Argument start index. 1 for straight command, 2 for padogrid command.
# @return String value of type_list
#
__cd_complete_arg()
{
   local dir_name="$1"
   local start_index=$2
   if [ "$start_index" == "" ]; then
      start_index=1
   fi
   local len cur_word type_list
   local len=${#COMP_WORDS[@]}
   local cur_word="${COMP_WORDS[COMP_CWORD]}"
   local COMPONENT_DIR="$PADOGRID_WORKSPACE/$dir_name"

   let i1=start_index
   let i2=start_index+1
   let i3=start_index+2
      
   if [ $len -lt $i2 ]; then
      type_list=""
   elif [ $len -lt $i3 ]; then
      type_list=$(__get_dir_list "${COMPONENT_DIR}")
   else
      local DIR=""
      local PARENT_DIR=""
      local count=0
      for i in ${COMP_WORDS[@]}; do
        let count=count+1
        if [ $count -gt $i1 ]; then
           DIR="$DIR/$i"
        fi
        if [ $count -lt $len ]; then
           PARENT_DIR=$DIR
        fi
      done
      if [ -d "${COMPONENT_DIR}${DIR}" ]; then
         type_list=$(__get_dir_list "${COMPONENT_DIR}${DIR}")
      elif [ -d "${COMPONENT_DIR}${PARENT_DIR}" ]; then
         type_list=$(__get_dir_list "${COMPONENT_DIR}${PARENT_DIR}")
      else
         type_list=""
      fi
   fi
   echo $type_list
}

__groups_complete_space()
{
   local cur_word="${COMP_WORDS[COMP_CWORD]}"
   local type_list=$(__cd_complete_arg "groups" 1)
   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__clusters_complete_space()
{
   local cur_word="${COMP_WORDS[COMP_CWORD]}"
   local type_list=$(__cd_complete_arg "clusters" 1)
   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__clusters_complete_nospace()
{
   local len=${#COMP_WORDS[@]}
   if [ $len -eq 2 ]; then
      if [ "$PADOGRID_WORKSPACE/clusters" != "$(pwd)" ]; then
         pushd $PADOGRID_WORKSPACE/clusters > /dev/null
      fi
   fi
   return 0
}

__pods_complete_space()
{
   local cur_word="${COMP_WORDS[COMP_CWORD]}"
   local type_list=$(__cd_complete_arg "pods" 1)
   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__pods_complete_nospace()
{
   local len=${#COMP_WORDS[@]}
   if [ $len -eq 2 ]; then
      if [ "$PADOGRID_WORKSPACE/pods" != "$(pwd)" ]; then
         pushd $PADOGRID_WORKSPACE/pods > /dev/null
      fi
   fi
   return 0
}

__k8s_complete_space()
{
   local cur_word="${COMP_WORDS[COMP_CWORD]}"
   local type_list=$(__cd_complete_arg "k8s" 1)
   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__k8s_complete_nospace()
{
   local len=${#COMP_WORDS[@]}
   if [ $len -eq 2 ]; then
      if [ "$PADOGRID_WORKSPACE/k8s" != "$(pwd)" ]; then
         pushd $PADOGRID_WORKSPACE/k8s > /dev/null
      fi
   fi
   return 0
}

__docker_complete_space()
{
   local cur_word="${COMP_WORDS[COMP_CWORD]}"
   local type_list=$(__cd_complete_arg "docker" 1)
   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__docker_complete_nospace()
{
   local len=${#COMP_WORDS[@]}
   if [ $len -eq 2 ]; then
      if [ "$PADOGRID_WORKSPACE/docker" != "$(pwd)" ]; then
         pushd $PADOGRID_WORKSPACE/docker > /dev/null
      fi
   fi
   return 0
}

__apps_complete_space()
{
   local cur_word="${COMP_WORDS[COMP_CWORD]}"
   local type_list=$(__cd_complete_arg "apps" 1)
   if [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

__apps_complete_nospace()
{
   local len=${#COMP_WORDS[@]}
   if [ $len -eq 2 ]; then
      if [ "$PADOGRID_WORKSPACE/apps" != "$(pwd)" ]; then
         pushd $PADOGRID_WORKSPACE/apps > /dev/null
      fi
   fi
   return 0
}

__get_str_position()
{ 
  x="${1%%$2*}"
  [[ "$x" = "$1" ]] && echo -1 || echo "${#x}"
}

# Returns filenames and directories, appending a slash to directory names.
__mycmd_compgen_filenames() {
    local cur="$1"

    # Files, excluding directories:
    grep -v -F -f <(compgen -d -P ^ -S '$' -- "$cur") \
        <(compgen -f -P ^ -S '$' -- "$cur") |
        sed -e 's/^\^//' -e 's/\$$/ /'

    # Directories:
    compgen -d -S / -- "$cur"
}

__command_complete()
{
   local command cur_word prev_word type_list

   # COMP_WORDS is an array of words in the current command line.
   # COMP_CWORD is the index of the current word (the one the cursor is
   # in). So COMP_WORDS[COMP_CWORD] is the current word.
   local command="${COMP_WORDS[0]}"
   local cur_word="${COMP_WORDS[COMP_CWORD]}"
   local prev_word="${COMP_WORDS[COMP_CWORD-1]}"
   local len=${#COMP_WORDS[@]}
   local is_path="false"

   case $prev_word in
   -?)
      type_list=""
      ;;
   -name)
      if [ "$command" == "create_workspace" ]; then
         type_list=`getWorkspaces`
      fi
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
         # If -product specified then get the product's app options
         __getArrayElementIndex "-product" "${COMP_WORDS[@]}"
         local index=$?
         local product_name=""
         if [ $index -ne 255 ]; then
             product_name="${COMP_WORDS[$index+1]}"
         fi
         type_list=$(getAppOptions $product_name)
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
      if [ "$command" == "create_group" ]; then
         type_list="grid"
      fi
      ;;
   -diag)
      if [ "$command" == "t_show_type" ]; then
         type_list="gfsh true false"
      fi
      ;;
   -type)
      if [ "$command" == "create_pod" ]; then
         type_list="local vagrant"
      elif [ "$command" == "create_cluster" ]; then
         case "$PRODUCT" in
         geode)
            type_list="default pado padolite";;
         *)
            type_list="default";;
         esac
      elif [ "$command" == "make_cluster" ] || [ "$command" == "create_group" ]; then
         type_list="default"
         local product=""
         for i in $(seq 1 $len); do
            if [ "${COMP_WORDS[i]}" == "-product" ]; then
               product="${COMP_WORDS[i+1]}"
               break;
            fi
         done
         if  [ "$product" == "geode" ] || [ "$product" == "gemfire" ]; then
            type_list="$type_list pado padolite"
         fi
      elif [ "$command" == "t_show_cluster_views" ]; then
         type_list="received sending"
      elif [ "$command" == "t_show_recovery_steps" ]; then
         type_list="0 1 2 3 4 5"
      fi
      ;;
   -product)
      if [ "$command" == "show_bundle" ]; then
         type_list="$BUNDLE_PRODUCT_LIST"
      elif [ "$command" == "make_cluster" ] || [ "$command" == "add_cluster" ] || [ "$command" == "create_group" ]; then
         type_list=$(getInstalledProducts)
      elif [ "$command" == "create_docker" ]; then
         type_list="$DOCKER_PRODUCT_LIST"
      elif [ "$command" == "create_k8s" ]; then
         type_list="$K8S_PRODUCT_LIST"
      elif [ "$command" == "create_app" ]; then
         type_list="$APP_PRODUCT_LIST"
      elif [ "$command" == "install_padogrid" ]; then
         type_list="$DOWNLOADABLE_PRODUCTS"
      elif [ "$command" == "uninstall_product" ]; then
         # Replace grafana-enterprise and granfana-oss with grafana
         local __DOWNLOADABLE_PRODUCTS=$(echo $DOWNLOADABLE_PRODUCTS | sed -e 's/grafana-enterprise//' -e 's/grafana-oss/grafana/')
         type_list="$__DOWNLOADABLE_PRODUCTS gemfire"
      elif [ "$command" == "update_products" ]; then
         # Replace grafana-enterprise and granfana-oss with grafana
         local __DOWNLOADABLE_PRODUCTS=$(echo $DOWNLOADABLE_PRODUCTS | sed -e 's/grafana-enterprise//' -e 's/grafana-oss/grafana/')
         type_list="$__DOWNLOADABLE_PRODUCTS coherence gemfire java jet-mc"
      else
         is_path="true"
      fi
      ;;
   -rwe)
      if [ "$command" != "find_padogrid" ]; then
         type_list=`getRweList`
      fi
      ;;
   -workspace)
      if [ "$command" == "install_bundle" ]; then
         type_list="default "`getWorkspaces`
      elif [ "$command" == "open_vscode" ]  \
        || [ "$command" == "list_apps" ]  \
        || [ "$command" == "list_clusters" ]  \
        || [ "$command" == "list_docker" ]  \
        || [ "$command" == "list_k8s" ]; then
         # If -rwe specified then get the rwe's workspaces
         __getArrayElementIndex "-rwe" "${COMP_WORDS[@]}"
         local index=$?
         local rwe_name=""
         if [ $index -ne 255 ]; then
             rwe_name="${COMP_WORDS[$index+1]}"
         fi
         if [ "$rwe_name" != "" ] && [[ "$rwe_name" != "-"** ]]; then
            type_list=`getWorkspaces "$(dirname $PADOGRID_WORKSPACES_HOME)/$rwe_name"`
         else
            type_list=`getWorkspaces`
         fi
      elif [ "$command" != "find_padogrid" ]; then
         type_list=`getWorkspaces`
      fi
      ;;
   -group)
      if [[ "$command" == *"group" ]] || [ "$command" == "add_cluster" ]; then
         # If -workspace specified then get the workspace's groups
         __getArrayElementIndex "-workspace" "${COMP_WORDS[@]}"
         local index=$?
         local workspace_name=""
         if [ $index -ne 255 ]; then
             workspace_name="${COMP_WORDS[$index+1]}"
         fi
         type_list=$(getClusters groups $workspace_name)
      fi
      ;;
   -k8s)
      if [ "$command" == "create_k8s" ]; then
         # If -product specified then get the product's k8s options
         __getArrayElementIndex "-product" "${COMP_WORDS[@]}"
         local index=$?
         local product_name=""
         if [ $index -ne 255 ]; then
             product_name="${COMP_WORDS[$index+1]}"
         fi
         if [ "$product_name" != "" ] && [[ "$product_name" != "-"** ]]; then
            type_list=$(getK8sOptions $product_name)
         else
            type_list="minikube"
         fi
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
   -ip)
      if [ "$command" == "open_jupyter" ] || [ "$command" == "start_jupyter" ]; then
         type_list="0.0.0.0 $(getHostIpAddresses)"
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
  -branch)
      type_list="master"
      ;;
   -connect)
      type_list="https ssh"
      ;;
   -log)
      local __product=$PRODUCT
      local __cluster_type=$CLUSTER_TYPE
      local cluster=$CLUSTER
      for i in $(seq 1 $len); do
         if [ "${COMP_WORDS[i]}" == "-cluster" ]; then
            cluster="${COMP_WORDS[i+1]}"
            break;
         fi
      done
      determineClusterProduct $cluster
      case "$PRODUCT" in
      geode)
         type_list="data gc locator";;
      hazelcast)
         type_list="data gc diag mc";;
      snappydata)
         type_list="data gc locator leader";;
      spark)
         type_list="data gc master";;
      coherence)
         type_list="data gc";;
      kafka)
         type_list="data gc controller";;
      hadoop)
         type_list="data gc namenode secondarynamenode nodemanager resourcemanager historyserver";;
      esac
      PRODUCT=$__product
      CLUSTER_TYPE=$__cluster_type
     ;;
   -num)
      type_list="1 2 3 4 5 6 7 8 9"
     ;;
   -port)
      if [ "$command" == "make_cluster" ] || [ "$command" == "create_cluster" ] || [ "$command" == "create_docker" ] || [ "$command" == "create_group" ]; then
         # If -product specified then get the product's app options
         __getArrayElementIndex "-product" "${COMP_WORDS[@]}"
         local index=$?
         local product_name=""
         if [ $index -ne 255 ]; then
             product_name="${COMP_WORDS[$index+1]}"
         fi
         if [ "$product_name" != "" ] && [[ "$product_name" != "-"** ]]; then
            prodcut_name=$product_name 
         else
            prodcut_name=$PRODUCT
         fi
         type_list=$(getClusterPortOptions $product_name)

         #case "$PRODUCT" in
         #geode|snappydata)
         #   type_list="$DEFAULT_LOCATOR_START_PORT";;
         #hazelcast|coherence|kafka)
         #   type_list="$DEFAULT_MEMBER_START_PORT";;
         #spark)
         #   type_list="$DEFAULT_MASTER_START_PORT";;
         #hadoop)
         #   type_list="$DEFAULT_NAMENODE_START_PORT";;
         #esac

      elif [ "$command" == "open_jupyter" ] || [ "$command" == "start_jupyter" ]; then
         type_list="8888"
      elif [ "$command" == "show_jupyter" ] || [ "$command" == "stop_jupyter" ]; then
         type_list=$(getActiveJupyterPorts)
      fi
     ;;

   -vm | -locator | -master)
      case "$PRODUCT" in
      geode|snappydata|spark)
         if [ "$command" == "create_cluster" ]; then
            type_list="" 
         fi
         ;;
      esac
     ;;

   -vm-user)
      type_list="$(whoami)"
      ;;

   -path | -java | -save | -load | -vm-java | -vm-product | -vm-padogrid | -vm-workspaces | -vm-key)
     is_path="true"
     ;;

   -scan)
      type_list=3
      ;;

   *)
      if [ "$command" == "vm_copy" ] && [[ "$cur_word" != "-"* ]]; then
         is_path="true"
      else
         # Command options
         type_list=`$command -options`
      fi
     ;;
   esac

   # Remove the help option if one or more options are already specified
   if [ $len -gt 2 ]; then
      type_list=${type_list/\-\?/}
   fi
   # Remove typed options from the list
   for ((i = 0; i < ${#COMP_WORDS[@]}; i++)); do
      __WORD="${COMP_WORDS[$i]}"
      if [[ "$__WORD" == "-"* ]] && [ "$__WORD" != "$cur_word" ]; then
         type_list=${type_list/$__WORD/}
      fi
   done

   if [ "$is_path" == "true" ]; then
      COMPREPLY=( $( compgen -f -- "$cur_word" ))
   elif [ "${type_list}" != "" ]; then
      COMPREPLY=( $(compgen -W "${type_list}" -- ${cur_word}) )
   fi
   return 0
}

# Register __command_complete to provide completion for all commands
commands=`ls $SCRIPT_DIR`
for i in $commands; do
   if [ "$i" != "setenv.sh" ]; then
      if [ "$i" == "cp_sub" ] || [ "$i" == "tools" ]; then
         sub_commands=`ls $PADOGRID_HOME/$PRODUCT/bin_sh/$i`
         for j in $sub_commands; do
            complete -F __command_complete -o filenames -o bashdefault $j
         done
         complete -F __command_complete -o filenames -o bashdefault  $i
      else
         complete -F __command_complete -o filenames -o bashdefault $i
      fi
   fi
done

# Register padogrid
complete -F __padogrid_complete -o filenames -o bashdefault padogrid

# Register switch_rwe, cd_rwe
complete -F __rwe_complete_space -o filenames -o bashdefault switch_rwe
complete -F __rwe_complete_space -o filenames -o bashdefault cd_rwe
#complete -F __rwe_complete_nospace -o filenames -o bashdefault -o nospace switch_rwe

# Register switch_workspace, cd_workspace
complete -F __workspace_complete_space -o filenames -o bashdefault switch_workspace
complete -F __workspace_complete_space -o filenames -o bashdefault cd_workspace

# Register switch_group, cd_group
complete -F __groups_complete_space -o filenames -o bashdefault switch_group
complete -F __groups_complete_space -o filenames -o bashdefault cd_group

# Register switch_cluster, cd_cluster
complete -F __clusters_complete_space -o filenames -o bashdefault switch_cluster
complete -F __clusters_complete_space -o filenames -o bashdefault cd_cluster

# Register switch_pod, cd_pod
complete -F __pods_complete_space -o filenames -o bashdefault switch_pod
complete -F __pods_complete_space -o filenames -o bashdefault cd_pod

# Register cd_k8s
complete -F __k8s_complete_space -o filenames -o bashdefault cd_k8s

# Register cd_docker
complete -F __docker_complete_space -o filenames -o bashdefault cd_docker

# Register cd_app
complete -F __apps_complete_space -o filenames -o bashdefault cd_app
