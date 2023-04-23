package org.mqtt.addon.client.cluster.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigUtil {
	/**
	 * Replaces a system property defined by ${property} and environment variable
	 * defined by ${env: envar} with the respective values. Only one of each is
	 * supported, i.e., multiple properties or multiple environment variables are
	 * not supported.
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
}
