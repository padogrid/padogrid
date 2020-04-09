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
# Set the following variables with your values and follow the instructions
# in Hazelcast-Kustom.pdf. Note that you would need to create a GKE cluster
# first before you can set INSTANCE_NAME.
#
export HAZELCAST_KUSTOM_DIR=~/Hazelcast/padogrid-kustom
export GCR_HOSTNAME=gcr.io
export PROJECT_ID=hazelcast-33
export CLUSTER_NAME=kustomize-test
export REGION=us-east1
export ZONE=us-east1-b
export INSTANCE_NAME=gke-kustomize-test-default-pool-c82eb23e-bzft
