package org.apache.geode.addon.test.cluster.junit;

import org.apache.geode.addon.demo.nw.data.Customer;
import org.apache.geode.addon.demo.nw.data.Order;
import org.apache.geode.addon.demo.nw.impl.CustomerFactoryImpl;
import org.apache.geode.addon.demo.nw.impl.OrderFactoryImpl;
import org.apache.geode.addon.test.perf.data.DataObjectFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HibernateSetTest {
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
	public void testCustomerPut() {
		Region<Object, Object> region = clientCache.getRegion("nw/customers");
		CustomerFactoryImpl factory = new CustomerFactoryImpl();
		factory.setKeyPrefix("000000-");
		factory.setKeyPrefixLength(11);
		for (int i = 0; i < 100; i++) {
			DataObjectFactory.Entry entry = factory.createEntry(i);
			region.put(entry.key, entry.value);
		}
		for (int i = 0; i < 5000; i++) {
			Customer customer = factory.createCustomer();
			System.out.println("customerId=" + customer.getCustomerId() + ", country=" + customer.getCountry());
			region.put(customer.getCustomerId(), customer);
		}
	}

	@Test
	public void testOrderPut() {
		Region<Object, Object> region = clientCache.getRegion("nw/orders");
		OrderFactoryImpl factory = new OrderFactoryImpl();
		factory.setKeyPrefix("000000-");
		factory.setKeyPrefixLength(11);
		for (int i = 0; i < 1000; i++) {
			DataObjectFactory.Entry entry = factory.createEntry(i);
			region.put(entry.key, entry.value);
		}
		for (int i = 0; i < 5000; i++) {
			Order order = factory.createOrder();
			System.out.println("orderId=" + order.getOrderId() + ", shipCountry=" + order.getShipCountry());
			region.put(order.getOrderId(), order);
		}
	}
}
