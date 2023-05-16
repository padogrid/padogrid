package org.mqtt.addon.client.cluster.internal;

import java.util.Arrays;

import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttSubscription;

/**
 * TopicSubscriptions contains subscription details for an array of
 * subscriptions. It is used to revive dead endpoints.
 * 
 * @author dpark
 *
 */
public class TopicSubscriptions extends TopicInfo {
	MqttSubscription[] subscriptions;
	IMqttMessageListener[] messageListeners;

	public TopicSubscriptions(MqttSubscription[] subscriptions) {
		this(subscriptions, null);
	}

	public TopicSubscriptions(MqttSubscription[] subscriptions, IMqttMessageListener[] messageListeners) {
		this.subscriptions = subscriptions;
		this.messageListeners = messageListeners;
	}

	@Override
	public void subscribe(MqttClient client) throws MqttException {
		if (subscriptions != null) {
			client.subscribe(subscriptions, messageListeners);
		}
	}

	@Override
	public void unsubscribe(MqttClient client) throws MqttException {
		if (subscriptions != null) {
			for (MqttSubscription subscription : subscriptions) {
				client.unsubscribe(subscription.getTopic());
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(subscriptions);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TopicSubscriptions other = (TopicSubscriptions) obj;
		return Arrays.equals(subscriptions, other.subscriptions);
	}

}