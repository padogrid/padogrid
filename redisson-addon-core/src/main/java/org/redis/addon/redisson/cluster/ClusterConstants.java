package org.redis.addon.redisson.cluster;

public interface ClusterConstants {
	public final String PROPERTY_CONFIG_FILE = "org.redis.addon.redisson.config.file";
	public final String PROPERTY_NODE_ADDRESSES = "org.redis.addon.redisson.node.addresses";
	
	public final String DEFAULT_CONFIG_FILE = "etc/redisson-client.yaml";
	public final String DEFAULT_NODE_ADDRESS = "redis://localhost:6379";
}
