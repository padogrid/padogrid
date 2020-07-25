package org.oracle.coherence.addon.test.cluster.junit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oracle.coherence.addon.demo.nw.data.Customer;
import org.oracle.coherence.addon.demo.nw.data.Order;
import org.oracle.coherence.addon.demo.nw.impl.CustomerFactoryImpl;
import org.oracle.coherence.addon.demo.nw.impl.OrderFactoryImpl;
import org.oracle.coherence.addon.test.perf.data.DataObjectFactory;

import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;

public class HibernateSetTest {
	private static Session session;

	@BeforeClass
	public static void setUp() throws Exception {
		setUpClient();
	}

	private static void setUpClient() {
		session = Session.create();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		session.close();
	}

	@Test
	public void testCustomerPut() {
		NamedCache<Object, Object> region = session.getCache("/nw/customers");
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
		NamedCache<Object, Object> region = session.getCache("/nw/orders");
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
