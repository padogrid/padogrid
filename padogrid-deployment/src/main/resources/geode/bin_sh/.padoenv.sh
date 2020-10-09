#
# .padoenv.sh file is souced in by start_member to intialize the Pado environment. Pado is
# enabled only if PADO_HOME is defined. PADO_HOME must be set in the workspace's setenv.sh file.
#

if [ "$PADO_HOME" == "" ]; then
  PADO_ENABLED="false"
else
  PADO_ENABLED="true"
fi

if [ "$PADO_ENABLED" == "false" ]; then
  return 0
fi

# parseFileName parses file names found in the lib directory
# to drop the version postfix from the select file names.
# Input:
#    arg1 fileName - file name
#    arg2 delimiterCount - delimiter count of postfix for determining the index number
# Output:
#    FILE_HEAD - File header without the postfix.
function parseFileName
{
   local FILE_NAME=$1
   local DELIMITER_COUNT=$2
   IFS='.'; vector=($FILE_NAME); unset IFS;
   let LAST_INDEX=${#vector[@]}-1
   let FILE_HEAD_LAST_INDEX=LAST_INDEX-DELIMITER_COUNT
   FILE_HEAD=
   for (( i = 0; i <= ${FILE_HEAD_LAST_INDEX}; i++ ))
   do
      if [ $i == 0 ]; then
         FILE_HEAD=${vector[$i]}
      else
         FILE_HEAD=$FILE_HEAD-${vector[$i]}
      fi
   done
}

#
# plugins jars
#
PLUGIN_JARS=
PREV_FILE_HEAD=
pushd $PADO_HOME/plugins > /dev/null 2>&1
for file in `ls *.jar | sort -r`
do
   parseFileName $file 2
   if [ "$FILE_HEAD" != "$PREV_FILE_HEAD" ]; then
      if [ "$PLUGIN_JARS" == "" ]; then
         PLUGIN_JARS=$BASE_DIR/plugins/$file
      else
         PLUGIN_JARS=$PLUGIN_JARS:$BASE_DIR/plugins/$file
      fi
   fi
   PREV_FILE_HEAD=$FILE_HEAD
done
popd > /dev/null 2>&1

export CLASSPATH=$CLASSPATH:$PLUGIN_JARS:$PADO_HOME/lib/*:$GEODE_HOME/lib/antlr-2.7.7.jar:$GEODE_HOME/lib/gfsh-dependencies.jar


GFSH_OPTS2="--cache-xml-file=/Users/dpark/Padogrid/workspaces/rwe-pado/ws-pado/clusters/mygrid/etc/cache.xml --dir=/Users/dpark/Padogrid/workspaces/rwe-pado/ws-pado/clusters/mygrid/run/mygrid-member-padomac.local-01 --initial-heap=1g --max-heap=1g --disable-default-server=true --name=mygrid-member-padomac.local-01 --locators=localhost[10334] --statistic-archive-file=/Users/dpark/Padogrid/workspaces/rwe-pado/ws-pado/clusters/mygrid/stats/mygrid-member-padomac.local-01.gfs --bind-address=localhost --hostname-for-clients=localhost --start-rest-api=true --http-service-port=7080 --http-service-bind-address=localhost"
JAVA_OPTS2="--J=-Dpado.vm.id=mygrid-member-padomac.local-01 --J=-Dpadogrid.workspace=ws-pado --J=-Dlog4j.configurationFile=/Users/dpark/Padogrid/workspaces/rwe-pado/ws-pado/clusters/mygrid/etc/log4j2.properties --J=-Dcom.sun.management.jmxremote.port=12001 --J=-Dcom.sun.management.jmxremote.ssl=false --J=-Dcom.sun.management.jmxremote.authenticate=false --J=-Dgeode.jmx=true --J=-Xdebug --J='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9101' --J=-javaagent:/Users/dpark/Padogrid/snapshots/padogrid_0.9.4-SNAPSHOT/lib/jmx_prometheus_javaagent-0.11.0.jar=8091:/Users/dpark/Padogrid/workspaces/rwe-pado/ws-pado/clusters/mygrid/etc/prometheus.yml --J=-Xloggc:/Users/dpark/Padogrid/workspaces/rwe-pado/ws-pado/clusters/mygrid/log/mygrid-member-padomac.local-01-gc.log --J=-XX:+PrintGCDetails --J=-XX:+PrintGCDateStamps --J=-XX:+UseParNewGC --J=-XX:+UseConcMarkSweepGC --J=-XX:CMSInitiatingOccupancyFraction=75 --J=-Djava.awt.headless=true --J=-Djava.net.preferIPv4Stack=true --J=-DgemfirePropertyFile=/Users/dpark/Padogrid/workspaces/rwe-pado/ws-pado/clusters/mygrid/etc/gemfire.properties --J=-Dgemfire.log-file=/Users/dpark/Padogrid/workspaces/rwe-pado/ws-pado/clusters/mygrid/log/mygrid-member-padomac.local-01.log --J=-DDISK_STORE_DIR= --J=-DREMOTE_SYSTEM_ID_1= --J=-DREMOTE_SYSTEM_ID_2= --J=-Dgemfire.PREFER_SERIALIZED=true --J=-Dgemfire.BucketRegion.alwaysFireLocalListeners=false --J=-Dgeode-addon.server.port=40404"

SYSTEM_ID=1
SITE=us
SITE_ID=$SITE
SITE_NAME=$SITE
GRID=$CLUSTER
GRID_ID=$CLUSTER
GRID_NAME=${GRID}-${SITE_ID}

# plugins directory
if [ "$PADO_PLUGINS_DIR" == "" ]; then
   PADO_PLUGINS_DIR=$CLUSTER_DIR/plugins
fi
if [ ! -d $PADO_PLUGINS_DIR ]; then
  mkdir -p $PADO_PLUGINS_DIR
fi
# db directory
if [ "$PADO_DB_DIR" == "" ]; then
   PADO_DB_DIR=$CLUSTER_DIR/db
fi
if [ ! -d $PADO_DB_DIR ]; then
  mkdir -p $PADO_DB_DIR
fi
# vp directory
if [ ! -d $PADO_DB_DIR/vp ]; then
  mkdir -p $PADO_DB_DIR/vp
fi
ETC_GRID_DIR=$ETC_DIR/$GRID_ID

# etc directories env passed into Pado
export PADO_ETC_DIR=$ETC_DIR
export PADO_ETC_GRID_DIR=$ETC_GRID_DIR

MEMBER_NUMBER=$MEMBER_NUM_NO_LEADING_ZERO
MEMBER_START_PORT=`getClusterProperty "tcp.startPort" $DEFAULT_MEMBER_START_PORT`
let MEMBER_PORT=MEMBER_START_PORT+MEMBER_NUMBER-1

MEMBER=`getMemberName $MEMBER_NUMBER`
MEMBER_DIR=$RUN_DIR/$MEMBER

PADO_JAVA_OPTS=" \
--J=-Dgemfire.distributed-system-id=$SYSTEM_ID \
--J=-Dpado.home.dir=$CLUSTER_DIR \
--J=-Djava.awt.headless=true \
--J=-Dpado.security.enabled=false \
--J=-Dgfinit.cacheserver.1.port=$MEMBER_PORT \
--J=-Dgfinit.cacheserver.1.notify-by-subscription=true \
--J=-Dgfinit.cacheserver.1.socket-buffer-size=131072 \
--J=-DSITE=$SITE \
--J=-DDISK_STORE_DIR=$MEMBER_DIR/store \
--J=-DREMOTE_SYSTEM_ID_1= \
--J=-DREMOTE_SYSTEM_ID_2= \
--J=-Dpado.grid.id=$GRID_ID \
--J=-Dpado.grid.name=$GRID_NAME \
--J=-Dpado.site.id=$SITE_ID \
--J=-Dpado.site.name=$SITE_NAME \
--J=-Dpado.plugins.dir=$PADO_PLUGINS_DIR \
--J=-Dpado.etc.dir=$CLUSTER_DIR/etc  \
--J=-Dpado.etc.grid.dir=$ETC_DIR/$GRID_ID \
--J=-Dpado.db.dir=$CLUSTER_DIR/db \
--J=-Dpado.properties=$ETC_DIR/$GRID_ID/pado.properties \
--J=-Dpado.appConfigDir=$ETC_DIR/$GRID_ID/app \
--J=-Dpado.server=true \
--J=-Dpado.config-file=$ETC_DIR/$GRID_ID/pado.xml \
--J=-Dpado.log.gridInfo=false \
--J=-Djavax.xml.accessExternalDTD=all \
--J=-Djavax.net.ssl.trustStore=$CLUSTER_DIR/security/pado.keystore"

#echo "*****************************************"
#echo PADO_JAVA_OPTS=$PADO_JAVA_OPTS
#echo "*****************************************"
