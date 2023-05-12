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
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.mqttv5.client.IMqttClient;
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
import org.mqtt.addon.client.cluster.config.ClusterConfig.Bridge;
import org.mqtt.addon.client.cluster.internal.BridgeCluster;
import org.mqtt.addon.client.cluster.internal.ConfigUtil;

/**
 * {@linkplain ClusterState} probes a collection of MQTT endpoints. The endpoint
 * string format must be compliant with the Mosquitto server URI format, e.g.,
 * tcp://localhost:1883, ssl://localhost:8883, ws://localhost:8083,
 * wss://localhost:8443.
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
	private int liveEndpointCount = -1;
	private long timeToWaitInMsec = DEFAULT_TIME_TO_WAIT_IN_MSEC;
	private boolean isEnabled = true;
	private int fos = 0;
	private int subscriberCount = -1;

	// Mutex lock to synchronize endpoint sets.
	private Object lock = new Object();

	private String primaryServerURI;
	private MqttClient primaryClient;
	private MqttClient stickySubscriber;
	private final Set<String> allEndpointSet = Collections.synchronizedSet(new HashSet<String>(5));
	private final Set<MqttClient> liveClientSet = Collections.synchronizedSet(new HashSet<MqttClient>(5));
	private final Set<MqttClient> liveSubscriptionClientSet = Collections.synchronizedSet(new HashSet<MqttClient>(5));
	private final Set<MqttClient> deadClientSet = Collections.synchronizedSet(new HashSet<MqttClient>(5));
	private final Set<String> deadEndpointSet = Collections.synchronizedSet(new HashSet<String>(5));

	private final Set<BridgeCluster> outBridgeSet = Collections.synchronizedSet(new HashSet<BridgeCluster>(5));
	private final Set<BridgeCluster> inBridgeSet = Collections.synchronizedSet(new HashSet<BridgeCluster>(5));

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
		this.liveEndpointCount = clusterConfig.getLiveEndpointCount();
		this.fos = clusterConfig.getFos();
		this.subscriberCount = clusterConfig.getSubscriberCount();
		this.timeToWaitInMsec = clusterConfig.getTimeToWait();
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

		// Set FoS dependent parameters
		switch (this.fos) {
		case 1:
			this.subscriberCount = 1;
			this.liveEndpointCount = 2;
			break;
		case 2:
			this.subscriberCount = 2;
			this.liveEndpointCount = 2;
			break;
		case 3:
			break;
		case 0:
		default:
			this.subscriberCount = -1;
			this.liveEndpointCount = -1;
			break;
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
				List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
				for (String ep : endpointList) {
					if (allEndpointSet.contains(ep) == false) {
						allEndpointSet.add(endpoint);
						deadEndpointSet.add(endpoint);
					}
				}
			}
			if (logger != null) {
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < endpoints.length; i++) {
					if (i > 0) {
						buffer.append(",");
					}
					buffer.append(endpoints[i]);
				}
				logger.info(String.format("Added/updated endpoints [endpoints=%s]. All endpoints %s.",
						buffer.toString(), getAllEndpoints()));
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

	/**
	 * Returns a comma separated list of live server URIs.
	 */
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

	/**
	 * Creates a unique client ID for the specified endpoint.
	 * 
	 * @param endpoint Endpoint, aka, serverURI.
	 */
	private String createClientId(String endpoint) {
		if (endpoint == null) {
			return clientIdPrefix;
		}
		String clientId = clientIdPrefix + "-" + endpoint.replace("/", "").replaceAll(":", "-");
		return clientId;
	}

	/**
	 * Publishes a test message on a private metadata topic managed by HaMqttClient.
	 * This method is used to "warm up" the client connections during the cluster
	 * initialization phase. Otherwise, MqttClient fails while connecting multiple
	 * instances.
	 * 
	 * @param client
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */
	private void publishConnectionTestMessage(MqttClient client) throws MqttPersistenceException, MqttException {
		String message = "connection test";
		client.publish("/__padogrid/__test", message.getBytes(), 0, false);
	}

	/**
	 * Publishes to the pub bridge clusters.
	 */
	void publishBridgeClusters(String topic, byte[] payload, int qos, boolean retained) throws MqttException {
		for (BridgeCluster bridgeCluster : outBridgeSet) {
			bridgeCluster.publish(topic, payload, qos, retained);
		}
	}

	/**
	 * Publishes to the pub bridge clusters.
	 */
	void publishBridgeClusters(String topic, MqttMessage message) throws MqttException {
		for (BridgeCluster bridgeCluster : outBridgeSet) {
			bridgeCluster.publish(topic, message);
		}
	}

	/**
	 * Connects to all dead endpoints listed in the specified deadEndpointSet, which
	 * becomes empty upon successful connections.
	 * 
	 * @return A non-empty array of user tokens.
	 */
	private IMqttToken[] connectDeadEndpoints(int maxSubscriptionCount, Set<String> deadEndpointSet) {
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
						// Paho's use of ExecutorService is extremely limited. It blocks indefinitely
						// if the application exceeds the thread pool size. Its use is discouraged.
						// Note: Passing null value for persistence creates MemoryPersistence.
						// Passing null value for executorService defaults to a non-scheduled
						// independent thread.
						client = new MqttClient(endpoint, clientId, persistence, executorService);
					} else {
						client = new MqttClient(endpoint, clientId, persistence, executorService);
					}
					client.setTimeToWait(timeToWaitInMsec);
					IMqttToken token = client.connectWithResult(connectionOptions);
					tokenList.add(token);

					// Test connection
					publishConnectionTestMessage(client);

					// Make subscriptions
					if (maxSubscriptionCount < 0 || maxSubscriptionCount > liveSubscriptionClientSet.size()) {
						client.setCallback(new MqttCallbackImpl(client));
						TopicInfo[] subscriptions = subscribedTopics.toArray(new TopicInfo[0]);
						for (TopicInfo subscription : subscriptions) {
							subscription.subscribe(client);
						}
						liveSubscriptionClientSet.add(client);
						stickySubscriber = selectStickySubscriber();
					}

					// Update live list
					liveClientSet.add(client);
					if (endpoint.equals(primaryServerURI)) {
						primaryClient = client;
					}
					iterator.remove();
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

	private MqttClient selectStickySubscriber() {
		if (liveSubscriptionClientSet.contains(stickySubscriber) == false) {
			for (MqttClient subscriber : liveSubscriptionClientSet) {
				stickySubscriber = subscriber;
				break;
			}
		}
		return stickySubscriber;
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

		// Iterate live list and remove all disconnected clients.
		// The live list normally contains only connected clients, but there is a
		// chance that some may have disconnected and did not get cleaned up
		// before entering this routine. They will get eventually cleaned up but
		// let's dot it here to be safe.
		Iterator<MqttClient> iterator = liveClientSet.iterator();
		while (iterator.hasNext()) {
			MqttClient client = iterator.next();
			if (client.isConnected() == false) {
				iterator.remove();
				liveSubscriptionClientSet.remove(client);
				deadClientSet.add(client);
			}
		}

		int count = 0;
		HashSet<String> revivedEndpointSet = new HashSet<String>(allEndpointSet.size());
		doFos(revivedEndpointSet);

		// Log revived endpoints
		if (revivedEndpointSet.size() > 0) {
			haclient.updateLiveClients(getLiveClients());
			logger.info(String.format("Revived endpoints %s. Live endpoints [%s]. Dead endpoints %s.",
					revivedEndpointSet, getLiveEndpoints(), deadEndpointSet));
			logConnectionStatus();
		}

		connectionInProgress = false;
	}

	/**
	 * Connects to dead endpoints.
	 * 
	 * @param connectionCount      Maximum number of connections to make
	 * @param maxSubscriptionCount Maximum number of subscribers
	 * @param revivedEndpointSet   Output of revived endpoint collection made by
	 *                             this method
	 * @return Connected MqttClient instance tokens
	 */
	private IMqttToken[] connectDeadEndpoints(int connectionCount, int maxSubscriptionCount,
			Set<String> revivedEndpointSet) {

		// Application can control the initial number of endpoints to connect during the
		// first probing cycle to reduce the initial connection latency.
		IMqttToken[] tokens = null;
		synchronized (lock) {
			Set<String> condensedDeadEndpointSet;
			int condensedMinCount = connectionCount;
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
				tokens = connectDeadEndpoints(maxSubscriptionCount, condensedDeadEndpointSet);
				for (IMqttToken token : tokens) {
					deadEndpointSet.remove(token.getClient().getServerURI());
					revivedEndpointSet.add(token.getClient().getServerURI());
					tokenList.add(token);
				}
				condensedDeadEndpointSet.clear();
			}
			tokens = tokenList.toArray(new IMqttToken[0]);
		}
		return tokens;
	}

	/**
	 * Revives the dead endpoints.
	 * 
	 * @param maxRevivalCount      Maximum number endpoints to revive.
	 * @param maxSubscriptionCount Maximum number of subscribers.
	 * @param revivedEndpointSet   Output collection of revived endpoints by this
	 *                             method.
	 * @return revivedEdnpointSet
	 */
	private Set<String> reviveDeadClients(int maxRevivalCount, int maxSubscriptionCount,
			Set<String> revivedEndpointSet) {

		if (maxRevivalCount <= 0 || deadClientSet.size() == 0) {
			return revivedEndpointSet;
		}

		int count = revivedEndpointSet.size();
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
					// Make connection
					client.connect(connectionOptions);

					// Test connection
					publishConnectionTestMessage(client);

					// Make subscriptions
					if (maxSubscriptionCount < 0 || maxSubscriptionCount > liveSubscriptionClientSet.size()) {
						for (TopicInfo subscription : subscriptions) {
							subscription.subscribe(client);
						}
						liveSubscriptionClientSet.add(client);
					}

					// Update live list
					liveClientSet.add(client);
					revivedEndpointSet.add(client.getServerURI());
					iterator.remove();

					count++;
					if (count == maxRevivalCount) {
						break;
					}
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
							liveClientSet.remove(client);
							liveSubscriptionClientSet.remove(client);
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

		// Handle failed endpoints
		if (failedEndpointSet != null) {
			deadEndpointSet.addAll(failedEndpointSet);
			if (count < maxRevivalCount) {
				connectDeadEndpoints(maxRevivalCount - count, maxSubscriptionCount, revivedEndpointSet);
			}
		}

		return revivedEndpointSet;
	}

	/**
	 * Revives dead endpoints based on the FoS level.
	 * 
	 * @param revivedEndpointSet Output collection of revived endpoints by this
	 *                           method.
	 * @return revivedEdnpointSet
	 */
	private Set<String> doFos(Set<String> revivedEndpointSet) {
		synchronized (lock) {
			int maxRevivalCount = liveEndpointCount - liveClientSet.size();
			int maxSubscriptionCount = subscriberCount - liveClientSet.size();
			IMqttToken[] mqttTokens = connectDeadEndpoints(maxRevivalCount, maxSubscriptionCount, revivedEndpointSet);
			if (mqttTokens != null) {
				maxRevivalCount -= mqttTokens.length;
			}
			reviveDeadClients(maxRevivalCount, maxSubscriptionCount, revivedEndpointSet);
		}
		return revivedEndpointSet;
	}

	private IMqttToken[] doFosOnEndpoints() {
		IMqttToken[] mqttTokens = null;
		synchronized (lock) {
			Set<String> revivedEndpointSet = new HashSet<String>(5);
			int maxRevivalCount = liveEndpointCount - liveClientSet.size();
			int maxSubscriptionCount = subscriberCount - liveClientSet.size();
			mqttTokens = connectDeadEndpoints(maxRevivalCount, maxSubscriptionCount, revivedEndpointSet);
		}
		if (mqttTokens == null) {
			mqttTokens = new IMqttToken[0];
		}
		return mqttTokens;
	}

	/**
	 * Logs the current connection status.
	 */
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
					liveSubscriptionClientSet.remove(client);
					client = c;
					break;
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
				addEndpoints(serverUris);
				options.setServerURIs(new String[0]);
			}
		}
		return options;
	}

	/**
	 * Connects to the cluster based on the specified options.
	 * 
	 * @param options MQTT connection options
	 * @throws MqttSecurityException
	 * @throws MqttException
	 */
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

	/**
	 * Invoked by ClusterService. This method must be invoked after creating all
	 * clusters.
	 * 
	 * @param clusterConfig Cluster config
	 */
	void buildBridgeClusters(ClusterConfig.Cluster clusterConfig) {
		buildBridgeClusterSet(clusterConfig, outBridgeSet, "outBridges");
		buildBridgeClusterSet(clusterConfig, inBridgeSet, "inBridges");

		// Add callback to forward messages to the bridge clusters.
		if (inBridgeSet.size() > 0) {
			addCallbackCluster(new BridgeCallbackImpl());
		}
	}

	/**
	 * Builds the specified bridge cluster.
	 * 
	 * @param cluster          Cluster config
	 * @param bridgeClusterSet Bridge cluster set (either this.inBridgeSet or
	 *                         this.outBridgeSet).
	 */
	private void buildBridgeClusterSet(ClusterConfig.Cluster cluster, Set<BridgeCluster> bridgeClusterSet,
			String bridgeName) {
		if (cluster.getBridges() == null) {
			return;
		}
		Bridge[] inBridges = cluster.getBridges().getIn();
		if (inBridges != null && inBridges.length > 0) {
			String clusterName = cluster.getName();
			for (Bridge bridge : inBridges) {
				// Ignore the same bridge cluster name as its own.
				String bridgeClusterName = bridge.getCluster();
				if (bridgeClusterName == null) {
					continue;
				}
				if (bridgeClusterName.equals(clusterName)) {
					logger.info(String.format(
							"Reference to the parent cluster is not allowed [%s: parent=%s, bridge=%s]. Discarded.",
							bridgeName, clusterName, bridgeClusterName));
					continue;
				}
				HaMqttClient client = HaClusters.getHaMqttClient(bridgeClusterName);
				if (client == null) {
					logger.info(String.format("Bride cluster undefined [%s: parent=%s, bridge=%s]. Discarded.",
							bridgeName, clusterName, bridgeClusterName));
				} else {
					BridgeCluster bridgeCluster = new BridgeCluster(client, bridge.getTopicFilters(), bridge.getQos());
					bridgeClusterSet.add(bridgeCluster);
				}
			}
		}
	}

	/**
	 * @see IMqttClient#connect(MqttConnectOptions)
	 */
	public IMqttToken[] connectWithResult(MqttConnectionOptions options) throws MqttSecurityException, MqttException {
		IMqttToken[] tokens = null;
		if (isEnabled) {
			switch (connectionState) {
			case DISCONNECTED:
				connectionState = ConnectionState.LIVE;
				this.connectionOptions = updateConnectionOptions(options);
				tokens = doFosOnEndpoints();
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
					liveSubscriptionClientSet.remove(client);
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
					liveSubscriptionClientSet.remove(client);
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
					liveSubscriptionClientSet.remove(client);
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
					liveSubscriptionClientSet.remove(client);
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
					liveSubscriptionClientSet.remove(client);
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
	 * Enables or disables this cluster state object. If false, then it effectively
	 * ceases all dead endpoint revival activities. Default is true.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.isEnabled = enabled;
	}

	/**
	 * Returns true if this cluster state object is enabled. If false, then this
	 * object is disabled and no revival activities take place. Default is true.
	 */
	public boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * Returns true if the current connection state is live.
	 */
	public boolean isLive() {
		return connectionState == ConnectionState.LIVE;
	}

	/**
	 * Returns true if the current connection state is disconnected.
	 */
	public boolean isDisconnected() {
		return connectionState == ConnectionState.DISCONNECTED;
	}

	/**
	 * Returns true if there is at least one live client.
	 */
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
					liveSubscriptionClientSet.remove(client);
					disconnectedClientList.add(client);
				}
				deadClientSet.addAll(disconnectedClientList);

				// Iterate deadClientSet
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
			liveSubscriptionClientSet.remove(client);
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
		synchronized (lock) {
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
		MqttClient[] clients = new MqttClient[0];
		synchronized (lock) {
			clients = liveClientSet.toArray(new MqttClient[0]);
		}
		String[] clientIds = new String[clients.length];
		for (int i = 0; i < clients.length; i++) {
			clientIds[i] = clients[i].getClientId();
		}
		return clientIds;
	}

	/**
	 * Returns disconnected client IDs.
	 * 
	 * @return An empty array if no disconnected (dead) clients exist.
	 */
	public String[] getDeadClientIds() {
		MqttClient[] clients = new MqttClient[0];
		synchronized (lock) {
			clients = deadClientSet.toArray(new MqttClient[0]);
		}
		String[] clientIds = new String[clients.length];
		for (int i = 0; i < clients.length; i++) {
			clientIds[i] = clients[i].getClientId();
		}
		return clientIds;
	}

	/**
	 * Returns all (connected and disconnected) server URIs that make up the
	 * cluster.
	 */
	public String[] getServerURIs() {
		ArrayList<String> list = new ArrayList<String>(allEndpointSet.size());
		MqttClient[] liveClients = new MqttClient[0];
		MqttClient[] deadClients = new MqttClient[0];
		synchronized (lock) {
			liveClients = liveClientSet.toArray(new MqttClient[0]);
			deadClients = deadClientSet.toArray(new MqttClient[0]);
		}
		for (MqttClient client : liveClients) {
			list.add(client.getServerURI());
		}
		for (MqttClient client : deadClients) {
			list.add(client.getServerURI());
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
		MqttClient[] clients = new MqttClient[0];
		synchronized (lock) {
			clients = liveClientSet.toArray(new MqttClient[0]);
		}
		String[] clientIds = new String[clients.length];
		for (int i = 0; i < clients.length; i++) {
			clientIds[i] = clients[i].getCurrentServerURI();
		}
		return clientIds;
	}

	/**
	 * Sets the MqttClient callback.
	 * 
	 * @param callback
	 */
	public void setCallback(MqttCallback callback) {
		this.callback = callback;
	}

	/**
	 * Adds the specified cluster callback.
	 * 
	 * @param haCallback Cluster callback
	 */
	public void addCallbackCluster(IHaMqttCallback haCallback) {
		haCallbackList.add(haCallback);
		haCallbacks = haCallbackList.toArray(new IHaMqttCallback[0]);
	}

	/**
	 * Removes the specified cluster callback.
	 * 
	 * @param haCallback Cluster callback
	 */
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

	/**
	 * Stores the specified topic filter for reinstating connection failures.
	 */
	public void subscribe(String topicFilter, int qos) {
		subscribedTopics.add(new TopicFilter(topicFilter, qos));
	}

	/**
	 * Stores the specified topic filters for reinstating connection failures.
	 */
	public void subscribe(String[] topicFilters, int[] qos) {
		subscribedTopics.add(new TopicFilters(topicFilters, qos));
	}

	/**
	 * Stores the specified subscriptions for reinstating connection failures.
	 */
	public void subscribe(MqttSubscription[] subscriptions) {
		subscribedTopics.add(new TopicSubscriptions(subscriptions));
	}

	/**
	 * Stores the specified topic filter for reinstating connection failures.
	 */
	public void subscribe(String topicFilter, int qos, IMqttMessageListener messageListener) {
		subscribedTopics.add(new TopicFilter(topicFilter, qos, messageListener));
	}

	/**
	 * Stores the specified subscriptions for reinstating connection failures.
	 */
	public void subscribe(MqttSubscription[] subscriptions, IMqttMessageListener[] messageListeners) {
		subscribedTopics.add(new TopicSubscriptions(subscriptions, messageListeners));
	}

	/**
	 * Removes the specified topic filter from the storage.
	 */
	public void unsubscribe(String topicFilter) {
		subscribedTopics.remove(new TopicFilter(topicFilter));
	}

	/**
	 * Removes the specified topic filters from the storage.
	 */
	public void unsubscribe(String[] topicFilters) {
		subscribedTopics.remove(new TopicFilters(topicFilters));
	}

	/**
	 * Sets time to wait to all of the clients including the dead clients.
	 * 
	 * @param timeToWaitInMillis Time to wait in milliseconds
	 * @throws IllegalArgumentException Thrown if timeToWaitInMillis is invalid
	 * @see MqttClient#setTimeToWait(long)
	 */
	public void setTimeToWait(long timeToWaitInMillis) throws IllegalArgumentException {
		synchronized (lock) {
			MqttClient[] clients = liveClientSet.toArray(new MqttClient[0]);
			for (MqttClient client : clients) {
				client.setTimeToWait(timeToWaitInMsec);
			}
			clients = deadClientSet.toArray(new MqttClient[0]);
			for (MqttClient client : clients) {
				client.setTimeToWait(timeToWaitInMsec);
			}
		}
	}

	/**
	 * Returns the time to wait in milliseconds. The default value is
	 * {@link IClusterConfig#DEFAULT_CLUSTER_PROBE_DELAY_IN_MSEC}.
	 * 
	 * @return
	 */
	public long getTimeToWait() {
		long timeToWaitInMillis = 0;
		synchronized (lock) {
			MqttClient[] clients = liveClientSet.toArray(new MqttClient[0]);
			for (MqttClient client : clients) {
				timeToWaitInMillis = client.getTimeToWait();
				break;
			}
			if (timeToWaitInMillis == 0) {
				clients = deadClientSet.toArray(new MqttClient[0]);
				for (MqttClient client : clients) {
					client.setTimeToWait(timeToWaitInMsec);
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

	/**
	 * TopicInfo contains subscription details for a given topic filter. It is used
	 * to revive dead endpoints.
	 * 
	 * @author dpark
	 *
	 */
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

	/**
	 * TopicInfos contains subscription details for an array of topic filters. It is
	 * used to revive dead endpoints.
	 * 
	 * @author dpark
	 *
	 */
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

	/**
	 * TopicSubscriptions contains subscription details for an array of
	 * subscriptions. It is used to revive dead endpoints.
	 * 
	 * @author dpark
	 *
	 */
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

	/**
	 * MqttCallbackImpl delivers received messages to the client callbacks.
	 * 
	 * @author dpark
	 *
	 */
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

			// Deliver messages to only sticky subscriber for FoS 0, 1, 2.
			switch (fos) {
			case 1:
			case 2:
			case 3:
				if (client != stickySubscriber) {
					return;
				}
				break;
			case 0:
			default:
				break;
			}

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

				// If sticky subscriber then pick a new one.
				if (client == stickySubscriber) {
					stickySubscriber = selectStickySubscriber();
				}

				// Notify callbacks and log.
				MqttCallback cb = callback;
				if (cb != null) {
					cb.disconnected(disconnectResponse);
				}
				IHaMqttCallback[] hacb = haCallbacks;
				for (IHaMqttCallback hacallback : hacb) {
					hacallback.disconnected(client, disconnectResponse);
				}
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

	/**
	 * BridgeCallbackImpl delivers bridged messages to the bridge clusters.
	 * 
	 * @author dpark
	 *
	 */
	class BridgeCallbackImpl implements IHaMqttCallback {

		BridgeCluster[] bridgeClusters = inBridgeSet.toArray(new BridgeCluster[0]);

		BridgeCallbackImpl() {
		}

		@Override
		public void disconnected(MqttClient client, MqttDisconnectResponse disconnectResponse) {
			// Ignore
		}

		@Override
		public void mqttErrorOccurred(MqttClient client, MqttException exception) {
			// Ignore
		}

		@Override
		public void messageArrived(MqttClient client, String topic, MqttMessage message) throws Exception {
			for (BridgeCluster bridgeCluster : bridgeClusters) {
				bridgeCluster.publish(topic, message);
			}
		}

		@Override
		public void deliveryComplete(MqttClient client, IMqttToken token) {
			// Ignore
		}

		@Override
		public void connectComplete(MqttClient client, boolean reconnect, String serverURI) {
			// Ignore
		}

		@Override
		public void authPacketArrived(MqttClient client, int reasonCode, MqttProperties properties) {
			// Ignore
		}
	}
}
