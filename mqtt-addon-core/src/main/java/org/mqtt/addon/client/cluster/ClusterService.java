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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.mqtt.addon.client.cluster.config.ClusterConfig;
import org.mqtt.addon.client.cluster.config.ClusterConfig.Persistence;
import org.mqtt.addon.client.cluster.config.ClusterConfig.Property;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 * {@linkplain ClusterService} is a singleton class for managing the broker
 * discovery service running on a dedicate thread.
 * 
 * @author dpark
 *
 */
public class ClusterService {

	private static ClusterService clusterService;

	private Logger logger = LogManager.getLogger(ClusterService.class);

	private ClusterConfig clusterConfig = new ClusterConfig();;

	private String tag;
	private boolean isServiceEnabled;
	private int initialDelayInMsec = 0;
	private int delayInMsec;
	private String defaultClusterName;

	private ScheduledExecutorService ses;
	private ConcurrentHashMap<HaMqttClient, ClusterState> haclientMap = new ConcurrentHashMap<HaMqttClient, ClusterState>();

	private volatile boolean isStarted = false;

	/**
	 * Returns the singleton instance of BrokerProbeManager. The
	 * {@link #initialize(Properties)} method must be invoked once prior to invoking
	 * this method. Otherwise, it will return null.
	 */
	public static ClusterService getClusterService() {
		return clusterService;
	}

	private ClusterService() {
	}

	/**
	 * Initializes and starts the BrokerProbeManager when invoked for the first
	 * time. Subsequent invocations have no effect.
	 * 
	 * @param clusterConfig Cluster configuration. If null, then the configuration
	 *                      file (yaml) defined by the system property,
	 *                      {@linkplain IClusterConfig#PROPERTY_CLIENT_CONFIG_FILE},
	 *                      is read. If the system property is not defined, then
	 *                      {@linkplain IClusterConfig#DEFAULT_CLIENT_CONFIG_FILE}
	 *                      in the class path is read. If all fails, then the
	 *                      default settings are applied.
	 * @param isStart       true to start the service. If false, then the
	 *                      {@link #start()} method must be invoked to start the
	 *                      service.
	 * 
	 * @return ClusterService instance
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	public static synchronized ClusterService initialize(ClusterConfig clusterConfig, boolean isStart) throws IOException {
		if (clusterService == null) {
			if (clusterConfig == null) {
				String configFile = System.getProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE);
				if (configFile != null && configFile.length() > 0) {
					File file = new File(configFile);
					Yaml yaml = new Yaml(new Constructor(ClusterConfig.class));
					yaml.setBeanAccess(BeanAccess.FIELD);
					FileReader reader = new FileReader(file);
					clusterConfig = yaml.load(reader);
				} else {
					InputStream inputStream = ClusterService.class.getClassLoader()
							.getResourceAsStream(IClusterConfig.DEFAULT_CLIENT_CONFIG_FILE);
					if (inputStream != null) {
						Yaml yaml = new Yaml(new Constructor(ClusterConfig.class));
						yaml.setBeanAccess(BeanAccess.FIELD);
						clusterConfig = yaml.load(inputStream);
					}
				}
			}

			clusterService = new ClusterService();
			clusterService.init(clusterConfig);
			if (isStart) {
				clusterService.start();
			}
		}
		return clusterService;
	}

	/**
	 * Initializes ClusterService when invoked for the first time.
	 * 
	 * @param clusterConfig Cluster configuration
	 */
	private synchronized void init(ClusterConfig clusterConfig) throws IOException {

		if (clusterConfig == null) {
			clusterConfig = this.clusterConfig;
		} else {
			this.clusterConfig = clusterConfig;
		}
		this.tag = clusterConfig.getTag();
		this.isServiceEnabled = clusterConfig.isEnabled();
		this.delayInMsec = clusterConfig.getProbeDelay();
		this.defaultClusterName = clusterConfig.getDefaultCluster();

		ClusterConfig.Cluster[] clusters = clusterConfig.getClusters();
		if (clusters != null) {
			for (ClusterConfig.Cluster cluster : clusters) {
				if (cluster.isEnabled()) {
					String clusterName = cluster.getName();
					if (clusterName == null || clusterName.length() == 0) {
						clusterName = IClusterConfig.DEFAULT_CLUSTER_NAME;
					}
				}
				
				// Create HaMqttClient. Connect only if autoConnect is enabled.
				try {
					HaMqttClient client = HaClusters.getOrCreateHaMqttClient(cluster);
					if (cluster.isAutoConnect()) {
						client.connect();
					}
				} catch (MqttException | IOException e) {
					// ignore
				}
			}
		}

		if (logger != null) {
			logger.info(
					String.format("initialized [isServiceEnabled=%s, delayInMsec=%s]", isServiceEnabled, delayInMsec));
		}
	}

