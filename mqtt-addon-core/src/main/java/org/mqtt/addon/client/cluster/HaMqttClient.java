package org.mqtt.addon.client.cluster;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Random;
import java.util.Set;

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

import com.mysql.cj.xdevapi.Client;

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
	private MqttClient publisherClient;
	private ClusterState clusterState;
	private Logger logger = LogManager.getLogger(HaMqttClient.class);

	private PublisherType publisherType = PublisherType.STICKY;
	private int roundRobinIndex = 0;
	private Random random = new Random();

	HaMqttClient() throws FileNotFoundException {
		this(null, null);
	}

	HaMqttClient(ClusterConfig.Cluster clusterConfig, MqttClientPersistence persistence) throws FileNotFoundException {
		if (clusterConfig != null) {
			this.clusterName = clusterConfig.getName();
			if (this.clusterName == null || this.clusterName.length() == 0) {
				clusterName = IClusterConfig.DEFAULT_CLUSTER_NAME;
			}
			this.publisherType = clusterConfig.getPublisherType();
			logger.info(String.format("%s created [clusterName=%s, publisherType=%s]",
					HaMqttClient.class.getSimpleName(), clusterName, publisherType));

			// Obtain cluster state
			clusterState = ClusterService.getClusterService().addHaClient(this, clusterConfig, persistence);
		}
	}

	private void removeMqttClient(MqttClient client) {
		clusterState.removeLiveClient(client);
		liveClients = clusterState.getLiveClients().toArray(new MqttClient[0]);
	}

	/**
	 * Invoked by the discovery service upon detecting live client updates.
	 * 
	 * @param liveClientCollection A collection containing the latest live clients.
	 */
	void updateLiveClients(Collection<MqttClient> liveClientCollection) {
		MqttClient[] clients = liveClientCollection.toArray(new MqttClient[0]);

		// Set publisherClient if the current one is not in the live list.
		// Also, set the callback for the new clients.
		boolean publisherFound = false;
		for (MqttClient client : clients) {
			if (publisherClient == client) {
				publisherFound = true;
				break;
			}
		}

		// Set liveClients here. getPublisher() depends on it.
		liveClients = clients;
		if (publisherFound == false) {
			publisherClient = getPublisher();
		}
	}

	private MqttClient getPublisher() {
		MqttClient client = null;
		MqttClient[] clients = liveClients;
		if (clients.length > 0) {
			switch (publisherType) {
			case RANDOM:
				int index = Math.abs(random.nextInt()) % clients.length;
				client = clients[index];
				break;
			case ROUND_ROBIN:
				roundRobinIndex = (++roundRobinIndex) % clients.length;
				client = clients[roundRobinIndex];
				break;
			case STICKY:
			default:
				client = publisherClient;
				if (client == null) {
					client = clusterState.getPrimaryClient();
					if (client == null || client.isConnected() == false) {
						client = clients[0];
					}
					publisherClient = client;
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

	@Override
	public void publish(String topicFilter, MqttMessage message) throws MqttException {
		// Live client variables are updated from another thread. We reassign them to
		// local variables to handle a race condition.
		MqttClient client = getPublisher();

		if (client == null || liveClients.length == 0) {
			publisherClient = null;
			throw new MqttException(-100);
		}
		try {
			client.publish(topicFilter, message);
		} catch (MqttException e) {

			// If publish() fails, then we assume the connection is
			// no longer valid. Remove the client from the live list
			// so that the discovery service can probe and reconnect.
			removeMqttClient(client);

			logger.debug(String.format("publish() failed. Removed %s[%s]", HaMqttClient.class.getSimpleName(),
					client.getServerURI()));

			// Upon removal, a new live client list is obtained.
			// Publish it again with the new publisherClient.
			MqttClient[] clients = liveClients;
			if (clients.length == 0) {
				throw e;
			} else {
				publisherClient = null;
				publish(topicFilter, message);
			}
		}
	}

	@Override
	public void publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException {
		// Live client variables are updated from another thread. We reassign them to
		// local variables to handle a race condition.
		MqttClient client = getPublisher();
		if (client == null || liveClients.length == 0) {
			publisherClient = null;
			throw new MqttException(-100);
		}
		try {
			client.publish(topic, payload, qos, retained);
		} catch (MqttException e) {
			// If publish() fails, then we assume the connection is
			// no longer valid. Remove the client from the live list
			// so that the discovery service can probe and reconnect.
			removeMqttClient(client);

			logger.debug(String.format("publish() failed. Removed %s[%s]", HaMqttClient.class.getSimpleName(),
					client.getServerURI()));

			// Upon removal, a new live client list is obtained.
			// Publish it again with the new publisherClient.
			MqttClient[] clients = liveClients;
			if (clients.length == 0) {
				throw e;
			} else {
				publisherClient = null;
				publish(topic, payload, qos, retained);
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
	public IMqttToken subscribe(String topicFilter, int qos) {
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

	public IMqttToken[] subscribeCluster(String topicFilter, int qos) {
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
	 * IMqttClient: {@inheritDoc}
	 */
	@Override
	public IMqttToken subscribe(String[] topicFilters, int[] qos) {
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

	public IMqttToken[] subscribeCluster(String[] topicFilters, int[] qos) {
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
//				// A bug in Paho 1.2.5. The following loops indefinitely.
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

	public IMqttToken[] subscribe(MqttSubscription[] subscriptions) {
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
//				// A bug in Paho 1.2.5. The following loops indefinitely.
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

	@Override
	public void unsubscribe(String topicFilter) {
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

	@Override
	public void unsubscribe(String[] topicFilters) {
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

	@Override
	public void setCallback(MqttCallback callback) {
		clusterState.setCallback(callback);
	}

	public void addCallbackCluster(IHaMqttClientCallback callback) {
		clusterState.addCallbackCluster(callback);
	}

	public void removeCallbackCluster(IHaMqttClientCallback callback) {
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
		if (isDisconnected()) {
			clusterState.connect();
			liveClients = clusterState.getLiveClients().toArray(new MqttClient[0]);

			if (liveClients.length > 0) {
				publisherClient = getPublisher();
			}
			clusterState.connect();
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
		if (isDisconnected()) {
			clusterState.connect(options);
			liveClients = clusterState.getLiveClients().toArray(new MqttClient[0]);

			if (liveClients.length > 0) {
				publisherClient = getPublisher();
			}
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
			if (publisherClient.getClientId().equals(t.getClient().getClientId())) {
				token = t;
				break;
			}
		}
		return token;
	}

	/**
	 * Connects to the cluster and returns all the connected client tokens. This
	 * method may modify the specified connection options to be compliant with this
	 * object.
	 * 
	 * @param options Connection options
	 * @return A non-null token array.
	 * @throws MqttSecurityException
	 * @throws MqttException
	 */
	public IMqttToken[] connectWithResultCluster(MqttConnectionOptions options)
			throws MqttSecurityException, MqttException {
		IMqttToken[] tokens;
		if (isDisconnected()) {
			tokens = clusterState.connectWithResult(options);
			liveClients = clusterState.getLiveClients().toArray(new MqttClient[0]);

			if (liveClients.length > 0) {
				publisherClient = getPublisher();
			}
		} else {
			tokens = new IMqttToken[0];
		}
		return tokens;
	}

	/**
	 * Returns true if the cluster state is disconnected, indicating all the
	 * endpoints in the cluster have been disconnected and the cluster is not
	 * maintained. From this state, the cluster can be reactivated by invoking any
	 * of the {@linkplain #connect()} methods.
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
	 * Forcibly closes the cluster. Once closed, this object is no longer
	 * operational.
	 * 
	 * @param force true to Forcibly close the cluster, false to gracefully close
	 *              the cluster.
	 */
	public void close(boolean force) throws MqttException {
		ClusterService.getClusterService().removeHaClient(this, force);
	}

	/**
	 * Returns true if the cluster is closed.
	 */
	public boolean isClosed() {
		return clusterState.isClosed();
	}

	/**
	 * Returns the primary MqttClient instance. It returns null if the primary
	 * client is not configured.
	 */
	public MqttClient getPrimaryMqttClient() {
		return clusterState.getPrimaryClient();
	}

	/**
	 * Returns all client IDs including live and disconnected. If this HA client has
	 * been closed, then it returns an empty array.
	 * 
	 * @return An empty array if no clients exist.
	 */
	public String[] getClientIds() {
		return clusterState.getClientIds();
	}

	/**
	 * Returns live client IDs. This method is analogous to
	 * {@link #getCurrentServerURIs()}.
	 * 
	 * @return An empty array if no live clients exist.
	 * @see #getCurrentServerURIs()
	 */
	public String[] getLiveClientIds() {
		return clusterState.getLiveClientIds();
	}

	/**
	 * Returns disconnected client IDs.
	 * 
	 * @return An empty array if no disconnected clients exist.
	 */
	public String[] getDisconnectedClientIds() {
		return clusterState.getDeadClientIds();
	}

	/**
	 * Returns all (connected and disconnected) server URIs that make up the
	 * cluster.
	 */
	public String[] getServerURIs() {
		return clusterState.getServerURIs();
	}

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
		if (publisherClient != null) {
			return publisherClient.getTopic(topic);
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
		if (publisherClient != null) {
			return publisherClient.getClientId();
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
		if (publisherClient != null) {
			return publisherClient.getServerURI();
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
		if (publisherClient != null) {
			return publisherClient.getPendingTokens();
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
		if (clusterState.getAllEndpoints().size() == 1 && publisherClient != null) {
			publisherClient.messageArrivedComplete(messageId, qos);
		} else {
			throw new UnsupportedOperationException(
					"This method is supported for a single endpoint (broker) cluster only. This cluster has more than one borker. Use messageArrivedComplete(MqttClient client, int messageId, int qos) instead.");
		}
	}

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
	public void messageArrivedComplete(MqttClient client, int messageId, int qos) throws MqttException {
		if (client != null) {
			client.messageArrivedComplete(messageId, qos);
		}
	}

	@Override
	public String toString() {
		return "HaMqttClient [clusterName=" + clusterName + ", publisherClient=" + publisherClient + ", publisherType="
				+ publisherType + "]";
	}

}
