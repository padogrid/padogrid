#
# Enter app specifics in this file.
#

# Cluster level variables:
# ------------------------
# BASE_DIR - padogrid base dir
# ETC_DIR - Cluster etc dir
# LOG_DIR - Cluster log dir

# App level variables:
# --------------------
# APPS_DIR - <padogrid>/apps dir
# APP_DIR - App base dir
# APP_ETC_DIR - App etc dir
# APP_LOG_DIR - App log dir

BLUE_FILE=$APP_DIR/etc/hazelcast-client-blue.xml
GREEN_FILE=$APP_DIR/etc/hazelcast-client-green.xml
if [[ $OS_NAME == CYGWIN* ]]; then
   BLUE_FILE=$(cygpath -wp "$BLUE_FILE")
   GREEN_FILE=$(cygpath -wp "$GREEN_FILE")
fi

# Set JAVA_OPT to include your app specifics.
JAVA_OPTS="-Xms1g -Xmx1g"

# For blue/green tests
# These are imported from hazelcast-client-failover.xml which is used
# to configure the clients apps only if the -failover option is specified.
JAVA_OPTS="$JAVA_OPTS -Dpadogrid.blue=$BLUE_FILE \
-Dpadogrid.green=$GREEN_FILE"

# HAZELCAST_CLIENT_CONFIG_FILE defaults to etc/hazelcast-client.xml
# HAZELCAST_CLIENT_FAILOVER_CONFIG_FILE defaults to etc/hazelcast-client-failover.xml. It is
# used only if the -failover option is specified.
JAVA_OPTS="$JAVA_OPTS -Dhazelcast.client.config=$HAZELCAST_CLIENT_CONFIG_FILE \
-Dhazelcast.client.failover.config=$HAZELCAST_CLIENT_FAILOVER_CONFIG_FILE"

# CLASSPATH="$CLASSPATH"
