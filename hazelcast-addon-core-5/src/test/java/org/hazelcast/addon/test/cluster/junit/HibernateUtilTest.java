package org.hazelcast.addon.test.cluster.junit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hazelcast.addon.cluster.util.HibernatePool;
import org.hazelcast.demo.nw.data.Customer;
import org.hazelcast.demo.nw.data.Order;
import org.hazelcast.demo.nw.impl.CustomerFactoryImpl;
import org.hazelcast.demo.nw.impl.OrderFactoryImpl;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HibernateUtilTest {
	
	@BeforeClass
	public static void setUp() throws Exception {
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		HibernatePool.getHibernatePool().shutdown();
	}
	
	private String getGetter(String fieldName) {
		char c = fieldName.charAt(0);
		if (Character.isAlphabetic(c)) {
			fieldName = Character.toUpperCase(c) + fieldName.substring(1);
		}
		return "get" + fieldName;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testQueryGenericPrimaryKeys() throws Exception {
		OrderFactoryImpl factory = new OrderFactoryImpl();
		factory.setKeyPrefix("000000-");
		factory.setKeyPrefixLength(11);
		ArrayList<String> pkList = new ArrayList<String>(1000);
		for (int i = 0; i < 1000; i++) {
			pkList.add(factory.getKey(i).toString());
		}
		Collection<String> col = pkList;
		
		Class<?> clazz = Class.forName("org.hazelcast.demo.nw.data.Order");
		Session session = HibernatePool.getHibernatePool().takeSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();

		CriteriaQuery<?> cr = cb.createQuery(clazz);
		Root root = cr.from(clazz);
		String pk = root.getModel().getId(String.class).getName();
		Iterator<String> iterator = col.iterator();
		int size = pkList.size();
		int i = 1;
		while (i <= size) {
			In<String> inClause = cb.in(root.get(pk));
			while (iterator.hasNext() && i % 100 > 0) {
				String key = iterator.next();
				inClause.value(key);
				cr.select(root).where(inClause);
				i++;
			}
			if (iterator.hasNext()) {
				String key = iterator.next();
				inClause.value(key);
				cr.select(root).where(inClause);
				i++;
			}
			Query<?> query = session.createQuery(cr);
			List<?> values = query.getResultList();
			HibernatePool.getHibernatePool().offerSession(session);
			
			Method method = clazz.getMethod(getGetter(pk));
			System.out.println(getGetter(pk));
			for (Object value : values) {
				Object key2 = method.invoke(value);
				System.out.println(key2 + ": " + value);
			}
		}
	}
	
	@Test
	public void testQueryOrderPrimaryKeys() throws Exception {
		Session session = HibernatePool.getHibernatePool().takeSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();

		CriteriaQuery<Order> cr = cb.createQuery(Order.class);
		Root<Order> root = cr.from(Order.class);
		In<String> inClause = cb.in(root.get("orderId"));
		String[] customerIds = new String[100];
		CustomerFactoryImpl factory = new CustomerFactoryImpl();
		factory.setKeyPrefix("000000-");
		factory.setKeyPrefixLength(11);
		for (int i = 0; i < customerIds.length; i++) {
			inClause.value(factory.getKey(i).toString());
			
		}
		cr.select(root).where(inClause);
		Query<Order> query = session.createQuery(cr);
		List<Order> customers = query.getResultList();
		HibernatePool.getHibernatePool().offerSession(session);
		customers.forEach(c -> System.out.println(c));
	}
	
	@Test
	public void testQueryCustomers() throws InterruptedException {
		Session session = HibernatePool.getHibernatePool().takeSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Customer> cr = cb.createQuery(Customer.class);
		Root<Customer> root = cr.from(Customer.class);
		cr.select(root);
		Query<Customer> query = session.createQuery(cr);
		List<Customer> customers = query.getResultList();
		HibernatePool.getHibernatePool().offerSession(session);
		customers.forEach(c -> System.out.println(c));
	}
}
