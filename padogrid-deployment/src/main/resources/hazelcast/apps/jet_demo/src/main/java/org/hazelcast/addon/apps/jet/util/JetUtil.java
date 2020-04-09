package org.hazelcast.addon.apps.jet.util;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.server.JetBootstrap;

/** 
 * JetUtil provides convenience methods.
 */
public class JetUtil {
	/**
	 * By default, return JetBoostrap.getInstance(). If the system property
	 * "jetinstance" is set then returns Jet.newJetInstance()
	 */
	public static JetInstance getJetInstance() {
		String nobootstrap = System.getProperty("jetinstance");
		if (nobootstrap != null && nobootstrap.equals("true")) {
			return Jet.newJetInstance();
		}
		return JetBootstrap.getInstance();
	}
}
