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

# Set JAVA_OPT to include your app specifics.
JAVA_OPTS="-Xms1g -Xmx1g"

# GEMFIRE_PROPERTY_FILE defaults to etc/client-gemfire.properties
# GEODE_CLIENT_CONFIG_FILE defaults to etc/client-cache.xml
JAVA_OPTS="$JAVA_OPTS -DgemfirePropertyFile=$GEMFIRE_PROPERTY_FILE \
	-Dgemfire.cache-xml-file=$GEODE_CLIENT_CONFIG_FILE"

# CLASSPATH="$CLASSPATH"
