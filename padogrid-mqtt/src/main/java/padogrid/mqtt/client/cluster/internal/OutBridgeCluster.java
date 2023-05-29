package padogrid.mqtt.client.cluster.internal;

import padogrid.mqtt.client.cluster.HaMqttClient;

public class OutBridgeCluster extends BridgeCluster {
	public OutBridgeCluster(HaMqttClient outClient, String[] topicFilters, int qos) {
		super(outClient, topicFilters, qos);
	}
}
