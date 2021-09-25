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

package org.hazelcast.addon.hql.impl;

import org.hazelcast.addon.hql.data.OrderBy;
import org.hazelcast.addon.hql.data.OrderByField;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

/**
 * PortableFactory class for all addon components. The default factory ID and
 * the first class ID are both 10000. These values can be changed by the
 * following system properties:
 * 
 * <pre>
 * -Dorg.hazelcast.addon.hql.impl.PortableFactoryImpl.factoryId=10000
 * -Dorg.hazelcast.addon.hql.impl.PortableFactoryImpl.firstClassId=10000
 * </pre>
 * 
 * @author dpark
 *
 */
public class PortableFactoryImpl implements PortableFactory {

	public static final int FACTORY_ID;

	private static final int __FIRST_CLASS_ID;

	static {
		String factoryIdStr = System.getProperty("org.hazelcast.addon.hql.impl.PortableFactoryImpl.factoryId", "10000");
		FACTORY_ID = new Integer(factoryIdStr);
		String firstClassId = System.getProperty("org.hazelcast.addon.hql.impl.PortableFactoryImpl.firstClassId",
				"10000");
		__FIRST_CLASS_ID = new Integer(firstClassId);
	}

	public static final int OrderBy_CLASS_ID = __FIRST_CLASS_ID;
	public static final int OrderByField_CLASS_ID = OrderBy_CLASS_ID + 1;

	private static final int __LAST_CLASS_ID = OrderByField_CLASS_ID;

	public static final int X = 1;

	public Portable create(final int classId) {
		if (classId == OrderBy_CLASS_ID) {
			return new OrderBy<Object, Object>();
		} else if (classId == OrderByField_CLASS_ID) {
			return new OrderByField();
		} else {
			return null;
		}
	}
}
