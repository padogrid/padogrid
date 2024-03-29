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
package padogrid.mqtt.client.cluster.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.client.persist.MqttDefaultFilePersistence;

import padogrid.mqtt.client.cluster.HaMqttConnectionOptions;
import padogrid.mqtt.client.cluster.IClusterConfig;
import padogrid.mqtt.client.cluster.PluginContext;
import padogrid.mqtt.client.cluster.PublisherType;
import padogrid.mqtt.client.cluster.internal.ConfigUtil;

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
	private Plugin[] plugins;

	public ClusterConfig() {
	}

	public String getDefaultCluster() {
		return ConfigUtil.parseStringValue(defaultCluster);
	}

	public void setDefaultCluster(String defaultCluster) {
		this.defaultCluster = defaultCluster;
	}

	public String getTag() {
		return ConfigUtil.parseStringValue(tag);
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

	/**
	 * @return the plugins
	 */
	public Plugin[] getPlugins() {
		return plugins;
	}

	/**
	 * @param plugins the plugins to set
	 */
	public void setPlugins(Plugin[] plugins) {
		this.plugins = plugins;
	}

	public static class Cluster {
		private String name;
		private String description;
		private int fos = 0;
		private PublisherType publisherType = PublisherType.STICKY;
		private int subscriberCount = -1;
		private String primaryServerURI;
		private boolean enabled = true;
		private boolean autoConnect = true;
		private boolean liveEndpointPoolEnabled = true;
		private int initialEndpointCount = -1;
		private int liveEndpointCount = -1;
		private long timeToWait = IClusterConfig.DEFAULT_TIME_TO_WAIT_IN_MSEC;
		private String defaultTopicBase;
		private Endpoint[] endpoints;
		private Persistence persistence;
		private HaMqttConnectionOptions[] connections;
		private String[] pluginNames;
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

		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
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

		public boolean isLiveEndpointPoolEnabled() {
			return liveEndpointPoolEnabled;
		}

		public void setLiveEndpointPoolEnabled(boolean liveEndpointPoolEnabled) {
			this.liveEndpointPoolEnabled = liveEndpointPoolEnabled;
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

		public HaMqttConnectionOptions[] getConnections() {
			if (connections != null) {
				for (int i = 0; i < connections.length; i++) {
					connections[i] = getConnection(connections[i]);
				}
			}
			return connections;
		}

		public void setConnections(HaMqttConnectionOptions... connections) {
			this.connections = connections;
		}

		private HaMqttConnectionOptions getConnection(HaMqttConnectionOptions connection) {
			if (connection != null && connection.getConnection() != null) {
				String[] serverURIs = connection.getConnection().getServerURIs();
				// Replace system properties and env vars with values.
				for (int i = 0; i < serverURIs.length; i++) {
					serverURIs[i] = ConfigUtil.parseStringValue(serverURIs[i]);
				}
				// Set serverURIs
				if (serverURIs != null && serverURIs.length > 0) {
					List<String> serverList = ConfigUtil.parseEndpoints(serverURIs);
					String primaryServerURI = getPrimaryServerURI();
					if (primaryServerURI != null && serverList.contains(primaryServerURI) == false) {
						serverList.add(primaryServerURI);
					}
					connection.getConnection().setServerURIs(serverList.toArray(new String[0]));
				}
			}
			return connection;
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
			return ConfigUtil.parseStringValue(primaryServerURI);
		}

		public void setPrimaryServerURI(String primaryServerURI) {
			this.primaryServerURI = primaryServerURI;
		}

		public String getDefaultTopicBase() {
			return ConfigUtil.parseStringValue(defaultTopicBase);
		}

		public void setDefaultTopicBase(String defaultTopicBase) {
			this.defaultTopicBase = defaultTopicBase;
		}

		public Endpoint[] getEndpoints() {
			return endpoints;
		}

		public void setEndpoints(Endpoint[] endpoints) {
			this.endpoints = endpoints;
		}

		/**
		 * @return the plugin names
		 */
		public String[] getPluginNames() {
			return pluginNames;
		}

		/**
		 * @param pluginName the plugin name to set
		 */
		public void setPluginNames(String[] pluginNames) {
			this.pluginNames = pluginNames;
		}

		public Bridges getBridges() {
			return bridges;
		}

		public void setBridges(Bridges bridges) {
			this.bridges = bridges;
		}

		/**
		 * @return the persistence
		 */
		public Persistence getPersistence() {
			return persistence;
		}

		/**
		 * @param persistence the persistence to set
		 */
		public void setPersistence(Persistence persistence) {
			this.persistence = persistence;
		}
	}

	public static class Persistence {
		private String className;
		private Properties props = new Properties();
		private Property[] properties;

		public String getClassName() {
			return ConfigUtil.parseStringValue(className);
		}

		public void setClassName(String className) {
			this.className = className;
		}

		/**
		 * Returns a new MqttClientPersistence instance of {@link #getClassName()}. It
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
		public MqttClientPersistence createMqttClientPersistence()
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
			String className = getClassName();
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

	public static class Plugin {
		private String name;
		private String description;
		private boolean enabled;
		private PluginContext context = PluginContext.CLUSTER;
		private String className;
		private Property[] properties;
		private Subscriptions[] subscriptions;

		/**
		 * @return the name
		 */
		public String getName() {
			return ConfigUtil.parseStringValue(name);
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * @return the enabled
		 */
		public boolean isEnabled() {
			return enabled;
		}

		/**
		 * @param enabled the enabled to set
		 */
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		/**
		 * @return the context
		 */
		public PluginContext getContext() {
			return context;
		}

		/**
		 * @param context the context to set
		 */
		public void setContext(PluginContext context) {
			this.context = context;
		}

		/**
		 * @return the props
		 */
		public Properties getProps() {
			Properties props = new Properties();
			if (properties != null) {
				for (Property property : properties) {
					if (property != null && property.getKey() != null && property.getValue() != null) {
						props.setProperty(property.getKey(), property.getValue());
					}
				}
			}
			return props;
		}

		/**
		 * @return the className
		 */
		public String getClassName() {
			return ConfigUtil.parseStringValue(className);
		}

		/**
		 * @param className the className to set
		 */
		public void setClassName(String className) {
			this.className = className;
		}

		/**
		 * @return the properties
		 */
		public Property[] getProperties() {
			return properties;
		}

		/**
		 * @param properties the properties to set
		 */
		public void setProperties(Property[] properties) {
			this.properties = properties;
		}

		/**
		 * @return the subscriptions
		 */
		public Subscriptions[] getSubscriptions() {
			return subscriptions;
		}

		/**
		 * @param subscriptions the subscriptions to set
		 */
		public void setSubscriptions(Subscriptions[] subscriptions) {
			this.subscriptions = subscriptions;
		}

	}

	public static class Property {
		private String key;
		private String value;

		public String getKey() {
			return ConfigUtil.parseStringValue(key);
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return ConfigUtil.parseStringValue(value);
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class Subscriptions {
		private String[] topicFilters;
		private int qos = 1;

		/**
		 * @return the topicFilters
		 */
		public String[] getTopicFilters() {
			return topicFilters;
		}

		/**
		 * @param topicFilters the topicFilters to set
		 */
		public void setTopicFilters(String[] topicFilters) {
			this.topicFilters = topicFilters;
		}

		/**
		 * @return the qos
		 */
		public int getQos() {
			return qos;
		}

		/**
		 * @param qos the qos to set
		 */
		public void setQos(int qos) {
			this.qos = qos;
		}
	}

	public static class Endpoint {
		private String name;
		private String endpoint;
		private String topicBase;

		public String getName() {
			return ConfigUtil.parseStringValue(name);
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getEndpoint() {
			return ConfigUtil.parseStringValue(endpoint);
		}

		public void setEndpoint(String endpoint) {
			this.endpoint = endpoint;
		}

		public String getTopicBase() {
			return ConfigUtil.parseStringValue(topicBase);
		}

		public void setTopicBase(String topicBase) {
			this.topicBase = topicBase;
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
		private int qos = 1;

		public String getCluster() {
			return ConfigUtil.parseStringValue(cluster);
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

	public static class Tls {
		String tlsVersion;
		String cafile;
		String certfile;
		String keyfile;
		String password;

		public String getTlsVersion() {
			return ConfigUtil.parseStringValue(tlsVersion);
		}

		public void setTlsVersion(String tlsVersion) {
			this.tlsVersion = tlsVersion;
		}

		public String getCafile() {
			return ConfigUtil.parseStringValue(cafile);
		}

		public void setCafile(String cafile) {
			this.cafile = cafile;
		}

		public String getCertfile() {
			return ConfigUtil.parseStringValue(certfile);
		}

		public void setCertfile(String certfile) {
			this.certfile = certfile;
		}

		public String getKeyfile() {
			return ConfigUtil.parseStringValue(keyfile);
		}

		public void setKeyfile(String keyfile) {
			this.keyfile = keyfile;
		}

		public String getPassword() {
			return ConfigUtil.parseStringValue(password);
		}

		public void setPassword(String password) {
			this.password = password;
		}

		@Override
		public String toString() {
			return "Tls [getTlsVersion()=" + getTlsVersion() + ", getCafile()=" + getCafile() + ", getCertfile()="
					+ getCertfile() + ", getKeyfile()=" + getKeyfile() + ", getPassword()=" + getPassword() + "]";
		}
	}
}
