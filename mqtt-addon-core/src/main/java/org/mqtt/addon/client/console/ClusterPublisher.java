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

package org.mqtt.addon.client.console;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptionsBuilder;
import org.mqtt.addon.client.cluster.HaClusters;
import org.mqtt.addon.client.cluster.HaMqttClient;
import org.mqtt.addon.client.cluster.IClusterConfig;
import org.mqtt.addon.client.cluster.config.ClusterConfig;

public class ClusterPublisher implements Constants {

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
		String executable = System.getProperty(PROPERTY_executableName, ClusterPublisher.class.getName());
		writeLine();
		writeLine("NAME");
		writeLine("   " + executable + " - Publish messages to an MQTT virtual cluster");
		writeLine();
		writeLine("SNOPSIS");
		writeLine("   " + executable
				+ " [[-cluster cluster_name] [-config config_file] | -endpoints serverURIs] [-name endpoint_name] [-fos fos] [-qos qos] [-r] -t topic_filter -m message [-?]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Publishes the specified message to the specified topic.");
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
		writeLine("   -name endpoint_name");
		writeLine("             Optional endpoint name that identifies and targets the endpoint to which the message");
		writeLine("             is published. If the endpoint name is not to found then the command aborts.");
		writeLine();
		writeLine("   -config config_file");
		writeLine("             Optional configuration file. Default: current cluster's etc/mqtt5-client.yaml");
		writeLine();
		writeLine("   -fos fos");
		writeLine("             Optional FoS value. Valid values are 0, 1, 2, 3. Default: 0.");
		writeLine();
		writeLine();
		writeLine("   -qos qos");
		writeLine("             Optional QoS value. Valid values are 0, 1, 2. Default: 0.");
		writeLine();
		writeLine("   -m message");
		writeLine("             Message to publish.");
		writeLine();
		writeLine("   -r");
		writeLine("             Retain message.");
		writeLine();
		System.exit(0);
	}

	private static String createVirtualClusterName() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	public static void main(String[] args) {
		String clusterName = null;
		String endpointName = null;
		String endpoints = null;
		String configFilePath = null;
		int qos = 0;
		int fos = 0;
		String topic = null;
		String message = null;
		boolean isRetained = false;

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
			} else if (arg.equals("-name")) {
				if (i < args.length - 1) {
					endpointName = args[++i].trim();
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
					topic = args[++i].trim();
				}
			} else if (arg.equals("-m")) {
				if (i < args.length - 1) {
					message = args[++i].trim();
				}
			} else if (arg.equals("-r")) {
				isRetained = true;
			} else if (arg.equals("-qos")) {
				if (i < args.length - 1) {
					String qosStr = args[++i].trim();
					try {
						qos = Integer.parseInt(qosStr);
					} catch (NumberFormatException ex) {
						System.err.printf("ERROR: Invalid qos: [%s]. Valid values are 0, 1, or 2.%n Command aborted.",
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
						System.err.printf("ERROR: Invalid fos: [%s]. Valid values are 0, 1, 2, 3.%n Command aborted.",
								fosStr);
						System.exit(1);
					}
				}
			}
		}

		// Validate inputs
		if (clusterName != null && endpoints != null) {
			System.err.printf("ERROR: -cluster, -endpoints are not allowed together. Command aborted.%n");
			System.exit(2);
		}
		if (configFilePath != null && endpoints != null) {
			System.err.printf("ERROR: -config, -endpoints are not allowed together. Command aborted.%n");
			System.exit(2);
		}
		if (topic == null) {
			System.err.printf("ERROR: Topic not specified: [-t]. Command aborted.%n");
			System.exit(3);
		}
		if (message == null) {
			System.err.printf("ERROR: Message not specified: [-m]. Command aborted.%n");
			System.exit(3);
		}

		// Collection system properties - passed in by the invoking script.
		if (configFilePath == null && clusterName == null) {
			clusterName = System.getProperty("cluster.name");
		}
		if (endpoints == null) {
			endpoints = System.getProperty("cluster.endpoints");
		}

		// Display all options
		if (clusterName != null) {
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
		writeLine("cluster: " + virtualClusterName + " (virtual)");

		// If endpoints is not set, then default to
		// IClusterConfig.DEFAULT_CLIENT_SERVER_URIS.
		if (configFilePath == null && endpoints == null) {
			endpoints = IClusterConfig.DEFAULT_CLIENT_SERVER_URIS;
		}
		if (endpoints != null) {
			writeLine("endpoints: " + endpoints);
		}
		writeLine("fos: " + fos);
		writeLine("qos: " + qos);
		if (configFilePath != null) {
			writeLine("config: " + configFilePath);
		}
		writeLine("topic: " + topic);
		if (endpointName != null) {
			writeLine("name: " + endpointName);
		}
		writeLine("message: " + message);

		// Create cluster
		HaMqttClient client = null;
		if (configFilePath == null) {
			ClusterConfig clusterConfig = new ClusterConfig();
			clusterConfig.setDefaultCluster(virtualClusterName);
			MqttConnectionOptions options = new MqttConnectionOptionsBuilder().serverURI(endpoints).build();
			ClusterConfig.Cluster cluster = new ClusterConfig.Cluster();
			cluster.setName(virtualClusterName);
			cluster.setFos(fos);
			cluster.setConnection(options);
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
			System.err.printf("ERROR: Unable to create the specified cluster. Command aborted.%n");
			System.exit(-1);
		}

		// Connect
		try {
			client.connect();
			if (client.isConnected() == false) {
				System.err
						.printf("ERROR: Unable to connect to any of the endpoints in the cluster. Command aborted.%n");
				HaClusters.stop();
				System.exit(-1);
			}
			if (endpointName != null) {
				client.publish(endpointName, topic, message.getBytes(), qos, isRetained);
			} else {
				client.publish(topic, message.getBytes(), qos, isRetained);
			}
			HaClusters.stop();
			System.exit(0);
		} catch (Exception e) {
			System.err.printf("ERROR: Error occured while publishing data. %s Command aborted.%n", e.getMessage());
			HaClusters.stop();
			System.exit(-2);
		}
	}
}
