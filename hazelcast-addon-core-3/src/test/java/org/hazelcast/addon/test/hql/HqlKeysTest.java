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

import org.hazelcast.addon.exception.HqlException;
import org.hazelcast.addon.hql.HqlQuery;
import org.hazelcast.addon.hql.IPageResults;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

/**
 * HqlTestKeys tests "select * from nw/orders.keys". It seems there is a bug in
 * Hazelcast. HQL runs the query using the following calls but it returns
 * results even though we are at the last page and there are no more results.
 * This is observed with Hazelcast 3.12.
 * <p>
 * This test has been disabled. To enable it, uncomment the @Test annocation in
 * the code.
 * 
 * <pre>
 * IMap.keySet(PagingPredicate.nextPage());
 * </pre>
 * 
 * *
 * 
 * @author dpark
 *
 */
public class HqlKeysTest {

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

//	@Test
	public void testKeys() throws HqlException {
		HqlQuery<String> hql = HqlQuery.newHqlQueryInstance(hz);
		String query = "select * from nw/orders.keys";
		IPageResults<String> results = hql.execute(query, 100);
		Collection<String> col;
		int i = 0;
		do {
			col = results.getResults();
			System.out.println("Page " + results.getPage());
			System.out.println("-------");
			for (String key : col) {
				System.out.println(i++ + ". " + key);
			}
		} while (results.nextPage());
	}
}
