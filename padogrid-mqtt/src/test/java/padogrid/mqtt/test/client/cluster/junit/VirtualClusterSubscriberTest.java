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

import padogrid.mqtt.client.console.ClusterSubscriber;

/**
 * Subscribes to the topic filter, "test/#" via {@linkplain ClusterSubscriber}.
 * It stops when the shutdown hook is invoked by JUnit.
 *
 */
public class VirtualClusterSubscriberTest {

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty("executable.name", "vc_publisher");
		System.setProperty("java.util.logging.config.file", "etc/subscriber-logging.properties");
		TestUtil.setEnv("LOG_FILE", "log/vc_subscribe.log");
		System.setProperty("log4j.configurationFile", "etc/log4j2.properties");
	}

	@Test
	public void testVirtualClustersSubscriberConfig() throws InterruptedException {
		ClusterSubscriber.main("-config", "etc/mqttv5-subscriber.yaml", "-t", "test/#");
	}

//	@Test
//	public void testVirtualClustersSubscriberEndpoints() throws InterruptedException {
//		ClusterSubscriber.main("-endpoints", "tcp://test.mosquitto.org:1883, ws://test.mosquitto.org:8080, tcp://broker.emqx.io:1883, ws://broker.emqx.io:8083, tcp://broker.hivemq.com:1883, ws://broker.hivemq.com:8000", "-t", "padogrid/#");
//	}
}
