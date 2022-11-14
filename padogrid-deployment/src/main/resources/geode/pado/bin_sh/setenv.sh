#
# Add cluster specific environment variables in this file.
#

#
# Set Java options, i.e., -Dproperty=xyz
#
#JAVA_OPTS=

# IMPORTANT:
#    If you are running on Windows, then you must convert the file paths from Unix notations
#    Windows notations. For example,
# HIBERNATE_CONFIG_FILE="$CLUSTER_DIR/etc/hibernate.cfg-mysql.xml"
# if [[ ${OS_NAME} == CYGWIN* ]]; then
#    HIBERNATE_CONFIG_FILE="$(cygpath -wp "$HIBERNATE_CONFIG_FILE")"
# fi
# JAVA_OPTS=" --J=-Dgeode-addon.hibernate.config="

# To use Hibernate backed CacheWriterLoaderPkDbImpl, set the following property and
# configure CacheWriterLoaderPkDbImpl in the $CLUSTER_DIR/etc/cache.xml file.
# MySQL and PostgreSQL Hibernate configuration files are provided to get
# you started. You should copy one of them and enter your DB information.
# You can include your JDBC driver in the ../pom.xml file and run ./build_app
# which downloads and places it in the $PADOGRID_WORKSPACE/lib
# directory. CLASSPATH includes all the jar files in that directory for
# the apps and clusters running in this workspace.
#
#JAVA_OPTS="$JAVA_OPTS -Dgeode-addon.hibernate.config=$CLUSTER_DIR/etc/hibernate.cfg-mysql.xml"
#JAVA_OPTS="$JAVA_OPTS -Dgeode-addon.hibernate.config=$CLUSTER_DIR/etc/hibernate.cfg-postgresql.xml"
#JAVA_OPTS="$JAVA_OPTS -Dgeode-addon.hibernate.config=$CLUSTER_DIR/etc/hibernate.cfg-derby.xml"

#
# Set RUN_SCRIPT. Absolute path required.
# If set, the 'start_member' command will run this script instead of 'gfsh start server'.
# Your run script will inherit the following:
#    JAVA      - Java executable.
#    JAVA_OPTS - Java options set by padogrid.
#    CLASSPATH - Class path set by padogrid. You can include additional libary paths.
#                You should, however, place your library files in the plugins directories
#                if possible.
#  CLUSTER_DIR - This cluster's top directory path, i.e., /Users/dpark/Padogrid/workspaces/rwe-pado/ws-pado/clusters/mypado
#
# Run Script Example:
#    "$JAVA" $JAVA_OPTS com.newco.MyMember &
#
# Although it is not required, your script should be placed in the bin_sh directory.
#
#RUN_SCRIPT=$CLUSTER_DIR/bin_sh/your-script

. $PADOGRID_HOME/geode/bin_sh/.padoenv.sh

# Locators
if [ "$LOCATORS" == "" ]; then
   LOCATOR_START_PORT=$(getClusterProperty "locator.tcp.startPort")
   let LOCATOR_PORT=LOCATOR_START_PORT+LOCATOR_NUMBER-1
   let LOCATOR_END_PORT=LOCATOR_START_PORT+MAX_LOCATOR_COUNT-1
   LOCATOR_TCP_LIST=""

   if [ "$POD" == "local" ]; then
      HOST_NAME=`hostname`
      BIND_ADDRESS=`getClusterProperty "cluster.bindAddress" "$HOST_NAME"`
      HOSTNAME_FOR_CLIENTS=`getClusterProperty "cluster.hostnameForClients" "$HOST_NAME"`
      LOCATOR_PREFIX=`getLocatorPrefix`
      pushd $RUN_DIR > /dev/null 2>&1
      for i in ${LOCATOR_PREFIX}*; do
         if [ -d "$i" ]; then
            __LOCATOR=$i
            __LOCATOR_NUM=${__LOCATOR##$LOCATOR_PREFIX}
            __LOCATOR_NUM=$(trimLeadingZero $__LOCATOR_NUM)
            let __LOCATOR_PORT=LOCATOR_START_PORT+__LOCATOR_NUM-1
            if [ "$LOCATOR_TCP_LIST" == "" ]; then
               LOCATOR_TCP_LIST="$BIND_ADDRESS[$__LOCATOR_PORT]"
            else
               LOCATOR_TCP_LIST="$LOCATOR_TCP_LIST,$BIND_ADDRESS[$__LOCATOR_PORT]"
            fi
         fi
      done
      popd > /dev/null 2>&1
   fi
   LOCATORS=$(echo $LOCATOR_TCP_LIST | sed -e "s/\[/:/g"  -e "s/\]//g")
   GEODE_LOCATORS=$LOCATOR_TCP_LIST
else
   GEODE_LOCATORS=$(echo $LOCATORS | sed -e "s/:/\[/g"  -e "s/,/\]/g" -e "s/$/\]/")
fi
SECURITY_DIR=$CLUSTER_DIR/security

if [ "$GEMFIRE_SECURITY_PROPERTY_FILE" == "" ]; then
   GEMFIRE_SECURITY_PROPERTY_FILE=$ETC_DIR/client/gfsecurity.properties
fi

#
# Check if security is enabled
#
GEMFIRE_SECURITY_PROPERTY_SYSTEM=
if [ -f $GEMFIRE_SECURITY_PROPERTY_FILE ]; then
   if [ "$SECURITY_ENABLED" == "true" ]; then
      GEMFIRE_SECURITY_PROPERTY_SYSTEM=-DgemfireSecurityPropertyFile=$GEMFIRE_SECURITY_PROPERTY_FILE
   fi
else
   if [ "$SECURITY_ENABLED" == "true" ]; then
      echo ""
      echo "Security is enabled but the following security file does not exist:"
      echo "   $GEMFIRE_SECURITY_PROPERTY_FILE"
      echo "start_server Aborted."
      echo ""
      exit
   fi
fi

if [ "$SECURITY_ENABLED" == "true" ]; then
   SECURITY_PROPERTIES=-Dpado.security.enabled=true
else
   SECURITY_PROPERTIES=-Dpado.security.enabled=false
fi

if [ "$SECURITY_ENABLED" == "true" ]; then
   GEMFIRE_SECURITY_PROPERTY_SYSTEM="$GEMFIRE_SECURITY_PROPERTY_SYSTEM -Dgemfire.security-client-auth-init=com.netcrest.pado.gemfire.security.PadoAuthInit.create"
fi
SECURITY_PROPERTIES="$SECURITY_PROPERTIES $GEMFIRE_SECURITY_PROPERTY_SYSTEM"
