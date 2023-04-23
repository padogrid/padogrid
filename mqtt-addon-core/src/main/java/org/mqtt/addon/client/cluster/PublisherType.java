package org.mqtt.addon.client.cluster;

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
	 * reconnection, the primary server is used again all operations. If the primary
	 * server is not defined, then the next server in the endpoint list is used
	 * upon failure.
	 */
	STICKY,

	/**
	 * Randomly selects a broker from the endpoint list for every publisher
	 * operation. This is a random load balancer.
	 */
	RANDOM,

	/**
	 * Selects a broker from the endpoint list in a round-robin fashion for every
	 * publisher operation. This is a round-robin load balancer.
	 */
	ROUND_ROBIN
}