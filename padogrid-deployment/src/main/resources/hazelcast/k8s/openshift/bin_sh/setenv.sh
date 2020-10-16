#
# Enter app specifics in this file.
#

# Cluster level variables:
# ------------------------
# BASE_DIR - padogrid base dir
# ETC_DIR - Cluster etc dir

# App level variables:
# --------------------
# APPS_DIR - <padogrid>/apps dir
# APP_DIR - App base dir
# APP_ETC_DIR - App etc dir

#
# Follow the instructions in README.md using the following
# environment variable.
#
export HAZELCAST_OPENSHIFT_DIR=$PADOGRID_WORKSPACE/k8s/wan1

#
# Default OpenShift project name
#
export PROJECT_NAME="$APP_NAME"

#
# Comma-separated list of WAN target endpoints, i.e., "ip-address1:5701,ipaddress2:5701"
# By default, the master node and port 30100 is assigned. Change to other endpoints here.
#
WAN_TARGET_PORT=30100
MASTER_NODE=$(oc cluster-info |grep "Kubernetes master" | sed -e "s/^.*https:\/\///" -e "s/:.*$//")
WAN_TARGET_ENDPOINTS="$MASTER_NODE:$WAN_TARGET_PORT"
