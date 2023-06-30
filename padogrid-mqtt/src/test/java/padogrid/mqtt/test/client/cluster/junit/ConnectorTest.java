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

import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import padogrid.mqtt.client.cluster.HaClusters;
import padogrid.mqtt.client.cluster.HaMqttClient;
import padogrid.mqtt.client.cluster.IClusterConfig;

/**
 * ConnectorTest configures with "etc/mqttv5-connector.yaml" to publish to
 * "test/mytopic". Run this test case with {@linkplain SubscriberTest}.
 * 
 * @author dpark
 *
 */
public class ConnectorTest {

	private static final String TOPIC = "test/mytopic";
	private static final int QOS = 2;
	private static HaMqttClient haclient;

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE, "etc/mqttv5-connector.yaml");
		System.setProperty("java.util.logging.config.file", "etc/publisher-logging.properties");
		TestUtil.setEnv("LOG_FILE", "log/publisher.log");
		System.setProperty("log4j.configurationFile", "etc/log4j2.properties");
		haclient = HaClusters.getHaMqttClient();
		haclient.connect();
		haclient.subscribe(TOPIC, QOS);
	}

	@Test
	public void testPublish() throws InterruptedException {
		int messageCount = 10;
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
		}
		System.out.printf("Successfully published %d messages to topics [%s]%n", messageCount, TOPIC);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (haclient != null) {
			haclient.close();
		}
		HaClusters.stop();
	}
}
