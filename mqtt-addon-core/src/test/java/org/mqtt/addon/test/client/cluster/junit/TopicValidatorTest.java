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
package org.mqtt.addon.test.client.cluster.junit;

import static org.junit.Assert.assertEquals;

import org.eclipse.paho.mqttv5.common.util.MqttTopicValidator;
import org.junit.Test;
import org.mqtt.addon.client.cluster.IClusterConfig;
import org.mqtt.addon.client.cluster.internal.ConfigUtil;

/**
 * EndpointParserTest tests {@linkplain ConfigUtil#parseEndpoints(String[])}.
 * 
 * @author dpark
 *
 */
public class TopicValidatorTest implements IClusterConfig {

	private void validate(String topicFilter, String topic, boolean isExpected) {
		boolean isMatched =  MqttTopicValidator.isMatched(topicFilter, topic);
		assertEquals(isExpected, isMatched);
	}
	
	@Test
	public void testTopicValidator() {
		String topicFilter = "city/+/house/+";
		validate(topicFilter, "city/street/house/livingroom", true);
		validate(topicFilter, "nyc/street/house/kitchen", true);
	}
}
