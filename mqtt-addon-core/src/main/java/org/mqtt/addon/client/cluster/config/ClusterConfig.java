package org.mqtt.addon.client.cluster.config;

import java.util.List;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.mqtt.addon.client.cluster.IClusterConfig;
import org.mqtt.addon.client.cluster.PublisherType;

public class ClusterConfig {
	private boolean enabled = true;
	private String defaultCluster = IClusterConfig.DEFAULT_CLUSTER_NAME;
	private String tag = IClusterConfig.DEFAULT_CLUSTER_TAG;
	private int probeDelay = IClusterConfig.DEFAULT_CLUSTER_PROBE_DELAY_IN_MSEC;
	private Cluster[] clusters;
	private Persistence persistence;

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
		private PublisherType publisherType = PublisherType.STICKY;
		private String primaryServerURI;
		private boolean enabled;
		private MqttConnectionOptions connection;

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

		public PublisherType getPublisherType() {
			return publisherType;
		}

		public void setPublisherType(PublisherType publisherType) {
			this.publisherType = publisherType;
		}

		public String getPrimaryServerURI() {
			return primaryServerURI;
		}

		public void setPrimaryServerURI(String primaryServerURI) {
			this.primaryServerURI = primaryServerURI;
		}
	}

}
