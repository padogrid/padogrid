package org.hazelcast.addon.cluster;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.hazelcast.core.HazelcastInstance;

/**
 * Bootstraps Spring context to start a Hazelcast member. The system property,
 * "applicationContext", sets the application context file which must be in the
 * class path. If this system property is not defined, then it defaults to
 * applicationContext.xml. If you are running members from the hazelcast-addon
 * workspace environment then you can place the applicationContext.xml file in the
 * cluster's etc directory which is part of the class path.
 * 
 * @author dpark
 *
 */
public class SpringContextMember {
	public static void main(String[] args) throws Exception {
		String applicationContextFile = System.getProperty("applicationContext", "applicationContext.xml");
		ApplicationContext context = new GenericXmlApplicationContext(applicationContextFile);
		HazelcastInstance instance = (HazelcastInstance) context.getBean("instance");
	}
}