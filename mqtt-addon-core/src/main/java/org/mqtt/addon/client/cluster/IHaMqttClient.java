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
package org.mqtt.addon.client.cluster;

import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttSecurityException;
import org.eclipse.paho.mqttv5.common.MqttSubscription;

/**
 * IHaMqttClient lists the methods that are implmented by
 * {@linkplain HaMqttClient} but are not part of {@linkplain MqttClient}.
 * 
 * @author dpark
 *
 */
public interface IHaMqttClient extends IMqttClient {
	
	/**
	 * Enables or disables the cluster. Default is true.
	 * @param enabled true to enable, false to disable.
	 */
	public void setEnabled(boolean enabled);
	
	/**
	 * Returns true if the cluster is enabled. Default is true.
	 */
	public boolean isEnabled();
	
	/**
	 * Returns true is the cluster state is live. If live, then the
	 * cluster is neither disconnected nor closed.
	 */
	boolean isLive();

	/**
	 * Returns all client IDs including live and disconnected. If this HA client has
	 * been closed, then it returns an empty array.
	 * 
	 * @return An empty array if no clients exist.
	 */
	String[] getClientIds();

	/**
	 * Returns live client IDs. This method is analogous to
	 * {@link #getCurrentServerURIs()}.
	 * 
	 * @return An empty array if no live clients exist.
	 * @see #getCurrentServerURIs()
	 */
	String[] getLiveClientIds();

	/**
	 * Returns the currently connected Server URIs Implemented due to:
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=481097.
	 * 
	 * This method is analogous to {@link #getLiveClientIds()}.
	 *
	 * @return the currently connected server URIs
	 * @see MqttClient#getCurrentServerURI()
	 * @see #getLiveClientIds()
	 */
	String[] getCurrentServerURIs();

	/**
	 * Returns disconnected client IDs.
	 * 
	 * @return An empty array if no disconnected clients exist.
	 */
	String[] getDisconnectedClientIds();

	/**
	 * Returns all (connected and disconnected) server URIs that make up the
	 * cluster.
	 */
	String[] getServerURIs();

	/**
	 * Subscribes to all the live cluster endpoints (brokers).
	 * 
	 * @param topicFilter the topic to subscribe to, which can include wildcards.
	 * @parm qos the maximum quality of service at which to subscribe. Messages
	 *       published at a lower quality of service will be received at the
	 *       published QoS. Messages published at a higher quality of service will
	 *       be received using the QoS specified on the subscribe.
	 * @return Tokens from all the subscribed cluster clients.
	 * @see #subscribe(String, int)
	 */
	IMqttToken[] subscribeCluster(String topicFilter, int qos) throws MqttException;

	/**
	 * Subscribes to the specified topic filters and returns the publisher's token.
	 * The publisher is the client that is responsible for publishing messages as
	 * well as receiving messages. To obtain all tokens, use
	 * {@link #subscribeCluster(MqttConnectionOptions)}.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 * 
	 * @param topicFilters the topics to subscribe to, which can include wildcards.
	 * @parm qos the maximum quality of service at which to subscribe. Messages
	 *       published at a lower quality of service will be received at the
	 *       published QoS. Messages published at a higher quality of service will
	 *       be received using the QoS specified on the subscribe.
	 * @return Tokens from all the subscribed cluster clients.
	 * @see #subscribe(String, int)
	 */
	IMqttToken[] subscribeCluster(String[] topicFilters, int[] qos) throws MqttException;

	/**
	 * Subscribes to all the live cluster endpoints (brokers).
	 * 
	 * @param topicFilters the topics to subscribe to, which can include wildcards.
	 * @parm qos the maximum quality of service at which to subscribe. Messages
	 *       published at a lower quality of service will be received at the
	 *       published QoS. Messages published at a higher quality of service will
	 *       be received using the QoS specified on the subscribe.
	 * @parm messageListeners Message listeners for the specified topics.
	 * @return Tokens from all the subscribed cluster clients.
	 * @see #subscribe(String, int, IMqttMessageListener)
	 */
	IMqttToken[] subscribeCluster(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners)
			throws MqttException;

	/**
	 * Subscribes to all the live cluster endpoints (brokers).
	 * 
	 * @param topicFilter the topic to subscribe to, which can include wildcards.
	 * @parm qos the maximum quality of service at which to subscribe. Messages
	 *       published at a lower quality of service will be received at the
	 *       published QoS. Messages published at a higher quality of service will
	 *       be received using the QoS specified on the subscribe.
	 * @parm messageListener Message listener for the specified topic.
	 * @return Tokens from all the subscribed cluster clients.
	 * @see #subscribe(String, int, IMqttMessageListener)
	 */
	IMqttToken[] subscribeCluster(String topicFilter, int qos, IMqttMessageListener messageListener)
			throws MqttException;

