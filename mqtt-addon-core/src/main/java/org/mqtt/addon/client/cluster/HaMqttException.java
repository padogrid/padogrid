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
package org.mqtt.addon.client.cluster;

import org.eclipse.paho.mqttv5.common.MqttException;

/**
 * HaMqttException is used for throwing HaMqttClient specific exceptions.
 * 
 * @author dpark
 *
 */
public class HaMqttException extends MqttException {

	private static final long serialVersionUID = 1L;
	private String message;

	public HaMqttException(int reasonCode, String message) {
		super(reasonCode);
		this.message = message;
	}

	public HaMqttException(Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		return message;
	}
}
