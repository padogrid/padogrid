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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.hazelcast.addon.exception.HqlException;
import org.hazelcast.addon.hql.CompiledQuery;
import org.hazelcast.addon.hql.HqlQuery;
import org.hazelcast.demo.nw.data.Order;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

/**
 * This test requires the a Hazelcast cluster running on localhost with the
 * default port (5701) and the test files included in the distribution in the
 * following directory:
 * 
 * <pre>
 * test / hql
 * </pre>
 * 
 * @author dpark
 *
 */
public class EqualityTest {

	private static HazelcastInstance hz;
	private static HqlQuery<Map.Entry<String, Order>> hql;

	@BeforeClass
	public static void setUp() throws Exception {
		setUpClient();
	}

	private static void setUpClient() {
		hz = HazelcastClient.newHazelcastClient();
		hql = HqlQuery.newHqlQueryInstance(hz);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		HazelcastClient.shutdownAll();
	}

	@Test
	public void testEquality1() throws IOException, HqlException {
		Path path = Paths.get("test/hql/equality1.txt");
		System.out.println(path);
		CompiledQuery<?> cq = hql.compile(path);
		cq.dump();
		System.out.println();
	}
}
