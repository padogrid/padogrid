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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttTopic;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSecurityException;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.mqtt.addon.client.cluster.config.ClusterConfig;
import org.mqtt.addon.client.cluster.config.ConfigUtil;

/**
 * HaMqttClient wraps {@linkplain MqttClient} and provides HA services. It
 * automatically forms a cluster consisting of the specified endpoints (server
 * URIs) and seamlessly performs cluster management tasks that include endpoint
 * probing, reconnection, automatic failover, load balancing, and scaling.
 * <p>
 * HaMqttClient implements {@linkplain IMqttClient}, to simplify legacy
 * application migration. To migrate to HaMqttClient or vice versa, simply cast
 * {@linkplain IMqttClient}.
 * <p>
 * HaMqttClient implements {@linkplain IHaMqttClient} which includes the methods
 * that are not part of {@linkplain IMqttClient}.
 * 
 * @author dpark
 *
 */
public class HaMqttClient implements IHaMqttClient {
	private String clusterName;
	private MqttClient liveClients[] = new MqttClient[0];
	private ClusterState clusterState;
	private Logger logger = LogManager.getLogger(HaMqttClient.class);

	private PublisherType publisherType = PublisherType.STICKY;

	// roundRobinThreadLocal allows each thread to independently round-robin brokers
	private ThreadLocal<Integer> roundRobinThreadLocal = ThreadLocal.withInitial(() -> -1);

	// stickyThreadLocal allows load-balancing threads for the STICKY publisher
	// type.
	// In works in conjunction with stickyIndex.
	private ThreadLocal<MqttClient> stickyThreadLocal = new ThreadLocal<MqttClient>();

	// stickyIndex is incremented per thread
	private volatile int stickyIndex = 0;

	// connectionInProgress is to prevent multiple threads from invoking connect()
	private volatile boolean connectionInProgress = false;

	// Random generator for RANDOM publisher type
	private Random random = new Random();

	HaMqttClient() throws IOException {
		this(null, null, null);
	}

	HaMqttClient(ClusterConfig.Cluster clusterConfig, MqttClientPersistence persistence,
			ScheduledExecutorService executorService) throws IOException {
		if (clusterConfig != null) {
			this.clusterName = clusterConfig.getName();
			if (this.clusterName == null || this.clusterName.length() == 0) {
				clusterName = IClusterConfig.DEFAULT_CLUSTER_NAME;
			}
			this.publisherType = clusterConfig.getPublisherType();
			logger.info(String.format("%s created [clusterName=%s, publisherType=%s]",
					HaMqttClient.class.getSimpleName(), clusterName, publisherType));

			// Obtain cluster state
			clusterState = ClusterService.getClusterService().addHaClient(this, clusterConfig, persistence,
					executorService);
		}
	}

	private void removeMqttClient(MqttClient client) {
		clusterState.removeLiveClient(client);
		liveClients = clusterState.getLiveClients().toArray(new MqttClient[0]);
	}

	/**
	 * Returns a new array containing shuffled clients.
	 * 
	 * @param clients An array of clients
	 * @return null or the same clients instance if the specified clients is null or
	 *         empty.
	 */
	private MqttClient[] shuffleEndpoints(MqttClient[] clients) {
		if (clients == null || clients.length == 0) {
			return clients;
		}
		int count = clients.length;
		int[] shuffledIndexes = ConfigUtil.shuffleRandom(count);
		MqttClient[] shuffled = new MqttClient[count];
		for (int i = 0; i < count; i++) {
			shuffled[i] = clients[shuffledIndexes[i]];
		}
		return shuffled;
	}

	/**
	 * Invoked by the discovery service upon detecting live client updates.
	 * 
	 * @param liveClientCollection A collection containing the latest live clients.
	 */
	void updateLiveClients(Collection<MqttClient> liveClientCollection) {
		MqttClient[] clients = liveClientCollection.toArray(new MqttClient[0]);
		liveClients = shuffleEndpoints(clients);
		logger.debug("Live client list recevied [size=%d].", liveClientCollection.size());
	}

