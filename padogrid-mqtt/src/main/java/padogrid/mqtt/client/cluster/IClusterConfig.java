/*
 * Copyright (c) 2023 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package padogrid.mqtt.client.cluster;

/**
 * ClusterConfig defines broker and probing properties.
 * 
 * @author dpark
 *
 */
public interface IClusterConfig {
	
	public static final String PROPERTY_CLIENT_CONFIG_FILE = "padogrid.mqtt.client.cluster.config.file";
	
	// Default property values
	public static final String DEFAULT_CLIENT_CONFIG_FILE = "mqttv5-client.yaml";
	public static final String DEFAULT_CLUSTER_NAME = "cluster-default";
	public static final String DEFAULT_CLUSTER_NAME_PREFIX = "cluster";
	public static final String DEFAULT_CLUSTER_TAG = "cluster-tag";
	public static final String DEFAULT_CLUSTER_INSTANCE = "haclient";
	public final static int DEFAULT_CLUSTER_PROBE_DELAY_IN_MSEC = 5000;
	public final static int DEFAULT_TIME_TO_WAIT_IN_MSEC = 5000;
	public static final String DEFAULT_CLIENT_SERVER_URIS = "tcp://localhost:1883-1885";
}
