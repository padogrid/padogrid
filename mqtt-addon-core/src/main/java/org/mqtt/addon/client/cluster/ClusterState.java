package org.mqtt.addon.client.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSecurityException;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.mqtt.addon.client.cluster.config.ClusterConfig;

/**
 * {@linkplain ClusterState} probes a collection of MQTT endpoints. The endpoint
 * string format must be compliant with the Mosquitto server URI format, e.g.,
 * tcp://localhost:1883, ssl://localhost:8883.
 * 
 * @author dpark
 *
 */
public class ClusterState implements IClusterConfig {
	private final HaMqttClient haclient;
	private final MqttClientPersistence persistence;

	private String primaryServerURI;
	private MqttClient primaryClient;
	private final Set<String> allEndpointSet = Collections.synchronizedSet(new HashSet<String>(5));
	private final Set<MqttClient> liveClientSet = Collections.synchronizedSet(new HashSet<MqttClient>(5));
	private final Set<MqttClient> deadClientSet = Collections.synchronizedSet(new HashSet<MqttClient>(5));
	private final Set<String> deadEndpointSet = Collections.synchronizedSet(new HashSet<String>(5));

	private Logger logger;
	private String clientIdPrefix;

	private Set<TopicInfo> subscribedTopics = Collections.synchronizedSet(new HashSet<TopicInfo>());
	private MqttCallback callback;
	private ArrayList<IHaMqttClientCallback> haCallbackList = new ArrayList<IHaMqttClientCallback>(2);
	private IHaMqttClientCallback[] haCallbacks = haCallbackList.toArray(new IHaMqttClientCallback[0]);

	private MqttConnectionOptions connectionOptions = new MqttConnectionOptions();

	enum ConnectionState {
		LIVE, DISCONNECTED, CLOSED
	}

	private volatile ConnectionState connectionState = ConnectionState.DISCONNECTED;

	ClusterState(HaMqttClient haclient, ClusterConfig.Cluster clusterConfig, MqttClientPersistence persistence) {
		this.haclient = haclient;

		this.primaryServerURI = clusterConfig.getPrimaryServerURI();
		this.persistence = persistence;
		this.logger = LogManager.getLogger(String.format("ClusterState[%s]", haclient.getClusterName()));
		clientIdPrefix = haclient.getClusterName();
		MqttConnectionOptions options = clusterConfig.getConnection();
		if (options == null) {
			options = connectionOptions;
			String endpoints = IClusterConfig.DEFAULT_CLIENT_SERVER_URIS;
			addEndpoints(endpoints);
		} else {
			String[] endpoints = clusterConfig.getConnection().getServerURIs();
			// Set server URLs to null to disable MqttClient HA. We do our own HA.
			clusterConfig.getConnection().setServerURIs(new String[0]);
			addEndpoints(endpoints);
		}
	}

