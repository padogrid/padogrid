package org.mqtt.addon.test.client.cluster.junit;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mqtt.addon.client.cluster.HaCluster;
import org.mqtt.addon.client.cluster.HaMqttClient;
import org.mqtt.addon.client.cluster.IClusterConfig;
import org.mqtt.addon.client.cluster.PublisherType;

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
		haclient1 = HaCluster.getOrCreateHaMqttClient("publisher-multi-01");
		haclient1.connect();
		
		haclient2 = HaCluster.getOrCreateHaMqttClient("publisher-multi-02");
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
