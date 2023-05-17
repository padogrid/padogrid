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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
import org.mqtt.addon.client.cluster.internal.ConfigUtil;
import org.mqtt.addon.client.cluster.internal.InBridgeCluster;
import org.mqtt.addon.client.cluster.internal.OutBridgeCluster;
import org.mqtt.addon.client.cluster.internal.TopicFilter;
import org.mqtt.addon.client.cluster.internal.TopicFilters;
import org.mqtt.addon.client.cluster.internal.TopicInfo;
import org.mqtt.addon.client.cluster.internal.TopicSubscriptions;

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
	private final String clusterName;
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
	private String defaultTopicBase;

	// Mutex lock to synchronize endpoint sets.
	private Object lock = new Object();

	private String primaryServerURI;
	private MqttClient primaryClient;
	private MqttClient stickySubscriber;

	private final List<String> allEndpointList = Collections.synchronizedList(new ArrayList<String>(10));
	// <endpointName, endpoint>
	private final Map<String, String> allEndpointMap = Collections.synchronizedMap(new HashMap<String, String>(10));
	private final Map<String, MqttClient> liveClientMap = Collections
			.synchronizedMap(new HashMap<String, MqttClient>(10));
	private final Map<String, MqttClient> deadClientMap = Collections
			.synchronizedMap(new HashMap<String, MqttClient>(10));
	private final Map<String, MqttClient> markedForDeadClientMap = Collections
			.synchronizedMap(new HashMap<String, MqttClient>(10));
	private final Map<String, String> deadEndpointMap = Collections.synchronizedMap(new HashMap<String, String>(10));

	// <endpointName, topicBase>
	private final Map<String, String> topicBaseMap = Collections.synchronizedMap(new HashMap<String, String>(5));
	// <topicBase, endpointName>
	private final Map<String, String> invertedTopicBaseMap = Collections
			.synchronizedMap(new HashMap<String, String>(5));

	private final Set<MqttClient> liveSubscriptionClientSet = Collections.synchronizedSet(new HashSet<MqttClient>(5));

	private final Set<OutBridgeCluster> outBridgeSet = Collections.synchronizedSet(new HashSet<OutBridgeCluster>(5));
	private final Set<InBridgeCluster> inBridgeSet = Collections.synchronizedSet(new HashSet<InBridgeCluster>(5));

	private Logger logger;
	private String clientIdPrefix;

	private Set<TopicInfo> subscribedTopicSet = Collections.synchronizedSet(new HashSet<TopicInfo>());
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
		this.clusterName = clusterConfig.getName();
		this.isEnabled = clusterConfig.isEnabled();
		this.initialEndpointCount = clusterConfig.getInitialEndpointCount();
		this.liveEndpointCount = clusterConfig.getLiveEndpointCount();
		this.fos = clusterConfig.getFos();
		this.subscriberCount = clusterConfig.getSubscriberCount();
		this.timeToWaitInMsec = clusterConfig.getTimeToWait();
		this.primaryServerURI = clusterConfig.getPrimaryServerURI();
		this.defaultTopicBase = clusterConfig.getDefaultTopicBase();
		if (this.defaultTopicBase != null) {
			this.defaultTopicBase = this.defaultTopicBase.trim();
			if (this.defaultTopicBase.endsWith("/") == false) {
				this.defaultTopicBase = this.defaultTopicBase + "/";
			}
		}
		this.persistence = persistence;
		this.executorService = executorService;
		this.logger = LogManager.getLogger(String.format("ClusterState[%s]", haclient.getClusterName()));
		this.clientIdPrefix = haclient.getClusterName();

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

		// Build topicBaseMap, invertedTopicBaseMap
		if (defaultTopicBase != null && defaultTopicBase.length() > 0) {
			Iterator<Map.Entry<String, String>> iterator = allEndpointMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, String> entry = iterator.next();
				String endpointName = entry.getKey();
				String topicBase = defaultTopicBase + endpointName + "/";
				topicBaseMap.put(endpointName, topicBase);
				invertedTopicBaseMap.put(topicBase, endpointName);
			}
		}
		if (clusterConfig.getEndpoints() != null) {
			for (ClusterConfig.Endpoint endpoint : clusterConfig.getEndpoints()) {
				if (endpoint.getEndpoint() != null) {
					String endpointName = endpoint.getName();
					if (allEndpointList.contains(endpoint.getEndpoint()) == false) {
						allEndpointList.add(endpoint.getEndpoint());
						if (endpointName == null) {
							endpointName = getEndpointName(allEndpointList.size());
						}
					}
					if (endpointName != null) {
						// Remove duplicate endpoints
						Iterator<Map.Entry<String, String>> iterator = allEndpointMap.entrySet().iterator();
						while (iterator.hasNext()) {
							Map.Entry<String, String> entry = iterator.next();
							if (endpoint.getEndpoint().equals(entry.getValue())) {
								iterator.remove();
								String ename = entry.getKey();
								deadEndpointMap.remove(ename);
								String ep = topicBaseMap.remove(ename);
								invertedTopicBaseMap.remove(ep);
							}
						}
						allEndpointMap.put(endpointName, endpoint.getEndpoint());
						deadEndpointMap.put(endpointName, endpoint.getEndpoint());
						String topicBase = endpoint.getTopicBase();
						if (topicBase == null) {
							topicBase = defaultTopicBase + endpointName + "/";
							topicBaseMap.put(endpointName, topicBase);
							invertedTopicBaseMap.put(topicBase, endpointName);
						} else {
							if (topicBase.endsWith("/") == false) {
								topicBase = topicBase + "/";
							}
							topicBaseMap.put(endpointName, topicBase);
							invertedTopicBaseMap.put(topicBase, endpointName);
						}
					}
				}

			}
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
				List<String> endpointList = ConfigUtil.parseEndpoints(endpoint);
				int index = allEndpointList.size();
				for (String ep : endpointList) {
					if (allEndpointList.contains(ep) == false) {
						String endpointName = getEndpointName(++index);
						allEndpointList.add(ep);
						allEndpointMap.put(endpointName, ep);
						deadEndpointMap.put(endpointName, ep);
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
				logger.info(
						String.format("Added/updated endpoints [endpoints=%s]. All endpoints %s. Dead endpoints [%s]",
								buffer.toString(), getAllEndpoints(), getDeadEndpoints()));
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
			int index = allEndpointList.size();
			for (String endpoint : endpointList) {
				if (allEndpointList.contains(endpoint) == false) {
					String endpointName = getEndpointName(++index);
					allEndpointList.add(endpoint);
					deadEndpointMap.put(endpointName, endpoint);
				}
			}
			if (logger != null) {
				logger.info(String.format(
						"Added/updated endpoints [endpoints=%s]. All endpoints [%s]. Dead endpoints [%s].", endpoints,
						getAllEndpoints(), getDeadEndpoints()));
			}
		}
	}

	/**
	 * Returns a comma separated, sorted list of live server URIs.
	 */
	private String getLiveEndpoints() {
		ArrayList<String> endpointList = new ArrayList<String>(liveClientMap.size());
		for (MqttClient client : liveClientMap.values()) {
			endpointList.add(client.getServerURI());
		}
		Collections.sort(endpointList);
		return endpointList.toString();
	}

	/**
	 * Returns the dead endpoint count.
	 */
	private int getDeadEndpointCount() {
		return deadClientMap.size() + deadEndpointMap.size() + markedForDeadClientMap.size();
	}

	/**
	 * Returns a comma separated, sorted list of dead server URIs.
	 */
	private String getDeadEndpoints() {
		ArrayList<String> endpointList = new ArrayList<String>(getDeadEndpointCount());
		for (MqttClient client : deadClientMap.values()) {
			endpointList.add(client.getServerURI());
		}
		for (String endpoint : deadEndpointMap.values()) {
			endpointList.add(endpoint);
		}
		for (MqttClient client : markedForDeadClientMap.values()) {
			endpointList.add(client.getServerURI());
		}
		Collections.sort(endpointList);
		return endpointList.toString();
	}

	/**
	 * Returns a unique client ID for the specified endpoint.
	 * 
	 * @param endpoint Endpoint, aka, serverURI.
	 */
	private String getClientId(String endpoint) {
		if (endpoint == null) {
			return clientIdPrefix;
		}
		String clientId = clientIdPrefix + "-" + endpoint.replace("/", "").replaceAll(":", "-");
		return clientId;
	}

	/**
	 * Returns the endpoint name for the specified endpoint number;
	 * 
	 * @param index Endpoint number
	 */
	private String getEndpointName(int endpointNumber) {
		return clusterName + "-" + endpointNumber;
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
	private String publishConnectionTestMessage(MqttClient client) throws MqttPersistenceException, MqttException {
		String message = "connection test " + client.getCurrentServerURI();
		client.publish("__padogrid/__test", message.getBytes(), 0, false);
		return message;
	}

	/**
	 * Publishes to the outgoing bridge clusters.
	 */
	void publishBridgeClusters(String topic, byte[] payload, int qos, boolean retained) throws MqttException {
		for (OutBridgeCluster bridgeCluster : outBridgeSet) {
			bridgeCluster.publish(topic, payload, qos, retained);
		}
	}

	/**
	 * Publishes to the outgoing bridge clusters.
	 */
	void publishBridgeClusters(String topic, MqttMessage message) throws MqttException {
		for (OutBridgeCluster bridgeCluster : outBridgeSet) {
			bridgeCluster.publish(topic, message);
		}
	}

	/**
	 * Connects to all dead endpoints listed in the specified deadEndpointSet, which
	 * becomes empty upon successful connections.
	 * 
	 * @return A non-empty array of user tokens.
	 */
	private IMqttToken[] connectDeadEndpoints(int maxSubscriptionCount, Map<String, String> deadEndpointMap) {
		ArrayList<IMqttToken> tokenList = new ArrayList<IMqttToken>(deadClientMap.size());
		synchronized (lock) {
			Iterator<Map.Entry<String, String>> iterator = deadEndpointMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, String> entry = iterator.next();
				String endpointName = entry.getKey();
				String endpoint = entry.getValue();
				MqttClient client = null;
				try {
					String clientId = getClientId(endpoint);

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
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("connectDeadEndpoints() - connecting [%s]...", endpoint));
					}
					IMqttToken token = client.connectWithResult(connectionOptions);
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("connectDeadEndpoints() - connected [%s]", endpoint));
					}
					tokenList.add(token);

					// Test connection
					String message = publishConnectionTestMessage(client);
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("connectDeadEndpoints() - published a test message [%s]", message));
					}

					// Make subscriptions
					if (maxSubscriptionCount < 0 || maxSubscriptionCount > liveSubscriptionClientSet.size()) {
						client.setCallback(new MqttCallbackImpl(endpointName, client));
						TopicInfo[] subscriptions = subscribedTopicSet.toArray(new TopicInfo[0]);
						for (TopicInfo subscription : subscriptions) {
							subscription.subscribe(client);
						}
						liveSubscriptionClientSet.add(client);
						stickySubscriber = selectStickySubscriber();
					}

					// Update live list
					liveClientMap.put(endpointName, client);
					if (endpoint.equals(primaryServerURI)) {
						primaryClient = client;
					}
					iterator.remove();
				} catch (MqttException e) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("Exception raised while making initial connection [%s].", endpoint),
								e);
					}
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
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("connectDeadEndpoints() - complete"));
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
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Probing [%s]. [All: %d, Live: %d, Dead: %d]. Dead endpoints %s...", clusterName,
					allEndpointMap.size(), liveClientMap.size(), getDeadEndpointCount(), getDeadEndpoints()));
		}

		// Iterate live list and remove all disconnected clients.
		// The live list normally contains only connected clients, but there is a
		// chance that some may have disconnected and did not get cleaned up
		// before entering this routine. They will get eventually cleaned up but
		// let's dot it here to be safe.
		Iterator<Map.Entry<String, MqttClient>> iterator = liveClientMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, MqttClient> entry = iterator.next();
			String entrypointName = entry.getKey();
			MqttClient client = entry.getValue();
			if (client.isConnected() == false) {
				iterator.remove();
				liveSubscriptionClientSet.remove(client);
				deadClientMap.remove(entrypointName);
			}
		}

		// Revive endpoints based on FoS
		// Use sorted set for logging purpose
		TreeSet<String> revivedEndpointSet = new TreeSet<String>();
		doFos(revivedEndpointSet);

		// Log revived endpoints
		if (revivedEndpointSet.size() > 0) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("reviveDeadEndpoints() - updating HaMqttClient.updateLiveClients() [Live: %d]...",
						liveClientMap.size()));
			}
			haclient.updateLiveClients(getLiveClientMap(), getDefaultTopicBase(), getTopicBaseMap());
			logger.info(String.format("Revived endpoints %s. Live endpoints %s. Dead endpoints %s.", revivedEndpointSet,
					getLiveEndpoints(), getDeadEndpoints()));
			logConnectionStatus();
		}
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Probed [%s]. [All: %d, Live: %d, Dead: %d]. Dead endpoints %s...", clusterName,
					allEndpointMap.size(), liveClientMap.size(), getDeadEndpointCount(), getDeadEndpoints()));
		}
		connectionInProgress = false;
	}

	/**
	 * Connects to dead endpoints.
	 * 
	 * @param connectionCount      Maximum number of connections to make. -1 for all
	 *                             endpoints.
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
			Map<String, String> condensedDeadEndpointMap;
			int condensedMinCount = connectionCount;
			if (condensedMinCount < 0) {
				condensedMinCount = deadEndpointMap.size();
			}
			List<IMqttToken> tokenList = new ArrayList<IMqttToken>(condensedMinCount + 1);
			Map<String, String> deadEndpointMapCopy = new HashMap<String, String>(deadEndpointMap);
			condensedDeadEndpointMap = Collections.synchronizedMap(new HashMap<String, String>(condensedMinCount, 1f));
			int count = 0;
			if (primaryClient == null && primaryServerURI != null) {
				// Find the primary server URI
				Iterator<Map.Entry<String, String>> iterator = deadEndpointMapCopy.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, String> entry = iterator.next();
					String serverURI = entry.getValue();
					if (serverURI.equals(primaryServerURI)) {
						condensedDeadEndpointMap.put(entry.getKey(), primaryServerURI);
						iterator.remove();
						count++;
						break;
					}
				}
			}
			while (tokenList.size() < condensedMinCount && deadEndpointMapCopy.size() > 0) {
				Iterator<Map.Entry<String, String>> iterator = deadEndpointMapCopy.entrySet().iterator();
				while (count < condensedMinCount && iterator.hasNext()) {
					Map.Entry<String, String> entry = iterator.next();
					condensedDeadEndpointMap.put(entry.getKey(), entry.getValue());
					iterator.remove();
					count++;
				}
//				if (logger.isDebugEnabled()) {
//					logger.debug(String.format(
//							"connectDeadEndpoints() - invoking connectDeadEndpoints() [maxSubscriptionCount=%d, condensedDeadEndpointMap.size()=%d]",
//							maxSubscriptionCount, condensedDeadEndpointMap.size()));
//				}
				tokens = connectDeadEndpoints(maxSubscriptionCount, condensedDeadEndpointMap);
//				if (logger.isDebugEnabled()) {
//					logger.debug(String.format(
//							"connectDeadEndpoints() - invoked connectDeadEndpoints() [maxSubscriptionCount=%d, condensedDeadEndpointMap.size()=%d]",
//							maxSubscriptionCount, condensedDeadEndpointMap.size()));
//				}
				for (IMqttToken token : tokens) {
					Iterator<Map.Entry<String, String>> iterator2 = deadEndpointMap.entrySet().iterator();
					while (iterator2.hasNext()) {
						Map.Entry<String, String> entry = iterator2.next();
						String endpoint = entry.getValue();
						if (endpoint.equals(token.getClient().getServerURI())) {
							iterator2.remove();
							revivedEndpointSet.add(endpoint);
							break;
						}
					}
					tokenList.add(token);
				}
				condensedDeadEndpointMap.clear();
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

		if (maxRevivalCount < 0) {
			maxRevivalCount = deadClientMap.size();
		}
		if (maxRevivalCount <= 0) {
			return revivedEndpointSet;
		}

		int count = revivedEndpointSet.size();
		Iterator<Map.Entry<String, MqttClient>> iterator = deadClientMap.entrySet().iterator();
		TopicInfo[] subscriptions;
		if (iterator.hasNext()) {
			subscriptions = subscribedTopicSet.toArray(new TopicInfo[0]);
		} else {
			subscriptions = new TopicInfo[0];
		}
		HashMap<String, String> failedEndpointMap = null;
		while (iterator.hasNext()) {
			Map.Entry<String, MqttClient> entry = iterator.next();
			String endpointName = entry.getKey();
			MqttClient client = entry.getValue();
			if (client.isConnected()) {
				try {
					client.disconnectForcibly(0, 0, false);
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
			if (client.isConnected() == false) {
				try {
					// Make connection
					// TODO: The following blocks! Need to move previously connected clients
					// to the endpoint list.
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
					liveClientMap.put(endpointName, client);

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
						logger.warn(String.format(
								"Broker disconnected a client: [endpoint=%s, exception=%s]. Closing connection and moving client to the dead endpoint list...",
								client.getServerURI(), e));
					}
						break;
					default:
						break;
					}
					try {
						iterator.remove();
						if (failedEndpointMap == null) {
							failedEndpointMap = new HashMap<String, String>(5);
						}
						failedEndpointMap.put(endpointName, client.getServerURI());
						liveClientMap.remove(endpointName);
						liveSubscriptionClientSet.remove(client);
						client.disconnectForcibly(0, 0, false);
						client.close();
					} catch (MqttException e1) {
						// ignore
					}
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("Exception raised while reviving a dead client [%s]. %s",
								client.getServerURI(), e));
					}
				}
			}
		}

		// Handle failed endpoints
		if (failedEndpointMap != null) {
			deadEndpointMap.putAll(failedEndpointMap);
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
			int maxRevivalCount = liveEndpointCount - liveClientMap.size();
			int maxSubscriptionCount = subscriberCount - liveClientMap.size();

			// Handle initalEndpointCount. This occurs only once during initialization.
			// TODO: see if we can speed up the connection process by introducing threads
			if (isFirstConnectionAttempt) {
				if (initialEndpointCount >= 0) {
					if (maxRevivalCount < 0 || initialEndpointCount < maxRevivalCount) {
						maxRevivalCount = initialEndpointCount;
					}
				}
				isFirstConnectionAttempt = false;
			}

			// Move marked clients to the dead endpoint list for revival
			Iterator<Map.Entry<String, MqttClient>> iterator2 = markedForDeadClientMap.entrySet().iterator();
			while (iterator2.hasNext()) {
				Map.Entry<String, MqttClient> entry = iterator2.next();
				String endpointName = entry.getKey();
				MqttClient client = entry.getValue();
				if (client.isConnected()) {
					try {
						client.disconnect();
					} catch (MqttException e) {
						// ignore
					}
				}
				try {
					client.close();
				} catch (MqttException e) {
					// ignore
				}
				liveClientMap.remove(endpointName);
				deadEndpointMap.put(endpointName, client.getServerURI());
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Moved marked client to dead endpoint list [%s]. Dead endpoints %s",
							client.getServerURI(), getDeadEndpoints()));
				}
				liveSubscriptionClientSet.remove(client);
				iterator2.remove();
			}

			// Connect dead endpoints which are usually the ones being connected for the
			// first time.
			IMqttToken[] mqttTokens = connectDeadEndpoints(maxRevivalCount, maxSubscriptionCount, revivedEndpointSet);
			if (mqttTokens != null) {
				maxRevivalCount -= mqttTokens.length;
			}

			// Revive the dead clients which are the ones that were connected once but
			// failed after.
//			if (logger.isDebugEnabled()) {
//				logger.debug(String.format(
//						"doFos() - invoking reviveDeadClients() [maxRevivalCount=%d, maxSubscriptionCount=%d, revivedEndpointSet.size()=%d]",
//						maxRevivalCount, maxSubscriptionCount, revivedEndpointSet.size()));
//			}
			reviveDeadClients(maxRevivalCount, maxSubscriptionCount, revivedEndpointSet);
//			if (logger.isDebugEnabled()) {
//				logger.debug(String.format(
//						"doFos() - invoked reviveDeadClients() [maxRevivalCount=%d, maxSubscriptionCount=%d, revivedEndpointSet.size()=%d]",
//						maxRevivalCount, maxSubscriptionCount, revivedEndpointSet.size()));
//			}
		}
		return revivedEndpointSet;
	}

	private IMqttToken[] doFosOnEndpoints() {
		IMqttToken[] mqttTokens = null;
		synchronized (lock) {
			Set<String> revivedEndpointSet = new HashSet<String>(5);
			int maxRevivalCount = liveEndpointCount - liveClientMap.size();
			int maxSubscriptionCount = subscriberCount - liveClientMap.size();
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
		if (liveClientMap.size() == allEndpointMap.size()) {
			logger.info(String.format("All endpoints connected [Connected: %d].", liveClientMap.size()));
		} else {
			logger.info(String.format("[Connected: %d, Not Connected: %d]", liveClientMap.size(),
					getDeadEndpointCount()));
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
			if (allEndpointList.contains(serverURI) == false) {
				int index = allEndpointList.size();
				String endpointName = getEndpointName(++index);
				deadEndpointMap.put(endpointName, serverURI);
			}
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
			Iterator<Map.Entry<String, String>> iterator2 = deadEndpointMap.entrySet().iterator();
			while (iterator2.hasNext()) {
				Map.Entry<String, String> entry = iterator2.next();
				String endpoint = entry.getValue();
				if (endpoint.equals(serverURI)) {
					iterator2.remove();
				}
			}

			MqttClient client = null;
			Iterator<Map.Entry<String, MqttClient>> iterator = liveClientMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, MqttClient> entry = iterator.next();
				MqttClient c = entry.getValue();
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
		return Collections.unmodifiableSet(new HashSet<String>(allEndpointMap.values()));
	}

	/**
	 * Returns an unmodifiable set containing live MqttClient instances.
	 */
	public Set<MqttClient> getLiveClients() {
		return Collections.unmodifiableSet(new HashSet<MqttClient>(liveClientMap.values()));
	}

	/**
	 * Returns an unmodifiable map containing live (endpointName, MqttClient)
	 * entries
	 */
	public Map<String, MqttClient> getLiveClientMap() {
		return Collections.unmodifiableMap(new HashMap<String, MqttClient>(liveClientMap));
	}

	/**
	 * Returns the default topic base aka publisherTopicBase
	 */
	public String getDefaultTopicBase() {
		return this.defaultTopicBase;
	}

	/**
	 * Returns an unmodifiable topic base map containing (endpointName, topicBase)
	 * entries
	 * 
	 * @return
	 */
	public Map<String, String> getTopicBaseMap() {
		return Collections.unmodifiableMap(new HashMap<String, String>(topicBaseMap));
	}

	/**
	 * Returns an unmodifiable set containing dead MqttClient instances.
	 */
	public Set<MqttClient> getDeadClients() {
		return Collections.unmodifiableSet(new HashSet<MqttClient>(deadClientMap.values()));
	}

	/**
	 * Returns an unmodifiable set containing dead endpoints.
	 */
	public Set<String> getDeadAddressSet() {
		return Collections.unmodifiableSet(new HashSet<String>(deadEndpointMap.values()));
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

//	public void reopen() {
//		switch (connectionState) {
//		case CLOSED:
//			connectionState = ConnectionState.LIVE;
//			// TODO: Need a way to rebuild deadEndpointMap
//			reviveDeadEndpoints();
//			break;
//		default:
//			break;
//		}
//	}

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
				allEndpointList.clear();
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
		synchronized (lock) {
			buildInBridgeClusterSet(clusterConfig);
			buildOutBridgeClusterSet(clusterConfig);

			// Add callback to forward messages to the bridge clusters.
			if (inBridgeSet.size() > 0) {
				addCallbackCluster(new InBridgeCallbackImpl());

				// Add incoming BridgeCluster subscriptions
				subscribedTopicSet.addAll(inBridgeSet);

				// Make subscriptions
				TopicInfo[] subscriptions = inBridgeSet.toArray(new TopicInfo[0]);
				MqttClient[] clients = liveClientMap.values().toArray(new MqttClient[0]);

				Iterator<Map.Entry<String, MqttClient>> iterator = liveClientMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, MqttClient> entry = iterator.next();
					String endpointName = entry.getKey();
					MqttClient client = entry.getValue();
					for (TopicInfo subscription : subscriptions) {
						try {
							subscription.subscribe(client);
						} catch (MqttException e) {
							// assume communication error
							// Revive it during the next probe cycle
							markClientForRevival(endpointName, client);
						}
					}
				}
			}
		}
	}

	/**
	 * Builds the specified incoming bridge cluster.
	 * 
	 * @param cluster          Cluster config
	 * @param bridgeClusterSet Bridge cluster set (either this.inBridgeSet or
	 *                         this.outBridgeSet).
	 * @return bridgeClusterSet
	 * @throws MqttException
	 */
	private void buildInBridgeClusterSet(ClusterConfig.Cluster cluster) {
		if (cluster.getBridges() == null) {
			return;
		}
		Bridge[] bridges;
		String bridgeName = "inBridges";
		bridges = cluster.getBridges().getIn();

		if (bridges != null && bridges.length > 0) {
			String clusterName = cluster.getName();
			for (Bridge bridge : bridges) {
				// Ignore the same bridge cluster name as its own.
				String bridgeClusterName = bridge.getCluster();
				if (bridgeClusterName == null) {
					continue;
				}
				// A reference to the parent cluster is not allowed. This is to prevent looping.
				if (bridgeClusterName.equals(clusterName)) {
					logger.info(String.format(
							"Reference to the parent cluster is not allowed [%s: parent=%s, bridge=%s]. Discarded.",
							bridgeName, clusterName, bridgeClusterName));
					continue;
				}
				HaMqttClient client = HaClusters.getHaMqttClient(bridgeClusterName);
				if (client == null) {
					logger.info(String.format("Bridge cluster undefined [%s: parent=%s, bridge=%s]. Discarded.",
							bridgeName, clusterName, bridgeClusterName));
				} else {
					InBridgeCluster bridgeCluster = new InBridgeCluster(client, bridge.getTopicFilters(),
							bridge.getQos());
					inBridgeSet.add(bridgeCluster);
				}
			}
		}
	}

	/**
	 * Builds the specified outgoing bridge cluster.
	 * 
	 * @param cluster          Cluster config
	 * @param bridgeClusterSet Bridge cluster set (either this.inBridgeSet or
	 *                         this.outBridgeSet).
	 * @return bridgeClusterSet
	 * @throws MqttException
	 */
	private void buildOutBridgeClusterSet(ClusterConfig.Cluster cluster) {
		if (cluster.getBridges() == null) {
			return;
		}
		Bridge[] bridges;
		String bridgeName = "outBridges";
		bridges = cluster.getBridges().getOut();
		if (bridges != null && bridges.length > 0) {
			String clusterName = cluster.getName();
			for (Bridge bridge : bridges) {
				// Ignore the same bridge cluster name as its own.
				String bridgeClusterName = bridge.getCluster();
				if (bridgeClusterName == null) {
					continue;
				}
				// A reference to the parent cluster is not allowed. This is to prevent looping.
				if (bridgeClusterName.equals(clusterName)) {
					logger.info(String.format(
							"Reference to the parent cluster is not allowed [%s: parent=%s, bridge=%s]. Discarded.",
							bridgeName, clusterName, bridgeClusterName));
					continue;
				}
				HaMqttClient client = HaClusters.getHaMqttClient(bridgeClusterName);
				if (client == null) {
					logger.info(String.format("Bridge cluster undefined [%s: parent=%s, bridge=%s]. Discarded.",
							bridgeName, clusterName, bridgeClusterName));
				} else {
					OutBridgeCluster bridgeCluster = new OutBridgeCluster(client, bridge.getTopicFilters(),
							bridge.getQos());
					outBridgeSet.add(bridgeCluster);
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
					haclient.updateLiveClients(getLiveClientMap(), getDefaultTopicBase(), getTopicBaseMap());
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
				HashMap<String, MqttClient> disconnectedClientMap = new HashMap<String, MqttClient>(
						liveClientMap.size());
				Iterator<Map.Entry<String, MqttClient>> iterator = liveClientMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, MqttClient> entry = iterator.next();
					String endpointName = entry.getKey();
					MqttClient client = entry.getValue();
					if (client.isConnected()) {
						try {
							client.disconnect();
						} catch (MqttException e) {
							if (logger.isDebugEnabled()) {
								logger.debug("Exception raised while gracefully disconnecting live client [%s]. %s",
										client.getServerURI(), e.getMessage());
							}
						}
					}
					iterator.remove();
					liveSubscriptionClientSet.remove(client);
					disconnectedClientMap.put(endpointName, client);
				}
				deadClientMap.putAll(disconnectedClientMap);
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
				HashMap<String, MqttClient> disconnectedClientMap = new HashMap<String, MqttClient>(
						liveClientMap.size());
				Iterator<Map.Entry<String, MqttClient>> iterator = liveClientMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, MqttClient> entry = iterator.next();
					String endpointName = entry.getKey();
					MqttClient client = entry.getValue();
					if (client.isConnected()) {
						try {
							client.disconnect(quiesceTimeout);
						} catch (MqttException e) {
							if (logger.isDebugEnabled()) {
								logger.debug("Exception raised while gracefully disconnecting live client [%s]. %s",
										client.getServerURI(), e.getMessage());
							}
						}
					}
					iterator.remove();
					liveSubscriptionClientSet.remove(client);
					disconnectedClientMap.put(endpointName, client);
				}
				deadClientMap.putAll(disconnectedClientMap);
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
				HashMap<String, MqttClient> disconnectedClientMap = new HashMap<String, MqttClient>(
						liveClientMap.size());
				Iterator<Map.Entry<String, MqttClient>> iterator = liveClientMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, MqttClient> entry = iterator.next();
					String endpointName = entry.getKey();
					MqttClient client = entry.getValue();
					if (client.isConnected()) {
						try {
							client.disconnectForcibly();
						} catch (MqttException e) {
							if (logger.isDebugEnabled()) {
								logger.debug("Exception raised while forcibly disconnecting live client [%s]. %s",
										client.getServerURI(), e.getMessage());
							}
						}
					}
					iterator.remove();
					liveSubscriptionClientSet.remove(client);
					disconnectedClientMap.put(endpointName, client);
				}
				deadClientMap.putAll(disconnectedClientMap);
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
				HashMap<String, MqttClient> disconnectedClientMap = new HashMap<String, MqttClient>(
						liveClientMap.size());
				Iterator<Map.Entry<String, MqttClient>> iterator = liveClientMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, MqttClient> entry = iterator.next();
					String endpointName = entry.getKey();
					MqttClient client = entry.getValue();
					if (client.isConnected()) {
						try {
							client.disconnectForcibly(disconnectTimeout);
						} catch (MqttException e) {
							if (logger.isDebugEnabled()) {
								logger.debug("Exception raised while forcibly disconnecting live client [%s]. %s",
										client.getServerURI(), e.getMessage());
							}
						}
					}
					iterator.remove();
					liveSubscriptionClientSet.remove(client);
					disconnectedClientMap.put(endpointName, client);
				}
				deadClientMap.putAll(disconnectedClientMap);
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
				HashMap<String, MqttClient> disconnectedClientMap = new HashMap<String, MqttClient>(
						liveClientMap.size());
				Iterator<Map.Entry<String, MqttClient>> iterator = liveClientMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, MqttClient> entry = iterator.next();
					String endpointName = entry.getKey();
					MqttClient client = entry.getValue();
					if (client.isConnected()) {
						try {
							client.disconnectForcibly(quiesceTimeout, disconnectTimeout);
						} catch (MqttException e) {
							if (logger.isDebugEnabled()) {
								logger.debug("Exception raised while forcibly disconnecting live client [%s]. %s",
										client.getServerURI(), e.getMessage());
							}
						}
					}
					iterator.remove();
					liveSubscriptionClientSet.remove(client);
					disconnectedClientMap.put(endpointName, client);
				}
				deadClientMap.putAll(disconnectedClientMap);
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
		return liveClientMap.size() > 0;
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

				// Iterate liveClientSet. Disconnect all live clients and move them to
				// deadClientMap
				HashMap<String, MqttClient> disconnectedClientMap = new HashMap<String, MqttClient>(
						liveClientMap.size());
				Iterator<Map.Entry<String, MqttClient>> iterator = liveClientMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, MqttClient> entry = iterator.next();
					String endpointName = entry.getKey();
					MqttClient client = entry.getValue();
					if (client.isConnected()) {
						try {
							client.disconnect();
						} catch (MqttException e) {
							if (logger.isDebugEnabled()) {
								logger.debug(
										String.format("Exception raised while gracefully disconnecting live client %s",
												e.getMessage()));
							}
						}
					}
					try {
						client.close(force);
					} catch (MqttException e) {
						if (logger.isDebugEnabled()) {
							logger.debug(String.format("Exception raised while closing disconnected client %s",
									e.getMessage()));
						}
					}
					iterator.remove();
					liveSubscriptionClientSet.remove(client);
					disconnectedClientMap.put(endpointName, client);
				}
				deadClientMap.putAll(disconnectedClientMap);

				// Iterate deadClientMap. Close all clients.
				iterator = deadClientMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, MqttClient> entry = iterator.next();
					MqttClient client = entry.getValue();
					if (client.isConnected()) {
						try {
							client.disconnect();
						} catch (MqttException e) {
							if (logger.isDebugEnabled()) {
								logger.debug(
										String.format("Exception raised while gracefully disconnecting dead client %s",
												e.getMessage()));
							}
						}
					}
					try {
						client.close(force);
					} catch (MqttException e) {
						if (logger.isDebugEnabled()) {
							logger.debug(
									String.format("Exception raised while closing dead client %s", e.getMessage()));
						}
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
	 * Marks the specified client for revival in the next probe cycle.
	 * 
	 * @param endpointName Endpoint name
	 * @param client       Client to revive
	 * @see #doFos(Set)
	 */
	public void markClientForRevival(String endpointName, MqttClient client) {
		if (endpointName != null && client != null) {
			markedForDeadClientMap.put(endpointName, client);
		}
	}

	/**
	 * Returns the first live MqttClient instance in the live list. It returns null
	 * if there are no live clients.
	 */
	public MqttClient getLiveClient() {
		MqttClient client = null;
		synchronized (lock) {
			Iterator<Map.Entry<String, MqttClient>> iterator = liveClientMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, MqttClient> entry = iterator.next();
				client = entry.getValue();
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
			clients = liveClientMap.values().toArray(new MqttClient[0]);
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
			clients = deadClientMap.values().toArray(new MqttClient[0]);
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
		ArrayList<String> list = new ArrayList<String>(allEndpointMap.size());
		MqttClient[] liveClients = new MqttClient[0];
		MqttClient[] deadClients = new MqttClient[0];
		synchronized (lock) {
			liveClients = liveClientMap.values().toArray(new MqttClient[0]);
			deadClients = deadClientMap.values().toArray(new MqttClient[0]);
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
			clients = liveClientMap.values().toArray(new MqttClient[0]);
		}
		String[] serverURIs = new String[clients.length];
		for (int i = 0; i < clients.length; i++) {
			serverURIs[i] = clients[i].getCurrentServerURI();
		}
		return serverURIs;
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
			deadEndpointMap.clear();
			for (Map.Entry<String, String> entry : allEndpointMap.entrySet()) {
				String endpointName = entry.getKey();
				String endpoint = entry.getValue();
				boolean found = false;
				for (MqttClient client : liveClientMap.values()) {
					if (endpoint.equals(client.getServerURI())) {
						found = true;
						break;
					}
				}
				if (found == false) {
					deadEndpointMap.put(endpointName, endpoint);
				}
			}
		}
	}

	/**
	 * Stores the specified topic filter for reinstating connection failures.
	 */
	public void subscribe(OutBridgeCluster bridgeCluster) {
		subscribedTopicSet.add(bridgeCluster);
	}

	/**
	 * Stores the specified topic filter for reinstating connection failures.
	 */
	public void subscribe(String topicFilter, int qos) {
		subscribedTopicSet.add(new TopicFilter(topicFilter, qos));
	}

	/**
	 * Stores the specified topic filters for reinstating connection failures.
	 */
	public void subscribe(String[] topicFilters, int[] qos) {
		subscribedTopicSet.add(new TopicFilters(topicFilters, qos));
	}

	/**
	 * Stores the specified subscriptions for reinstating connection failures.
	 */
	public void subscribe(MqttSubscription[] subscriptions) {
		subscribedTopicSet.add(new TopicSubscriptions(subscriptions));
	}

	/**
	 * Stores the specified topic filter for reinstating connection failures.
	 */
	public void subscribe(String topicFilter, int qos, IMqttMessageListener messageListener) {
		subscribedTopicSet.add(new TopicFilter(topicFilter, qos, messageListener));
	}

	/**
	 * Stores the specified subscriptions for reinstating connection failures.
	 */
	public void subscribe(MqttSubscription[] subscriptions, IMqttMessageListener[] messageListeners) {
		subscribedTopicSet.add(new TopicSubscriptions(subscriptions, messageListeners));
	}

	/**
	 * Removes the specified topic filter from the storage.
	 */
	public void unsubscribe(String topicFilter) {
		subscribedTopicSet.remove(new TopicFilter(topicFilter));
	}

	/**
	 * Removes the specified topic filters from the storage.
	 */
	public void unsubscribe(String[] topicFilters) {
		subscribedTopicSet.remove(new TopicFilters(topicFilters));
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
			MqttClient[] clients = liveClientMap.values().toArray(new MqttClient[0]);
			for (MqttClient client : clients) {
				client.setTimeToWait(timeToWaitInMsec);
			}
			clients = deadClientMap.values().toArray(new MqttClient[0]);
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
			MqttClient[] clients = liveClientMap.values().toArray(new MqttClient[0]);
			for (MqttClient client : clients) {
				timeToWaitInMillis = client.getTimeToWait();
				break;
			}
			if (timeToWaitInMillis == 0) {
				clients = deadClientMap.values().toArray(new MqttClient[0]);
				for (MqttClient client : clients) {
					client.setTimeToWait(timeToWaitInMsec);
				}
			}
		}
		return timeToWaitInMillis;
	}

	/**
	 * MqttCallbackImpl delivers received messages to the client callbacks.
	 * 
	 * @author dpark
	 *
	 */
	class MqttCallbackImpl implements MqttCallback {

		private String endpointName;
		private MqttClient client;

		MqttCallbackImpl(String endpointName, MqttClient client) {
			this.endpointName = endpointName;
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
			logger.warn(String.format("MqttCallbackImpl().mqttErrorOccurred() - MQTT error received: %s.", exception));
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
				// Move client from the live list to the dead list
				synchronized (lock) {
					// Mosquitto broker may have disconnected with a severe error
					// such as a protocol error. Once disconnected by the broke, it
					// is seen that the client is unable to reconnect. Mark the
					// client for revival.
					ClusterState.this.markClientForRevival(endpointName, client);
				}
				logger.warn(String.format(
						"MqttCallbackImpl().disconnected() - Client disconnected: [%s]. disconnectResponse: [%s]. Moved to dead client list: [endpointName=%s, endpoint=%s].",
						client.getServerURI(), disconnectResponse, endpointName, client.getServerURI()));
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
	class InBridgeCallbackImpl implements IHaMqttCallback {

		InBridgeCluster[] bridgeClusters = inBridgeSet.toArray(new InBridgeCluster[0]);

		InBridgeCallbackImpl() {
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
			for (InBridgeCluster bridgeCluster : bridgeClusters) {
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
