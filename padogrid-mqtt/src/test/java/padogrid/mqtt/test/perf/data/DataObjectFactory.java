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
package padogrid.mqtt.test.perf.data;

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