	/**
	 * Adds the specified callback.
	 * 
	 * @param callback Callback
	 */
	void addCallbackCluster(IHaMqttCallback callback);

	/**
	 * Removes the specified callback.
	 * 
	 * @param callback Callback
	 */
	void removeCallbackCluster(IHaMqttCallback callback);

	/**
	 * Connects to the cluster and returns all the connected client tokens. This
	 * method may modify the specified connection options to be compliant with this
	 * object.
	 * 
	 * @param options Connection options
	 * @return A non-null token array.
	 * @throws MqttSecurityException when the server rejects the connect for
	 *                               security reasons
	 * 
	 * @throws MqttException         for non security related problems including
	 *                               communication errors
	 */
	IMqttToken[] connectWithResultCluster(MqttConnectionOptions options) throws MqttSecurityException, MqttException;

	/**
	 * Indicates that the application has completed processing the message with id
	 * messageId. This will cause the MQTT acknowledgement to be sent to the server.
	 * 
	 * @param client    the client that received the message
	 * @param messageId the MQTT message id to be acknowledged
	 * @param qos       the MQTT QoS of the message to be acknowledged
	 * @throws MqttException if there was a problem sending the acknowledgement
	 * @see #messageArrivedComplete(int, int)
	 */
	void messageArrivedComplete(MqttClient client, int messageId, int qos) throws MqttException;

	/**
	 * Returns true if the cluster state is disconnected, indicating all the
	 * endpoints in the cluster have been disconnected and the cluster is not
	 * maintained. From this state, the cluster can be reactivated by invoking any
	 * of the {@linkplain #connect()} methods.
	 */
	boolean isDisconnected();

	/**
	 * Returns the primary MqttClient instance. It returns null if the primary
	 * client is not configured.
	 */
	MqttClient getPrimaryMqttClient();

	/**
	 * Adds the specified server URI to the cluster.
	 * 
	 * @param serverURI Server URI
	 */
	void addServerURI(String serverURI);

	/**
	 * Removes the specified server URI from the cluster.
	 * 
	 * @param serverURI Server URI
	 * @return true if the specified server URI existed and is removed.
	 */
	boolean removeServerURI(String serverURI);

	// ===========================================================================
	// The following method are in MqttClient but not in IMattClient (Paho v1.2.5)
	// ===========================================================================

	/**
	 * Set the maximum time to wait for an action to complete.
	 * <p>
	 * Set the maximum time to wait for an action to complete before returning
	 * control to the invoking application. Control is returned when:
	 * </p>
	 * <ul>
	 * <li>the action completes</li>
	 * <li>or when the timeout if exceeded</li>
	 * <li>or when the client is disconnect/shutdown</li>
	 * </ul>
	 * <p>
	 * The default value is -1 which means the action will not timeout. In the event
	 * of a timeout the action carries on running in the background until it
	 * completes. The timeout is used on methods that block while the action is in
	 * progress.
	 * </p>
	 * 
	 * @param timeToWaitInMillis
	 *            before the action times out. A value or 0 or -1 will wait until
	 *            the action finishes and not timeout.
	 * @throws IllegalArgumentException
	 *             if timeToWaitInMillis is invalid
	 */
	void setTimeToWait(long timeToWaitInMillis) throws IllegalArgumentException;
	
	/**
	 * Return the maximum time to wait for an action to complete.
	 * 
	 * @return the time to wait
	 * @see MqttClient#setTimeToWait(long)
	 */
	long getTimeToWait();
	
	/**
	 * Returns true if the cluster connection is closed. A closed cluster is no
	 * longer operational and cannot be reconnected.
	 */
	boolean isClosed();

	/**
	 * Subscribes to the specified array of subscriptions.
	 * 
	 * @param subscriptions an array of subscriptions
	 * @throws MqttException if there was an error registering the subscriptions.
	 * @return token for the subscriptions
	 * @see IMqttClient#subscribe(String[], int[])
	 */
	IMqttToken[] subscribe(MqttSubscription[] subscriptions) throws MqttException;

	/**
	 * Subscribes to the specified array of subscriptions.
	 * 
	 * @param subscriptions    an array of subscriptions
	 * @param messageListeners an array of message listeners
	 * @return Tokens for the subscriptions
	 * @throws MqttException if there was an error registering the subscription.
	 */
	IMqttToken[] subscribe(MqttSubscription[] subscriptions, IMqttMessageListener[] messageListeners)
			throws MqttException;

	/**
	 * Forcibly closes the cluster. Once closed, this object is no longer
	 * operational.
	 * 
	 * @param force true to Forcibly close the cluster, false to gracefully close
	 *              the cluster.
	 * @throws MqttException thrown if broker communications error
	 */
	void close(boolean force) throws MqttException;
}
