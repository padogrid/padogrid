package padogrid.mqtt.client.cluster.internal;

import java.util.Objects;

import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttSubscription;

/**
 * TopicInfo contains subscription details for a given topic filter. It is used
 * to revive dead endpoints.
 * 
 * @author dpark
 *
 */
public class TopicFilter extends TopicInfo {
	String topicFilter;
	int qos;
	IMqttMessageListener messageListener;

	public TopicFilter(String topicFilter) {
		this(topicFilter, 0, null);
	}

	public TopicFilter(String topicFilter, int qos) {
		this(topicFilter, qos, null);
	}

	public TopicFilter(String topicFilter, int qos, IMqttMessageListener messageListener) {
		this.topicFilter = topicFilter;
		this.qos = qos;
		this.messageListener = messageListener;
	}

	@Override
	public void subscribe(MqttClient client) throws MqttException {
		if (topicFilter != null) {
			// A bug in Paho 1.2.5. The following loops indefinitely.
			// client.subscribe(topicFilter, qos, messageListener);

			// A workaround to the above bug.
			MqttSubscription subscription = new MqttSubscription(topicFilter);
			subscription.setQos(qos);
			client.subscribe(new MqttSubscription[] { subscription },
					new IMqttMessageListener[] { messageListener });
		}
	}

	@Override
	public void unsubscribe(MqttClient client) throws MqttException {
		if (topicFilter != null) {
			client.unsubscribe(topicFilter);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(topicFilter);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TopicFilter other = (TopicFilter) obj;
		return Objects.equals(topicFilter, other.topicFilter);
	}
}