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
package org.mqtt.addon.test.client.cluster.junit;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mqtt.addon.client.cluster.HaClusters;
import org.mqtt.addon.client.cluster.HaMqttClient;
import org.mqtt.addon.client.cluster.IClusterConfig;
import org.mqtt.addon.client.cluster.IHaMqttCallback;

/**
 * EndpointsTest configures with "etc/mqttv5-endpoints.yaml" to subscribe to
 * topics by endpoint names. To run the test case, follow the steps below.
 * <ul>
 * <li>Start endpoints-test with 1883-1885 ports</li>
 * </ul>
 * <p>
 * The following configuration files is used for this test case: <br>
 * <ul>
 * <li>etc/mqttv5-endpoints.yaml</li>
 * </ul>
 * <p>
 * The callback should report the following messages.
 * <p>
 * <code>
 * SubscriberMqttCallback.deliveryComplete(): endpoint=tcp://localhost:1884, topics=test/edge-test/edge-2/1884 <br>
 * SubscriberMqttCallback.deliveryComplete(): endpoint=tcp://localhost:1883, topics=test/edge/endpoints-test-1/1883<br>
 * SubscriberMqttCallback.deliveryComplete(): endpoint=tcp://localhost:1884, topics=test/any/edge-2/1884
 * </code>
 * @author dpark
 *
 */
public class EndpointsTest {

	private static final int QOS = 2;
	private static HaMqttClient haclient;

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE, "etc/mqttv5-endpoints.yaml");
		System.setProperty("java.util.logging.config.file", "etc/publisher-logging.properties");
		TestUtil.setEnv("LOG_FILE", "log/endpoints.log");
		System.setProperty("log4j.configurationFile", "etc/log4j2.properties");
		haclient = HaClusters.getHaMqttClient();
		haclient.addCallbackCluster(new PublisherHaMqttClientCallback());
		haclient.connect();
	}

	@Test
	public void testPublish() throws InterruptedException {
		int messageCount = 1000;
		for (long i = 0; i < messageCount; i++) {
			byte[] payload = ("my message " + i).getBytes();
			MqttMessage message = new MqttMessage(payload);
			message.setQos(QOS);
			boolean isRetry = false;
			do {
				try {
					// Publish by default endpoint name: endpoints-test-1 -> tcp://localhost:1883,
					// test/edge/endpoints-test-1/1883
					haclient.publish("endpoints-test-1", "test/edge/endpoints-test-1/1883", message);

					// Publish by endpoint name: edge-2 -> tcp://localhost:1884,
					// test/any/edge-2/1884
					haclient.publish("edge-2", "test/any/edge-2/1884", message);

					// Publish by topic: tcp://localhost:1884, test/edge-test/edge-2/1884
					haclient.publish("test/edge-test/edge-2/1884", message);
					isRetry = false;
				} catch (MqttException e) {
					e.printStackTrace();
					System.err.printf("testPublish(): %s%n", e.getMessage());
					isRetry = true;
					Thread.sleep(1000L);
				}
			} while (isRetry);
			System.out.println("Published " + message);

			Thread.sleep(1000L);
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (haclient != null) {
			haclient.close();
		}
	}

	static class PublisherHaMqttClientCallback implements IHaMqttCallback {

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
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < token.getTopics().length; i++) {
				if (i > 0) {
					buffer.append(",");
				}
				buffer.append(token.getTopics()[i]);
			}
			System.out.printf("SubscriberMqttCallback.deliveryComplete(): endpoint=%s, topics=%s%n",
					client.getServerURI(), buffer.toString());
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
