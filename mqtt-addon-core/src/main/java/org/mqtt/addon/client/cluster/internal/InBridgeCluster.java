package org.mqtt.addon.client.cluster.internal;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.mqtt.addon.client.cluster.HaMqttClient;

public class InBridgeCluster extends BridgeCluster {
	public InBridgeCluster(HaMqttClient inClient, String[] topicFilters, int qos) {
		super(inClient, topicFilters, qos);
	}

	@Override
	public void subscribe(MqttClient client) throws MqttException {
		for (String topicFilter : topicFilters) {
			if (client.isConnected()) {
				client.subscribe(topicFilter, qos);
			}
		}
	}

	@Override
	public void unsubscribe(MqttClient client) throws MqttException {
		for (String topicFilter : topicFilters) {
			if (client.isConnected()) {
				client.unsubscribe(topicFilter);
			}
		}
	}
}