	/**
	 * Returns a new MqttClientPersistence instance to be used for the clusters that
	 * have not defined persistence. It returns a new MqttClientPersistence instance
	 * if it is defined and can be created. Otherwise, it returns null. If it
	 * returns null, then the default persistence, i.e., MqttDefaultFilePersistence
	 * should be used.
	 * <p>
	 * If it fails to create a new instance then it logs a warning message and
	 * returns null.
	 */
	public MqttClientPersistence createMqttClientPersistence() {
		MqttClientPersistence persistence = null;
		if (clusterConfig.getPersistence() != null) {
			try {
				persistence = clusterConfig.getPersistence().getMqttClientPersistence();
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
					| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.warn(String.format(
						"Exception raised while creating MqttClientPersistence [%s]. Proceeding with the default persistence instead.",
						e.getMessage()));
			}
		}
		return persistence;
	}

	ClusterState addHaClient(HaMqttClient haclient, ClusterConfig.Cluster clusterConfig,
			MqttClientPersistence persistence, ScheduledExecutorService executorService) {
		ClusterState state = haclientMap.get(haclient);
		if (state == null) {
			state = new ClusterState(haclient, clusterConfig, persistence, executorService);
			haclientMap.put(haclient, state);
		}
		return state;
	}

	/**
	 * Closes and removes the specified client. Once removed, the client is no
	 * longer operational.
	 * 
	 * @param haclient Client to remove
	 * @param force    true to forcibly close the client
	 */
	void removeHaClient(HaMqttClient haclient, boolean force) {
		if (haclient == null) {
			return;
		}
		ClusterState state = haclientMap.remove(haclient);
		if (state != null) {
			state.close(force);
		}
	}

	/**
	 * Starts the service when invoked for the first time. Subsequent invocations
	 * have no effect. It starts only if {@link #isServiceEnabled()} is true. This
	 * method must be invoked to activate the service.
	 */
	public synchronized void start() {
		if (isServiceEnabled && isStarted == false) {
			if (ses != null) {
				if (ses.isShutdown()) {
					return;
				}
			}
			ses = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r, ClusterState.class.getSimpleName());
					thread.setDaemon(true);
					return thread;
				}
			});
			ses.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					for (Map.Entry<HaMqttClient, ClusterState> entry : haclientMap.entrySet()) {
						entry.getValue().reviveDeadEndpoints();
					}
				}
			}, initialDelayInMsec, delayInMsec, TimeUnit.MILLISECONDS);
			isStarted = true;
			if (logger != null) {
				logger.info(String.format("ClusterService started: %s", this));
			}
		}
	}

	/**
	 * Stops the service. Once stopped, the service is no longer operational and
	 * usable.
	 */
	public synchronized void stop() {
		if (ses != null) {
			ses.shutdown();
			if (logger != null) {
				logger.info("ClusterService stopped. No longer operational.");
			}
		}
	}

	/**
	 * Returns the default cluster name. The default cluster name is configurable.
	 * If not configured, then it returns
	 * {@link IClusterConfig#DEFAULT_CLUSTER_NAME}.
	 */
	public String getDefaultClusterName() {
		return this.defaultClusterName;
	}

	/**
	 * Returns true if the discovery service is enabled. Disabled discovery service
	 * means no Broker probing is conducted in the dedicated thread.
	 */
	public boolean isServiceEnabled() {
		return this.isServiceEnabled;
	}

	/**
	 * Returns the service delay in msec between probes.
	 */
	public int getServiceDelayInMsec() {
		return delayInMsec;
	}

	public ClusterState getClusterState(HaMqttClient haclient) {
		return haclientMap.get(haclient);
	}

	/**
	 * Returns true if the service has been started, i.e., the {@link #start} method
	 * has been invoked.
	 */
	public boolean isStarted() {
		return isStarted;
	}

	/**
	 * Returns true if the thread has been terminated.
	 */
	public boolean isTerminated() {
		if (ses == null) {
			return true;
		} else {
			return ses.isTerminated();
		}
	}

	@Override
	public String toString() {
		return "ClusterService [isServiceEnabled=" + isServiceEnabled + ", delayInMsec=" + delayInMsec + ", tag=" + tag
				+ ", isStarted=" + isStarted + "]";
	}
}
