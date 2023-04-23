package org.mqtt.addon.client.cluster;

import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttSecurityException;
import org.eclipse.paho.mqttv5.common.MqttSubscription;

public interface IHaMqttClient extends IMqttClient {
	boolean isLive();
	String[] getClientIds();
	String[] getLiveClientIds();
	String[] getCurrentServerURIs();
	String[] getDisconnectedClientIds();
	String[] getServerURIs();
	
	IMqttToken[] subscribeCluster(String topicFilter, int qos) throws MqttException;
	IMqttToken[] subscribeCluster(String[] topicFilters, int[] qos) throws MqttException;
	IMqttToken[] subscribeCluster(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners) throws MqttException;
	IMqttToken[] subscribeCluster(String topicFilter, int qos, IMqttMessageListener messageListener) throws MqttException;
	
	void addCallbackCluster(IHaMqttClientCallback callback);
	void removeCallbackCluster(IHaMqttClientCallback callback);
	IMqttToken[] connectWithResultCluster(MqttConnectionOptions options) throws MqttSecurityException, MqttException;
	void messageArrivedComplete(MqttClient client, int messageId, int qos) throws MqttException;

	boolean isDisconnected();
	boolean isClosed();
	
	public MqttClient getPrimaryMqttClient();
	
	// The following method are in MqttClient but not in IMattClient (Paho v1.2.5)
	IMqttToken[] subscribe(MqttSubscription[] subscriptions) throws MqttException;
	IMqttToken[] subscribe(MqttSubscription[] subscriptions, IMqttMessageListener[] messageListeners) throws MqttException;
	void close(boolean force) throws MqttException;
}
