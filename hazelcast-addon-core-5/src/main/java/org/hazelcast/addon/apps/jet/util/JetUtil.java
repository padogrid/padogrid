package org.hazelcast.addon.apps.jet.util;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
/** 
 * JetUtil provides convenience methods.
 */
public class JetUtil {
	/**
	 * By default, return Hazelcast.bootstrappedInstance(). If the system property
	 * "jetinstance" is set then returns Hazelcast.newHazelcastInstance().
	 */
	public static HazelcastInstance getHazelcastInstance() {
		String nobootstrap = System.getProperty("jetinstance");
		if (nobootstrap != null && nobootstrap.equals("true")) {
			return Hazelcast.newHazelcastInstance();
		}
		return Hazelcast.bootstrappedInstance();
	}
}
