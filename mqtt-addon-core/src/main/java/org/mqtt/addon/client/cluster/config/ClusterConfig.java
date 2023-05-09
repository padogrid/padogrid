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
package org.mqtt.addon.client.cluster.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.client.persist.MqttDefaultFilePersistence;
import org.mqtt.addon.client.cluster.IClusterConfig;
import org.mqtt.addon.client.cluster.PublisherType;
import org.mqtt.addon.client.cluster.internal.ConfigUtil;

/**
 * ClusterConfig configures one or more clusters. This class directly maps to
 * the cluster configuration file.
 * 
 * @author dpark
 *
 */
public class ClusterConfig {
	private boolean enabled = true;
	private String defaultCluster = IClusterConfig.DEFAULT_CLUSTER_NAME;
	private String tag = IClusterConfig.DEFAULT_CLUSTER_TAG;
	private int probeDelay = IClusterConfig.DEFAULT_CLUSTER_PROBE_DELAY_IN_MSEC;
	private Cluster[] clusters = new Cluster[0];
	private Persistence persistence = new Persistence();

	public ClusterConfig() {
	}

	public String getDefaultCluster() {
		return defaultCluster;
	}

	public void setDefaultCluster(String defaultCluster) {
		this.defaultCluster = defaultCluster;
	}

