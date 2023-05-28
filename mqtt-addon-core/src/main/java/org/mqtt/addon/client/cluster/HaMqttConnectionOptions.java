package org.mqtt.addon.client.cluster;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.mqtt.addon.client.cluster.config.ClusterConfig.Tls;

/**
 * HaMqttConnectionOptions wraps {@linkplain MqttConnectionOptions} and includes
 * additional options specific to {@linkplain HaMqttClient}.
 * 
 * @author dpark
 *
 */
public class HaMqttConnectionOptions {
	private MqttConnectionOptions connection;
	private Tls tls;

	/**
	 * Returns TLS configuration.
	 * 
	 * @return null if undefined.
	 */
	public Tls getTls() {
		return tls;
	}

	/**
	 * Sets TLS configuration
	 * 
	 * @param tls TLS configuration
	 */
	public void setTls(Tls tls) {
		this.tls = tls;
	}

	/**
	 * Returns an instance of MqttConnectionOptions. Returns a default instance if
	 * undefined.
	 */
	public MqttConnectionOptions getConnection() {
		if (connection == null) {
			connection = new MqttConnectionOptions();
		}
		return connection;
	}

	/**
	 * Sets an instance of MqttConnectionOptions
	 * 
	 * @param connection MqttConnectionOptions instance
	 */
	public void setConnection(MqttConnectionOptions connection) {
		this.connection = connection;
	}
}
