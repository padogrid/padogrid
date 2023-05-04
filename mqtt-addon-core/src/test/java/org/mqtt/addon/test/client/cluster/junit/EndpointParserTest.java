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

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mqtt.addon.client.cluster.IClusterConfig;
import org.mqtt.addon.client.cluster.internal.ConfigUtil;

/**
 * EndpointParserTest tests {@linkplain ConfigUtil#parseEndpoints(String[])}.
 * 
 * @author dpark
 *
 */
public class EndpointParserTest implements IClusterConfig {
	private List<String> buildExpectedEnpointList(int startAddress, int stopAddress, int startPort, int endPort) {
		List<String> expectedList = new ArrayList<String>();
		for (int i = startAddress; i <= stopAddress; i++) {
			if (startPort == -1) {
				String endpoint = String.format("tcp://192.168.1.%d", i);
				expectedList.add(endpoint);
			} else {
				for (int j = startPort; j <= endPort; j++) {
					String endpoint = String.format("tcp://192.168.1.%d:%d", i, j);
					expectedList.add(endpoint);
				}
			}
		}
		return expectedList;
	}

	private List<String> buildExpectedEnpointList(String hostName, int startPort, int endPort) {
		List<String> expectedList = new ArrayList<String>();
		if (startPort == -1) {
			String endpoint = String.format("tcp://%s", hostName);
			expectedList.add(endpoint);
		} else {
			for (int j = startPort; j <= endPort; j++) {
				String endpoint = String.format("tcp://%s:%d", hostName, j);
				expectedList.add(endpoint);
			}
		}
		return expectedList;
	}

	@Test
	public void testEnpointParser1() {
		String[] endpoints = new String[] { "tcp://192.168.1.10-12:1883-1893" };
		List<String> expectedList = buildExpectedEnpointList(10, 12, 1883, 1893);
		List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
		assertArrayEquals(expectedList.toArray(), endpointList.toArray());
	}

	@Test
	public void testEnpointParser2() {
		String[] endpoints = new String[] { "tcp://192.168.1.10:1883-1893" };
		List<String> expectedList = buildExpectedEnpointList(10, 10, 1883, 1893);
		List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
		assertArrayEquals(expectedList.toArray(), endpointList.toArray());
	}

	@Test
	public void testEnpointParser3() {
		String[] endpoints = new String[] { "tcp://192.168.1.10:1883" };
		List<String> expectedList = buildExpectedEnpointList(10, 10, 1883, 1883);
		List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
		assertArrayEquals(expectedList.toArray(), endpointList.toArray());
	}

	@Test
	public void testEnpointParser4() {
		String[] endpoints = new String[] { "tcp://192.168.1.10" };
		List<String> expectedList = buildExpectedEnpointList(10, 10, -1, -1);
		List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
		assertArrayEquals(expectedList.toArray(), endpointList.toArray());
	}

	@Test
	public void testEnpointParser5() {
		String[] endpoints = new String[] { "tcp://localhost:1883-1893" };
		List<String> expectedList = buildExpectedEnpointList("localhost", 1883, 1893);
		List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
		assertArrayEquals(expectedList.toArray(), endpointList.toArray());
	}

	@Test
	public void testEnpointParser6() {
		String[] endpoints = new String[] { "tcp://localhost:1883" };
		List<String> expectedList = buildExpectedEnpointList("localhost", 1883, 1883);
		List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
		assertArrayEquals(expectedList.toArray(), endpointList.toArray());
	}

	@Test
	public void testEnpointParser7() {
		String[] endpoints = new String[] { "tcp://localhost" };
		List<String> expectedList = buildExpectedEnpointList("localhost", -1, -1);
		List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
		assertArrayEquals(expectedList.toArray(), endpointList.toArray());
	}

	@Test
	public void testEnpointParser8() {
		String[] endpoints = new String[] { "tcp://mqtt.org:1883-1893" };
		List<String> expectedList = buildExpectedEnpointList("mqtt.org", 1883, 1893);
		List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
		assertArrayEquals(expectedList.toArray(), endpointList.toArray());
	}

	@Test
	public void testEnpointParser9() {
		String[] endpoints = new String[] { "tcp://mqtt.org:1883" };
		List<String> expectedList = buildExpectedEnpointList("mqtt.org", 1883, 1883);
		List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
		assertArrayEquals(expectedList.toArray(), endpointList.toArray());
	}

	@Test
	public void testEnpointParser10() {
		String[] endpoints = new String[] { "tcp://mqtt.org" };
		List<String> expectedList = buildExpectedEnpointList("mqtt.org", -1, -1);
		List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
		assertArrayEquals(expectedList.toArray(), endpointList.toArray());
	}
	
	@Test
	public void testEnpointParser11() {
		String[] endpoints = new String[] { "tcp://p-1.newco.com:1883-1885" };
		List<String> expectedList = buildExpectedEnpointList("p-1.newco.com", 1883, 1885);
		List<String> endpointList = ConfigUtil.parseEndpoints(endpoints);
		assertArrayEquals(expectedList.toArray(), endpointList.toArray());
	}
}
