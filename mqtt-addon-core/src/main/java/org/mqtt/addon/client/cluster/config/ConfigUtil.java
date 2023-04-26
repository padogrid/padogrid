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
package org.mqtt.addon.client.cluster.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConfigUtil {
	/**
	 * Replaces a system property defined by <code>${property}</code> and
	 * environment variable defined by <code>${env:envar}</code> with the respective
	 * values. Only one of each in the specified value is supported, i.e., multiple
	 * properties or multiple environment variables are not supported.
	 * 
	 * @param value String value with a system property and/or environment variable.
	 */
	public static String parseStringValue(String value) {
		String pvalue = value;
		if (pvalue != null) {
			if (pvalue.contains("${")) {
				String property = pvalue.replaceAll("^.*\\$\\{", "");
				property = property.replaceAll("}.*", "");
				if (property != "") {
					String value2 = System.getProperty(property, "");
					pvalue = pvalue.replaceAll("\\$\\{.*\\}", value2);
				}
			}
			if (pvalue.contains("${env:")) {
				String envvar = pvalue.replaceAll("^.*\\$\\{env:", "");
				envvar = envvar.replaceAll("}.*", "");
				if (envvar != "") {
					String value2 = System.getenv(envvar);
					if (value2 == null) {
						value2 = "";
					}
					pvalue = pvalue.replaceAll("\\$\\{env:.*\\}", value2);
				}
			}
			pvalue = pvalue.trim();
		}
		return pvalue;
	}

	/**
	 * Returns a list of parsed endpoints in the form of
	 * &#9001;protocol&#9002;://&#9001;address&#9002;[:&#9001;port&#9002;]. The port
	 * number is optional.
	 * 
	 * @param endpoints A comma separated endpoints. Each endpoint may have the last
	 *                  IPv4 octect in a range in addition to a range of port
	 *                  numbers, e.g., <code>tcp://10.1.2.10-12:32000-32010</code>.
	 */
	public static List<String> parseEndpoints(String endpoints) {
		String[] split = endpoints.split(",");
		ArrayList<String> list = new ArrayList<String>(split.length);
		for (String endpoint : split) {
			endpoint = endpoint.trim();
			if (list.contains(endpoint) == false) {
				list.add(endpoint);
			}
		}
		return parseEndpoints(list.toArray(new String[0]));
	}

	/**
	 * Returns a list of parsed endpoints in the form of
	 * &#9001;protocol&#9002;://&#9001;address&#9002;[:&#9001;port&#9002;]. The port
	 * number is optional.
	 * 
	 * @param endpoints An array of endpoints. Each endpoint may have the last IPv4
	 *                  octect in a range in addition to a range of port numbers,
	 *                  e.g., <code>tcp://10.1.2.10-12:32000-32010</code>.
	 */
	public static List<String> parseEndpoints(String[] endpoints) {
		List<String> endpointList = new ArrayList<String>(10);

		if (endpoints != null) {

			// tcp://localhost:1883
			// tcp://192.168.1.10-20:1883-1893
			for (String endpoint : endpoints) {
				String protocol = endpoint.replaceAll(":.*", "");
				String tmp = endpoint.replaceAll(".*\\/\\/", "");
				String addressRange = tmp.replaceAll(":.*", "");
				String portRange = null;
				if (tmp.contains(":")) {
					portRange = endpoint.replaceAll(".*:", "");
				}

				String[] addressParts = addressRange.split("-");

				// Determine port range
				int startPort = -1;
				int endPort = -1;
				if (portRange != null) {
					String[] portParts = portRange.split("-");
					if (portParts.length > 0) {
						try {
							startPort = Integer.parseInt(portParts[0]);
						} catch (NumberFormatException ex) {
							// ignore
						}
					}
					endPort = startPort;
					if (portParts.length == 2) {
						try {
							endPort = Integer.parseInt(portParts[1]);
						} catch (NumberFormatException ex) {
							// ignore
						}
					}
				}

				int index = addressParts[0].lastIndexOf(".");

				// Determine whether IP address or host name.
				boolean isHostName = index == -1;
				int startOctet = -1;
				if (isHostName == false) {
					String startOctetStr = addressParts[0].substring(index + 1);
					try {
						startOctet = Integer.parseInt(startOctetStr);
					} catch (NumberFormatException ex) {
						isHostName = true;
					}
				}
				if (isHostName) {
					String address = addressParts[0];
					if (startPort == -1) {
						String ep = String.format("%s://%s", protocol, address);
						endpointList.add(ep);
					} else {
						for (int port = startPort; port <= endPort; port++) {
							String ep = String.format("%s://%s:%d", protocol, address, port);
							endpointList.add(ep);
						}
					}
				} else {
					String firstPart = addressParts[0].substring(0, index);
					int endOctet = startOctet;
					if (addressParts.length == 2) {
						endOctet = Integer.parseInt(addressParts[1]);
					}
					for (int octet = startOctet; octet <= endOctet; octet++) {
						if (startPort == -1) {
							String ep = String.format("%s://%s.%d", protocol, firstPart, octet);
							endpointList.add(ep);
						} else {
							for (int port = startPort; port <= endPort; port++) {
								String ep = String.format("%s://%s.%d:%d", protocol, firstPart, octet, port);
								endpointList.add(ep);
							}
						}
					}
				}
			}
		}

		return endpointList;
	}

	/**
	 * Returns an array of randomly selected integer values from 0 to count-1.
	 * 
	 * @param count Number of integer values.
	 * @return An empty array if the specified count is less than or equal to 0.
	 */
	public static int[] shuffleRandom(int count) {
		if (count <= 0) {
			return new int[0];
		}
		Random random = new Random();
		int[] shuffled = new int[count];
		for (int i = 0; i < count; i++) {
			shuffled[i] = -1;
		}
		for (int i = 0; i < count; i++) {
			boolean assigned = false;
			while (assigned == false) {
				int index = random.nextInt(count);
				if (shuffled[index] == -1) {
					shuffled[index] = i;
					assigned = true;
				}
			}
		}
		return shuffled;
	}
}
