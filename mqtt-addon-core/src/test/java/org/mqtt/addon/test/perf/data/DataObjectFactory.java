package org.mqtt.addon.test.perf.data;

import java.util.Properties;

/**
 * DataObjectFactory provides data ingestion properties for creating MQTT
 * payloads.
 * 
 * @author dpark
 *
 */
public interface DataObjectFactory {

	/**
	 * Initializes the data object factory with the specified properties.
	 * 
	 * @param props Data object factory properties
	 */
	public void initialize(Properties props);

	/**
	 * @return the data object class
	 */
	public Class<?> getDataObjectClass();

	/**
	 * Creates a new MQTT payload with the specified ID.
	 * 
	 * @param idNum Optional ID for constructing a unique payload
	 * @return an MQTT payload
	 */
	public byte[] createPayload(int idNum);
}
