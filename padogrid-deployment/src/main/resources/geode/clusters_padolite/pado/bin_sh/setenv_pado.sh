#
# Enables Pado in this cluster only if Pado is installed.
# This script is sourced in by setenv.sh.
#

if [ "$PADO_HOME" != "" ]; then
   NODE_NAME_PREFIX=`getPodProperty "node.name.prefix" $NODE_NAME_PREFIX`
   
   MEMBER_NUMBER=$MEMBER_NUM_NO_LEADING_ZERO
   if [ "$VM_ENABLED" == "true" ]; then
      # POD needed to get the correct node name
      POD=`getClusterProperty "pod.name" "local"`
      MEMBER=`getVmMemberName`
   else
      MEMBER=`getMemberName $MEMBER_NUMBER`
   fi
   MEMBER_DIR=$RUN_DIR/$MEMBER
   
   MEMBER_START_PORT=`getClusterProperty "tcp.startPort" $DEFAULT_MEMBER_START_PORT`
   let MEMBER_PORT=MEMBER_START_PORT+MEMBER_NUMBER-1
   
   if [ "$SITE_ID" == "" ]; then
      SITE_ID=$CLUSTER
   fi
   if [ "$GRID_ID" == "" ]; then
      GRID_ID=$SITE_ID
   fi
   GRID_NAME=$GRID_ID
   PADO_DIR=$CLUSTER_DIR/pado
   PADO_ETC_DIR=$PADO_DIR/etc
   PADO_SECURITY_DIR=$PADO_DIR/security
   
   JAVA_OPTS="$JAVA_OPTS \
   --J=-Dpado.home.dir=$CLUSTER_DIR \
   --J=-Dpado.security.enabled=false \
   --J=-DSITE=$SITE_ID \
   --J=-DDISK_STORE_DIR=$MEMBER_DIR/store \
   --J=-Dpado.grid.id=$GRID_ID \
   --J=-Dpado.grid.name=$GRID_NAME \
   --J=-Dpado.site.id=$SITE_ID \
   --J=-Dpado.site.name=$SITE_ID \
   --J=-Dpado.plugins.dir=$CLUSTER_DIR/plugins \
   --J=-Dpado.etc.dir=$PADO_ETC_DIR \
   --J=-Dpado.etc.grid.dir=$PADO_ETC_DIR \
   --J=-Dpado.db.dir=$CLUSTER_DIR/pado/db \
   --J=-Dpado.properties=$PADO_ETC_DIR/pado.properties \
   --J=-Dpado.appConfigDir=$PADO_ETC_DIR/app \
   --J=-Dpado.server=true \
   --J=-Dpado.config-file=$PADO_ETC_DIR/pado.xml \
   --J=-Dpado.log.gridInfo=false \
   --J=-Djavax.xml.accessExternalDTD=all \
   --J=-Djavax.net.ssl.trustStore=$PADO_SECURITY_DIR/pado.keystore" 
   
   CLASSPATH="$CLASSPATH:$PADO_HOME/plugins/*:$PADO_HOME/lib/*"
fi
