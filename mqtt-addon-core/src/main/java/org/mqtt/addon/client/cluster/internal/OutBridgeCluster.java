package org.mqtt.addon.client.cluster.internal;

import org.mqtt.addon.client.cluster.HaMqttClient;

public class OutBridgeCluster extends BridgeCluster {
	public OutBridgeCluster(HaMqttClient outClient, String[] topicFilters, int qos) {
		super(outClient, topicFilters, qos);
	}
}
