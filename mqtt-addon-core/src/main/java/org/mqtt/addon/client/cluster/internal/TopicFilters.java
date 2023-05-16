package org.mqtt.addon.client.cluster.internal;

import java.util.Arrays;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;

/**
 * TopicInfos contains subscription details for an array of topic filters. It is
 * used to revive dead endpoints.
 * 
 * @author dpark
 *
 */
public class TopicFilters extends TopicInfo {
	String[] topicFilters;
	int[] qos;

	public TopicFilters(String[] topicFilters) {
		this(topicFilters, null);
	}

	public TopicFilters(String[] topicFilters, int[] qos) {
		this.topicFilters = topicFilters;
		if (qos == null) {
			this.qos = new int[topicFilters.length];
			for (int i = 0; i < this.qos.length; i++) {
				this.qos[i] = 0;
			}
		} else {
			this.qos = qos;
		}
	}

	@Override
	public void subscribe(MqttClient client) throws MqttException {
		if (topicFilters != null) {
			client.subscribe(topicFilters, qos);
		}
	}

	@Override
	public void unsubscribe(MqttClient client) throws MqttException {
		if (topicFilters != null) {
			client.unsubscribe(topicFilters);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(topicFilters);
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
		TopicFilters other = (TopicFilters) obj;
		return Arrays.equals(topicFilters, other.topicFilters);
	}

}