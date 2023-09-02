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
package padogrid.mqtt.test.cluster.console;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import padogrid.mqtt.client.cluster.HaClusters;
import padogrid.mqtt.client.cluster.HaMqttClient;
import padogrid.mqtt.client.cluster.IClusterConfig;
import padogrid.mqtt.client.cluster.IHaMqttCallback;

/**
 * TopicSubscriber dumps the specified topic messages.
 * 
 * @author dpark
 *
 */
public class TopicSubscriber {

	public final static String PROPERTY_executableName = "executable.name";
	private static String clusterName = System.getProperty("cluster.name", IClusterConfig.DEFAULT_CLUSTER_NAME);
	private static Logger logger = LogManager.getLogger(TopicSubscriber.class);

	private static void usage() {
		String executableName = System.getProperty(PROPERTY_executableName, TopicSubscriber.class.getName());
		writeLine();
		writeLine("NAME");
		writeLine("   " + executableName + " - Listen on the specified topics and print received messages");
		writeLine();
		writeLine("SYNOPSIS");
		writeLine("   " + executableName + " [-qos 0|1|2] topic_name [-?]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Listens on the specified topics and prints received messages.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -qos");
		writeLine("             QoS. Default: 0");
		writeLine();
		writeLine("   topic_name");
		writeLine("             Topic name.");
		writeLine();
		writeLine("EXAMPLES");
		writeLine("   # Subscribe all topics that begind with nw/");
		writeLine("   ./" + executableName + " -qos 2 nw/#");
		writeLine();
	}

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	public static void main(String[] args) throws IOException, MqttException {
		String topicFilter = null;
		int qos = 0;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
				System.exit(0);
			} else if (arg.equals("-qos")) {
				if (i < args.length - 1) {
					boolean isError = false;
					try {
						qos = Integer.valueOf(args[++i].trim());
						isError = qos < 0 || qos > 2;
					} catch (NumberFormatException ex) {
						isError = true;
					}
					if (isError) {
						System.err.println("ERROR: Invalid qos. Must be 0, 1, or 2.");
						System.exit(1);
					}
				}
			} else if (arg.startsWith("-") == false) {
				topicFilter = arg;
			}
		}

		if (topicFilter != null && topicFilter.equals("-?")) {
			usage();
			System.exit(0);
		}
		
		if (topicFilter == null) {
			System.err.println("Topic filter not specified.");
			System.exit(1);
		}

		HaMqttClient client = HaClusters.getOrCreateHaMqttClient(clusterName);
		client.addCallbackCluster(new IHaMqttCallback() {
			
			@Override
			public void mqttErrorOccurred(MqttClient client, MqttException exception) {
				exception.printStackTrace();
			}
			
			@Override
			public void messageArrived(MqttClient client, String topic, MqttMessage message) throws Exception {
				writeLine(String.format("topic=%s, payloadSize=%d, clientId=%s", topic, message.getPayload().length, client.getClientId()));
			}
			
			@Override
			public void disconnected(MqttClient client, MqttDisconnectResponse disconnectResponse) {
//				writeLine(String.format("disconnected(): %s", disconnectResponse));
				logger.info(String.format("disconnected(): %s", disconnectResponse));
			}
			
			@Override
			public void deliveryComplete(MqttClient client, IMqttToken token) {
//				writeLine(String.format("deliveryComplete(): %s", token));
				logger.info(String.format("deliveryComplete(): %s", token));
			}
			
			@Override
			public void connectComplete(MqttClient client, boolean reconnect, String serverURI) {
//				writeLine(String.format("connectComplete(): reconnect=%s, serverURI=%s", reconnect, serverURI));
				logger.info(String.format("connectComplete(): reconnect=%s, serverURI=%s", reconnect, serverURI));
			}
			
			@Override
			public void authPacketArrived(MqttClient client, int reasonCode, MqttProperties properties) {
//				writeLine(String.format("authPacketArrived(): reasonCode=%s, properties=%s", reasonCode, properties));	
				String.format(String.format("authPacketArrived(): reasonCode=%s, properties=%s", reasonCode, properties));
			}
		});
		client.connect();
		
		writeLine();
		if (topicFilter != null) {
			writeLine("Topic: " + topicFilter);
			client.subscribe(topicFilter, qos);
			writeLine("---------------------------------------------------------------------------------");
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		writeLine();
		client.close();
	}
}