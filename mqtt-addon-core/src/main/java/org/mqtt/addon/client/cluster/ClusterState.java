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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.internal.NetworkModuleService;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttPersistenceException;
import org.eclipse.paho.mqttv5.common.MqttSecurityException;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.mqtt.addon.client.cluster.config.ClusterConfig;
import org.mqtt.addon.client.cluster.config.ConfigUtil;

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
	private ScheduledExecutorService executorService;

	// The first iteration of connections is done on a small number of brokers
	// to reduce the connection blocking time.
	private boolean isFirstConnectionAttempt = true;
	private int initialEndpointCount = -1;
	private boolean isEnabled = true;

	// Mutex lock to synchronize endpoint sets.
	private Object lock = new Object();

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
	private ArrayList<IHaMqttCallback> haCallbackList = new ArrayList<IHaMqttCallback>(2);
	private IHaMqttCallback[] haCallbacks = haCallbackList.toArray(new IHaMqttCallback[0]);

	private MqttConnectionOptions connectionOptions = new MqttConnectionOptions();

	enum ConnectionState {
		LIVE, DISCONNECTED, CLOSED
	}

	private volatile boolean connectionInProgress = false;
	private volatile ConnectionState connectionState = ConnectionState.DISCONNECTED;

	ClusterState(HaMqttClient haclient, ClusterConfig.Cluster clusterConfig, MqttClientPersistence persistence,
			ScheduledExecutorService executorService) {
		this.haclient = haclient;
		this.isEnabled = clusterConfig.isEnabled();
		this.initialEndpointCount = clusterConfig.getInitialEndpointCount();
		this.primaryServerURI = clusterConfig.getPrimaryServerURI();
		this.persistence = persistence;
		this.executorService = executorService;
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
		synchronized (lock) {
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
	}

	/**
	 * Adds the specified target endpoints. New endpoints are placed in the dead
	 * endpoint list and eventually moved to the live endpoint list upon successful
	 * connections.
	 * 
	 * @param endpoints A comma-separated list of endpoint URLs. If null, then it is
	 *                  treated as empty.
	 */
	public void addEndpoints(String endpoints) {
		if (endpoints == null) {
			return;
		}
		synchronized (lock) {
			List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
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

	private void publishConnectionTestMessage(MqttClient client) throws MqttPersistenceException, MqttException {
		String message = "connection test";
		client.publish("/__padogrid/__test", message.getBytes(), 0, false);
	}

	/**
	 * Connects to all dead endpoints listed in deadClientSet, which becomes empty
	 * upon successful connections.
	 * 
	 * @return A non-empty array of user tokens.
	 */
	private IMqttToken[] connectDeadEndpoints(Set<String> deadEndpointSet) {
		int count = 0;
		StringBuffer buffer = new StringBuffer();
		ArrayList<IMqttToken> tokenList = new ArrayList<IMqttToken>(deadClientSet.size());
		synchronized (lock) {
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
//						ScheduledExecutorService executorService = this.executorService;
//						if (executorService == null) {
//							executorService = Executors.newScheduledThreadPool(allEndpointSet.size() * 3 + 1);
//							this.executorService = executorService;
//						}
						if (persistence == null) {
							// Note: the following creates MqttDefaultFilePersistence.
							client = new MqttClient(endpoint, clientId);
						} else {
							// Note: Passing null value for persistence creates MemoryPersistence.
							client = new MqttClient(endpoint, clientId, persistence, executorService);
						}
					} else {
						client = new MqttClient(endpoint, clientId, persistence, executorService);
					}
					client.setTimeToWait(DEFAULT_TIME_TO_WAIT_IN_MSEC);
					client.setCallback(new MqttCallbackImpl(client));
					IMqttToken token = client.connectWithResult(connectionOptions);
					tokenList.add(token);

					// Test connection
					publishConnectionTestMessage(client);

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
					logger.debug(String.format("Exception raised while making initial connection [%s].", endpoint), e);
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

	private IMqttToken[] connectDeadEndpoints() {

//		return connectDeadEndpoints(deadEndpointSet);

		// See if we can reduced the connection time by connecting to a few brokers for
		// the first pass.
		IMqttToken[] tokens = null;
		synchronized (lock) {
			if (isFirstConnectionAttempt) {
				Set<String> condensedDeadEndpointSet;
				int condensedMinCount = initialEndpointCount;
				if (condensedMinCount < 0) {
					condensedMinCount = deadEndpointSet.size();
				}
				List<IMqttToken> tokenList = new ArrayList<IMqttToken>(condensedMinCount + 1);
				Set<String> deadEndpointSetCopy = new HashSet<String>(deadEndpointSet);
				condensedDeadEndpointSet = Collections.synchronizedSet(new HashSet<String>(condensedMinCount, 1f));
				if (primaryClient == null && primaryServerURI != null) {
					condensedDeadEndpointSet.add(primaryServerURI);
				}
				while (tokenList.size() < condensedMinCount && deadEndpointSetCopy.size() > 0) {
					Iterator<String> iterator = deadEndpointSetCopy.iterator();
					int count = 0;
					while (count < condensedMinCount && iterator.hasNext()) {
						condensedDeadEndpointSet.add(iterator.next());
						iterator.remove();
						count++;
					}
					tokens = connectDeadEndpoints(condensedDeadEndpointSet);
					for (IMqttToken token : tokens) {
						deadEndpointSet.remove(token.getClient().getServerURI());
						tokenList.add(token);
					}
					condensedDeadEndpointSet.clear();
				}
				tokens = tokenList.toArray(new IMqttToken[0]);
				isFirstConnectionAttempt = false;
			} else {
				tokens = connectDeadEndpoints(deadEndpointSet);
			}
		}
		return tokens;
	}

	/**
	 * Revives the dead endpoints if any. The revived endpoints are promoted to the
	 * live client list.
	 */
	void reviveDeadEndpoints() {

		// Revive only if in LiVE state
		if (isEnabled == false || connectionInProgress || connectionState != ConnectionState.LIVE) {
			return;
		}

		connectionInProgress = true;

		synchronized (lock) {

			int count = 0;
			StringBuffer buffer = new StringBuffer();

			// First pass, iterate endpoints (String)
			count = connectDeadEndpoints().length;

			// Second pass - iterate dead clients (MqttClient)
			Iterator<MqttClient> iterator = deadClientSet.iterator();
			TopicInfo[] subscriptions;
			if (iterator.hasNext()) {
				subscriptions = subscribedTopics.toArray(new TopicInfo[0]);
			} else {
				subscriptions = new TopicInfo[0];
			}
			HashSet<String> failedEndpointSet = null;
			while (iterator.hasNext()) {
				MqttClient client = iterator.next();
				if (client.isConnected() == false) {
					try {
						client.connect(connectionOptions);

						// Test connection
						publishConnectionTestMessage(client);

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
						// Timed out waiting for a response from the server (32000)
						// Connect already in progress (32110)
						// The Server Disconnected the client. Disconnect RC: 130 (32204)
						switch (e.getReasonCode()) {
						case 32000:
						case 32110:
						case 32204:
						case 0: {
							try {
								client.disconnectForcibly(0, 0, false);
								client.close();
								iterator.remove();
								if (failedEndpointSet == null) {
									failedEndpointSet = new HashSet<String>(2, 1f);
								}
								failedEndpointSet.add(client.getServerURI());
							} catch (MqttException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
							break;
						default:
							break;
						}
						logger.debug(String.format("Exception raised while reviving a dead client [%s]. %s",
								client.getServerURI(), e));
					}
				}
			}

			// Third pass - in case deadEndpointSet is set
			if (failedEndpointSet != null) {
				count += connectDeadEndpoints(failedEndpointSet).length;
			}

			// Fourth pass - iterate live list and remove all disconnected clients
			iterator = liveClientSet.iterator();
			while (iterator.hasNext()) {
				MqttClient client = iterator.next();
				if (client.isConnected() == false) {
					iterator.remove();
					deadClientSet.add(client);
					count--;
				}
			}

			if (count > 0) {
				haclient.updateLiveClients(getLiveClients());
				logger.info(String.format("Revived endpoint [%s]. Live endpoints [%s]. Dead endpoints %s.",
						buffer.toString(), getLiveEndpoints(), deadEndpointSet));
				logConnectionStatus();
			}
		}
		connectionInProgress = false;
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
	 * Adds the specified endpoint.
	 * 
	 * @param serverURI Endpoint URI. If null, ignored.
	 * @throws IllegalArgumentException Thrown if the specified serverURI is not
	 *                                  valid.
	 */
	public void addEndpoint(String serverURI) throws IllegalArgumentException {
		if (serverURI == null) {
			return;
		}
		NetworkModuleService.validateURI(serverURI);
		synchronized (lock) {
			deadEndpointSet.add(serverURI);
		}
	}

	/**
	 * Removes the specified endpoint from the cluster. It disconnects and closes
	 * the corresponding MqttClient instance.
	 * 
	 * @param serverURI Server URI representing the endpoint to remove.
	 * @return true if the specified endpoint existed and is removed.
	 */
	public boolean removeEndpoint(String serverURI) {
		if (serverURI == null) {
			return false;
		}
		synchronized (lock) {
			deadEndpointSet.remove(serverURI);

			MqttClient client = null;
			Iterator<MqttClient> iterator = liveClientSet.iterator();
			while (iterator.hasNext()) {
				MqttClient c = iterator.next();
				if (c.getServerURI().equals(serverURI)) {
					iterator.remove();
					client = c;
					break;
				}
			}

			if (client == null) {
				iterator = liveClientSet.iterator();
				while (iterator.hasNext()) {
					MqttClient c = iterator.next();
					if (c.getServerURI().equals(serverURI)) {
						iterator.remove();
						client = c;
						break;
					}
				}
			}

			if (client != null) {
				try {
					if (client.isConnected()) {
						client.disconnect();
					}
					client.close();
				} catch (MqttException e) {
					// ignore
				}
			}
			return client != null;
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
		if (isEnabled) {
			switch (connectionState) {
			case DISCONNECTED:
				connectionState = ConnectionState.LIVE;
				this.connectionOptions = updateConnectionOptions(options);
				tokens = connectDeadEndpoints();
				if (tokens != null && tokens.length > 0) {
					haclient.updateLiveClients(getLiveClients());
				}
				break;
			default:
				tokens = new IMqttToken[0];
				break;
			}
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
			synchronized (lock) {
				List<MqttClient> disconnectedClientList = new ArrayList<MqttClient>(liveClientSet.size());
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
					disconnectedClientList.add(client);
				}
				deadClientSet.addAll(disconnectedClientList);
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
			synchronized (lock) {
				List<MqttClient> disconnectedClientList = new ArrayList<MqttClient>(liveClientSet.size());
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
					disconnectedClientList.add(client);
				}
				deadClientSet.addAll(disconnectedClientList);
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
			synchronized (lock) {
				List<MqttClient> disconnectedClientList = new ArrayList<MqttClient>(liveClientSet.size());
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
					disconnectedClientList.add(client);
				}
				deadClientSet.addAll(disconnectedClientList);
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
			synchronized (lock) {
				List<MqttClient> disconnectedClientList = new ArrayList<MqttClient>(liveClientSet.size());
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
					disconnectedClientList.add(client);
				}
				deadClientSet.addAll(disconnectedClientList);
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
			synchronized (lock) {
				List<MqttClient> disconnectedClientList = new ArrayList<MqttClient>(liveClientSet.size());
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
					disconnectedClientList.add(client);
				}
				deadClientSet.addAll(disconnectedClientList);
			}
			break;
		default:
			break;
		}
	}

	public void setEnabled(boolean enabled) {
		this.isEnabled = enabled;
	}

	public boolean isEnabled() {
		return isEnabled;
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
	public synchronized void close(boolean force) {
		switch (connectionState) {
		case LIVE:
		case DISCONNECTED:
			connectionState = ConnectionState.CLOSED;
			synchronized (lock) {

				// Iterate liveClientSet
				List<MqttClient> disconnectedClientList = new ArrayList<MqttClient>(liveClientSet.size());
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
					disconnectedClientList.add(client);
				}
				deadClientSet.addAll(disconnectedClientList);

				// Iterate deadClistSet
				iterator = deadClientSet.iterator();
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
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Returns true if the cluster connection is closed. A closed cluster is no
	 * longer operational and cannot be reconnected.
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
		if (client != null) {
			liveClientSet.remove(client);
			deadClientSet.add(client);
			reviveDeadEndpoints();
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

	public void addCallbackCluster(IHaMqttCallback haCallback) {
		haCallbackList.add(haCallback);
		haCallbacks = haCallbackList.toArray(new IHaMqttCallback[0]);
	}

	public void removeCallbackCluster(IHaMqttCallback haCallback) {
		haCallbackList.remove(haCallback);
		haCallbacks = haCallbackList.toArray(new IHaMqttCallback[0]);
	}

	/**
	 * Refreshes the discovery service by reconstructing the dead client list.
	 */
	public void refresh() {
		synchronized (lock) {
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

	public void setTimeToWait(long timeToWaitInMillis) throws IllegalArgumentException {
		synchronized (liveClientSet) {
			MqttClient[] clients = liveClientSet.toArray(new MqttClient[0]);
			for (MqttClient client : clients) {
				client.setTimeToWait(initialEndpointCount);
			}
		}
		synchronized (deadClientSet) {
			MqttClient[] clients = deadClientSet.toArray(new MqttClient[0]);
			for (MqttClient client : clients) {
				client.setTimeToWait(initialEndpointCount);
			}
		}
	}

	public long getTimeToWait() {
		long timeToWaitInMillis = 0;
		synchronized (liveClientSet) {
			MqttClient[] clients = liveClientSet.toArray(new MqttClient[0]);
			for (MqttClient client : clients) {
				timeToWaitInMillis = client.getTimeToWait();
				break;
			}
		}
		if (timeToWaitInMillis == 0) {
			synchronized (deadClientSet) {
				MqttClient[] clients = deadClientSet.toArray(new MqttClient[0]);
				for (MqttClient client : clients) {
					client.setTimeToWait(initialEndpointCount);
				}
			}
		}
		return timeToWaitInMillis;
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
			IHaMqttCallback[] hacb = haCallbacks;
			for (IHaMqttCallback hacallback : hacb) {
				hacallback.mqttErrorOccurred(client, exception);
			}
			logger.info(String.format("MqttCallbackImpl().mqttErrorOccurred() - MQTT error received: %s.", exception));
			logger.debug(String.format("MqttCallbackImpl().mqttErrorOccurred() - MQTT error received."), exception);
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			MqttCallback cb = callback;
			if (cb != null) {
				cb.messageArrived(topic, message);
			}
			IHaMqttCallback[] hacb = haCallbacks;
			for (IHaMqttCallback hacallback : hacb) {
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
				IHaMqttCallback[] hacb = haCallbacks;
				for (IHaMqttCallback hacallback : hacb) {
					hacallback.disconnected(client, disconnectResponse);
				}
//				removeLiveClient(client);
//				haclient.updateLiveClients(getLiveClients());
				logger.warn(String.format("MqttCallbackImpl().disconnected() - Client disconnected [%s]. %s.",
						client.getServerURI(), disconnectResponse));
				logger.debug("MqttCallbackImpl().disconnected() - Client disconnected.",
						new RuntimeException("MqttCallbackImpl().disconnected() - Client disconnected"));
				logConnectionStatus();
			}
		}

		@Override
		public void deliveryComplete(IMqttToken token) {
			MqttCallback cb = callback;
			if (cb != null) {
				cb.deliveryComplete(token);
			}
			IHaMqttCallback[] hacb = haCallbacks;
			for (IHaMqttCallback hacallback : hacb) {
				hacallback.deliveryComplete(client, token);
			}
		}

		@Override
		public void connectComplete(boolean reconnect, String serverURI) {
			MqttCallback cb = callback;
			if (cb != null) {
				cb.connectComplete(reconnect, serverURI);
			}
			IHaMqttCallback[] hacb = haCallbacks;
			for (IHaMqttCallback hacallback : hacb) {
				hacallback.connectComplete(client, reconnect, serverURI);
			}
			logger.info(String.format(
					"MqttCallbackImpl().connectComplete() - Client connected and moved to the live list [reconnect=%s, serverURI=%s].",
					reconnect, serverURI));
			logger.debug(String.format("MqttCallbackImpl().connectComplete() - client.isConnected()=%s",
					client.isConnected()));
		}

		@Override
		public void authPacketArrived(int reasonCode, MqttProperties properties) {
			MqttCallback cb = callback;
			if (cb != null) {
				cb.authPacketArrived(reasonCode, properties);
			}
			IHaMqttCallback[] hacb = haCallbacks;
			for (IHaMqttCallback hacallback : hacb) {
				hacallback.authPacketArrived(client, reasonCode, properties);
			}
		}
	}
}
