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

import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mqtt.addon.client.cluster.HaClusters;
import org.mqtt.addon.client.cluster.HaMqttClient;
import org.mqtt.addon.client.cluster.IClusterConfig;

public class MutiClusterPublisherTest {

	private static final String TOPIC1 = "mytopic1";
	private static final String TOPIC2 = "mytopic2";
	private static final int QOS = 2;
	private static HaMqttClient haclient1;
	private static HaMqttClient haclient2;

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE, "etc/mqttv5-publisher-multi.yaml");
		System.setProperty("java.util.logging.config.file", "etc/publisher-logging.properties");
		TestUtil.setEnv("LOG_FILE", "log/publisher.log");
		System.setProperty("log4j.configurationFile", "etc/log4j2.properties");
		haclient1 = HaClusters.getOrCreateHaMqttClient("publisher-multi-01");
		haclient1.connect();
		
		haclient2 = HaClusters.getOrCreateHaMqttClient("publisher-multi-02");
		haclient2.connect();
	}

	@Test
	public void testPublish() throws InterruptedException {
		for (long i = 0; i < 1000; i++) {
			byte[] payload = ("my message " + i).getBytes();
			MqttMessage message = new MqttMessage(payload);
			message.setQos(QOS);
			message.setRetained(true);
			boolean isRetry = false;
			do {
				try {
					haclient1.publish(TOPIC1, message);
					haclient2.publish(TOPIC2, message);
					isRetry = false;
				} catch (MqttException e) {
					System.err.printf("testPublish(): %s%n", e.getMessage());
					isRetry = true;
					Thread.sleep(1000L);
				}
			} while (isRetry);
			System.out.println("Published " + message);
			
			Thread.sleep(1000L);
		}
		System.out.printf("Successfully produced 10 messages to a topic called %s%n", TOPIC1);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (haclient1 != null) {
			haclient1.close();
		}
	}
}
