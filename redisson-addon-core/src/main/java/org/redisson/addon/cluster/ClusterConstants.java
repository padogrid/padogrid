package org.redisson.addon.cluster;

public interface ClusterConstants {
	public final String PROPERTY_CONFIG_FILE = "org.redisson.addon.config.file";
	public final String PROPERTY_NODE_ADDRESSES = "org.redisson.addon.node.addresses";
	
	public final String DEFAULT_CONFIG_FILE = "etc/redisson-client.yaml";
	public final String DEFAULT_NODE_ADDRESS = "redis://localhost:6379";
}
