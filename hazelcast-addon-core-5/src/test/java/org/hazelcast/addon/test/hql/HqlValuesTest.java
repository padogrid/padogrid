/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hazelcast.addon.test.hql;

import java.util.Collection;

import org.hazelcast.addon.hql.HqlQuery;
import org.hazelcast.addon.hql.IPageResults;
import org.hazelcast.demo.nw.data.Order;
import org.hazelcast.demo.nw.data.PortableFactoryImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

/**
 * This test requires a local cluster with "nw" data populated. By default,
 * this test is disabled. To enabled it uncomment test @Test annotation in the
 * code.
 * 
 * @author dpark
 *
 */
public class HqlValuesTest {

	private static HazelcastInstance hz;

	@BeforeClass
	public static void setUp() throws Exception {
		ClientConfig config = new ClientConfig();
		config.getSerializationConfig().addPortableFactory(PortableFactoryImpl.FACTORY_ID, new PortableFactoryImpl());
		hz = HazelcastClient.newHazelcastClient(config);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		HazelcastClient.shutdownAll();
	}

//	@Test
	public void testValues() {
		HqlQuery<Order> hql = HqlQuery.newHqlQueryInstance(hz);
		String query = "select * from nw/orders order by shipCountry asc, customerId desc";
		IPageResults<Order> results = hql.execute(query, 100);
		Collection<Order> col;
		int i = 0;
		do {
			col = results.getResults();
			System.out.println("Page " + results.getPage());
			System.out.println("-------");
			for (Order order : col) {
				System.out.println(i++ + ". " + order.getShipCountry() + ", " + order.getCustomerId() + " " + order);
			}
		} while (results.nextPage());
	}
}
