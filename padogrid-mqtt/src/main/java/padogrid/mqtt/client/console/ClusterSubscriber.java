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

package padogrid.mqtt.client.console;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import padogrid.mqtt.client.cluster.HaClusters;
import padogrid.mqtt.client.cluster.HaMqttClient;
import padogrid.mqtt.client.cluster.HaMqttConnectionOptions;
import padogrid.mqtt.client.cluster.IClusterConfig;
import padogrid.mqtt.client.cluster.IHaMqttCallback;
import padogrid.mqtt.client.cluster.config.ClusterConfig;

public class ClusterSubscriber implements Constants {

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	@SuppressWarnings("unused")
	private static void write(String str) {
		System.out.print(str);
	}

	private static void usage() {
		String executable = System.getProperty(PROPERTY_executableName, ClusterSubscriber.class.getName());
		writeLine();
		writeLine("NAME");
		writeLine("   " + executable
				+ " - Subscribe to the specified topic filter in the specified MQTT virtual cluster");
		writeLine();
		writeLine("SNOPSIS");
		writeLine("   " + executable
				+ " [[-cluster cluster_name] [-config config_file] | [-endpoints serverURIs]]");
		writeLine("                [-log log_file] [-fos fos] [-qos qos] [-quiet] -t topic_filter [-?]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Subscribes to the specified topic filter in the specified virtual cluster.");
		writeLine();
		writeLine("   - If '-cluster' is specified and -config is not specified, then '-cluster'");
		writeLine("     represents a PadoGrid cluster and maps it to a unique virtual cluster name.");
		writeLine();
		writeLine("   - If '-config' is specified, then '-cluster' represents a virtual cluster");
		writeLine("     defined in the configuration file.");
		writeLine();
		writeLine("   - If '-config' is specified and '-cluster' is not specified, then the default");
		writeLine("     virtual cluster defined in the configuration file is used.");
		writeLine();
		writeLine("   - If '-endpoints' is specified then '-cluster' and '-config' are not allowed.");
		writeLine();
		writeLine("   - If '-cluster', '-config', and '-endpoints' are not specified, then the PadoGrid's");
		writeLine("     current context cluster is used.");
		writeLine();
		writeLine(
				"   - If PadoGrid cluster is not an MQTT cluster it defaults to endpoints, 'tcp://localhost:1883-1885'.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -cluster cluster_name");
		writeLine("             Connects to the specified PadoGrid cluster. Exits if it does not exist in the");
		writeLine("             current workspace.");
		writeLine();
		writeLine("   -endpoints serverURIs");
		writeLine("             Connects to the specified endpoints. Exits if none of the endpoints exist.");
		writeLine("             Default: tcp://localhost:1883-1885");
		writeLine();
		writeLine("   -config config_file");
		writeLine("             Optional configuration file.");
		writeLine();
		writeLine("   -log log_file");
		writeLine("             Optional log file.");
		writeLine("             Default: ~/.padogrid/log/" + PROPERTY_executableName + ".log");
		writeLine();
		writeLine("   -fos fos");
		writeLine("             Optional FoS value. Valid values are 0, 1, 2, 3. Default: 0.");
		writeLine();
		writeLine("   -qos qos");
		writeLine("             Optional QoS value. Valid values are 0, 1, 2. Default: 0.");
		writeLine("");
		writeLine("   -quiet");
		writeLine("             If specified, then outputs received messages only.");
		writeLine();
		writeLine("   -t topic_filter");
		writeLine("             Topic filter.");
		writeLine();
	}

