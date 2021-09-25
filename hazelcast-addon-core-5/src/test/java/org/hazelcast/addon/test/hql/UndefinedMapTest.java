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

import org.hazelcast.addon.exception.HqlException;
import org.hazelcast.addon.hql.HqlQuery;
import org.hazelcast.addon.hql.IPageResults;
import org.hazelcast.demo.nw.data.PortableFactoryImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

public class UndefinedMapTest {

	private static HazelcastInstance hz;

	@BeforeClass
	public static void setUp() throws Exception {
		setUpClient();
	}

	private static void setUpClient() {
		ClientConfig config = new ClientConfig();
		config.getSerializationConfig().addPortableFactory(PortableFactoryImpl.FACTORY_ID, new PortableFactoryImpl());
		hz = HazelcastClient.newHazelcastClient(config);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		HazelcastClient.shutdownAll();
	}

	/**
	 * Tests a query that contains an undefined map name.
	 */
	@Test
	public void testUndefinedMap() {
		HqlQuery<String> hql = HqlQuery.newHqlQueryInstance(hz);
		String query = "select * from undefeind_map";
		HqlException ex = null;
		try {
			IPageResults<String> results = hql.execute(query, 100);
		} catch (HqlException e) {
			ex = e;
		}
		Assert.assertNotNull(ex);
	}
}
