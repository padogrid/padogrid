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
package padogrid.mqtt.client.cluster.internal;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;

import padogrid.mqtt.client.cluster.HaMqttClient;

/**
 * ConfigUtil contains convenience methods for configuring
 * {@linkplain HaMqttClient}.
 * 
 * @author dpark
 *
 */
public final class ConfigUtil {
	/**
	 * Replaces a system property defined by <code>${property}</code> and
	 * environment variable defined by <code>${env:envar}</code> with the respective
	 * values. Only one of each in the specified value is supported, i.e., multiple
	 * properties or multiple environment variables are not supported.
	 * 
	 * @param value String value with a system property and/or environment variable.
	 */
	public final static String parseStringValue(String value) {
		String pvalue = value;
		if (pvalue != null) {
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
			if (pvalue.contains("${")) {
				String property = pvalue.replaceAll("^.*\\$\\{", "");
				property = property.replaceAll("}.*", "");
				if (property != "") {
					String value2 = System.getProperty(property, "");
					pvalue = pvalue.replaceAll("\\$\\{.*\\}", value2);
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
	public final static List<String> parseEndpoints(String endpoints) {
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
	public final static List<String> parseEndpoints(String[] endpoints) {
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

				// Determine whether IP address or host name.
				String[] addressParts = addressRange.split("\\.");
				boolean isHostName = false;
				String hostName = null;
				for (int i = 0; i < addressParts.length; i++) {
					try {
						int octet = Integer.parseInt(addressParts[i]);
					} catch (NumberFormatException ex) {
						if (i != 3) {
							isHostName = true;
							hostName = addressRange;
						}
					}
				}

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

				if (isHostName) {
					String address = hostName;
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
					// Set firstPart without the last octet
					String firstPart = "";
					for (int i = 0; i < addressParts.length - 1; i++) {
						firstPart += addressParts[i] + ".";
					}

					// Set last octet
					String lastOctet = addressParts[addressParts.length - 1];

					// Determine start and end octets
					int startOctet;
					int endOctet;
					String octets[] = lastOctet.split("-");
					startOctet = Integer.parseInt(octets[0]);
					endOctet = startOctet;
					if (octets.length > 1) {
						endOctet = Integer.parseInt(octets[1]);
					}

					// Set endpoints
					for (int octet = startOctet; octet <= endOctet; octet++) {
						if (startPort == -1) {
							String ep = String.format("%s://%s%d", protocol, firstPart, octet);
							endpointList.add(ep);
						} else {
							for (int port = startPort; port <= endPort; port++) {
								String ep = String.format("%s://%s%d:%d", protocol, firstPart, octet, port);
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
	public final static int[] shuffleRandom(int count) {
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

	public final static SSLSocketFactory getSocketFactory(final Logger logger, final String sslProtocol,
			final String caCertFile, final String certFile, final String keyFile, final String password)
			throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException,
			UnrecoverableKeyException, KeyManagementException {
		Security.addProvider(new BouncyCastleProvider());

		// load CA certificate
		X509Certificate caCert = null;

		FileInputStream fis = new FileInputStream(caCertFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		while (bis.available() > 0) {
			caCert = (X509Certificate) cf.generateCertificate(bis);
			// System.out.println(caCert.toString());
		}

		// load client certificate
		bis = new BufferedInputStream(new FileInputStream(certFile));
		X509Certificate cert = null;
		while (bis.available() > 0) {
			cert = (X509Certificate) cf.generateCertificate(bis);
			// System.out.println(caCert.toString());
		}

		// load client private key
		PEMParser pemParser = new PEMParser(new FileReader(keyFile));
		Object object = pemParser.readObject();
		String pwd = password;
		if (pwd == null) {
			pwd = "";
		}
		PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(pwd.toCharArray());
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
		KeyPair keyPair = null;
		PrivateKey privateKey = null;
		if (logger.isDebugEnabled()) {
			logger.debug("SSLSocketFactory: KeyPair has the type " + object.getClass());
		}
		if (object instanceof PEMEncryptedKeyPair) {
			if (logger.isDebugEnabled())
				logger.debug("SSLSocketFactory: Encrypted key - Provided password used");
			keyPair = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
			privateKey = keyPair.getPrivate();
		} else if (object instanceof PrivateKeyInfo) {
			if (logger.isDebugEnabled())
				logger.debug("PrivateKeyInfo");
			privateKey = converter.getPrivateKey((PrivateKeyInfo)object);
		} else {
			if (logger.isDebugEnabled())
				logger.debug("SSLSocketFactory: Unencrypted key - no password needed");
			keyPair = converter.getKeyPair((PEMKeyPair) object);
			privateKey = keyPair.getPrivate();
		}
		pemParser.close();

		// CA certificate is used to authenticate server
		KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
		caKs.load(null, null);
		caKs.setCertificateEntry("ca-certificate", caCert);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
		tmf.init(caKs);

		// client key and certificates are sent to server so it can authenticate
		// us
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("certificate", cert);
		ks.setKeyEntry("private-key", privateKey, pwd.toCharArray(),
				new java.security.cert.Certificate[] { cert });
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, pwd.toCharArray());

		// finally, create SSL socket factory
		String protocol;
		if (sslProtocol == null) {
			protocol = "TLSv1.2";
		} else {
			protocol = sslProtocol;
		}
		SSLContext context = SSLContext.getInstance(protocol);
		context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		return context.getSocketFactory();
	}

	/**
	 * Returns a shallow copy of the specified options.
	 * @param options MqttConnectionOptions object to clone
	 * @return null if the specified options is null
	 */
	public final static MqttConnectionOptions cloneMqttConnectionOptions(MqttConnectionOptions options) {
		if (options == null) {
			return null;
		}
		MqttConnectionOptions cloned = new MqttConnectionOptions();
		cloned.setAuthData(options.getAuthData());
		cloned.setAuthMethod(options.getAuthMethod());
		cloned.setAutomaticReconnect(options.isAutomaticReconnect());
		cloned.setAutomaticReconnectDelay(options.getAutomaticReconnectMaxDelay(), options.getAutomaticReconnectMaxDelay());
		cloned.setCleanStart(options.isCleanStart());
		cloned.setConnectionTimeout(options.getConnectionTimeout());
		cloned.setCustomWebSocketHeaders(options.getCustomWebSocketHeaders());
		cloned.setExecutorServiceTimeout(options.getExecutorServiceTimeout());
		cloned.setHttpsHostnameVerificationEnabled(options.isHttpsHostnameVerificationEnabled());
		cloned.setKeepAliveInterval(options.getKeepAliveInterval());
		cloned.setMaximumPacketSize(options.getMaximumPacketSize());
		cloned.setMaxReconnectDelay(options.getMaxReconnectDelay());
		cloned.setPassword(options.getPassword());
		cloned.setReceiveMaximum(options.getReceiveMaximum());
		cloned.setRequestProblemInfo(options.getRequestProblemInfo());
		cloned.setRequestResponseInfo(options.getRequestResponseInfo());
		cloned.setSendReasonMessages(options.isSendReasonMessages());
		cloned.setServerURIs(options.getServerURIs());
		cloned.setSessionExpiryInterval(options.getSessionExpiryInterval());
		cloned.setSocketFactory(options.getSocketFactory());
		cloned.setSSLHostnameVerifier(options.getSSLHostnameVerifier());
		cloned.setSSLProperties(options.getSSLProperties());
		cloned.setTopicAliasMaximum(options.getTopicAliasMaximum());
		cloned.setUserName(options.getUserName());
		cloned.setUserProperties(options.getUserProperties());
		cloned.setUseSubscriptionIdentifiers(options.useSubscriptionIdentifiers());
		cloned.setWill(options.getWillDestination(), options.getWillMessage());
		cloned.setWillMessageProperties(options.getWillMessageProperties());
		return cloned;
	}
}