	private void cleanupThreadLocals() {
		roundRobinThreadLocal.remove();
		stickyThreadLocal.remove();
	}

	ClusterState getClusterState() {
		return clusterState;
	}

	public void setEnabled(boolean enabled) {
		clusterState.setEnabled(enabled);
	}

	public boolean isEnabled() {
		return clusterState.isEnabled();
	}

	/**
	 * Returns the publisher extracted from the live client list based on the
	 * publisher type as follows.
	 * <p>
	 * <b>RANDOM, ROUND_ROBIN</b>
	 * <ul>
	 * <li>May return a different publisher instance per invocation.</li>
	 * </ul>
	 * <b>STICKY</b>
	 * <ul>
	 * <li>Always returns the same the publisher instance until the publisher
	 * fails.</li>
	 * <li>If the publisher fails, then it returns another instance retrieved from
	 * the live list. The new instance becomes sticky.</li>
	 * <li>If the primary publisher has been configured then it always returns the
	 * primary publisher instance. If the primary publisher fails, then another
	 * instance is returned instead. The new instance becomes sticky until the
	 * primary publisher becomes available again.</li>
	 * </ul>
	 * 
	 * @return null if the publisher is not available.
	 */
	public MqttClient getPublisher() {
		MqttClient client = null;
		MqttClient[] clients = liveClients;
		int len = clients.length;
		if (len > 0) {
			switch (publisherType) {
			case RANDOM:
				int index = Math.abs(random.nextInt()) % len;
				client = clients[index];
				break;
			case ROUND_ROBIN:
				int roundRobinIndex = (roundRobinThreadLocal.get() + 1) % len;
				client = clients[roundRobinIndex];
				roundRobinThreadLocal.set(roundRobinIndex);
				break;
			case STICKY:
			default:
				client = clusterState.getPrimaryClient();
				if (client == null || client.isConnected() == false) {
					client = stickyThreadLocal.get();
					if (client == null) {
						index = stickyIndex % len;
						client = clients[index];
						stickyThreadLocal.set(client);
						stickyIndex = index + 1;
					}
				}
				break;
			}
		}
		return client;
	}

	/**
	 * Returns the cluster name. A unique cluster name is assigned if it is not
	 * specified when this object was initially created.
	 */
	public String getClusterName() {
		return clusterName;
	}

