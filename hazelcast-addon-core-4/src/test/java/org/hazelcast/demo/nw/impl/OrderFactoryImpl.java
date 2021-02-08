package org.hazelcast.demo.nw.impl;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.hazelcast.addon.test.perf.data.DataObjectFactory;
import org.hazelcast.demo.nw.data.Order;

import com.github.javafaker.Address;
import com.github.javafaker.Company;
import com.github.javafaker.Faker;

public class OrderFactoryImpl extends AbstractDataObjectFactory {
	
	private Faker faker = new Faker();
	private Random random = new Random();

	public Order createOrder() {
		Order order = new Order();
		Company company = faker.company();
		order.setCustomerId(faker.idNumber().invalidSvSeSsn());
		order.setEmployeeId(faker.idNumber().invalidSvSeSsn());
		order.setFreight(200 * random.nextDouble());
		order.setOrderDate(faker.date().past(7, TimeUnit.DAYS));
		order.setOrderId(faker.idNumber().invalidSvSeSsn());
		order.setRequiredDate(faker.date().future(20, TimeUnit.DAYS));
		Address address = faker.address();
		order.setShipAddress(address.fullAddress());
		order.setShipCity(address.city());
		order.setShipCountry(address.country());
		order.setShipName(company.name());
		order.setShippedDate(faker.date().future(5, TimeUnit.DAYS, order.getOrderDate()));
		order.setShipPostalCode(address.zipCode());
		order.setShipRegion(address.stateAbbr());
		order.setShipVia(Integer.toString(random.nextInt(5) + 1));
		return order;
	}
	
	/**
	 * @return the data object class
	 */
	@Override
	public Class<?> getDataObjectClass()
	{
		return Order.class;
	}
	
	/**
	 * Returns an entry with the specified idNum as part of the primary key
	 */
	@Override
	public DataObjectFactory.Entry createEntry(int idNum, Object erKey) {
		Order order = createOrder();
		if (isKeyRandom == false) {
			order.setOrderId(createKey(idNum));
		}
		
		// parent ER
		if (erKey != null) {
			order.setCustomerId(erKey.toString());
		}
		return new DataObjectFactory.Entry(order.getOrderId(), order);
	}
}
