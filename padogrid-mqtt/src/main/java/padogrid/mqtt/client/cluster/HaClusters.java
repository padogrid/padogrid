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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttClientPersistence;

import padogrid.mqtt.client.cluster.config.ClusterConfig;

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
	 * Returns the {@linkplain HaMqttClient} instance identified by the specified
	 * cluster name. If not found, then it creates and returns a new instance.
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

	/**
	 * Initializes and starts the cluster service with the default cluster when
	 * invoked for the first time. Subsequent invocations have no effect. Invoking
	 * this method is not necessary as it is lazily invoked by the
	 * {@link #getOrCreateHaMqttClient()} methods. However, it is recommended that
	 * the application should eagerly invoke this method to start the cluster
	 * initialization process to reduce the overall cluster creation latency.
	 * 
	 * @return ClusterService instance
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	static final void initialize(boolean isStart) throws IOException {
		ClusterService.initialize(isStart);
	}

	/**
	 * Initializes and starts the cluster service when invoked for the first time.
	 * Subsequent invocations have no effect. Invoking this method is not necessary
	 * as it is lazily invoked by the {@link #getOrCreateHaMqttClient()} methods.
	 * However, it is recommended that the application should eagerly invoke this
	 * method to start the cluster initialization process to reduce the overall
	 * cluster creation latency.
	 * 
	 * @param clusterConfig Cluster configuration. If null, then the configuration
	 *                      file (yaml) defined by the system property,
	 *                      {@linkplain IClusterConfig#PROPERTY_CLIENT_CONFIG_FILE},
	 *                      is read. If the system property is not defined, then
	 *                      {@linkplain IClusterConfig#DEFAULT_CLIENT_CONFIG_FILE}
	 *                      in the class path is read. If all fails, then the
	 *                      default settings are applied.
	 * 
	 * @return ClusterService instance
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	public final static void initialize(ClusterConfig clusterConfig) throws IOException {
		ClusterService.initialize(clusterConfig, true);
	}

	/**
	 * Initializes and starts the cluster service when invoked for the first time.
	 * Subsequent invocations have no effect. Invoking this method is not necessary
	 * as it is lazily invoked by the {@link #getOrCreateHaMqttClient()} methods.
	 * However, it is recommended that the application should eagerly invoke this
	 * method to start the cluster initialization process to reduce the overall
	 * cluster creation latency.
	 * 
	 * @param configFile Cluster configuration file. If null, then the configuration
	 *                   file (yaml) defined by the system property,
	 *                   {@linkplain IClusterConfig#PROPERTY_CLIENT_CONFIG_FILE}, is
	 *                   read. If the system property is not defined, then
	 *                   {@linkplain IClusterConfig#DEFAULT_CLIENT_CONFIG_FILE} in
	 *                   the class path is read. If all fails, then the default
	 *                   settings are applied.
	 * 
	 * @return ClusterService instance
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	public final static void initialize(File clusterConfigFile) throws IOException {
		ClusterService.initialize(clusterConfigFile, true);
	}

	/**
	 * Initializes and starts the cluster service when invoked for the first time.
	 * Subsequent invocations have no effect. Invoking this method is not necessary
	 * as it is lazily invoked by the {@link #getOrCreateHaMqttClient()} methods.
	 * However, it is recommended that the application should eagerly invoke this
	 * method to start the cluster initialization process to reduce the overall
	 * cluster creation latency.
	 * <p>
	 * This method should be invoked if the configuration file is specified by the
	 * system property, {@linkplain IClusterConfig#DEFAULT_CLIENT_CONFIG_FILE}. If
	 * this property is not specified, then the default settings are applied.
	 * 
	 * @throws IOException Thrown if unable to read the configuration source
	 *                     specified by the system property,
	 *                     {@linkplain IClusterConfig#DEFAULT_CLIENT_CONFIG_FILE}.
	 */
	public final static void initialize() throws IOException {
		ClusterService.initialize(true);
	}

	/**
	 * Connects all clusters. Normally, the application connects each cluster
	 * individually. This is a convenience method that connects all clusters without
	 * having the application to connect each cluster individually. If the cluster
	 * service has not been initialized then it silently returns and there is no
	 * effect.
	 * 
	 * @see #initialize(boolean)
	 * @see #initialize(ClusterConfig)
	 * @see #initialize(File)
	 */
	public final static void connect() {
		if (ClusterService.getClusterService() == null) {
			return;
		}
		ClusterService.getClusterService().connect();
	}

	/**
	 * Disconnects and closes all the clusters.
	 */
	public final static void closeClusters() {
		if (ClusterService.getClusterService() == null) {
			return;
		}
		ClusterService.getClusterService().close();
	}

	/**
	 * Closes all the clusters and then stops the cluster service. Once stopped, the
	 * cluster service is no longer operational.
	 */
	public final static void stop() {
		if (ClusterService.getClusterService() == null) {
			return;
		}
		ClusterService.getClusterService().stop();
	}

	/**
	 * Returns the default cluster name.
	 */
	public final static String getDefaultClusterName() {
		return ClusterService.getClusterService().getDefaultClusterName();
	}
}
