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

package org.hazelcast.addon.hql.data;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hazelcast.addon.hql.impl.PortableFactoryImpl;
import org.hazelcast.addon.hql.impl.SearchType;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;

/**
 * OrderBy compares the specified fields in the order they are specified.
 * Individual OrderByField object determines key or value to sort, the field
 * name, and the ascending or descending order. The field names represent the
 * property names of key or value objects, i.e., the getter methods.
 * <p>
 * <b>Note:</b> Field names are case-sensitive. They must exactly match the
 * getter method name without the prefix "get".</b>
 * <p>
 * <b>PortableFactory:</b> To use this class, the following PortableFactory must
 * be registered in both server and client sides:
 * 
 * <pre>
 * {@link org.hazelcast.addon.hql.impl.PortableFactoryImpl}
 * </pre>
 * 
 * @see org.hazelcast.addon.hql.impl.PortableFactoryImpl
 * @author dpark
 *
 */
@SuppressWarnings("rawtypes")
public class OrderBy<K, V> implements VersionedPortable, Comparator<Map.Entry<K, V>> {

	private List<OrderByField> fieldList;

	/**
	 * Constructs a new OrderBy object.
	 */
	public OrderBy() {
	}

	/**
	 * Constructs a new OrderBy object with the specified field names of value (not
	 * key) objects to perform "order by" in ascending order. For mixing keys and
	 * values with ascending and descending order, use
	 * {@link #OrderBy(OrderByField...)}.
	 * 
	 * @param fieldNames List of order by field names. The field names must match
	 *                   the getter method names of the value object without the
	 *                   pretext "get". Field names are case sensitive except for
	 *                   the first letter. For example, the field name for
	 *                   "getCustomerId()" would be "CustomerId" or "customerId".
	 */
	public OrderBy(String... fieldNames) {
		fieldList = new ArrayList<OrderByField>(fieldNames.length);
		for (int i = 0; i < fieldNames.length; i++) {
			fieldList.add(new OrderByField(fieldNames[i], SearchType.VALUE_FIELD, true));
		}
	}

	/**
	 * Constructs a new OrderBy object with the specified fields to perform "order
	 * by".
	 * 
	 * @param fields List of order by field names. The field names must match the
	 *               getter method names of the value object without the pretext
	 *               "get". Field names are case sensitive except for the first
	 *               letter. For example, the field name for "getCustomerId()" would
	 *               be "CustomerId" or "customerId".
	 */
	public OrderBy(OrderByField... fields) {
		this.fieldList = Arrays.asList(fields);
	}

	/**
	 * Adds the specified field in the order-by list.
	 * 
	 * @param fieldName Field name
	 * @param isKey     true if the field is in the key objects, false if the field
	 *                  is in the value objects.
	 * @param isAsc     true to sort in ascending order, false to sort in descending
	 *                  order
	 */
	public void addField(String fieldName, SearchType searchType, boolean isAsc) {
		if (fieldList == null) {
			fieldList = new ArrayList<OrderByField>(5);
		}
		OrderByField field = new OrderByField(fieldName, searchType, isAsc);
		fieldList.add(field);
	}

	/**
	 * Returns the OrderByField list. null if undefined.
	 */
	public List<OrderByField> getFieldList() {
		return fieldList;
	}

	/**
	 * Compares the value objects found in the specified Map.Entry objects.
	 */
	@Override
	public int compare(Entry<K, V> e1, Entry<K, V> e2) {
		if (fieldList == null || fieldList.size() == 0) {
			return 0;
		}

		// Iterate thru each field in the order they are defined
		// to perform "order by".
		for (OrderByField field : fieldList) {
			switch (field.getSearchType()) {
			case KEY_FIELD:
				K k1 = e1.getKey();
				K k2 = e2.getKey();
				int c = compareField(k1, k2, field.getFieldName(), field.isAsc());
				if (c != 0) {
					return c;
				}
				break;
			case VALUE_FIELD:
				V v1 = e1.getValue();
				V v2 = e2.getValue();
				c = compareField(v1, v2, field.getFieldName(), field.isAsc());
				if (c != 0) {
					return c;
				}
				break;
			case KEY_OBJECT:
				k1 = e1.getKey();
				k2 = e2.getKey();
				if (field.isAsc()) {
					c = compareAsc(k1, k2);
				} else {
					c = compareAsc(k2, k1);
				}
				if (c != 0) {
					return c;
				}
				break;
			case VALUE_OBJECT:
				v1 = e1.getValue();
				v2 = e2.getValue();
				if (field.isAsc()) {
					c = compareAsc(v1, v2);
				} else {
					c = compareAsc(v2, v1);
				}
				if (c != 0) {
					return c;
				}
				break;
			}
		}

		return 0;
	}

	/**
	 * Compares the specified field value in the specified objects.
	 * 
	 * @param v1
	 * @param v2
	 * @param fieldName
	 * @param isAsc
	 */
	private int compareField(Object v1, Object v2, String fieldName, boolean isAsc) {
		if (fieldName == null) {
			return compareAsc(v1, v2);
		} else {
			Method method = null;
			try {
				method = v1.getClass().getMethod("get" + fieldName);
			} catch (Exception e) {
				// ignores
			}
			try {
				if (method == null) {
					method = v1.getClass().getMethod(
							"get" + Character.toString(fieldName.charAt(0)).toUpperCase() + fieldName.substring(1));
				}
				if (method == null) {
					return 0;
				}

				Object o1 = method.invoke(v1);
				Object o2 = method.invoke(v2);

				if (isAsc) {
					return compareAsc(o1, o2);
				} else {
					return compareAsc(o2, o1);
				}

			} catch (Exception e) {
				// ignore
			}

			return 0;
		}
	}

	/**
	 * Compares the specified objects in ascending order
	 * 
	 * @param o1
	 * @param o2
	 */
	@SuppressWarnings("unchecked")
	private int compareAsc(Object o1, Object o2) {

		if (o1 == null) {
			return -1;
		}
		if (o2 == null) {
			return 1;
		}
		if (o1 instanceof Comparable && o2 instanceof Comparable) {
			int c = ((Comparable) o1).compareTo(o2);
			if (c != 0) {
				return c;
			}
		}
		// This should not occur.
		if (o1.equals(o2) == false) {
			if (o1.hashCode() > o2.hashCode()) {
				return 1;
			} else if (o1.hashCode() < o2.hashCode()) {
				return -1;
			}
		}
		return 0;
	}

	@Override
	public String toString() {
		return "OrderBy fieldList=" + fieldList;
	}

	@Override
	public int getFactoryId() {
		return PortableFactoryImpl.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableFactoryImpl.OrderBy_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		if (fieldList == null) {
			writer.writePortableArray("fields", new OrderByField[0]);
		} else {
			writer.writePortableArray("fields", (Portable[]) fieldList.toArray(new OrderByField[fieldList.size()]));
		}
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		Portable[] fields = reader.readPortableArray("fields");
		if (fields != null) {
			fieldList = new ArrayList<OrderByField>(fields.length);
			for (Portable field : fields) {
				fieldList.add((OrderByField) field);
			}
		}
	}

	@Override
	public int getClassVersion() {
		return 1;
	}
}
