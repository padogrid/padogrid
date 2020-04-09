package org.apache.geode.addon.test.cluster.junit;

import org.apache.geode.addon.demo.nw.data.Order;
import org.apache.geode.addon.demo.nw.impl.OrderFactoryImpl;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Performs "get" tests on Order objects. You must first run
 * {@link HibernateSetTest} and then restart the Geode cluster.
 * 
 * @author dpark
 *
 */
public class HibernateGetTest {
	private static ClientCache clientCache;

	@BeforeClass
	public static void setUp() throws Exception {
		setUpClient();
	}

	private static void setUpClient() {
		clientCache = ClientCacheFactory.getAnyInstance();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		clientCache.close();
	}

	@Test
	public void testOrderGet() {
		Region<Object, Object> map = clientCache.getRegion("nw/orders");
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
