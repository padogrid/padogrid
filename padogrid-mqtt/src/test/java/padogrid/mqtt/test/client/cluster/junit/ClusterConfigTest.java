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
package padogrid.mqtt.test.client.cluster.junit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;

import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import padogrid.mqtt.client.cluster.IClusterConfig;
import padogrid.mqtt.client.cluster.config.ClusterConfig;
import padogrid.mqtt.client.cluster.internal.ConfigUtil;

/**
 * ClusterConfigTest tests {@linkplain ClusterConfig}.
 * 
 * @author dpark
 *
 */
public class ClusterConfigTest implements IClusterConfig {
	@Test
	public void testConfig() {
		System.setProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE, "src/main/resources/mqttv5-client.yaml");
		String configFile = System.getProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE);
		File file = new File(configFile);
		if (file.exists() == false) {
			Assert.fail(String.format("Config file not found [%s]", configFile));
		}
		try {
			Yaml yaml = new Yaml(new Constructor(ClusterConfig.class));
			yaml.setBeanAccess(BeanAccess.FIELD);
			FileReader reader = new FileReader(file);
			ClusterConfig config = yaml.load(reader);
			System.out.println(config);
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}
	
	@Test
	public void testShuffleEndpoints() {
		int count = 10;
		int expectedSum = 0;
		for (int i = 0; i < count; i++) {
			expectedSum += i;
		}
		
		int[] shuffled = ConfigUtil.shuffleRandom(count);
		int sum = 0;
		for (int i = 0; i < shuffled.length; i++) {
			sum += shuffled[i];
		}
		for (int i = 0; i < shuffled.length; i++) {
			System.out.println(String.format("shuffled[%d]=%d", i, shuffled[i]));
		}
		assertEquals(expectedSum, sum);
	}
}
