package org.mqtt.addon.client.cluster.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.client.persist.MqttDefaultFilePersistence;

public class Persistence {
	private MqttClientPersistence mqttClientPersistence;
	private String className;
	private Properties props = new Properties();
	private Property[] properties;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Returns a MqttClientPersistence instance of {@link #getClassName()}. It
	 * returns null if the class name is undefined, i.e., null.
	 * 
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public MqttClientPersistence getMqttClientPersistence()
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//		if (mqttClientPersistence == null) {
			if (properties != null) {
				for (Property property : properties) {
					if (property != null && property.getKey() != null && property.getValue() != null) {
						props.setProperty(property.getKey(), property.getValue());
					}
				}
			}
			if (className != null) {
				if (className.equals("MqttDefaultFilePersistence")) {
					String path = props.getProperty("path");
					if (path != null) {
						mqttClientPersistence = new MqttDefaultFilePersistence(path);
					}
				} else if (className.equals("MqttDefaultFilePersistence")) {
					mqttClientPersistence = new MemoryPersistence();
				} else {
					Class<?> clazz = Class.forName(className);
					Constructor<?> constructor = clazz.getConstructor(Properties.class);
					mqttClientPersistence = (MqttClientPersistence) constructor.newInstance(props);
				}
			}
//		}
		return mqttClientPersistence;
	}
}
