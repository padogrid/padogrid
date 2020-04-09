package org.hazelcast.addon.test.cluster.junit;

import org.hazelcast.demo.nw.data.Order;
import org.hazelcast.demo.nw.impl.OrderFactoryImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * Performs "get" tests on Order objects. You must first run
 * {@link HibernateSetTest} and then restart the Hazelcast cluster.
 * 
 * @author dpark
 *
 */
public class HibernateGetTest {
	private static HazelcastInstance hz;

	@BeforeClass
	public static void setUp() throws Exception {
		setUpClient();
	}

	private static void setUpClient() {
		hz = HazelcastClient.newHazelcastClient();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		HazelcastClient.shutdownAll();
	}

	@Test
	public void testOrderGet() {
		IMap<Object, Object> map = hz.getMap("nw/orders");
		OrderFactoryImpl factory = new OrderFactoryImpl();
		factory.setKeyPrefix("000000-");
		factory.setKeyPrefixLength(11);
		for (int i = 0; i < 100; i++) {
			Object orderId = factory.getKey(i);
			Order order = (Order)map.get(orderId);
			Assert.assertNotNull(order);
		}
	}
}
