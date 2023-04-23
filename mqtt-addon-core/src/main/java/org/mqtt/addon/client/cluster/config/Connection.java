package org.mqtt.addon.client.cluster.config;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;

import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

class Connection {

	// Connection Behaviour Properties
	private String[] serverURIs = null; // List of Servers to connect to in order
	private boolean automaticReconnect = false; // Automatic Reconnect
	private int automaticReconnectMinDelay = 1; // Time to wait before first automatic reconnection attempt in seconds.
	private int automaticReconnectMaxDelay = 120; // Max time to wait for automatic reconnection attempts in seconds.
	private boolean useSubscriptionIdentifiers = true; // Whether to automatically assign subscription identifiers.
	private int keepAliveInterval = 60; // Keep Alive Interval
	private int connectionTimeout = 30; // Connection timeout in seconds
	private boolean httpsHostnameVerificationEnabled = true;
	private int maxReconnectDelay = 128000;
	private boolean sendReasonMessages = false;
	
	MqttProperties willMessageProperties = new MqttProperties();

	// Connection packet properties
	private boolean cleanStart = true; // Clean Session
	private String willDestination = null; // Will Topic
	private MqttMessage willMessage = null; // Will Message
	private String userName; // Username
	private byte[] password; // Password
	private Long sessionExpiryInterval = null; // The Session expiry Interval in seconds, null is the default of
												// never.
	private Integer receiveMaximum = null; // The Receive Maximum, null defaults to 65,535, cannot be 0.
	private Long maximumPacketSize = null; // The Maximum packet size, null defaults to no limit.
	private Integer topicAliasMaximum = null; // The Topic Alias Maximum, null defaults to 0.
	private Boolean requestResponseInfo = null; // Request Response Information, null defaults to false.
	private Boolean requestProblemInfo = null; // Request Problem Information, null defaults to true.
	private List<UserProperty> userProperties = null; // User Defined Properties.
	private String authMethod = null; // Authentication Method, If null, Extended Authentication is not performed.
	private byte[] authData = null; // Authentication Data.

	// TLS Properties
	private SocketFactory socketFactory; // SocketFactory to be used to connect
	private Properties sslClientProps = null; // SSL Client Properties
	private HostnameVerifier sslHostnameVerifier = null; // SSL Hostname Verifier
	private Map<String, String> customWebSocketHeaders;

	// Client Operation Parameters
	private int executorServiceTimeout = 1; // How long to wait in seconds when terminating the executor service.

	public String getAuthMethod() {
		return authMethod;
	}

	public void setAuthMethod(String authMethod) {
		this.authMethod = authMethod;
	}

	public boolean getAutomaticReconnect() {
		return automaticReconnect;
	}

	public void setAutomaticReconnect(boolean automaticReconnect) {
		this.automaticReconnect = automaticReconnect;
	}

}