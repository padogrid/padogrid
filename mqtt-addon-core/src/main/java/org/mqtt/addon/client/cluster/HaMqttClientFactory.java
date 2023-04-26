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
package org.mqtt.addon.client.cluster;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.mqtt.addon.client.cluster.config.ClusterConfig;

/**
 * HaMqttClientFactory manages all HaMqttClient instances.
 * 
 * @author dpark
 *
 */
class HaMqttClientFactory {

	private static final AtomicInteger clusterNum = new AtomicInteger();
	private static final ConcurrentMap<String, HaMqttClient> clusterMap = new ConcurrentHashMap<>(3);

	/**
	 * Returns the {@linkplain HaMqttClient} instance identified by the specified
	 * {@linkplain ClusterConfig.Cluster#getName()} with the specified persistence
	 * enabled. If not found, then it applies the specified cluster configuration to
	 * create and return a new instance.
	 * <p>
	 * <b>IMPORTANT (MqttClient Deadlock)</b>: If <code>executorService</code> is
	 * specified, then it uses
	 * {@linkplain MqttClient#MqttClient(String, String, MqttClientPersistence, ScheduledExecutorService)}
	 * for client instances. <code>MqttClient</code> dedicates a control thread plus
	 * three (3) threads per broker connection. This means the executor service's
	 * thread pool must provide at least three (3) times the number of brokers in
	 * the cluster plus one (1). For example, if the cluster has 10 endpoints then
	 * the executor service's thread pool must provide at least 3*10+1=31 threads;
	 * otherwise, a deadlock may occur.
	 * 
	 * @param clusterConfig   Cluster configuration
	 * @param persistence     MqttClient persistence data store. It overrides
	 *                        clusterConfig. If null, then clusterConfig is used.
	 * @param executorService Scheduled executor service.
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	final static HaMqttClient getOrCreateHaMqttClient(ClusterConfig.Cluster clusterConfig,
			MqttClientPersistence persistence, ScheduledExecutorService executorService)
			throws IOException {

		// Initialize the cluster service. Initialization is done once.
		ClusterService.initialize(null, true);
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
			client = new HaMqttClient(clusterConfig, persistence, executorService);
			clusterMap.put(clusterName, client);
		}
		return client;
	}

	final static HaMqttClient getOrCreateHaMqttClient() throws IOException {
		return getOrCreateHaMqttClient(null, null, null);
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
	final static HaMqttClient getHaMqttClient(String clusterName) {
		return clusterMap.get(clusterName);
	}

	/**
	 * Returns the default cluster client.
	 * @throws IOException
	 */
	final static HaMqttClient getHaMqttClient() throws IOException {
		// Initialize the cluster service. Initialization is done once.
		ClusterService.initialize(null, true);
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
	final static HaMqttClient getOrCreateHaMqttClient(String clusterName) throws IOException {
		HaMqttClient client = clusterMap.get(clusterName);
		if (client == null) {
			// Initialize the cluster service. Initialization is done once.
			ClusterService.initialize(null, true);
			// Check if the initialization created the client
			client = clusterMap.get(clusterName);
			if (client == null) {
				ClusterConfig.Cluster clusterConfig = new ClusterConfig.Cluster();
				clusterConfig.setName(clusterName);
				client = new HaMqttClient(clusterConfig, null, null);
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
	void remove(String clusterName) throws MqttException {
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
	void remove(HaMqttClient client) throws MqttException {
		if (client == null) {
			return;
		}
		remove(client.getClusterName());
	}
}