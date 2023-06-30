package padogrid.mqtt.client.cluster;

import org.eclipse.paho.mqttv5.client.MqttClient;

/**
 * {@link IHaMqttConnectorSubscriber} provides the entry point to virtual
 * cluster subscriber connectors that can intercept and manipulate MQTT
 * messages. The intercepted messages can be cleansed, filtered, transformed,
 * saved, streamed, and etc. upon arrival.
 * 
 * @author dpark
 *
 */
public interface IHaMqttConnectorSubscriber extends IHaMqttPlugin {
	
	/**
	 * Starts the connector. This method is invoked after {@link HaMqttClient} is
	 * connected.
	 * 
	 * @param haclient {@link HaMqttClient} instance that is responsible for
	 *                 streaming data for this connector's virtual cluster.
	 */
	void start(HaMqttClient haclient);
	
	/**
	 * Invoked when the payload is received from the virtual cluster.
	 * 
	 * @param client  Live client that received the message.
	 * @param topic   Subscribed topic.
	 * @param payload Received payload.
	 */
	void messageArrived(MqttClient client, String topic, byte[] payload);
}
