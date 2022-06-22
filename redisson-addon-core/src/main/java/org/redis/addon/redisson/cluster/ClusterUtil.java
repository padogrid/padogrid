package org.redis.addon.redisson.cluster;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.redisson.Redisson;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.api.RType;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * ClusterUtil provides cluster/member specific convenience methods.
 * 
 * @author dpark
 *
 */
public class ClusterUtil {

	/**
	 * Returns the Redisson client config file defined by the system property
	 * {@link ClusterConstants#PROPERTY_CONFIG_FILE}. If the property is not
	 * defined, then it returns {@link ClusterConstants#DEFAULT_CONFIG_FILE}
	 */
	public static String getConfigFile() {
		return System.getProperty(ClusterConstants.PROPERTY_CONFIG_FILE, ClusterConstants.DEFAULT_CONFIG_FILE);
	}

	/**
	 * Returns a comma separated list of node addresses defined by the system
	 * property {@link ClusterConstants#PROPERTY_NODE_ADDRESSES}. If the property is
	 * not defined, then it returns {@link ClusterConstants#DEFAULT_NODE_ADDRESS}
	 */
	public static String[] getConfigNodeAddresses() {
		String nodes = System.getProperty(ClusterConstants.PROPERTY_NODE_ADDRESSES,
				ClusterConstants.DEFAULT_NODE_ADDRESS);
		String[] split = nodes.split(",");
		return split;
	}

	/**
	 * Returns a Config instance created with the config file defined by the system
	 * property {@link ClusterConstants#PROPERTY_CONFIG_FILE}. If the config file is
	 * not found, then it returns a default Config instance.
	 * 
	 * @throws IOException Thrown if an error occurred while reading the config file
	 */
	public static Config createConfig() throws IOException {
		File file = new File(getConfigFile());
		Config config = null;
		if (file.exists()) {
			config = Config.fromYAML(file);
		}
		if (config == null) {
			config = new Config();
		}
		return config;
	}

	/**
	 * Returns a cluster RedissonClient instance created with the config file
	 * defined by the system property {@link ClusterConstants#PROPERTY_CONFIG_FILE}.
	 * If the config file is not found, then it uses a default Config instance. The
	 * returned RedissonClient instance is configured with cluster nodes defined by
	 * the system property {@link ClusterConstants#PROPERTY_NODE_ADDRESSES}.
	 */
	public static RedissonClient createRedissonClient() {
		Config config;
		try {
			config = createConfig();
		} catch (IOException e) {
			config = new Config();
		}
		String[] nodeAddresses = ClusterUtil.getConfigNodeAddresses();
		config.useClusterServers().addNodeAddress(nodeAddresses);
		return Redisson.create(config);
	}

	/**
	 * Returns all RMaps defined in the cluster.
	 * 
	 * @param redisson Redisson client
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, RMap> getAllMaps(RedissonClient redisson) {
		TreeMap<String, RMap> mapOfMaps = new TreeMap<String, RMap>();
		RKeys keys = redisson.getKeys();
		keys.getKeys().forEach(key -> {
			if (RType.MAP.equals(keys.getType(key))) {
				mapOfMaps.put(key, redisson.getMap(key));
			}
		});
		return mapOfMaps;
	}
}
