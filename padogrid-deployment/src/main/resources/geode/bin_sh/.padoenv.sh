#
# .padoenv.sh file is sourced in by start_member to intialize the Pado environment. Pado
# is enabled if PADO_HOME is defined and the pado.properties file exists in the cluster's
# etc/grid directory. PADO_HOME must be set in the workspace's setenv.sh file.
#
PADO_ENABLED="false"
if [ "$PADO_HOME" != "" ] && [ -f "$CLUSTER_DIR/etc/grid/pado.properties" ]; then
  PADO_ENABLED="true"
fi

if [ "$PADO_ENABLED" == "false" ]; then
  return 0
fi

#
# parseFileName parses the specified file name to replace '.'  with '-'
# based on the file name that includes the dot notation based version number
# For example, the file name, 'parseFileName pado-biz-gemfire-0.6.0-B1.jar 2'
# returns FILE_HEAD=pado-biz-gemfire-0-6, replacing the first '.' with '-' end
# ending it at the seoncd '.'.
#
# @param fileName       file name
# @param delimiterCount dot delimiter count where to end the file name
# @return FILE_HEAD     File head with '.' replaced with '-' up to the specified dot
#                       delimiter count, but not including the last dot.
#
function parseFileName
{
   local FILE_NAME=$1
   local DELIMITER_COUNT=$2
   local IFS='.'; vector=($FILE_NAME); unset IFS;
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

# Pado version
PADO_VERSION="${PADO_HOME#*pado_}"

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

if [ "$CLUSTER_TYPE" == "geode" ]; then
   export CLASSPATH=$CLASSPATH:$PLUGIN_JARS:$PADO_HOME/lib/*:$GEODE_HOME/lib/antlr-2.7.7.jar:$GEODE_HOME/lib/gfsh-dependencies.jar
else
   export CLASSPATH=$CLASSPATH:$PLUGIN_JARS:$PADO_HOME/lib/*:$GEMFIRE_HOME/lib/antlr-2.7.7.jar:$GEMFIRE_HOME/lib/gfsh-dependencies.jar
fi

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
--J="-javaagent:$PADO_HOME/lib/pado-core-$PADO_VERSION.jar" \
--J=-Dgemfire.distributed-system-id=$SYSTEM_ID \
--J=-Dpado.home.dir=$CLUSTER_DIR \
--J=-Djava.awt.headless=true \
--J=-Dpado.security.enabled=false \
--J=-Dgfinit.cacheserver.1.port=$MEMBER_PORT \
--J=-Dgfinit.cacheserver.1.notify-by-subscription=true \
--J=-Dgfinit.cacheserver.1.socket-buffer-size=131072 \
--J=-DSITE=$SITE \
--J=-DDISK_STORE_DIR="$MEMBER_DIR/store" \
--J=-DREMOTE_SYSTEM_ID_1= \
--J=-DREMOTE_SYSTEM_ID_2= \
--J=-Dpado.grid.id=$GRID_ID \
--J=-Dpado.grid.name=$GRID_NAME \
--J=-Dpado.site.id=$SITE_ID \
--J=-Dpado.site.name=$SITE_NAME \
--J=-Dpado.plugins.dir="$PADO_PLUGINS_DIR" \
--J=-Dpado.etc.dir="$CLUSTER_DIR/etc"  \
--J=-Dpado.etc.grid.dir="$ETC_DIR/grid" \
--J=-Dpado.db.dir="$CLUSTER_DIR/db" \
--J=-Dpado.properties="$ETC_DIR/grid/pado.properties" \
--J=-Dpado.appConfigDir="$ETC_DIR/grid/app" \
--J=-Dpado.server=true \
--J=-Dpado.config-file="$ETC_DIR/grid/pado.xml" \
--J=-Dpado.log.gridInfo=false \
--J=-Djavax.xml.accessExternalDTD=all \
--J=-Djavax.net.ssl.trustStore="$CLUSTER_DIR/security/pado.keystore""

#echo "*****************************************"
#echo PADO_JAVA_OPTS=$PADO_JAVA_OPTS
#echo "*****************************************"
