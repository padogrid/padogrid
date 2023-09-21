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

import org.junit.BeforeClass;
import org.junit.Test;

import padogrid.mqtt.client.console.ClusterPublisher;

/**
 * Publishes message via {@linkplain ClusterPublisher}.
 * @author dpark
 *
 */
public class VirtualClusterPublisherTest {

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty("executable.name", "vc_publisher");
		System.setProperty("java.util.logging.config.file", "etc/publisher-publisher-logging.properties");
		TestUtil.setEnv("LOG_FILE", "log/vc_publish.log");
		System.setProperty("log4j.configurationFile", "etc/log4j2.properties");
	}

	@Test
	public void testVirtualClustersPublisherConfig() throws InterruptedException {
		ClusterPublisher.main("-config", "etc/mqttv5-publisher.yaml", "-t", "test/message", "-m", "hello world");
	}
	
	@Test
	public void testVirtualClustersPublisherEndpoints() throws InterruptedException {
		ClusterPublisher.main("-endpoints", "tcp://localhost:1883-1885", "-t", "test/message", "-m", "hello world");
	}
}
