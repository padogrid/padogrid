package padogrid.mqtt.client.cluster.internal;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;

public abstract class TopicInfo {
	public void subscribe(MqttClient client) throws MqttException {
	}

	public void unsubscribe(MqttClient client) throws MqttException {
	}
}