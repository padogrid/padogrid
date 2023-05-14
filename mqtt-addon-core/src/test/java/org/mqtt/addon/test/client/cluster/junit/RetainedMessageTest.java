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

public class RetainedMessageTest {

	private static final String TOPIC = "mytopic";
	private static final int QOS = 2;
	private static HaMqttClient haclient;

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE, "etc/mqttv5-retained.yaml");
		TestUtil.setEnv("LOG_FILE", "log/subscriber.log");
		System.setProperty("log4j.configurationFile", "etc/log4j2.properties");
		haclient = HaClusters.getHaMqttClient();
		haclient.addCallbackCluster(new SubscriberHaMqttClientCallback());
		haclient.connect();
	}

	@Test
	public void testSubscriber() throws Exception {
		for (int i = 1; i <= 3; i++) {
			byte[] message = ("Retained message " + i).getBytes();
			haclient.publish(TOPIC, message, 2, true);
		}
		Thread.sleep(1000);
		haclient.disconnect();
		Thread.sleep(1000);
		
		// Should receive three (3) retained messages
		haclient.subscribe(TOPIC, QOS);
		haclient.connect();
		Thread.sleep(1000);
		haclient.close();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (haclient != null) {
			haclient.close();
		}
	}

	static class SubscriberHaMqttClientCallback implements IHaMqttCallback {

		@Override
		public void disconnected(MqttClient client, MqttDisconnectResponse disconnectResponse) {
			System.out.printf("SubscriberHaMqttClientCallback.disconnected(): client=%s, disconnectResponse=%s%n", client, disconnectResponse);
		}

		@Override
		public void mqttErrorOccurred(MqttClient client, MqttException exception) {
			System.out.printf("SubscriberHaMqttClientCallback.mqttErrorOccurred(): client=%s%n", client);
			exception.printStackTrace();
		}

		@Override
		public void messageArrived(MqttClient client, String topic, MqttMessage message) throws Exception {
			System.out.printf(
					"SubscriberHaMqttClientCallback.messageArrived(): client=%s, topic=%s, message=%s, payload=%s, id=%d, qos=%d, props=%s%n",
					client, topic, message, message.getPayload(), message.getId(), message.getQos(),
					message.getProperties());
//			System.out.printf(
//					"SubscriberHaMqttClientCallback.messageArrived(): topic=%s, message=%s%n", topic, message);
		}

		@Override
		public void deliveryComplete(MqttClient client, IMqttToken token) {
			System.out.printf("SubscriberMqttCallback.deliveryComplete(): client=%s, token=%s%n", client, token);
		}

		@Override
		public void connectComplete(MqttClient client, boolean reconnect, String serverURI) {
			System.out.printf("SubscriberHaMqttClientCallback.connectComplete(): client=%s, reconnect=%s, serverURI=%s%n",
					client, reconnect, serverURI);
		}

		@Override
		public void authPacketArrived(MqttClient client, int reasonCode, MqttProperties properties) {
			System.out.printf("SubscriberHaMqttClientCallback.authPacketArrived(): client=%s, reasonCode=%s, properties=%s%n",
					client, reasonCode, properties);
		}
	}
}