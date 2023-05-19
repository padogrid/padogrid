package org.mqtt.addon.client.cluster.internal;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttClientInterface;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.MqttWireMessage;

/**
 * A dummy token class used as a marker for revival. It is provided solely to
 * satisfy the {@linkplain MqttClient#connectWithResult} method which returns
 * {@linkplain IMqttToken}. The application should never get an instance of this
 * class unless a shared endpoint connection fails during a probing cycle. (See
 * liveEndpointPoolEnabled in configuration.)
 */
public class SharedMqttToken implements IMqttToken {

	@Override
	public void waitForCompletion() throws MqttException {
		// TODO Auto-generated method stub

	}

	@Override
	public void waitForCompletion(long timeout) throws MqttException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isComplete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MqttException getException() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setActionCallback(MqttActionListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public MqttActionListener getActionCallback() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MqttClientInterface getClient() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getTopics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUserContext(Object userContext) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getUserContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMessageId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getGrantedQos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getReasonCodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getSessionPresent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MqttWireMessage getResponse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MqttProperties getResponseProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MqttMessage getMessage() throws MqttException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MqttWireMessage getRequestMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MqttProperties getRequestProperties() {
		// TODO Auto-generated method stub
		return null;
	}

}
