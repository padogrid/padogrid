package org.hazelcast.addon.cluster;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.hazelcast.client.impl.proxy.ClientReliableTopicProxy;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.replicatedmap.ReplicatedMap;
import com.hazelcast.topic.ITopic;

/**
 * ClusterUtil provides cluster/member specific convenience methods.
 * @author dpark
 *
 */
public class ClusterUtil {
	/**
	 * Returns all reliable topics defined in the cluster.
	 * 
	 * @param hz Hazelcast instance
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, ITopic> getAllReliableTopics(HazelcastInstance hz) {
		TreeMap<String, ITopic> mapOfTopics = new TreeMap<String, ITopic>();
		Collection<DistributedObject> col = hz.getDistributedObjects();
		for (DistributedObject dobj : col) {
			if (dobj instanceof ITopic) {
				ITopic topic = (ITopic)dobj;
				if (topic instanceof ClientReliableTopicProxy) {
					mapOfTopics.put(topic.getName(), topic);
				}
			}
		}
		return mapOfTopics;
	}
	
	/**
	 * Returns all non-reliable topics defined in the cluster.
	 * 
	 * @param hz Hazelcast instance
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, ITopic> getAllTopics(HazelcastInstance hz) {
		TreeMap<String, ITopic> mapOfTopics = new TreeMap<String, ITopic>();
		Collection<DistributedObject> col = hz.getDistributedObjects();
		for (DistributedObject dobj : col) {
			if (dobj instanceof ITopic) {
				ITopic topic = (ITopic)dobj;
				if (topic instanceof ClientReliableTopicProxy == false) {
					mapOfTopics.put(topic.getName(), topic);
				}
			}
		}
		return mapOfTopics;
	}

	/**
	 * Returns all IMaps defined in the cluster.
	 * 
	 * @param hz Hazelcast instance
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, IMap> getAllMaps(HazelcastInstance hz) {
		TreeMap<String, IMap> mapOfMaps = new TreeMap<String, IMap>();
		Collection<DistributedObject> col = hz.getDistributedObjects();
		for (DistributedObject dobj : col) {
			if (dobj instanceof IMap) {
				mapOfMaps.put(((IMap) dobj).getName(), (IMap) dobj);
			}
		}
		return mapOfMaps;
	}
	
	/**
	 * Returns all ReplicatedMap defined in the cluster.
	 * 
	 * @param hz Hazelcast instance
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, ReplicatedMap> getAllReplicatedMaps(HazelcastInstance hz) {
		TreeMap<String, ReplicatedMap> mapOfRMaps = new TreeMap<String, ReplicatedMap>();
		Collection<DistributedObject> col = hz.getDistributedObjects();
		for (DistributedObject dobj : col) {
			if (dobj instanceof ReplicatedMap) {
				mapOfRMaps.put(((ReplicatedMap) dobj).getName(), (ReplicatedMap) dobj);
			}
		}
		return mapOfRMaps;
	}
}
