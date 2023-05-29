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
import org.eclipse.paho.mqttv5.client.MqttCallback;
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
 * SubscriberTest configures with "etc/mqttv5-subscriber.yaml" to subscribe to
 * "test/mytopic". Run this test case with {@linkplain PublisherSharedTest}.
 * 
 * @author dpark
 *
 */
public class SubscriberTest {

	private static final String TOPIC_FILTER = "test/mytopic";
	private static final int QOS = 2;
	private static HaMqttClient haclient;

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE, "etc/mqttv5-subscriber.yaml");
		System.setProperty("java.util.logging.config.file", "etc/publisher-logging.properties");
		TestUtil.setEnv("LOG_FILE", "log/subscriber.log");
		System.setProperty("log4j.configurationFile", "etc/log4j2.properties");
		haclient = HaClusters.getHaMqttClient();
		haclient.addCallbackCluster(new SubscriberHaMqttClientCallback());
		haclient.connect();
		haclient.subscribe(TOPIC_FILTER, QOS);
	}

	@Test
	public void testSubscriber() throws Exception {
		while (true) {
			Thread.sleep(1000);
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (haclient != null) {
			haclient.close();
		}
	}

	static class SubscriberMqttCallback implements MqttCallback {
		@Override
		public void mqttErrorOccurred(MqttException exception) {
			System.out.printf("SubscriberHaMqttClientCallback.mqttErrorOccurred():%n");
			exception.printStackTrace();
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			System.out.printf("SubscriberMqttCallback.messageArrived(): topic=%s, message=%s", topic, message);
		}

		@Override
		public void disconnected(MqttDisconnectResponse disconnectResponse) {
			System.out.printf("SubscriberMqttCallback.disconnected(): disconnectResponse=%s%n", disconnectResponse);
		}

		@Override
		public void deliveryComplete(IMqttToken token) {
			System.out.printf("SubscriberMqttCallback.deliveryComplete(): token=%s%n", token);
		}

		@Override
		public void connectComplete(boolean reconnect, String serverURI) {
			System.out.printf("SubscriberMqttCallback.connectComplete(): reconnect=%s, serverURI=%s%n", reconnect,
					serverURI);
		}

		@Override
		public void authPacketArrived(int reasonCode, MqttProperties properties) {
			System.out.printf("SubscriberMqttCallback.authPacketArrived(): reasonCode=%s, properties=%s%n", reasonCode,
					properties);
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
