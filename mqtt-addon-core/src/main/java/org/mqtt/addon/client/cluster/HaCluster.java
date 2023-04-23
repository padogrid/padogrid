package org.mqtt.addon.client.cluster;

import java.io.IOException;

import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.mqtt.addon.client.cluster.config.ClusterConfig;

/**
 * HaCluster provides HaMqttClient instances.
 * 
 * @author dpark
 *
 */
public final class HaCluster {
	/**
	 * Returns the default {@linkplain HaMqttClient} instance. The default
	 * instance's cluster name is defined by
	 * {@link ClusterConfig#setDefaultCluster(String)}. If undefined, then
	 * {@linkplain IClusterConfig#DEFAULT_CLUSTER_NAME} is assigned.
	 * 
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	public final static HaMqttClient getOrCreateHaMqttClient() throws IOException {
		return HaClientInstanceFactory.getOrCreateHaMqttClient();
	}

	/**
	 * Returns the {@linkplain HaMqttClient} instance identified by the specified
	 * cluster name. It returns null if the HaMqttClient instance is not found. To
	 * create an instance, invoke {@link #getOrCreateHaMqttClient(String)}.
	 * 
	 * @param clusterName Cluster name
	 * @return null if the HaMqttClient instance is not found.
	 */
	public final static HaMqttClient getHaMqttClient(String clusterName) {
		return HaClientInstanceFactory.getHaMqttClient(clusterName);
	}

	/**
	 * Returns the default {@linkplain HaMqttClient} instance. The default instance
	 * is identified by {@link ClusterConfig#getDefaultCluster()} or
	 * {@linkplain IClusterConfig#DEFAULT_CLUSTER_NAME}.
	 * 
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	public final static HaMqttClient getHaMqttClient() throws IOException {
		return HaClientInstanceFactory.getHaMqttClient();
	}

	/**
	 * Returns the {@linkplain HaMqttClient} identified by the specified cluster
	 * name. If not found, then it creates and returns a new instance.
	 * 
	 * @param clusterName Cluster name
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	public final static HaMqttClient getOrCreateHaMqttClient(String clusterName) throws IOException {
		return HaClientInstanceFactory.getOrCreateHaMqttClient(clusterName);
	}

	/**
	 * Returns the {@linkplain HaMqttClient} identified by the specified
	 * {@linkplain ClusterConfig.Cluster#getName()}. If not found, then it applies
	 * the specified cluster configuration to create and return a new instance.
	 * 
	 * @param clusterConfig Cluster configuration
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	public final static HaMqttClient getOrCreateHaMqttClient(ClusterConfig.Cluster clusterConfig) throws IOException {
		return HaClientInstanceFactory.getOrCreateHaMqttClient(clusterConfig, null);
	}

	/**
	 * Returns the {@linkplain HaMqttClient} identified by the specified
	 * {@linkplain ClusterConfig.Cluster#getName()} with the specified persistence
	 * enabled. If not found, then it applies the specified cluster configuration to
	 * create and return a new instance.
	 * 
	 * @param clusterConfig Cluster configuration
	 * @param persistence   MqttClient persistence data store
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	public final static HaMqttClient getOrCreateHaMqttClient(ClusterConfig.Cluster clusterConfig,
			MqttClientPersistence persistence) throws IOException {
		return HaClientInstanceFactory.getOrCreateHaMqttClient(clusterConfig, persistence);
	}
}