	private List<String> getEndpointList(String endpoints) {
		String[] split = endpoints.split(",");
		ArrayList<String> list = new ArrayList<String>(split.length);
		for (String endpoint : split) {
			endpoint = endpoint.trim();
			if (list.contains(endpoint) == false) {
				list.add(endpoint);
			}
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Adds the specified target endpoints. New endpoints are placed in the dead
	 * endpoint list and eventually moved to the live endpoint list upon successful
	 * connections.
	 * 
	 * @param endpoints Endpoint URLs. If null, then it is treated as empty.
	 */
	public void addEndpoints(String[] endpoints) {
		if (endpoints == null) {
			return;
		}
		for (String endpoint : endpoints) {
			if (allEndpointSet.contains(endpoint) == false) {
				allEndpointSet.add(endpoint);
				deadEndpointSet.add(endpoint);
			}
		}
		if (logger != null) {
			logger.info(String.format("Added/updated endpoints [endpoints=%s]. All endpoints [%s].", endpoints,
					getAllEndpoints()));
		}
	}

	/**
	 * Adds the specified target endpoints. New endpoints are placed in the dead
	 * endpoint list and eventually moved to the live endpoint list upon successful
	 * connections.
	 * 
	 * @param endpoints Endpoint URLs. If null, then it is treated as empty.
	 */
	public void addEndpoints(String endpoints) {
		if (endpoints == null) {
			return;
		}
		List<String> endpointList = getEndpointList(endpoints);
		for (String endpoint : endpointList) {
			if (allEndpointSet.contains(endpoint) == false) {
				allEndpointSet.add(endpoint);
				deadEndpointSet.add(endpoint);
			}
		}
		if (logger != null) {
			logger.info(String.format("Added/updated endpoints [endpoints=%s]. All endpoints [%s].", endpoints,
					getAllEndpoints()));
		}
	}

	private String getLiveEndpoints() {
		StringBuffer buffer = new StringBuffer();
		int count = 0;
		for (MqttClient client : liveClientSet) {
			if (count > 0) {
				buffer.append(", ");
			}
			buffer.append(client.getServerURI());
			count++;
		}
		return buffer.toString();
	}

	private String createClientId(String endpoint) {
		if (endpoint == null) {
			return clientIdPrefix;
		}
		String clientId = clientIdPrefix + "-" + endpoint.replace("/", "").replaceAll(":", "-");
		return clientId;
	}

	/**
	 * Connects to all dead endpoints listed in deadClientSet, which becomes empty
	 * upon successful connections.
	 * 
	 * @return A non-empty array of user tokens.
	 */
	private IMqttToken[] connectDeadEndpoints() {
		int count = 0;
		StringBuffer buffer = new StringBuffer();
		ArrayList<IMqttToken> tokenList = new ArrayList<IMqttToken>(deadClientSet.size());
		synchronized (deadEndpointSet) {
			Iterator<String> iterator = deadEndpointSet.iterator();
			while (iterator.hasNext()) {
				String endpoint = iterator.next();
				MqttClient client = null;
				try {
					String clientId = createClientId(endpoint);

					// Use the global persistence if defined. The global persistence is initialized
					// and kept in ClusterService.
					if (persistence == null) {
						MqttClientPersistence persistence = ClusterService.getClusterService()
								.createMqttClientPersistence();
						if (persistence == null) {
							client = new MqttClient(endpoint, clientId);
						} else {
							client = new MqttClient(endpoint, clientId, persistence);
						}
					} else {
						client = new MqttClient(endpoint, clientId, persistence);
					}
					client.setCallback(new MqttCallbackImpl(client));
					IMqttToken token = client.connectWithResult(connectionOptions);
					tokenList.add(token);
					TopicInfo[] subscriptions = subscribedTopics.toArray(new TopicInfo[0]);
					for (TopicInfo subscription : subscriptions) {
						subscription.subscribe(client);
					}
					liveClientSet.add(client);
					if (endpoint.equals(primaryServerURI)) {
						primaryClient = client;
					}
					iterator.remove();

					if (count > 0) {
						buffer.append(",");
					}
					buffer.append(client.getServerURI());
					count++;
				} catch (MqttException e) {
					logger.debug(String.format("Exception raised while making initial connection [%s]. %s", endpoint,
							e.getMessage()));
					if (client != null) {
						try {
							client.close();
						} catch (MqttException e1) {
							// ignore
						}
					}
				}
			}
		}
		return tokenList.toArray(new IMqttToken[0]);
	}

	/**
	 * Revives the dead endpoints if any. The revived endpoints are promoted to the
	 * live client list.
	 */
	void reviveDeadEndpoints() {

		// Revive only if in LiVE state
		if (connectionState != ConnectionState.LIVE) {
			return;
		}

		int count = 0;
		StringBuffer buffer = new StringBuffer();

		// First pass, iterate endpoints (String)
		count = connectDeadEndpoints().length;

		// Second pass - iterate dead clients (MqttClient) - Not used
		synchronized (deadClientSet) {
			Iterator<MqttClient> iterator = deadClientSet.iterator();
			TopicInfo[] subscriptions;
			if (iterator.hasNext()) {
				subscriptions = subscribedTopics.toArray(new TopicInfo[0]);
			} else {
				subscriptions = new TopicInfo[0];
			}
			while (iterator.hasNext()) {
				MqttClient client = iterator.next();
				if (client.isConnected() == false) {
					try {
						client.connect(connectionOptions);
						for (TopicInfo subscription : subscriptions) {
							subscription.subscribe(client);
						}
						liveClientSet.add(client);
						iterator.remove();

						if (count > 0) {
							buffer.append(",");
						}
						buffer.append(client.getServerURI());
						count++;
					} catch (MqttException e) {
						logger.debug(String.format("Exception raised while reviving a dead client [%s]. %s",
								client.getServerURI(), e.getMessage()));
					}
				}
			}
		}

		// Third pass - iterate live list and remove all disconnected clients
		synchronized (liveClientSet) {
			Iterator<MqttClient> iterator = liveClientSet.iterator();
			while (iterator.hasNext()) {
				MqttClient client = iterator.next();
				if (client.isConnected() == false) {
					iterator.remove();
					deadClientSet.add(client);
					count--;
				}
			}
		}

		if (count > 0) {
			haclient.updateLiveClients(getLiveClients());
			logger.info(String.format("Revived endpoint [%s]. Live endpoints [%s]. Dead endpoints %s.",
					buffer.toString(), getLiveEndpoints(), deadEndpointSet));
			logConnectionStatus();
		}
	}

	private void logConnectionStatus() {
		if (liveClientSet.size() == allEndpointSet.size()) {
			logger.info(String.format("All endpoints connected."));
		} else {
			logger.info(String.format("%d endpoint(s) connected and %d endpoint(s) not connected.",
					liveClientSet.size(), deadEndpointSet.size() + deadClientSet.size()));
		}
	}

	/**
	 * Returns the primary client. The returned client is not necessary live. It is
	 * the caller's responsibility to check the returned client status.
	 * 
	 * @return Null if the primary server URI is not configured.
	 */
	public MqttClient getPrimaryClient() {
		return primaryClient;
	}

	/**
	 * Returns an unmodifiable set containing all endpoints.
	 */
	public Set<String> getAllEndpoints() {
		return Collections.unmodifiableSet(allEndpointSet);
	}

	/**
	 * Returns an unmodifiable set containing live MqttClient instances.
	 */
	public Set<MqttClient> getLiveClients() {
		return Collections.unmodifiableSet(liveClientSet);
	}

	/**
	 * Returns an unmodifiable set containing dead MqttClient instances.
	 */
	public Set<MqttClient> getDeadClients() {
		return Collections.unmodifiableSet(deadClientSet);
	}

	/**
	 * Returns an unmodifiable set containing dead endpoints.
	 */
	public Set<String> getDeadAddressSet() {
		return Collections.unmodifiableSet(deadEndpointSet);
	}

	/**
	 * Immediately attempts to connect to all endpoints if the cluster is not
	 * closed. The cluster is closed if the {@link #close()} method has previously
	 * been invoked. This method is useful when the application wishes to resume the
	 * cluster connection after invoking {@link #disconnect()}.
	 */
	public void connect() {
		switch (connectionState) {
		case DISCONNECTED:
			connectionState = ConnectionState.LIVE;
			reviveDeadEndpoints();
			break;
		default:
			break;
		}
	}

	/**
	 * Updates the specified connection options to be compliant with HaMqttClient.
	 * This method has no effect if the the specified options is null or connection
	 * state is not {@linkplain ConnectionState#DISCONNECTED}.
	 * 
	 * @param options Connection options
	 * @return null if the specified <code>options</code> is null.
	 */
	private MqttConnectionOptions updateConnectionOptions(MqttConnectionOptions options) {
		if (options != null && isDisconnected()) {
			String[] serverUris = options.getServerURIs();
			if (serverUris != null && serverUris.length > 0) {
				close(isClosed());
				allEndpointSet.clear();
				for (String serverUri : serverUris) {
					allEndpointSet.add(serverUri);
				}
				options.setServerURIs(new String[0]);
			}
		}
		return options;
	}

	public void connect(MqttConnectionOptions options) throws MqttSecurityException, MqttException {
		switch (connectionState) {
		case DISCONNECTED:
			connectionState = ConnectionState.LIVE;
			this.connectionOptions = updateConnectionOptions(options);
			reviveDeadEndpoints();
			break;
		default:
			break;
		}
	}

	public IMqttToken[] connectWithResult(MqttConnectionOptions options) throws MqttSecurityException, MqttException {
		IMqttToken[] tokens = null;
		switch (connectionState) {
		case DISCONNECTED:
			connectionState = ConnectionState.LIVE;
			this.connectionOptions = updateConnectionOptions(options);
			tokens = connectDeadEndpoints();
			break;
		default:
			tokens = new IMqttToken[0];
			break;
		}
		return tokens;
	}

	/**
	 * Disconnects all client instances and deactivates the discovery service for
	 * this cluster state. All disconnected clients are moved from the live list to
	 * the dead list. To reconnect, invoke the {@link #connect()} method, which also
	 * reactivates the discovery service.
	 */
	public void disconnect() {
		switch (connectionState) {
		case LIVE:
			connectionState = ConnectionState.DISCONNECTED;
			synchronized (liveClientSet) {
				Iterator<MqttClient> iterator = liveClientSet.iterator();
				while (iterator.hasNext()) {
					MqttClient client = iterator.next();
					if (client.isConnected()) {
						try {
							client.disconnect();
						} catch (MqttException e) {
							logger.debug("Exception raised while gracefully disconnecting live client [%s]. %s",
									client.getServerURI(), e.getMessage());
						}
					}
					iterator.remove();
					// deadEndpointSet.add(client.getServerURI());
					deadClientSet.add(client);
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Disconnects all client instances and deactivates the discovery service for
	 * this cluster state. All disconnected clients are moved from the live list to
	 * the dead list. To reconnect, invoke the {@link #connect()} method, which also
	 * reactivates the discovery service.
	 */
	public void disconnect(long quiesceTimeout) {
		switch (connectionState) {
		case LIVE:
			connectionState = ConnectionState.DISCONNECTED;
			synchronized (liveClientSet) {
				Iterator<MqttClient> iterator = liveClientSet.iterator();
				while (iterator.hasNext()) {
					MqttClient client = iterator.next();
					if (client.isConnected()) {
						try {
							client.disconnect(quiesceTimeout);
						} catch (MqttException e) {
							logger.debug("Exception raised while gracefully disconnecting live client [%s]. %s",
									client.getServerURI(), e.getMessage());
						}
					}
					iterator.remove();
					// deadEndpointSet.add(client.getServerURI());
					deadClientSet.add(client);
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Disconnects all client instances and deactivates the discovery service for
	 * this cluster state. All disconnected clients are moved from the live list to
	 * the dead list. To reconnect, invoke the {@link #connect()} method, which also
	 * reactivates the discovery service.
	 */
	public void disconnectForcibly() {
		switch (connectionState) {
		case LIVE:
			connectionState = ConnectionState.DISCONNECTED;
			synchronized (liveClientSet) {
				Iterator<MqttClient> iterator = liveClientSet.iterator();
				while (iterator.hasNext()) {
					MqttClient client = iterator.next();
					if (client.isConnected()) {
						try {
							client.disconnectForcibly();
						} catch (MqttException e) {
							logger.debug("Exception raised while forcibly disconnecting live client [%s]. %s",
									client.getServerURI(), e.getMessage());
						}
					}
					iterator.remove();
					deadClientSet.add(client);
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Disconnects all client instances and deactivates the discovery service for
	 * this cluster state. All disconnected clients are moved from the live list to
	 * the dead list. To reconnect, invoke the {@link #connect()} method, which also
	 * reactivates the discovery service.
	 */
	public void disconnectForcibly(long disconnectTimeout) {
		switch (connectionState) {
		case LIVE:
			connectionState = ConnectionState.DISCONNECTED;
			synchronized (liveClientSet) {
				Iterator<MqttClient> iterator = liveClientSet.iterator();
				while (iterator.hasNext()) {
					MqttClient client = iterator.next();
					if (client.isConnected()) {
						try {
							client.disconnectForcibly(disconnectTimeout);
						} catch (MqttException e) {
							logger.debug("Exception raised while forcibly disconnecting live client [%s]. %s",
									client.getServerURI(), e.getMessage());
						}
					}
					iterator.remove();
					deadClientSet.add(client);
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Disconnects all client instances and deactivates the discovery service for
	 * this cluster state. All disconnected clients are moved from the live list to
	 * the dead list. To reconnect, invoke the {@link #connect()} method, which also
	 * reactivates the discovery service.
	 */
	public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout) {
		switch (connectionState) {
		case LIVE:
			connectionState = ConnectionState.DISCONNECTED;
			synchronized (liveClientSet) {
				Iterator<MqttClient> iterator = liveClientSet.iterator();
				while (iterator.hasNext()) {
					MqttClient client = iterator.next();
					if (client.isConnected()) {
						try {
							client.disconnectForcibly(quiesceTimeout, disconnectTimeout);
						} catch (MqttException e) {
							logger.debug("Exception raised while forcibly disconnecting live client [%s]. %s",
									client.getServerURI(), e.getMessage());
						}
					}
					iterator.remove();
					deadClientSet.add(client);
				}
			}
			break;
		default:
			break;
		}
	}

	public boolean isLive() {
		return connectionState == ConnectionState.LIVE;
	}

	public boolean isDisconnected() {
		return connectionState == ConnectionState.DISCONNECTED;
	}

	public boolean isConnected() {
		return liveClientSet.size() > 0;
	}

	/**
	 * Closes the discovery service for this cluster state by closing all clients.
	 * Once closed, HaMqttClient is no longer operational.
	 * 
	 * @param force true to forcibly close the client
	 */
	public void close(boolean force) {
		switch (connectionState) {
		case LIVE:
		case DISCONNECTED:
			connectionState = ConnectionState.CLOSED;
			synchronized (liveClientSet) {
				Iterator<MqttClient> iterator = liveClientSet.iterator();
				while (iterator.hasNext()) {
					MqttClient client = iterator.next();
					if (client.isConnected()) {
						try {
							client.disconnect();
						} catch (MqttException e) {
							logger.debug(String.format("Exception raised while gracefully disconnecting live client %s",
									e.getMessage()));
						}
					}
					try {
						client.close(force);
					} catch (MqttException e) {
						logger.debug(
								String.format("Exception raised while closing disconnected client %s", e.getMessage()));
					}
					iterator.remove();
					deadEndpointSet.add(client.getServerURI());
				}
			}
			synchronized (deadClientSet) {
				Iterator<MqttClient> iterator = deadClientSet.iterator();
				while (iterator.hasNext()) {
					MqttClient client = iterator.next();
					if (client.isConnected()) {
						try {
							client.disconnect();
						} catch (MqttException e) {
							logger.debug(String.format("Exception raised while gracefully disconnecting dead client %s",
									e.getMessage()));
						}
					}
					try {
						client.close(force);
					} catch (MqttException e) {
						logger.debug(String.format("Exception raised while closing dead client %s", e.getMessage()));
					}
					iterator.remove();
					deadEndpointSet.add(client.getServerURI());
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Returns true if the cluster connection is closed. A closed cluster is no
	 * longer operational.
	 */
	public boolean isClosed() {
		return connectionState == ConnectionState.CLOSED;
	}

	/**
	 * Disconnects the specified client, and moves it from the live list to the dead
	 * list.
	 * 
	 * @param client Client to remove.
	 */
	public void removeLiveClient(MqttClient client) {
		if (client != null && liveClientSet.remove(client)) {
			if (client.isConnected()) {
				try {
					client.disconnect();
				} catch (MqttException e) {
					logger.debug(String.format("Exception raised while gracefully disconnecting live client %s",
							e.getMessage()));
				}
			}
			deadClientSet.add(client);
		}
	}

	/**
	 * Returns the first live MqttClient instance in the live list. It returns null
	 * if there are no live clients.
	 */
	public MqttClient getLiveClient() {
		MqttClient client = null;
		synchronized (liveClientSet) {
			Iterator<MqttClient> iterator = liveClientSet.iterator();
			while (iterator.hasNext()) {
				client = iterator.next();
				break;
			}
		}
		return client;
	}

	/**
	 * Returns all client IDs including live and disconnected (dead). If this HA
	 * client has been closed, then it returns an empty array.
	 * 
	 * @return An empty array if no clients exist.
	 */
	public String[] getClientIds() {
		String[] liveClientIds = getLiveClientIds();
		String[] deadClientIds = getDeadClientIds();
		String[] clientIds = new String[liveClientIds.length + deadClientIds.length];
		System.arraycopy(liveClientIds, 0, clientIds, 0, liveClientIds.length);
		if (liveClientIds.length > 0) {
			System.arraycopy(deadClientIds, 0, clientIds, liveClientIds.length - 1, liveClientIds.length);
		}
		return clientIds;
	}

	/**
	 * Returns live client IDs.
	 * 
	 * @return An empty array if no live clients exist.
	 */
	public String[] getLiveClientIds() {
		String[] clientIds;
		synchronized (liveClientSet) {
			MqttClient[] clients = liveClientSet.toArray(new MqttClient[0]);
			clientIds = new String[clients.length];
			for (int i = 0; i < clients.length; i++) {
				clientIds[i] = clients[i].getClientId();
			}
		}
		return clientIds;
	}

	/**
	 * Returns disconnected client IDs.
	 * 
	 * @return An empty array if no disconnected (dead) clients exist.
	 */
	public String[] getDeadClientIds() {
		String[] clientIds;
		synchronized (deadClientSet) {
			MqttClient[] clients = deadClientSet.toArray(new MqttClient[0]);
			clientIds = new String[clients.length];
			for (int i = 0; i < clients.length; i++) {
				clientIds[i] = clients[i].getClientId();
			}
		}
		return clientIds;
	}

	/**
	 * Returns all (connected and disconnected) server URIs that make up the
	 * cluster.
	 */
	public String[] getServerURIs() {
		ArrayList<String> list = new ArrayList<String>(allEndpointSet.size());
		synchronized (liveClientSet) {
			MqttClient[] clients = liveClientSet.toArray(new MqttClient[0]);
			for (MqttClient client : clients) {
				list.add(client.getServerURI());
			}
		}
		synchronized (deadClientSet) {
			MqttClient[] clients = deadClientSet.toArray(new MqttClient[0]);
			for (MqttClient client : clients) {
				list.add(client.getServerURI());
			}
		}
		return list.toArray(new String[0]);
	}

	/**
	 * Returns the currently connected Server URIs Implemented due to:
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=481097.
	 *
	 * @return the currently connected server URI
	 * @see MqttClient#getCurrentServerURI()
	 */
	public String[] getCurrentServerURIs() {
		String[] clientIds;
		synchronized (liveClientSet) {
			MqttClient[] clients = liveClientSet.toArray(new MqttClient[0]);
			clientIds = new String[clients.length];
			for (int i = 0; i < clients.length; i++) {
				clientIds[i] = clients[i].getCurrentServerURI();
			}
		}
		return clientIds;
	}

	public void setCallback(MqttCallback callback) {
		this.callback = callback;
	}

	public void addCallbackCluster(IHaMqttClientCallback haCallback) {
		haCallbackList.add(haCallback);
		haCallbacks = haCallbackList.toArray(new IHaMqttClientCallback[0]);
	}

	public void removeCallbackCluster(IHaMqttClientCallback haCallback) {
		haCallbackList.remove(haCallback);
		haCallbacks = haCallbackList.toArray(new IHaMqttClientCallback[0]);
	}

	/**
	 * Refreshes the discovery service by reconstructing the dead client list.
	 */
	public void refresh() {
		deadEndpointSet.clear();
		for (String endpoint : allEndpointSet) {
			boolean found = false;
			for (MqttClient client : liveClientSet) {
				if (endpoint.equals(client.getServerURI())) {
					found = true;
					break;
				}
			}
			if (found == false) {
				deadEndpointSet.add(endpoint);
			}
		}
	}

	public void subscribe(String topicFilter, int qos) {
		subscribedTopics.add(new TopicFilter(topicFilter, qos));
	}

	public void subscribe(String[] topicFilters, int[] qos) {
		subscribedTopics.add(new TopicFilters(topicFilters, qos));
	}

	public void subscribe(MqttSubscription[] subscriptions) {
		subscribedTopics.add(new TopicSubscriptions(subscriptions));
	}

	public void subscribe(String topicFilter, int qos, IMqttMessageListener messageListener) {
		subscribedTopics.add(new TopicFilter(topicFilter, qos, messageListener));
	}

	public void subscribe(MqttSubscription[] subscriptions, IMqttMessageListener[] messageListeners) {
		subscribedTopics.add(new TopicSubscriptions(subscriptions, messageListeners));
	}

	public void unsubscribe(String topicFilter) {
		subscribedTopics.remove(new TopicFilter(topicFilter));
	}

	public void unsubscribe(String[] topicFilters) {
		subscribedTopics.remove(new TopicFilters(topicFilters));
	}

	abstract class TopicInfo {
		void subscribe(MqttClient client) throws MqttException {
		}

		void unsubscribe(MqttClient client) throws MqttException {
		}
	}

	class TopicFilter extends TopicInfo {
		String topicFilter;
		int qos;
		IMqttMessageListener messageListener;

		TopicFilter(String topicFilter) {
			this(topicFilter, 0, null);
		}

		TopicFilter(String topicFilter, int qos) {
			this(topicFilter, qos, null);
		}

		TopicFilter(String topicFilter, int qos, IMqttMessageListener messageListener) {
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
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + Objects.hash(topicFilter);
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
			TopicFilter other = (TopicFilter) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			return Objects.equals(topicFilter, other.topicFilter);
		}

		private ClusterState getEnclosingInstance() {
			return ClusterState.this;
		}
	}

	class TopicFilters extends TopicInfo {
		String[] topicFilters;
		int[] qos;

		TopicFilters(String[] topicFilters) {
			this(topicFilters, null);
		}

		TopicFilters(String[] topicFilters, int[] qos) {
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
			result = prime * result + getEnclosingInstance().hashCode();
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
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			return Arrays.equals(topicFilters, other.topicFilters);
		}

		private ClusterState getEnclosingInstance() {
			return ClusterState.this;
		}
	}

	class TopicSubscriptions extends TopicInfo {
		MqttSubscription[] subscriptions;
		IMqttMessageListener[] messageListeners;

		TopicSubscriptions(MqttSubscription[] subscriptions) {
			this(subscriptions, null);
		}

		TopicSubscriptions(MqttSubscription[] subscriptions, IMqttMessageListener[] messageListeners) {
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
			result = prime * result + getEnclosingInstance().hashCode();
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
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			return Arrays.equals(subscriptions, other.subscriptions);
		}

		private ClusterState getEnclosingInstance() {
			return ClusterState.this;
		}
	}

	class MqttCallbackImpl implements MqttCallback {

		private MqttClient client;

		MqttCallbackImpl(MqttClient client) {
			this.client = client;
		}

		@Override
		public void mqttErrorOccurred(MqttException exception) {
			MqttCallback cb = callback;
			if (cb != null) {
				cb.mqttErrorOccurred(exception);
			}
			IHaMqttClientCallback[] hacb = haCallbacks;
			for (IHaMqttClientCallback hacallback : hacb) {
				hacallback.mqttErrorOccurred(client, exception);
			}
			logger.info(String.format("MQTT error received: %s.", exception));
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			MqttCallback cb = callback;
			if (cb != null) {
				cb.messageArrived(topic, message);
			}
			IHaMqttClientCallback[] hacb = haCallbacks;
			for (IHaMqttClientCallback hacallback : hacb) {
				hacallback.messageArrived(client, topic, message);
			}
		}

		@Override
		public void disconnected(MqttDisconnectResponse disconnectResponse) {
			// Client may have been reconnected by the discovery service.
			if (client.isConnected() == false) {
				MqttCallback cb = callback;
				if (cb != null) {
					cb.disconnected(disconnectResponse);
				}
				IHaMqttClientCallback[] hacb = haCallbacks;
				for (IHaMqttClientCallback hacallback : hacb) {
					hacallback.disconnected(client, disconnectResponse);
				}
				removeLiveClient(client);
				haclient.updateLiveClients(getLiveClients());
				logger.info(String.format("Client disconnected and removed from the live list [%s]. %s.",
						client.getServerURI(), disconnectResponse));
				logConnectionStatus();
			}
		}

		@Override
		public void deliveryComplete(IMqttToken token) {
			MqttCallback cb = callback;
			if (cb != null) {
				cb.deliveryComplete(token);
			}
			IHaMqttClientCallback[] hacb = haCallbacks;
			for (IHaMqttClientCallback hacallback : hacb) {
				hacallback.deliveryComplete(client, token);
			}
		}

		@Override
		public void connectComplete(boolean reconnect, String serverURI) {
			MqttCallback cb = callback;
			if (cb != null) {
				cb.connectComplete(reconnect, serverURI);
			}
			IHaMqttClientCallback[] hacb = haCallbacks;
			for (IHaMqttClientCallback hacallback : hacb) {
				hacallback.connectComplete(client, reconnect, serverURI);
			}
			logger.info(String.format("Client connected and moved to the live list [reconnect=%s, serverURI=%s].",
					reconnect, serverURI));
		}

		@Override
		public void authPacketArrived(int reasonCode, MqttProperties properties) {
			MqttCallback cb = callback;
			if (cb != null) {
				cb.authPacketArrived(reasonCode, properties);
			}
			IHaMqttClientCallback[] hacb = haCallbacks;
			for (IHaMqttClientCallback hacallback : hacb) {
				hacallback.authPacketArrived(client, reasonCode, properties);
			}
		}
	}
}
