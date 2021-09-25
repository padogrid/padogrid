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

import org.hazelcast.addon.hql.ResultType;
import org.hazelcast.addon.hql.data.OrderBy;

/**
 * HqlContext is an internal class that contains the HQL parser extracted
 * values.
 * 
 * @author dpark
 *
 */
public class HqlContext {
	private String path;
	private String pathAlias;
	private ResultType resultType = ResultType.VALUES;
	private String whereClause;
	// non-null OrderBy required by Hazelcast.
	private OrderBy orderBy = new OrderBy();

	public String getWhereClause() {
		return whereClause;
	}

	public boolean isWhereClause() {
		return whereClause != null && whereClause.length() > 0;
	}

	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return this.path;
	}

	public void setPathAlias(String pathAlias) {
		this.pathAlias = pathAlias;
	}

	public String getPathAlias() {
		return this.pathAlias;
	}

	public boolean isPathAlias() {
		return this.pathAlias != null;
	}

	public boolean isValidPath(String path) {
		if (path == null) {
			return false;
		}
		if (path.equals(this.path)) {
			return true;
		} else {
			return path.equals(this.pathAlias);
		}
	}

	public ResultType getResultType() {
		return resultType;
	}

	public void setResultType(ResultType resultType) {
		this.resultType = resultType;
	}

	public void addOrderByExpression(String columnName, SearchType searchType, boolean isAsc) {
		orderBy.addField(columnName, searchType, isAsc);
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void dump() {
		System.out.println("      Path: " + getPath());
		System.out.println("Path Alias: " + pathAlias);
		System.out.println("ResultType: " + getResultType());
		System.out.println(" Predicate: " + getWhereClause());
		System.out.println("  Order By: " + getOrderBy());
	}

	@Override
	public String toString() {
		return "HqlContext [path=" + path + ", pathAlias=" + pathAlias + ", resultType=" + resultType + ", whereClause="
				+ whereClause + ", orderBy=" + orderBy + "]";
	}
}
