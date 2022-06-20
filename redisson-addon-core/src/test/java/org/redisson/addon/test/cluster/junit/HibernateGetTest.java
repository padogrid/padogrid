package org.redisson.addon.test.cluster.junit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.addon.cluster.ClusterUtil;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.demo.nw.data.Order;
import org.redisson.demo.nw.impl.OrderFactoryImpl;

/**
 * This test case was meant for synchronizing Redis with a relational database
 * but Redisson has no concept of cache writer/loader/listener. Without such
 * support, it only gets data into the "nw/orders" maps.
 * 
 * @author dpark
 *
 */
public class HibernateGetTest {
	private static RedissonClient redisson;

	@BeforeClass
	public static void setUp() throws Exception {
		setUpClient();
	}

	private static void setUpClient() {
		redisson = ClusterUtil.createRedissonClient();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		redisson.shutdown();
	}

	@Test
	public void testOrderGet() {
		RMap<Object, Object> map = redisson.getMap("nw/orders");
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