	/**
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void publish(String topicFilter, MqttMessage message) throws MqttException {
		if (isDisconnected()) {
			throw new HaMqttException(-101, "Cluster disconnected");
		}
		if (clusterState.isClosed()) {
			throw new HaMqttException(-102, "Cluster closed");
		}
		// Live client variables are updated from another thread. We reassign them to
		// local variables to handle a race condition.
		MqttClient client = getPublisher();

		if (client == null || liveClients.length == 0) {
			cleanupThreadLocals();
			throw new HaMqttException(-100, String.format("Cluster unreachable"));
		}
		try {
			client.publish(topicFilter, message);
		} catch (MqttException e) {

			if (client.isConnected() == false) {
				// If publish() fails, then we assume the connection is
				// no longer valid. Remove the client from the live list
				// so that the discovery service can probe and reconnect.
				removeMqttClient(client);

				logger.debug(String.format("publish() failed. Removed %s[%s]", HaMqttClient.class.getSimpleName(),
						client.getServerURI()), e);

				// Upon removal, a new live client list is obtained.
				// Publish it again with the new publisherClient.
				MqttClient[] clients = liveClients;

				if (clients.length == 0) {
					cleanupThreadLocals();
					throw e;
				} else {
					publish(topicFilter, message);
				}
			} else {
				throw e;
			}
		}
	}

	/**
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException {
		if (isDisconnected()) {
			throw new HaMqttException(-101, "Cluster disconnected");
		}
		if (clusterState.isClosed()) {
			throw new HaMqttException(-102, "Cluster closed");
		}
		// Live client variables are updated from another thread. We reassign them to
		// local variables to handle a race condition.
		MqttClient client = getPublisher();
		if (client == null || liveClients.length == 0) {
			cleanupThreadLocals();
			throw new HaMqttException(-100, String.format("Cluster unreachable [client=%s, liveClients.length=%d]",
					client, liveClients.length));
		}
		try {
			client.publish(topic, payload, qos, retained);
		} catch (MqttException e) {
			if (client.isConnected() == false) {
				// If publish() fails, then we assume the connection is
				// no longer valid. Remove the client from the live list
				// so that the discovery service can probe and reconnect.
				removeMqttClient(client);
				stickyThreadLocal.set(null);

				logger.debug(String.format("publish() failed. Removed %s[%s]", HaMqttClient.class.getSimpleName(),
						client.getServerURI()), e);

				// Upon removal, a new live client list is obtained.
				// Publish it again with the new publisherClient.
				MqttClient[] clients = liveClients;
				if (clients.length == 0) {
					cleanupThreadLocals();
					throw e;
				} else {
					publish(topic, payload, qos, retained);
				}
			} else {
				throw e;
			}
		}
	}

	/**
	 * Subscribes to the specified topic filter and returns the publisher's token.
	 * The publisher is the client that is responsible for publishing messages as
	 * well as receiving messages. To obtain all tokens, use
	 * {@link #subscribeCluster(String, int)}
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public IMqttToken subscribe(String topicFilter, int qos) throws MqttException {
		IMqttToken[] tokens = subscribeCluster(topicFilter, qos);
		IMqttToken token = null;
		MqttClient client = getPublisher();
		if (client != null) {
			for (IMqttToken t : tokens) {
				if (client.getClientId().equals(t.getClient().getClientId())) {
					token = t;
					break;
				}
			}
		}
		return token;
	}

	/**
	 * IHaMqttClient: {@inheritDoc}
	 */
	public IMqttToken[] subscribeCluster(String topicFilter, int qos) throws MqttException {
		clusterState.subscribe(topicFilter, qos);

		// Live client variables are updated from another thread. We reassign them to
		// local variables to handle a race condition.
		MqttClient[] clients = liveClients;

		// Return empty tokens (null values) if topicFilter is null
		IMqttToken[] tokens = new IMqttToken[clients.length];
		if (topicFilter == null) {
			return tokens;
		}

		int index = 0;
		for (MqttClient client : clients) {
			try {
				tokens[index++] = client.subscribe(topicFilter, qos);
			} catch (MqttException e) {
				removeMqttClient(client);
			}
		}
		return tokens;
	}

	/**
	 * Subscribes to the specified topic filters and returns the publisher's token.
	 * The publisher is the client that is responsible for publishing messages as
	 * well as receiving messages. To obtain all tokens, use
	 * {@link #subscribeCluster(MqttConnectionOptions)}.
	 * <p>
	 * IMqttClient: /** Subscribes to the specified topic filters and returns the
	 * publisher's token. The publisher is the client that is responsible for
	 * publishing messages as well as receiving messages. To obtain all tokens, use
	 * {@link #subscribeCluster(MqttConnectionOptions)}.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public IMqttToken subscribe(String[] topicFilters, int[] qos) throws MqttException {
		IMqttToken[] tokens = subscribeCluster(topicFilters, qos);
		IMqttToken token = null;
		MqttClient client = getPublisher();
		if (client != null) {
			for (IMqttToken t : tokens) {
				if (client.getClientId().equals(t.getClient().getClientId())) {
					token = t;
					break;
				}
			}
		}
		return token;
	}

	/**
	 * IHaMqttClient: {@inheritDoc}
	 */
	public IMqttToken[] subscribeCluster(String[] topicFilters, int[] qos) throws MqttException {
		clusterState.subscribe(topicFilters, qos);

		// Live client variables are updated from another thread. We reassign them to
		// local variables to handle a race condition.
		MqttClient[] clients = liveClients;

		// Return empty tokens (null values) if topicFilters is null
		IMqttToken[] tokens = new IMqttToken[clients.length];
		if (topicFilters == null) {
			return tokens;
		}

		int index = 0;
		for (MqttClient client : clients) {
			try {
				tokens[index++] = client.subscribe(topicFilters, qos);
			} catch (MqttException e) {
				removeMqttClient(client);
			}
		}
		return tokens;
	}

