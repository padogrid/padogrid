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
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.mqtt.addon.client.cluster.config.ClusterConfig;

/**
 * HaClusters provides HaMqttClient instances.
 * 
 * @author dpark
 *
 */
public final class HaClusters {
	/**
	 * Returns the default {@linkplain HaMqttClient} instance. The default
	 * instance's cluster name is defined by
	 * {@link ClusterConfig#setDefaultCluster(String)}. If undefined, then
	 * {@linkplain IClusterConfig#DEFAULT_CLUSTER_NAME} is assigned.
	 * 
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	public final static HaMqttClient getOrCreateHaMqttClient() throws IOException {
		return HaMqttClientFactory.getOrCreateHaMqttClient();
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
		return HaMqttClientFactory.getHaMqttClient(clusterName);
	}

	/**
	 * Returns the default {@linkplain HaMqttClient} instance. The default instance
	 * is identified by {@link ClusterConfig#getDefaultCluster()} or
	 * {@linkplain IClusterConfig#DEFAULT_CLUSTER_NAME}.
	 * 
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	public final static HaMqttClient getHaMqttClient() throws IOException {
		return HaMqttClientFactory.getHaMqttClient();
	}

	/**
	 * Returns the {@linkplain HaMqttClient} instance identified by the specified cluster
	 * name. If not found, then it creates and returns a new instance.
	 * 
	 * @param clusterName Cluster name
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	public final static HaMqttClient getOrCreateHaMqttClient(String clusterName) throws IOException {
		return HaMqttClientFactory.getOrCreateHaMqttClient(clusterName);
	}

	/**
	 * Returns the {@linkplain HaMqttClient} instance identified by the specified
	 * {@linkplain ClusterConfig.Cluster#getName()}. If not found, then it applies
	 * the specified cluster configuration to create and return a new instance.
	 * 
	 * @param clusterConfig Cluster configuration
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	public final static HaMqttClient getOrCreateHaMqttClient(ClusterConfig.Cluster clusterConfig) throws IOException {
		return HaMqttClientFactory.getOrCreateHaMqttClient(clusterConfig, null, null);
	}

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
	public final static HaMqttClient getOrCreateHaMqttClient(ClusterConfig.Cluster clusterConfig,
			MqttClientPersistence persistence, ScheduledExecutorService executorService) throws IOException {
		return HaMqttClientFactory.getOrCreateHaMqttClient(clusterConfig, persistence, executorService);
	}
}
