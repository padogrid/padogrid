package org.mqtt.addon.client.cluster;

/**
 * ClusterConfig defines broker and probing properties.
 * 
 * @author dpark
 *
 */
public interface IClusterConfig {
	
	public static final String PROPERTY_CLIENT_CONFIG_FILE = "org.mqtt.addon.client.cluster.config.file";
	
	// Default property values
	public static final String DEFAULT_CLIENT_CONFIG_FILE = "mqttv5-client.yaml";
	public static final String DEFAULT_CLUSTER_NAME = "cluster-default";
	public static final String DEFAULT_CLUSTER_NAME_PREFIX = "cluster";
	public static final String DEFAULT_CLUSTER_TAG = "cluster-tag";
	public static final String DEFAULT_CLUSTER_INSTANCE = "haclient";
	public final static int DEFAULT_CLUSTER_PROBE_DELAY_IN_MSEC = 5000;
	public static final String DEFAULT_CLIENT_SERVER_URIS = "tcp://localhost:1883,tcp://localhost:1884,tcp://localhost:1885";
}
