package padogrid.mqtt.client.cluster.internal;

import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.util.MqttTopicValidator;

import padogrid.mqtt.client.cluster.HaMqttClient;

public class BridgeCluster extends TopicInfo {
	protected HaMqttClient client;
	protected String[] topicFilters;
	protected int qos = 1;

	public BridgeCluster(HaMqttClient client, String[] topicFilters, int qos) {
		this.client = client;
		this.topicFilters = topicFilters;
		this.qos = qos;
		if (this.qos < 0 || this.qos > 2) {
			this.qos = 1;
		}
	}

	public void publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException {
		for (int i = 0; i < topicFilters.length; i++) {
			String topicFilter = topicFilters[i];
			if (MqttTopicValidator.isMatched(topicFilter, topic)) {
				if (qos < 0 || qos > 2) {
					qos = this.qos;
				}
				client.publish(topic, payload, this.qos, retained);
				break;
			}
		}
	}

	public void publish(String topic, MqttMessage message) throws MqttException {
		for (int i = 0; i < topicFilters.length; i++) {
			String topicFilter = topicFilters[i];
			if (MqttTopicValidator.isMatched(topicFilter, topic)) {
				int qos = message.getQos();
				if (qos < 0 || qos > 2) {
					qos = this.qos;
				}
				client.publish(topic, message.getPayload(), qos, message.isRetained());
				break;
			}
		}
	}
}
