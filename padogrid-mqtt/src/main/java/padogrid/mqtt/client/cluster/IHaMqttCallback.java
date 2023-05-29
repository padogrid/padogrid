/*
 * Copyright (c) 2023 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package padogrid.mqtt.client.cluster;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

/**
 * Enables an application to be notified when asynchronous events related to the
 * cluster clients occur. Classes implementing this interface can be registered on both
 * types of client: {@link IHaMqttClient#addCallbackCluster(IHaMqttClientCallback)} and
 * {@link IHaMqttAsyncClient#addCallbackCluster(IHaMqttClientCallback)}
 */
public interface IHaMqttCallback {
	/**
	 * This method is called when the server gracefully disconnects from the client
	 * by sending a disconnect packet, or when the TCP connection is lost due to a
	 * network issue or if the client encounters an error.
	 * 
	 * @param client             client that is disconnected
	 * @param disconnectResponse a {@link MqttDisconnectResponse} containing
	 *                           relevant properties related to the cause of the
	 *                           disconnection.
	 * @see MqttCallback#disconnected(MqttDisconnectResponse)
	 */
	void disconnected(MqttClient client, MqttDisconnectResponse disconnectResponse);

	/**
	 * This method is called when an exception is thrown within the MQTT client. The
	 * reasons for this may vary, from malformed packets, to protocol errors or even
	 * bugs within the MQTT client itself. This callback surfaces those errors to
	 * the application so that it may decide how best to deal with them.
	 * 
	 * For example, The MQTT server may have sent a publish message with an invalid
	 * topic alias, the MQTTv5 specification suggests that the client should
	 * disconnect from the broker with the appropriate return code, however this is
	 * completely up to the application itself.
	 * 
	 * @param client    client that raised the specified error
	 * @param exception - The exception thrown causing the error.
	 * @see MqttCallback#mqttErrorOccurred(MqttException)
	 */
	void mqttErrorOccurred(MqttClient client, MqttException exception);

	/**
	 * This method is called when a message arrives from the server.
	 *
	 * <p>
	 * This method is invoked synchronously by the MQTT client. An acknowledgment is
	 * not sent back to the server until this method returns cleanly.
	 * </p>
	 * <p>
	 * If an implementation of this method throws an <code>Exception</code>, then
	 * the client will be shut down. When the client is next re-connected, any QoS 1
	 * or 2 messages will be redelivered by the server.
	 * </p>
	 * <p>
	 * Any additional messages which arrive while an implementation of this method
	 * is running, will build up in memory, and will then back up on the network.
	 * </p>
	 * <p>
	 * If an application needs to persist data, then it should ensure the data is
	 * persisted prior to returning from this method, as after returning from this
	 * method, the message is considered to have been delivered, and will not be
	 * reproducible.
	 * </p>
	 * <p>
	 * It is possible to send a new message within an implementation of this
	 * callback (for example, a response to this message), but the implementation
	 * must not disconnect the client, as it will be impossible to send an
	 * acknowledgment for the message being processed, and a deadlock will occur.
	 * </p>
	 *
	 * @param client  client that received the specified message
	 * @param topic   name of the topic on the message was published to
	 * @param message the actual message.
	 * @throws Exception if a terminal error has occurred, and the client should be
	 *                   shut down.
	 * @see MqttCallback#messageArrived(String, MqttMessage)
	 */
	void messageArrived(MqttClient client, String topic, MqttMessage message) throws Exception;

	/**
	 * Called when delivery for a message has been completed, and all
	 * acknowledgments have been received. For QoS 0 messages it is called once the
	 * message has been handed to the network for delivery. For QoS 1 it is called
	 * when PUBACK is received and for QoS 2 when PUBCOMP is received. The token
	 * will be the same token as that returned when the message was published.
	 *
	 * @param client client that completed the message delivery.
	 * @param token  the delivery token associated with the message.
	 * @see MqttCallback#deliveryComplete(IMqttToken)
	 */
	void deliveryComplete(MqttClient client, IMqttToken token);

	/**
	 * Called when the connection to the server is completed successfully.
	 * 
	 * @parm client client that completed connection.
	 * @param reconnect If true, the connection was the result of automatic
	 *                  reconnect.
	 * @param serverURI The server URI that the connection was made to.
	 * @see MqttCallback#connectComplete(boolean, String)
	 */
	void connectComplete(MqttClient client, boolean reconnect, String serverURI);

	/**
	 * Called when the specified AUTH packet is received by the client.
	 * 
	 * @param client     client that received an auth packet.
	 * @param reasonCode The Reason code, can be Success (0), Continue
	 *                   authentication (24) or Re-authenticate (25).
	 * @param properties The {@link MqttProperties} to be sent, containing the
	 *                   Authentication Method, Authentication Data and any required
	 *                   User Defined Properties.
	 * @see MqttCallback#authPacketArrived(int, MqttProperties)
	 */
	void authPacketArrived(MqttClient client, int reasonCode, MqttProperties properties);

}