	/**
	 * Subscribes to the specified topic filters and returns the publisher's token.
	 * The publisher is the client that is responsible for publishing messages as
	 * well as receiving messages. To obtain all tokens, use
	 * {@link #subscribeCluster(String[], int[], IMqttMessageListener[])}
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public IMqttToken subscribe(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners)
			throws MqttException {
		IMqttToken[] tokens = subscribeCluster(topicFilters, qos);
		IMqttToken token = null;
		MqttClient client = getPublisher();
		if (client != null) {
			for (IMqttToken t : tokens) {
				if (client.getClientId().equals(t.getClient().getClientId())) {
					token = t;
					break;
				}
			}
		}
		return token;
	}

	/**
	 * IHaMqttClient: {@inheritDoc}
	 */
	public IMqttToken[] subscribeCluster(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners)
			throws MqttException {
		clusterState.subscribe(topicFilters, qos);

		// Live client variables are updated from another thread. We reassign them to
		// local variables to handle a race condition.
		MqttClient[] clients = liveClients;

		// Return empty tokens (null values) if topicFilters is null
		IMqttToken[] tokens = new IMqttToken[clients.length];
		if (topicFilters == null) {
			return tokens;
		}

		int index = 0;

//		for (MqttClient client : clients) {
//			try {
//				// A bug in Paho 1.2.5. The following loops indefinitely. This is fixed
//		        // in the next version yet to be released (4/25/2023)
//				tokens[index++] = client.subscribe(topicFilters, qos, messageListeners);
//			} catch (MqttException e) {
//				removeMqttClient(client);
//			}
//		}

		// A workaround to the above bug.
		MqttSubscription[] subscriptions = new MqttSubscription[topicFilters.length];
		for (int i = 0; i < topicFilters.length; ++i) {
			subscriptions[i] = new MqttSubscription(topicFilters[i], qos[i]);
		}
		for (MqttClient client : clients) {
			try {
				tokens[index++] = client.subscribe(subscriptions, messageListeners);
			} catch (MqttException e) {
				removeMqttClient(client);
			}
		}
		return tokens;
	}

	/**
	 * MqttClient: {@inheritDoc}
	 * 
	 * @see IMqttClient#subscribe(String[], int[])
	 */
	public IMqttToken[] subscribe(MqttSubscription[] subscriptions) throws MqttException {
		clusterState.subscribe(subscriptions);

		// Live client variables are updated from another thread. We reassign them to
		// local variables to handle a race condition.
		MqttClient[] clients = liveClients;

		// Return empty tokens (null values) if subscriptions is null
		IMqttToken[] tokens = new IMqttToken[clients.length];
		if (subscriptions == null) {
			return tokens;
		}

		int index = 0;
		for (MqttClient client : clients) {
			try {
				tokens[index++] = client.subscribe(subscriptions);
			} catch (MqttException e) {
				removeMqttClient(client);
			}
		}
		return tokens;
	}

