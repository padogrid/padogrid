package org.redisson.addon.test.cluster.junit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.addon.cluster.ClusterUtil;
import org.redisson.addon.test.perf.data.DataObjectFactory;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.demo.nw.data.Customer;
import org.redisson.demo.nw.data.Order;
import org.redisson.demo.nw.impl.CustomerFactoryImpl;
import org.redisson.demo.nw.impl.OrderFactoryImpl;

/**
 * This test case was meant for synchronizing Redis with a relational database
 * but Redisson has no concept of cache writer/loader/listener. Without such
 * support, it only puts data into the "nw/customers" and "nw/orders" maps.
 * 
 * @author dpark
 *
 */
public class HibernatePutTest {
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
	public void testCustomerSet() {
		RMap<Object, Object> map = redisson.getMap("nw/customers");
		CustomerFactoryImpl factory = new CustomerFactoryImpl();
		factory.setKeyPrefix("000000-");
		factory.setKeyPrefixLength(11);
		for (int i = 0; i < 100; i++) {
			DataObjectFactory.Entry entry = factory.createEntry(i, null);
			map.put(entry.key, entry.value);
		}
		for (int i = 0; i < 5000; i++) {
			Customer customer = factory.createCustomer();
			System.out.println("customerId=" + customer.getCustomerId() + ", country=" + customer.getCountry());
			map.put(customer.getCustomerId(), customer);
		}
	}

	@Test
	public void testOrderSet() {
		RMap<Object, Object> map = redisson.getMap("nw/orders");
		OrderFactoryImpl factory = new OrderFactoryImpl();
		factory.setKeyPrefix("000000-");
		factory.setKeyPrefixLength(11);
		for (int i = 0; i < 1000; i++) {
			DataObjectFactory.Entry entry = factory.createEntry(i, null);
			map.put(entry.key, entry.value);
		}
		for (int i = 0; i < 5000; i++) {
			Order order = factory.createOrder();
			System.out.println("orderId=" + order.getOrderId() + ", shipCountry=" + order.getShipCountry());
			map.put(order.getOrderId(), order);
		}
	}
}
