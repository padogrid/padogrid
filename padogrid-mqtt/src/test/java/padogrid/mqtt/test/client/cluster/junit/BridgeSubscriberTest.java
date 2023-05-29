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
package padogrid.mqtt.test.client.cluster.junit;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import padogrid.mqtt.client.cluster.HaClusters;
import padogrid.mqtt.client.cluster.HaMqttClient;
import padogrid.mqtt.client.cluster.IClusterConfig;
import padogrid.mqtt.client.cluster.IHaMqttCallback;

/**
 * This test case demonstrates the cluster, "bridge-subscriber-edge", bridges
 * the cluster, "bridge-subscriber-enterprise", by forwarding subscribed
 * messages. To run the test case, follow the steps below.
 * 
 * <ul>
 * <li>Start bridge-subscriber-edge with 1883-1885 ports</li>
 * <li>Start bridge-subscriber-enterprise with 32001-32003 ports</li>
 * <li>Run this test case</li>
 * <li>Run subscriber listening on 32001-32003</li>
 * <ul>
 * <li>mosquitto_sub -p 32001 -t test/#</li>
 * </ul>
 * <li>Run publisher on 1883-1885:</li>
 * <ul>
 * <li>mosquitto_pub -p 1883 -t test/mytopic -m hello</li>
 * </ul>
 * </ul>
 * 
 * This test case should receive message from bridge-subscriber-edge
 * (1883-1885). The subscriber on bridge-subscriber-enterprise (32001-32003)
 * should also receive the same messages.
 * 
 * @author dpark
 *
 */
public class BridgeSubscriberTest {

	private static final String TOPIC_FILTER = "test/#";
	private static final int QOS = 2;
	private static HaMqttClient haclient1;

	@BeforeClass
	public static void setUp() throws Exception {
		TestUtil.setEnv("LOG_FILE", "log/subscriber-bridge.log");
		System.setProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE, "etc/mqttv5-bridge-subscriber.yaml");
		System.setProperty("java.util.logging.config.file", "etc/publisher-subscriber-logging.properties");
		System.setProperty("log4j.configurationFile", "etc/log4j2.properties");

		haclient1 = HaClusters.getOrCreateHaMqttClient("bridge-subscriber-enterprise");
		haclient1.addCallbackCluster(new SubscriberHaMqttClientCallback());
		haclient1.connect();
		haclient1.subscribe(TOPIC_FILTER, QOS);
	}

	@Test
	public void testSubscriber() throws Exception {
		while (true) {
			Thread.sleep(1000);
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (haclient1 != null) {
			haclient1.close();
		}
	}

	static class SubscriberHaMqttClientCallback implements IHaMqttCallback {

		@Override
		public void disconnected(MqttClient client, MqttDisconnectResponse disconnectResponse) {
			System.out.printf("SubscriberHaMqttClientCallback.disconnected(): endpoint=%s, disconnectResponse=%s%n",
					client.getServerURI(), disconnectResponse);
		}

		@Override
		public void mqttErrorOccurred(MqttClient client, MqttException exception) {
			System.out.printf("SubscriberHaMqttClientCallback.mqttErrorOccurred(): endpoint=%s%n",
					client.getServerURI());
			exception.printStackTrace();
		}

		@Override
		public void messageArrived(MqttClient client, String topic, MqttMessage message) throws Exception {
			System.out.printf(
					"SubscriberHaMqttClientCallback.messageArrived(): endpoint=%s, topic=%s, message=%s, payload=%s, id=%d, qos=%d, props=%s%n",
					client.getServerURI(), topic, message, message.getPayload(), message.getId(), message.getQos(),
					message.getProperties());
		}

		@Override
		public void deliveryComplete(MqttClient client, IMqttToken token) {
			System.out.printf("SubscriberMqttCallback.deliveryComplete(): endpoint=%s, token=%s%n",
					client.getServerURI(), token);
		}

		@Override
		public void connectComplete(MqttClient client, boolean reconnect, String serverURI) {
			System.out.printf(
					"SubscriberHaMqttClientCallback.connectComplete(): client=%s, reconnect=%s, serverURI=%s%n", client,
					reconnect, serverURI);
		}

		@Override
		public void authPacketArrived(MqttClient client, int reasonCode, MqttProperties properties) {
			System.out.printf(
					"SubscriberHaMqttClientCallback.authPacketArrived(): endpoint=%s, reasonCode=%s, properties=%s%n",
					client.getServerURI(), reasonCode, properties);
		}
	}
}
