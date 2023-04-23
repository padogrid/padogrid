package org.mqtt.addon.test.client.cluster.junit;

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
import org.mqtt.addon.client.cluster.HaCluster;
import org.mqtt.addon.client.cluster.HaMqttClient;
import org.mqtt.addon.client.cluster.IClusterConfig;
import org.mqtt.addon.client.cluster.IHaMqttClientCallback;

public class SubscriberTest {

	private static final String TOPIC = "mytopic";
	private static final int QOS = 2;
	private static HaMqttClient haclient;

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE, "etc/mqttv5-subscriber.yaml");
		TestUtil.setEnv("LOG_FILE", "log/subscriber.log");
		System.setProperty("log4j.configurationFile", "etc/log4j2.properties");
		haclient = HaCluster.getHaMqttClient();
		haclient.addCallbackCluster(new SubscriberHaMqttClientCallback());
		haclient.connect();
		haclient.subscribe(TOPIC, QOS);
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
			System.out.printf(
					"SubscriberMqttCallback.messageArrived(): topic=%s, message=%s", topic, message);
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

	static class SubscriberHaMqttClientCallback implements IHaMqttClientCallback {

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