	/**
	 * Subscribes to the specified topic filter and returns the publisher's token.
	 * The publisher is the client that is responsible for publishing messages as
	 * well as receiving messages. To obtain all tokens, use
	 * {@link #subscribeCluster(String, int, IMqttMessageListener)}.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public IMqttToken subscribe(String topicFilter, int qos, IMqttMessageListener messageListener)
			throws MqttException {
		IMqttToken[] tokens = subscribeCluster(topicFilter, qos, messageListener);
		IMqttToken token = null;
		MqttClient client = getPublisher();
		if (client != null) {
			for (IMqttToken t : tokens) {
				if (client.getClientId().equals(t.getClient().getClientId())) {
					token = t;
					break;
				}
			}
		}
		return token;
	}

	/**
	 * IHaMqttClient: {@inheritDoc}
	 */
	public IMqttToken[] subscribeCluster(String topicFilter, int qos, IMqttMessageListener messageListener)
			throws MqttException {
		clusterState.subscribe(topicFilter, qos, messageListener);

		// Live client variables are updated from another thread. We reassign them to
		// local variables to handle a race condition.
		MqttClient[] clients = liveClients;

		// Return empty tokens (null values) if topicFilter is null
		IMqttToken[] tokens = new IMqttToken[clients.length];
		if (topicFilter == null) {
			return tokens;
		}

		int index = 0;

//		for (MqttClient client : clients) {
//			try {
//		        // A bug in Paho 1.2.5. The following loops indefinitely. This is fixed
//              // in the next version yet to be released (4/25/2023)
//				tokens[index++] = client.subscribe(topicFilter, qos, messageListener);
//			} catch (MqttException e) {
//				removeMqttClient(client);
//			}
//		}

		// A workaround to the above bug.
		MqttSubscription subscription = new MqttSubscription(topicFilter);
		subscription.setQos(qos);
		for (MqttClient client : clients) {
			try {
				tokens[index++] = client.subscribe(new MqttSubscription[] { subscription },
						new IMqttMessageListener[] { messageListener });
			} catch (MqttException e) {
				removeMqttClient(client);
			}
		}
		return tokens;
	}

	/**
	 * MqttClient: {@inheritDoc}
	 */
	public IMqttToken[] subscribe(MqttSubscription[] subscriptions, IMqttMessageListener[] messageListeners)
			throws MqttException {
		clusterState.subscribe(subscriptions, messageListeners);

		// Live client variables are updated from another thread. We reassign them to
		// local variables to handle a race condition.
		MqttClient[] clients = liveClients;

		// Return empty tokens (null values) if subscriptions is null
		IMqttToken[] tokens = new IMqttToken[clients.length];
		if (subscriptions == null) {
			return tokens;
		}

		int index = 0;
		for (MqttClient client : clients) {
			try {
				tokens[index++] = client.subscribe(subscriptions, messageListeners);
			} catch (MqttException e) {
				removeMqttClient(client);
			}
		}
		return tokens;
	}

	/**
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void unsubscribe(String topicFilter) throws MqttException {
		clusterState.unsubscribe(topicFilter);

		// Live client variables are updated from another thread. We reassign them to
		// local variables to handle a race condition.
		MqttClient[] clients = liveClients;

		for (MqttClient client : clients) {
			try {
				client.unsubscribe(topicFilter);
			} catch (MqttException e) {
				removeMqttClient(client);
			}
		}
	}

	/**
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void unsubscribe(String[] topicFilters) throws MqttException {
		clusterState.unsubscribe(topicFilters);

		// Live client variables are updated from another thread. We reassign them to
		// local variables to handle a race condition.
		MqttClient[] clients = liveClients;

		for (MqttClient client : clients) {
			try {
				client.unsubscribe(topicFilters);
			} catch (MqttException e) {
				removeMqttClient(client);
			}
		}
	}

	/**
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void setCallback(MqttCallback callback) {
		clusterState.setCallback(callback);
	}

	/**
	 * IHaMqttClient: {@inheritDoc}
	 */
	public void addCallbackCluster(IHaMqttCallback callback) {
		clusterState.addCallbackCluster(callback);
	}

	/**
	 * IHaMqttClient: {@inheritDoc}
	 */
	public void removeCallbackCluster(IHaMqttCallback callback) {
		clusterState.removeCallbackCluster(callback);
	}

	/**
	 * Returns true if the cluster state is live, indicating the cluster service is
	 * actively probing and maintaining the cluster.
	 */
	public boolean isLive() {
		return clusterState.isLive();
	}

