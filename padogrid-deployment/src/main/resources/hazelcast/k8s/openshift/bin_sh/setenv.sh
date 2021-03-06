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
# Default OpenShift project name
#
export PROJECT_NAME="$APP_NAME"

#
# Number of members
#
MEMBER_COUNT=3

#
# Gap between the first load balancer port and the first node port. This numer is used
# to separate the load balancer ports from node ports. That means only up to this number
# of node ports (or services) can be created.
#
NODE_PORT_GAP=10

#
# Hazelcast starting serivce port number. All service port numbers are incremented starting
# from this port number as follows.
#  - This port number is assigned to the headless cluster IP for load balancing Hazelcast pods.
#  - All load-balancer ports are incremented from this number.
#  - $NODE_PORT_GAP is added to the load-balancer ports for the counterpart node ports
#
START_SERVICE_PORT=30000

#
# Comma-separated list of WAN target endpoints, i.e., "ip-address1:5701,ipaddress2:5701"
# By default, the master node and port 30100 is assigned. Change to other endpoints here.
#
WAN_TARGET_PORT=30100
MASTER_NODE=$(oc cluster-info |grep "Kubernetes master" | sed -e "s/^.*https:\/\///" -e "s/:.*$//")
WAN_TARGET_ENDPOINTS="$MASTER_NODE:$WAN_TARGET_PORT"
