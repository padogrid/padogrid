package org.mqtt.addon.client.cluster.internal;

import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.util.MqttTopicValidator;
import org.mqtt.addon.client.cluster.HaMqttClient;

public class BridgeCluster extends TopicInfo {
	protected HaMqttClient client;
	protected String[] topicFilters;
	protected int qos;

	public BridgeCluster(HaMqttClient client, String[] topicFilters, int qos) {
		this.client = client;
		this.topicFilters = topicFilters;
		this.qos = qos;
		if (this.qos < 0 || this.qos > 2) {
			this.qos = 0;
		}
	}

	public void publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException {
		for (int i = 0; i < topicFilters.length; i++) {
			String topicFilter = topicFilters[i];
			if (MqttTopicValidator.isMatched(topicFilter, topic)) {
				if (0 <= this.qos && this.qos <= 2) {
					client.publish(topic, payload, this.qos, retained);
				} else {
					client.publish(topic, payload, qos, retained);
				}
				break;
			}
		}
	}

	public void publish(String topic, MqttMessage message) throws MqttException {
		for (int i = 0; i < topicFilters.length; i++) {
			String topicFilter = topicFilters[i];
			if (MqttTopicValidator.isMatched(topicFilter, topic)) {
				if (0 <= this.qos && this.qos <= 2) {
					client.publish(topic, message.getPayload(), this.qos, message.isRetained());
				} else {
					client.publish(topic, message.getPayload(), message.getQos(), message.isRetained());
				}
				break;
			}
		}
	}
}
