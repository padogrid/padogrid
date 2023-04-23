package org.mqtt.addon.client.cluster;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.mqtt.addon.client.cluster.config.ClusterConfig;

/**
 * HaClientInstanceFactory manages all HaMqttClient instances.
 * 
 * @author dpark
 *
 */
class HaClientInstanceFactory {

	private static final AtomicInteger clusterNum = new AtomicInteger();
	private static final ConcurrentMap<String, HaMqttClient> clusterMap = new ConcurrentHashMap<>(3);

	public final static HaMqttClient getOrCreateHaMqttClient(ClusterConfig.Cluster clusterConfig,
			MqttClientPersistence persistence)
			throws IOException {

		// Initialize the cluster service. Initialization is done once.
		ClusterService.initialize(null);
		if (clusterConfig == null) {
			clusterConfig = new ClusterConfig.Cluster();
		}
		String clusterName = clusterConfig.getName();
		if (clusterName == null || clusterName.length() == 0) {
			if (clusterName == null) {
				clusterName = createClusterName();
				clusterConfig.setName(clusterName);
			}
		}
		HaMqttClient client = clusterMap.get(clusterName);
		if (client == null) {
			client = new HaMqttClient(clusterConfig, persistence);
			clusterMap.put(clusterName, client);
		}
		return client;
	}

	public final static HaMqttClient getOrCreateHaMqttClient() throws IOException {
		return getOrCreateHaMqttClient(null, null);
	}

	private final static String createClusterName() {
		String clusterName = IClusterConfig.DEFAULT_CLUSTER_NAME_PREFIX + clusterNum.incrementAndGet();
		return clusterName;
	}
	
	/**
	 * Returns the client identified by the specified cluster name.
	 * 
	 * @param clusterName
	 * @return null if the client is not found.
	 */
	public final static HaMqttClient getHaMqttClient(String clusterName) {
		return clusterMap.get(clusterName);
	}

	/**
	 * Returns the default cluster client.
	 * @throws IOException
	 */
	public final static HaMqttClient getHaMqttClient() throws IOException {
		// Initialize the cluster service. Initialization is done once.
		ClusterService.initialize(null);
		String clusterName = ClusterService.getClusterService().getDefaultClusterName();
		return getOrCreateHaMqttClient(clusterName);
	}

	/**
	 * Returns the client identified by the specified cluster name. If the client is
	 * not found then it creates and returns a new client with the default settings.
	 * 
	 * @param clusterName Cluster name.
	 * @throws IOException
	 */
	public final static HaMqttClient getOrCreateHaMqttClient(String clusterName) throws IOException {
		HaMqttClient client = clusterMap.get(clusterName);
		if (client == null) {
			// Initialize the cluster service. Initialization is done once.
			ClusterService.initialize(null);
			// Check if the initialization created the client
			client = clusterMap.get(clusterName);
			if (client == null) {
				ClusterConfig.Cluster clusterConfig = new ClusterConfig.Cluster();
				clusterConfig.setName(clusterName);
				client = new HaMqttClient(clusterConfig, null);
				clusterMap.put(clusterName, client);
			}
		}
		return client;
	}

	/**
	 * Removes the client identified by the specified cluster name. It closes the
	 * client if not already closed.
	 * 
	 * @param clusterName Cluster name
	 * @throws MqttException 
	 */
	public void remove(String clusterName) throws MqttException {
		if (clusterName == null) {
			return;
		}
		HaMqttClient c = clusterMap.remove(clusterName);
		if (c.isClosed() == false) {
			c.close();
		}
	}

	/**
	 * Closes if not already closed and removes the specified client.
	 * 
	 * @param client Client to remove.
	 * @throws MqttException 
	 */
	public void remove(HaMqttClient client) throws MqttException {
		if (client == null) {
			return;
		}
		remove(client.getClusterName());
	}
}