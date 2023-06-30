package padogrid.mqtt.client.cluster;

import org.eclipse.paho.mqttv5.client.MqttClient;

/**
 * {@link IHaMqttConnectorPublisher} provides the entry point to virtual cluster
 * publisher connectors that can intercept and manipulate MQTT messages
 * published by the application. The intercepted messages can be cleansed,
 * filtered, transformed, saved, streamed, and etc. before republishing or after
 * published.
 * 
 * @author dpark
 *
 */
public interface IHaMqttConnectorPublisher extends IHaMqttPlugin {

	/**
	 * Starts the connector. This method is invoked after {@link HaMqttClient} is
	 * connected.
	 * 
	 * @param haclient {@link HaMqttClient} instance that is responsible for
	 *                 streaming data for this connector's virtual cluster.
	 */
	void start(HaMqttClient haclient);
	
	/**
	 * Invoked just before publishing the passed-in payload. This method provides
	 * the connector an opportunity to alter the payload as needed before publishing
	 * it to the virtual cluster. The returned payload is published to the virtual
	 * cluster consisting of the passed-in live clients. To abort publishing data,
	 * return null. The returned payload is passed on to the
	 * {@link #afterMessagePublished(MqttClient[], String, byte[])}.
	 * 
	 * @param clients Live clients that will be used to publish the returned
	 *                payload. The number of clients is determined by
	 *                {@linkplain PublisherType}.
	 * @param topic   Topic to which the returned payload will be published.
	 * @param payload The original payload submitted by the application.
	 * @return Payload to publish. null to abort the "publish" operation.
	 */
	byte[] beforeMessagePublished(MqttClient[] clients, String topic, byte[] payload);

	/**
	 * Invoked just after publishing the passed-in payload. This method provides the
	 * connector an opportunity to work with the passed-in payload after it has been
	 * published to the virtual cluster. For example, the application may choose to
	 * deliver a notification along with the payload to another application from
	 * this method confirming that the payload has been delivered to the virtual
	 * cluster.
	 * 
	 * @param clients Live clients that are used to publish the payload.
	 * @param topic   Topic to which the passed-in payload was published. The number
	 *                of clients is determined by {@linkplain PublisherType}.
	 * @param payload Published payload. This payload is the same payload returned
	 *                by
	 *                {@link #beforeMessagePublished(MqttClient[], String, byte[])}.
	 */
	void afterMessagePublished(MqttClient[] clients, String topic, byte[] payload);
}
