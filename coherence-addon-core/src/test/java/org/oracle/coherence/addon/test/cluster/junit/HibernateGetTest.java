package org.oracle.coherence.addon.test.cluster.junit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oracle.coherence.addon.demo.nw.data.Order;
import org.oracle.coherence.addon.demo.nw.impl.OrderFactoryImpl;

import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;

/**
 * Performs "get" tests on Order objects. You must first run
 * {@link HibernateSetTest} and then restart the Geode cluster.
 * 
 * @author dpark
 *
 */
public class HibernateGetTest {
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
	public void testOrderGet() {
		NamedCache<Object, Object> map = session.getCache("/nw/orders");
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
