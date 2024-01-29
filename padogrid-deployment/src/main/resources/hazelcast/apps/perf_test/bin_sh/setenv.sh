# ========================================================================
# Copyright (c) 2020-2024 Netcrest Technologies, LLC. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ========================================================================

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

# Hibernate
JAVA_OPTS="$JAVA_OPTS -Dhazelcast-addon.hibernate.config=$APP_ETC_DIR/hibernate.cfg-mysql.xml"
#JAVA_OPTS="$JAVA_OPTS -Dhazelcast-addon.hibernate.config=$APP_ETC_DIR/hibernate.cfg-postgresql.xml"
#JAVA_OPTS="$JAVA_OPTS -Dhazelcast-addon.hibernate.config=$APP_ETC_DIR/hibernate.cfg-derby.xml"

# CLASSPATH="$CLASSPATH"

# Set Hazelcast version to download it via the 'build_app' script. This value overrides
# the workspace product (Hazelcast) version. This variable should be set if your
# application does not require Hazelcast locally installed.
# HAZELCAST_VERSION=
