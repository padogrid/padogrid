#!/usr/bin/env bash
SCRIPT_DIR="$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
. $SCRIPT_DIR/.addonenv.sh $@

#
# There are multiple setenv.sh files sourced in in the order shown below,
# each overriding the variables set in the previous one(s).
#
#    $PADOGRID_HOME/bin_sh/setenv.sh
#    setenv.sh (this file)
#    $PADOGRID_WORKSPACE/setenv.sh
#    $PADOGRID_WORKSPACE/clusters/<cluster>/bin_sh/setenv.sh
#

#
# Set default product home paths. All workspaces belonging to this RWE inherit
# these values as their default values. Each workspace can override them in
# their respective setenv.sh file. Note that for non-local pods, they are set
# separately in the node initialization file, i.e., /vagrant/.nodeenv.sh.
#
if [ "$IN_POD" != "true" ]; then
   export JAVA_HOME="${TEMPLATE_JAVA_HOME}"
   export PADOGRID_HOME="${TEMPLATE_PADOGRID_HOME}"
   export PADO_HOME="${TEMPLATE_PADO_HOME}"
   export GEMFIRE_HOME="${TEMPLATE_GEMFIRE_HOME}"
   export GEODE_HOME="${TEMPLATE_GEODE_HOME}"
   export HAZELCAST_HOME="${TEMPLATE_HAZELCAST_HOME}"
   export HAZELCAST_MC_HOME="${TEMPLATE_HAZELCAST_MC_HOME}"
   export JET_HOME="${TEMPLATE_JET_HOME}"
   export JET_MC_HOME="${TEMPLATE_JET_MC_HOME}"
   export COHERENCE_HOME="${TEMPLATE_COHERENCE_HOME}"
   export SNAPPYDATA_HOME="${TEMPLATE_SNAPPYDATA_HOME}"
   export HADOOP_HOME="${TEMPLATE_HADOOP_HOME}"
   export KAFKA_HOME="${TEMPLATE_KAFKA_HOME}"
   export CONFLUENT_HOME="${TEMPLATE_CONFLUENT_HOME}"
   export SPARK_HOME="${TEMPLATE_SPARK_HOME}"
   export PRODUCT_HOME="${TEMPLATE_PRODUCT_HOME}"
fi

#
# Set the default product.
#
PRODUCT=${TEMPLATE_DEFAULT_PRODUCT}

#
# Source in Geode/GemFire specific environment variables. Set your Geode/GemFire specifics
# such as license keys in .geodeenv.sh. The environment variables set in .geodeenv.sh
# are inherited by all workspaces running under this directory.
#
if [ -f $SCRIPT_DIR/.geodeenv.sh ]; then
   . $SCRIPT_DIR/.geodeenv.sh
fi

#
# Source in Hazelcast (IMDG and Jet) specific environment variables. Set your IMDG/Jet specifics
# such as license keys in .hazelcastenv.sh. The environment variables set in .hazelcastenv.sh
# are inherited by all workspaces running under this directory.
#
if [ -f $SCRIPT_DIR/.hazelcastenv.sh ]; then
   . $SCRIPT_DIR/.hazelcastenv.sh
fi

#
# Source in SnappyData/ComputeDB specific environment variables. Set your SnappyData/ComputeDB specifics
# such as license keys in .snappydataenv.sh. The environment variables set in .snappydataenv.sh
# are inherited by all workspaces running under this directory.
#
if [ -f $SCRIPT_DIR/.snappydataenv.sh ]; then
   . $SCRIPT_DIR/.snappydataenv.sh
fi

#
# Source in Coherence specific environment variables. Set your Coherence specifics
# such as license keys in .coherenceenv.sh. The environment variables set in .coherenceenv.sh
# are inherited by all workspaces running under this directory.
#
if [ -f $SCRIPT_DIR/.coherenceenv.sh ]; then
   . $SCRIPT_DIR/.coherenceenv.sh
fi

#
# Source in Spark specific environment variables. Set your Spark specifics
# such as license keys in .sparkenv.sh. The environment variables set in .sparkenv.sh
# are inherited by all workspaces running under this directory.
#
if [ -f $SCRIPT_DIR/.sparkenv.sh ]; then
   . $SCRIPT_DIR/.sparkenv.sh
fi

#
# Source in Kafka specific environment variables. Set your Kafka specifics
# such as license keys in .kafkaenv.sh. The environment variables set in .kafkaenv.sh
# are inherited by all workspaces running under this directory.
#
if [ -f $SCRIPT_DIR/.kafkaenv.sh ]; then
   . $SCRIPT_DIR/.kafkaenv.sh
fi

#
# Source in Hadoop specific environment variables. Set your Hadoop specifics
# such as license keys in .hadoopenv.sh. The environment variables set in .hadoopenv.sh
# are inherited by all workspaces running under this directory.
#
if [ -f $SCRIPT_DIR/.hadoopenv.sh ]; then
   . $SCRIPT_DIR/.hadoopenv.sh
fi

#
# Add your workspaces-wide environment variables below. The environment
# variables set in this file are used for all workspaces running under this
# directory. Workspace specifics should be added in <workspace>/setenv.sh.
#

# Enable group permissions for workspace owners. If "true" then RWX permissions are
# given to the user's primary group.
GROUP_PERMISSIONS_ENABLED="false"
