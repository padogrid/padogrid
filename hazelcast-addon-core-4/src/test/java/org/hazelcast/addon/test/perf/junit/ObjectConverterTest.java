package org.hazelcast.addon.test.perf.junit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ObjectConverterTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testObjectConverter() throws Exception {
		String keyClassName = "java.lang.String";
		String keyFieldNames[];
		String fnames = "orderId";
		String tokens[];
		tokens = fnames.split(",");
		keyFieldNames = new String[tokens.length];
		for (int j = 0; j < tokens.length; j++) {
			keyFieldNames[j] = tokens[j].trim();
		}

		String valueClassName = "org.hazelcast.demo.nw.data.Order";
		String valueFieldNames[];

		fnames = "orderId, customerId, employeeId, freight, orderDate, requiredDate, shipAddress, shipCity, shipCountry, shipName, shipPostalCode, shipRegion, shipVia, shippedDate";
		tokens = fnames.split(",");
		valueFieldNames = new String[tokens.length];
		for (int j = 0; j < tokens.length; j++) {
			valueFieldNames[j] = tokens[j].trim();
		}
	}
}