	public String getTag() {
		return tag;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getProbeDelay() {
		return probeDelay;
	}

	public void setProbeDelay(int probeDelay) {
		this.probeDelay = probeDelay;
	}

	public Cluster[] getClusters() {
		return clusters;
	}

	public void setClusters(Cluster[] clusters) {
		this.clusters = clusters;
	}

	public Persistence getPersistence() {
		return persistence;
	}

	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	public static class Cluster {
		private String name;
		private int fos = -1;
		private PublisherType publisherType = PublisherType.STICKY;
		private int subscriberCount = -1;
		private String primaryServerURI;
		private boolean enabled = true;
		private boolean autoConnect = true;
		private int initialEndpointCount = -1;
		private int liveEndpointCount = -1;
		private long timeToWait = IClusterConfig.DEFAULT_TIME_TO_WAIT_IN_MSEC;
		private MqttConnectionOptions connection;
		private Bridges bridges;

		/**
		 * Returns the cluster name.
		 */
		public String getName() {
			if (name == null) {
				name = IClusterConfig.DEFAULT_CLUSTER_NAME;
			} else {
				return ConfigUtil.parseStringValue(name);
			}
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public boolean isAutoConnect() {
			return autoConnect;
		}

		public void setAutoConnect(boolean autoConnect) {
			this.autoConnect = autoConnect;
		}

		public int getFos() {
			return fos;
		}

		public void setFos(int fos) {
			this.fos = fos;
		}

		/**
		 * Returns the initial endpoint count. The default value is -1, i.e., all
		 * endpoints.
		 */
		public int getInitialEndpointCount() {
			return initialEndpointCount;
		}

		/**
		 * Sets the initial endpoint count. The default value is -1, i.e., all
		 * endpoints.
		 * 
		 * @param initialEndpointCount Initial endpoint count. Less than 0 for all
		 *                             endpoints.
		 */
		public void setInitialEndpointCount(int initialEndpointCount) {
			if (initialEndpointCount < 0) {
				this.initialEndpointCount = 0;
			} else {
				this.initialEndpointCount = initialEndpointCount;
			}
		}

		public int getLiveEndpointCount() {
			return liveEndpointCount;
		}

		public void setLiveEndpointCount(int liveEndpointCount) {
			this.liveEndpointCount = liveEndpointCount;
		}

		public MqttConnectionOptions getConnection() {
			if (connection != null) {
				String[] serverURIs = connection.getServerURIs();
				if (serverURIs != null && serverURIs.length > 0) {
					List<String> serverList = ConfigUtil.parseEndpoints(serverURIs);
					if (primaryServerURI != null && serverList.contains(primaryServerURI) == false) {
						serverList.add(primaryServerURI);
					}
					connection.setServerURIs(serverList.toArray(new String[0]));
				}
			}
			return connection;
		}

		public void setConnection(MqttConnectionOptions connection) {
			this.connection = connection;
		}

		/**
		 * Returns the connection time to wait in milliseconds. Default:
		 * {@linkplain IClusterConfig#DEFAULT_TIME_TO_WAIT_IN_MSEC}
		 */
		public long getTimeToWait() {
			return timeToWait;
		}

		/**
		 * Sets the connection time to wait.
		 * 
		 * @param timeToWait Time to wait in milliseconds
		 */
		public void setTimeToWait(long timeToWait) {
			this.timeToWait = timeToWait;
		}

		public PublisherType getPublisherType() {
			return publisherType;
		}

		public void setPublisherType(PublisherType publisherType) {
			this.publisherType = publisherType;
		}

		public int getSubscriberCount() {
			return subscriberCount;
		}

		public void setSubscriberCount(int subscriberCount) {
			this.subscriberCount = subscriberCount;
		}

		public String getPrimaryServerURI() {
			return primaryServerURI;
		}

		public void setPrimaryServerURI(String primaryServerURI) {
			this.primaryServerURI = primaryServerURI;
		}

		public Bridges getBridges() {
			return bridges;
		}

		public void setBridges(Bridges bridges) {
			this.bridges = bridges;
		}
	}

	public static class Persistence {
		private String className;
		private Properties props = new Properties();
		private Property[] properties;

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		/**
		 * Returns a MqttClientPersistence instance of {@link #getClassName()}. It
		 * returns null if the class name is undefined, i.e., null.
		 * 
		 * @throws ClassNotFoundException
		 * @throws NoSuchMethodException
		 * @throws SecurityException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 * @throws IllegalArgumentException
		 * @throws InvocationTargetException
		 */
		public MqttClientPersistence getMqttClientPersistence()
				throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
				IllegalAccessException, IllegalArgumentException, InvocationTargetException {

			MqttClientPersistence mqttClientPersistence = null;
			if (properties != null) {
				for (Property property : properties) {
					if (property != null && property.getKey() != null && property.getValue() != null) {
						props.setProperty(property.getKey(), property.getValue());
					}
				}
			}
			if (className != null) {
				if (className.equals("MqttDefaultFilePersistence")) {
					String path = props.getProperty("path");
					if (path != null) {
						mqttClientPersistence = new MqttDefaultFilePersistence(path);
					}
				} else if (className.equals("MemoryPersistence")) {
					mqttClientPersistence = new MemoryPersistence();
				} else {
					Class<?> clazz = Class.forName(className);
					Constructor<?> constructor = clazz.getConstructor(Properties.class);
					mqttClientPersistence = (MqttClientPersistence) constructor.newInstance(props);
				}
			}
			return mqttClientPersistence;
		}
	}

	public static class Property {
		private String key;
		private String value;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			if (value != null) {
				return ConfigUtil.parseStringValue(value);
			}
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class Bridges {
		private Bridge[] in;
		private Bridge[] out;

		public Bridge[] getIn() {
			return in;
		}

		public void setIn(Bridge[] in) {
			this.in = in;
		}

		public Bridge[] getOut() {
			return out;
		}

		public void setOut(Bridge[] out) {
			this.out = out;
		}
	}

	public static class Bridge {
		private String cluster;
		private String[] topicFilters;
		private int qos = -1;

		public String getCluster() {
			return cluster;
		}

		public void setCluster(String cluster) {
			this.cluster = cluster;
		}

		public String[] getTopicFilters() {
			return topicFilters;
		}

		public void setTopicFilters(String[] topicFilters) {
			this.topicFilters = topicFilters;
		}

		public int getQos() {
			return qos;
		}

		public void setQos(int qos) {
			this.qos = qos;
		}
	}
}
