package org.hazelcast.demo.nw.impl;

import org.hazelcast.addon.test.perf.data.DataObjectFactory;
import org.hazelcast.demo.nw.data.Customer;

import com.github.javafaker.Address;
import com.github.javafaker.Company;
import com.github.javafaker.Faker;
import com.github.javafaker.PhoneNumber;

public class CustomerFactoryImpl extends AbstractDataObjectFactory {
	
	private Faker faker = new Faker();
	
	public Customer createCustomer() {
		Customer customer = new Customer();
		Address address = faker.address();
		Company company = faker.company();
		PhoneNumber phone = faker.phoneNumber();
		customer.setAddress(address.fullAddress());
		customer.setCity(address.city());
		customer.setCompanyName(company.name());
		customer.setContactName(address.lastName());
		customer.setContactTitle(faker.job().title());
		customer.setCountry(address.country());
		customer.setCustomerId(faker.idNumber().invalidSvSeSsn());
		customer.setFax(phone.cellPhone());
		customer.setPhone(phone.phoneNumber());
		customer.setPostalCode(address.zipCode());
		customer.setRegion(address.stateAbbr());
		return customer;
	}
	
	/**
	 * Returns an entry with the specified idNum as part of the primary key
	 */
	@Override
	public DataObjectFactory.Entry createEntry(int idNum) {
		Customer customer = createCustomer();
		if (isKeyRandom == false) {
			customer.setCustomerId(createKey(idNum));
		}
		return new DataObjectFactory.Entry(customer.getCustomerId(), customer);
	}
}