	/**
	 * Connects to the cluster. This method has no effect if the cluster state is
	 * live or closed. It only applies to the cluster state of disconnected, i.e.,
	 * {@link #isDisconnected()} == true.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void connect() throws MqttSecurityException, MqttException {
		if (connectionInProgress == false) {
			connectionInProgress = true;
			try {
				if (isDisconnected()) {
					clusterState.connect();
				}
			} catch (Throwable th) {
				connectionInProgress = false;
				throw th;
			}
			connectionInProgress = false;
		}
	}

	/**
	 * Connects to the cluster. This method has no effect if the cluster state is
	 * live or closed. It only applies to the cluster state of disconnected, i.e.,
	 * {@link #isDisconnected()} == true. This method may modify the specified
	 * connection options to be compliant with this object.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void connect(MqttConnectionOptions options) throws MqttSecurityException, MqttException {
		if (connectionInProgress == false) {
			connectionInProgress = true;
			try {
				if (isDisconnected()) {
					clusterState.connect(options);
				}
			} catch (Throwable th) {
				throw th;
			}
			connectionInProgress = false;
		}
	}

	/**
	 * Connects to the cluster and returns the publisher's token. It returns null if
	 * it is unable to connect to the cluster. To obtain all connected tokens, use
	 * {@link #connectWithResultCluster(MqttConnectionOptions)}. This method may
	 * modify the specified connection options to be compliant with this object.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public IMqttToken connectWithResult(MqttConnectionOptions options) throws MqttSecurityException, MqttException {
		IMqttToken[] tokens = connectWithResultCluster(options);
		IMqttToken token = null;
		for (IMqttToken t : tokens) {
			MqttClient client = getPublisher();
			if (client != null && client.getClientId().equals(t.getClient().getClientId())) {
				token = t;
				break;
			}
		}
		return token;
	}

	/**
	 * IHaMqttClient: {@inheritDoc}
	 */
	public IMqttToken[] connectWithResultCluster(MqttConnectionOptions options)
			throws MqttSecurityException, MqttException {
		IMqttToken[] tokens;
		if (isDisconnected()) {
			tokens = clusterState.connectWithResult(options);
		} else {
			tokens = new IMqttToken[0];
		}
		return tokens;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDisconnected() {
		return clusterState.isDisconnected();
	}

	/**
	 * Disconnects the cluster. Disconnected clusters can be reconnected by invoking
	 * any of the {@link #connect()} methods.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void disconnect() {
		clusterState.disconnect();
	}

	/**
	 * Disconnects the cluster. Disconnected clusters can be reconnected by invoking
	 * any of the {@link #connect()} methods.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void disconnect(long quiesceTimeout) throws MqttException {
		clusterState.disconnect(quiesceTimeout);
	}

	/**
	 * Gracefully closes the cluster. Analogous to invoking
	 * <code>close(false)</code>. Once closed, this object is no longer operational.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void close() throws MqttException {
		close(false);
	}

	/**
	 * MqttClient: Forcibly closes the cluster. Once closed, this object is no
	 * longer operational.
	 * 
	 * 
	 * @param force true to Forcibly close the cluster, false to gracefully close
	 *              the cluster.
	 */
	public void close(boolean force) throws MqttException {
		ClusterService.getClusterService().removeHaClient(this, force);
		roundRobinThreadLocal.remove();
	}

	/**
	 * HaMqttClient: {@inheritDoc}
	 */
	public boolean isClosed() {
		return clusterState.isClosed();
	}

	/**
	 * HaMqttClient: {@inheritDoc}
	 */
	public MqttClient getPrimaryMqttClient() {
		return clusterState.getPrimaryClient();
	}

	/**
	 * HaMqttClient: {@inheritDoc}
	 */
	public String[] getClientIds() {
		return clusterState.getClientIds();
	}

	/**
	 * HaMqttClient: {@inheritDoc}
	 */
	public String[] getLiveClientIds() {
		return clusterState.getLiveClientIds();
	}

	/**
	 * HaMqttClient: {@inheritDoc}
	 */
	public String[] getDisconnectedClientIds() {
		return clusterState.getDeadClientIds();
	}

	/**
	 * HaMqttClient: {@inheritDoc}
	 */
	public String[] getServerURIs() {
		return clusterState.getServerURIs();
	}

	/**
	 * HaMqttClient: {@inheritDoc}
	 */
	public String[] getCurrentServerURIs() {
		return clusterState.getCurrentServerURIs();
	}

	/**
	 * Forcibly disconnects the cluster.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void disconnectForcibly() throws MqttException {
		clusterState.disconnectForcibly();
	}

	/**
	 * Forcibly disconnects the cluster.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void disconnectForcibly(long disconnectTimeout) throws MqttException {
		clusterState.disconnectForcibly(disconnectTimeout);
	}

	/**
	 * Forcibly disconnects the cluster.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout) throws MqttException {
		clusterState.disconnectForcibly(quiesceTimeout, disconnectTimeout);
	}

	/**
	 * Returns a publisher's topic object.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public MqttTopic getTopic(String topic) {
		MqttClient client = getPublisher();
		if (client != null) {
			return client.getTopic(topic);
		} else {
			return null;
		}
	}

	/**
	 * Returns true if at least one client is connected.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public boolean isConnected() {
		return clusterState.isConnected();
	}

	/**
	 * Returns the publisher's client ID. It returns null, if the publisher does not
	 * exist.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public String getClientId() {
		MqttClient client = getPublisher();
		if (client != null) {
			return client.getClientId();
		} else {
			return null;
		}
	}

	/**
	 * Returns the publisher's server URI. It returns null, if the publisher does
	 * not exist.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public String getServerURI() {
		MqttClient client = getPublisher();
		if (client != null) {
			return client.getServerURI();
		} else {
			return null;
		}
	}

	/**
	 * Returns the publisher's pending tokens. It returns null, if the publisher
	 * does not exist.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public IMqttToken[] getPendingTokens() {
		MqttClient client = getPublisher();
		if (client != null) {
			return client.getPendingTokens();
		} else {
			return null;
		}
	}

	/**
	 * Sets manual acks.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void setManualAcks(boolean manualAcks) {
		Set<MqttClient> set = clusterState.getLiveClients();
		for (MqttClient client : set) {
			client.setManualAcks(manualAcks);
		}
		set = clusterState.getDeadClients();
		for (MqttClient client : set) {
			client.setManualAcks(manualAcks);
		}
	}

	/**
	 * Reconnects the cluster.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public void reconnect() throws MqttException {
		connect();
	}

	/**
	 * This method only works for a cluster with a single endpoint. For a cluster
	 * with more than one endpoint, use
	 * {@linkplain #messageArrivedComplete(MqttClient, int, int)} instead.
	 * <p>
	 * IMqttClient: {@inheritDoc}
	 * 
	 * @throws UnsupportedOperationException Thrown if the cluster has more than one
	 *                                       broker.
	 * @see #messageArrivedComplete(MqttClient, int, int)
	 */
	@Override
	public void messageArrivedComplete(int messageId, int qos) throws MqttException {
		MqttClient client = getPublisher();
		if (client != null && clusterState.getAllEndpoints().size() == 1) {
			client.messageArrivedComplete(messageId, qos);
		} else {
			throw new UnsupportedOperationException(
					"This method is supported for a single endpoint (broker) cluster only. This cluster has more than one borker. Use messageArrivedComplete(MqttClient client, int messageId, int qos) instead.");
		}
	}

	/**
	 * IMqttClient: {@inheritDoc}
	 */
	public void messageArrivedComplete(MqttClient client, int messageId, int qos) throws MqttException {
		if (client != null) {
			client.messageArrivedComplete(messageId, qos);
		}
	}

	/**
	 * IMqttClient: {@inheritDoc}
	 */
	public void addServerURI(String serverURI) {
		clusterState.addEndpoint(serverURI);
	}

	/**
	 * IMqttClient: {@inheritDoc}
	 */
	public boolean removeServerURI(String serverURI) {
		return clusterState.removeEndpoint(serverURI);
	}

	/**
	 * MqttClient: {@inheritDoc}
	 */
	public void setTimeToWait(long timeToWaitInMillis) throws IllegalArgumentException {
		clusterState.setTimeToWait(timeToWaitInMillis);
	}

	/**
	 * MqttClient: {@inheritDoc}
	 */
	public long getTimeToWait() {
		return clusterState.getTimeToWait();
	}

	@Override
	public String toString() {
		return "HaMqttClient [clusterName=" + clusterName + ", currentThreadPublisher=" + getPublisher()
				+ ", publisherType=" + publisherType + "]";
	}

}