	public static void main(String[] args) {
		String clusterName = null;
		String endpoints = null;
		String configFilePath = null;
		int qos = 0;
		int fos = 0;
		boolean isQuietTmp = false;
		String topicFilter = null;

		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
				System.exit(0);
			} else if (arg.equals("-cluster")) {
				if (i < args.length - 1) {
					clusterName = args[++i].trim();
				}
			} else if (arg.equals("-endpoints")) {
				if (i < args.length - 1) {
					endpoints = args[++i].trim();
				}
			} else if (arg.equals("-config")) {
				if (i < args.length - 1) {
					configFilePath = args[++i].trim();
				}
			} else if (arg.equals("-t")) {
				if (i < args.length - 1) {
					topicFilter = args[++i].trim();
				}
			} else if (arg.equals("-qos")) {
				if (i < args.length - 1) {
					String qosStr = args[++i].trim();
					try {
						qos = Integer.parseInt(qosStr);
					} catch (NumberFormatException ex) {
						System.err.printf("ERROR: Invalid qos: [%s]. Valid values are 0, 1, or 2. Command aborted.%n",
								qosStr);
						System.exit(1);
					}
				}
			} else if (arg.equals("-fos")) {
				if (i < args.length - 1) {
					String fosStr = args[++i].trim();
					try {
						qos = Integer.parseInt(fosStr);
					} catch (NumberFormatException ex) {
						System.err.printf("ERROR: Invalid fos: [%s]. Valid values are 0, 1, 2, 3. Command aborted.%n",
								fosStr);
						System.exit(1);
					}
				}
			} else if (arg.equals("-quiet")) {
				isQuietTmp = true;
			}
		}

		// final var for callback
		final boolean isQuiet = isQuietTmp;

		// Validate inputs
		if (clusterName != null && endpoints != null) {
			System.err.printf("ERROR: -cluster, -endpoints are not allowed together. Command aborted.%n");
			System.exit(2);
		}
		if (configFilePath != null && endpoints != null) {
			System.err.printf("ERROR: -config, -endpoints are not allowed together. Command aborted.%n");
			System.exit(2);
		}
		if (topicFilter == null) {
			System.err.printf("ERROR: Topic filter not specified: [-t].%n");
			System.exit(3);
		}

		// Collect system properties - passed in by the invoking script.
		if (configFilePath == null && clusterName == null) {
			clusterName = System.getProperty("cluster.name");
		}
		if (endpoints == null) {
			endpoints = System.getProperty("cluster.endpoints");
		}

		// Display all options
		if (isQuiet == false && clusterName != null) {
			writeLine("PadoGrid Cluster: " + clusterName);
		}
		String virtualClusterName = clusterName;
		if (configFilePath != null) {
			try {
				// We need to do this here in order to get the default
				// cluster name.
				HaClusters.initialize(new File(configFilePath));
				if (virtualClusterName == null) {
					virtualClusterName = HaClusters.getDefaultClusterName();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.err.printf(
						"ERROR: Exception occurred while initializing virtual clusters: [file=%s]. Command aborted.%n",
						configFilePath);
				System.exit(-1);
			}
		}
		if (virtualClusterName == null) {
			virtualClusterName = "subscriber";
		}

		if (isQuiet == false) {
			writeLine("cluster: " + virtualClusterName + " (virtual)");
		}

		// If endpoints is not set, then default to
		// IClusterConfig.DEFAULT_CLIENT_SERVER_URIS.
		if (configFilePath == null && endpoints == null) {
			endpoints = IClusterConfig.DEFAULT_CLIENT_SERVER_URIS;
		}

		if (isQuiet == false) {
			if (endpoints != null) {
				writeLine("endpoints: " + endpoints);
			}
			writeLine("fos: " + fos);
			writeLine("qos: " + qos);
			if (configFilePath != null) {
				writeLine("config: " + configFilePath);
			}
			writeLine("topicFilter: " + topicFilter);
		}

		// Create cluster
		HaMqttClient client = null;
		if (configFilePath == null) {
			ClusterConfig clusterConfig = new ClusterConfig();
			clusterConfig.setDefaultCluster(virtualClusterName);
			HaMqttConnectionOptions options = new HaMqttConnectionOptions();
			endpoints = endpoints.replaceAll(" ", "");
			options.getConnection().setServerURIs(endpoints.split(","));
			ClusterConfig.Cluster cluster = new ClusterConfig.Cluster();
			cluster.setName(virtualClusterName);
			cluster.setFos(fos);
			cluster.setConnections(options);
			clusterConfig.setClusters(new ClusterConfig.Cluster[] { cluster });
			try {
				HaClusters.initialize(clusterConfig);
				client = HaClusters.getOrCreateHaMqttClient(cluster);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.printf(
						"ERROR: Exception occurred while creating a virtual cluster: [%s]. Command aborted.%n",
						virtualClusterName);
				System.exit(-1);
			}
		} else {
			try {
				client = HaClusters.getOrCreateHaMqttClient(virtualClusterName);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.printf(
						"ERROR: Exception occurred while creating a virtual cluster: [file=%s]. Command aborted.%n",
						configFilePath);
				System.exit(-1);
			}
		}

		// This should never occur
		if (client == null) {
			System.err.printf("ERROR: Unable to create the virtual cluster: [%s]. Command aborted.%n",
					virtualClusterName);
			System.exit(-1);
		}

		// Register callback to display received messages
		client.addCallbackCluster(new IHaMqttCallback() {

			@Override
			public void mqttErrorOccurred(MqttClient client, MqttException exception) {
				// do nothing
			}

			@Override
			public void messageArrived(MqttClient client, String topic, MqttMessage message) throws Exception {
				byte[] payload = message.getPayload();
				if (isQuiet) {
					System.out.println(String.format("%s", new String(payload, StandardCharsets.UTF_8)));
				} else {
					System.out.println(String.format("%s - %s: %s", client.getServerURI(), topic,
							new String(payload, StandardCharsets.UTF_8)));
				}
			}

			@Override
			public void disconnected(MqttClient client, MqttDisconnectResponse disconnectResponse) {
				// do nothing
			}

			@Override
			public void deliveryComplete(MqttClient client, IMqttToken token) {
				// do nothing
			}

			@Override
			public void connectComplete(MqttClient client, boolean reconnect, String serverURI) {
				// do nothing
			}

			@Override
			public void authPacketArrived(MqttClient client, int reasonCode, MqttProperties properties) {
				// do nothing
			}
		});

		// Connect
		try {
			client.connect();
			if (client.isConnected() == false) {
				System.err
						.printf("ERROR: Unable to connect to any of the endpoints in the cluster. Command aborted.%n");
				HaClusters.stop();
				System.exit(-1);
			}
			client.subscribe(topicFilter, qos);
			if (isQuiet == false) {
				writeLine("Waiting for messages...");
			}

			while (true) {
				Thread.sleep(5000);
			}
		} catch (Exception e) {
			System.err.printf("ERROR: Error occured while subscribing to the topic filter. Command aborted.%n");
			e.printStackTrace();
			HaClusters.stop();
			System.exit(-3);
		}
	}
}
