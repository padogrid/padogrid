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

import org.hazelcast.addon.hql.impl.PortableFactoryImpl;
import org.hazelcast.addon.hql.impl.SearchType;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;

/**
 * OrderByField contains individual field names and their search types to be
 * included in the order-by execution.
 * 
 * @author dpark
 *
 */
public class OrderByField implements VersionedPortable {

	private String fieldName;
	private SearchType searchType;
	private boolean isAsc;

	public OrderByField() {
	}

	public OrderByField(String fieldName, SearchType searchType, boolean isAsc) {
		this.fieldName = fieldName;
		this.searchType = searchType;
		this.isAsc = isAsc;
	}

	public String getFieldName() {
		return fieldName;
	}

	public boolean isAsc() {
		return isAsc;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public void setAsc(boolean isAsc) {
		this.isAsc = isAsc;
	}

	public SearchType getSearchType() {
		return searchType;
	}

	public void setSearchType(SearchType searchType) {
		this.searchType = searchType;
	}

	@Override
	public String toString() {
		return "OrderByField [fieldName=" + fieldName + ", searchType=" + searchType + ", isAsc=" + isAsc + "]";
	}

	@Override
	public int getFactoryId() {
		return PortableFactoryImpl.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableFactoryImpl.OrderByField_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeUTF("fieldName", fieldName);
		writer.writeByte("searchType", (byte) searchType.ordinal());
		writer.writeBoolean("isAsc", isAsc);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		fieldName = reader.readUTF("fieldName");
		searchType = SearchType.values()[reader.readByte("searchType")];
		isAsc = reader.readBoolean("isAsc");
	}

	@Override
	public int getClassVersion() {
		return 1;
	}
}