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

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import padogrid.mqtt.client.cluster.HaClusters;
import padogrid.mqtt.client.cluster.HaMqttClient;
import padogrid.mqtt.client.cluster.IClusterConfig;

/**
 * FosTest loads in etc/mqttv5-fos.yaml, which defines clusters with all
 * possible FoS options. To run the test case, follow the steps below.
 * <ul>
 * <li>Start cluster with tcp://localhost:1883-1885 ports</li>
 * </ul>
 * <p>
 * The following configuration file is used for this test case: <br>
 * <ul>
 * <li>etc/mqttv5-fos.yaml</li>
 * </ul>
 * 
 * @author dpark
 *
 */
public class FosTest {

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE, "etc/mqttv5-fos.yaml");
		TestUtil.setEnv("LOG_FILE", "log/proxy.log");
		System.setProperty("log4j.configurationFile", "etc/log4j2.properties");
		HaClusters.initialize();
		HaClusters.connect();
	}

	@Test
	public void testFos1() throws Exception {
		HaMqttClient proxy1 = HaClusters.getHaMqttClient("proxy1");
		HaMqttClient custom1 = HaClusters.getHaMqttClient("custom1");
		String[] proxy1URIs = proxy1.getCurrentServerURIs();
		String[] custom1URIs = custom1.getCurrentServerURIs();
		System.out.printf("proxy1=%d, custom1=%d%n", proxy1URIs.length, custom1URIs.length);
		assertTrue(proxy1URIs.length == custom1URIs.length);
	}

	@Test
	public void testFos2() throws Exception {
		HaMqttClient proxy2 = HaClusters.getHaMqttClient("proxy2");
		HaMqttClient custom2 = HaClusters.getHaMqttClient("custom2");
		String[] proxy2URIs = proxy2.getCurrentServerURIs();
		String[] custom2URIs = custom2.getCurrentServerURIs();
		System.out.printf("proxy2=%d, custom2=%d%n", proxy2URIs.length, custom2URIs.length);
		assertTrue(proxy2URIs.length == custom2URIs.length);
	}

	@Test
	public void testFos3() throws Exception {
		HaMqttClient proxy3 = HaClusters.getHaMqttClient("proxy3");
		HaMqttClient custom3 = HaClusters.getHaMqttClient("custom3");
		String[] proxy3URIs = proxy3.getCurrentServerURIs();
		String[] custom3URIs = custom3.getCurrentServerURIs();
		System.out.printf("proxy3=%d, custom3=%d%n", proxy3URIs.length, custom3URIs.length);
		assertTrue(proxy3URIs.length == custom3URIs.length);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		HaClusters.close();
	}
}
