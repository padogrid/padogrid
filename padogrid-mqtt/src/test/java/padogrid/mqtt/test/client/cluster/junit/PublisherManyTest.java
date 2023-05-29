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
 * PublisherManyTest configures with "etc/mqttv5-publisher-many.yaml" to test
 * 'initialEndpointCount'. To run the test case, follow the steps below.
 * 
 * <ul>
 * <li>Start endpoints-test with 1883-1889 ports</li>
 * </ul>
 * <p>
 * The following configuration and log files are used for this test case: <br>
 * <ul>
 * <li>etc/mqttv5-endpoints.yaml</li>
 * <li>log/publisher-many.log</li>
 * </ul>
 * <p>
 * Check the log file to see if there are two (2) passes to probing. The first
 * pass should connect three(3) endpoints and the next pass should connect the
 * remaining endpoints.
 * 
 * @author dpark
 *
 */
public class PublisherManyTest {

	private static final String TOPIC = "test/mytopic";
	private static final int QOS = 2;
	private static HaMqttClient haclient;

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE, "etc/mqttv5-publisher-many.yaml");
		System.setProperty("java.util.logging.config.file", "etc/publisher-logging.properties");
		TestUtil.setEnv("LOG_FILE", "log/publisher-many.log");
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
			message.setRetained(true);
			boolean isRetry = false;
			do {
				try {
					haclient.publish(TOPIC, message);
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
		System.out.printf("Successfully published %d messages to topics [%s]%n", messageCount, TOPIC);
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
