package org.apache.geode.addon.demo.nw.impl;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.geode.addon.demo.nw.data.Order;
import org.apache.geode.addon.test.perf.data.DataObjectFactory;

import com.github.javafaker.Address;
import com.github.javafaker.Company;
import com.github.javafaker.Faker;

public class OrderFactoryImpl extends AbstractDataObjectFactory {

	private Faker faker = new Faker();
	private String customerIdPrefix;
	private int customerIdMax;

	@Override
	public void initialize(Properties props) {
		super.initialize(props);
		customerIdPrefix = props.getProperty("factory.customerId.prefix", "000000-");
		String customerIdMaxStr = props.getProperty("factory.customerId.max", "100");
		customerIdMax = Integer.parseInt(customerIdMaxStr);
	}

	public Order createOrder() {
		Order order = new Order();
		Company company = faker.company();
		order.setCustomerId(faker.idNumber().invalidSvSeSsn());
		order.setEmployeeId(faker.idNumber().invalidSvSeSsn());
		order.setFreight(200 * random.nextDouble());
		order.setOrderDate(faker.date().past(7, TimeUnit.DAYS));
		order.setOrderId(faker.idNumber().invalidSvSeSsn());
		order.setRequiredDate(faker.date().future(10, TimeUnit.DAYS, order.getOrderDate()));
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
	public Class<?> getDataObjectClass() {
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
		} else {
			order.setCustomerId(createForeignKey(customerIdPrefix, customerIdMax));
		}
		String key = order.getOrderId() + "." + order.getCustomerId();
		return new DataObjectFactory.Entry(key, order);
	}
}
