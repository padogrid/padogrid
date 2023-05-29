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
package padogrid.mqtt.client.cluster;

/**
 * PublisherType is used to load-balance publishing messages to the cluster.
 * 
 * @author dpark
 *
 */
public enum PublisherType {
	/**
	 * Sticks to the primary server, if defined, or the first endpoint found in the
	 * live endpoint list for all "publish" operations. If the primary server is
	 * defined then it always takes a priority over other servers. Upon
	 * reconnection, the primary server is used again for all operations. If the
	 * primary server is not defined, then the next server in the endpoint list is
	 * used upon failure. The live endpoint list is initially shuffled once to provide
	 * randomness.
	 */
	STICKY,

	/**
	 * Randomly selects a broker from the live endpoint list for every publisher
	 * operation. This is a random load balancer. It provides the true
	 * randomness but is the most expensive publisher type as it generates a random
	 * number per publisher operation.
	 */
	RANDOM,

	/**
	 * Selects a broker from the live endpoint list in a round-robin fashion for every
	 * publisher operation. The live endpoint list is initially shuffled once to provide
	 * randomness. This is a round-robin load balancer.
	 */
	ROUND_ROBIN,
	
	/**
	 * Selects all brokers from the live endpoint list for every publisher operations.
	 */
	ALL
}