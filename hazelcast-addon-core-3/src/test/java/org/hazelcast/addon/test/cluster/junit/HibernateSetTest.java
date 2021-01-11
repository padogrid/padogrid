package org.hazelcast.addon.test.cluster.junit;


import org.hazelcast.addon.test.perf.data.DataObjectFactory;
import org.hazelcast.demo.nw.data.Customer;
import org.hazelcast.demo.nw.data.Order;
import org.hazelcast.demo.nw.impl.CustomerFactoryImpl;
import org.hazelcast.demo.nw.impl.OrderFactoryImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class HibernateSetTest {
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
	public void testCustomerSet() {
		IMap<Object, Object> map = hz.getMap("nw/customers");
		CustomerFactoryImpl factory = new CustomerFactoryImpl();
		factory.setKeyPrefix("000000-");
		factory.setKeyPrefixLength(11);
		for (int i = 0; i < 100; i++) {
			DataObjectFactory.Entry entry = factory.createEntry(i, null);
			map.set(entry.key, entry.value);
		}
		for (int i = 0; i < 5000; i++) {
			Customer customer = factory.createCustomer();
			System.out.println("customerId=" + customer.getCustomerId() + ", country=" + customer.getCountry());
			map.set(customer.getCustomerId(), customer);
		}
	}
	
	@Test
	public void testOrderSet() {
		IMap<Object, Object> map = hz.getMap("nw/orders");
		OrderFactoryImpl factory = new OrderFactoryImpl();
		factory.setKeyPrefix("000000-");
		factory.setKeyPrefixLength(11);
		for (int i = 0; i < 1000; i++) {
			DataObjectFactory.Entry entry = factory.createEntry(i, null);
			map.set(entry.key, entry.value);
		}
		for (int i = 0; i < 5000; i++) {
			Order order = factory.createOrder();
			System.out.println("orderId=" + order.getOrderId() + ", shipCountry=" + order.getShipCountry());
			map.set(order.getOrderId(), order);
		}
	}
}
