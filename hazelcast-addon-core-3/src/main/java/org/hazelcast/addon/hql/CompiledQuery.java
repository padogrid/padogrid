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

package org.hazelcast.addon.hql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;

import org.hazelcast.addon.hql.data.OrderBy;
import org.hazelcast.addon.hql.impl.HqlContext;
import org.hazelcast.addon.hql.impl.HqlEvalDriver;
import org.hazelcast.addon.hql.impl.PageResultsImpl;
import org.hazelcast.addon.hql.impl.SearchType;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.SqlPredicate;

/**
 * CompiledQuery compiles the query string before it can be explicitly executed
 * by invoking the {@link #execute()} method. This class is useful when queries
 * need be be validated before executing them.
 * 
 * @author dpark
 *
 * @param <T> Returned result set object type.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CompiledQuery<T> {
	HazelcastInstance hz;
	PagingPredicate pagingPredicate;
	HqlContext hqlContext;

	/**
	 * Constructs a CompiledQuery object with the specified query string. The fetch
	 * size defaults to 100.
	 * 
	 * @param hz    Hazelcast instance
	 * @param query HQL query string
	 */
	CompiledQuery(HazelcastInstance hz, String query) {
		this(hz, query, 100);
	}

	/**
	 * Constructs a CompiledQuery object with the specified query string.
	 * 
	 * @param hz        Hazelcast instance
	 * @param query     HQL query string
	 * @param fetchSize Result set page size
	 */
	CompiledQuery(HazelcastInstance hz, String query, int fetchSize) {
		this.hz = hz;
		HqlEvalDriver driver = new HqlEvalDriver(query);
		init(driver, fetchSize);
	}

	/**
	 * Constructs a CompiledQuery object with the query text read from the specified
	 * path. The fetch size defaults to 100.
	 * 
	 * @param hz   Hazelcast instance
	 * @param path File containing HQL query string
	 * @throws IOException Thrown if the specified path is invalid
	 */
	CompiledQuery(HazelcastInstance hz, Path path) throws IOException {
		this(hz, path, 100);
	}

	/**
	 * Constructs a CompiledQuery object with the query text read from the specified
	 * path.
	 * 
	 * @param hz        Hazelcast instance
	 * @param path      File containing HQL query string
	 * @param fetchSize Result set page size
	 * @throws IOException Thrown if the specified path is invalid
	 */
	CompiledQuery(HazelcastInstance hz, Path path, int fetchSize) throws IOException {
		this.hz = hz;
		HqlEvalDriver driver = new HqlEvalDriver(path);
		init(driver, fetchSize);
	}

	/**
	 * Constructs a CompiledQuery object with the query text read from the specified
	 * input stream. The fetch size defaults to 100.
	 * 
	 * @param hz Hazelcast instance
	 * @param is Input stream providing HQL query
	 * @throws IOException Thrown if the specified input stream is invalid
	 */
	CompiledQuery(HazelcastInstance hz, InputStream is) throws IOException {
		this(hz, is, 100);
	}

	/**
	 * Constructs a CompiledQuery object with the query text read from the specified
	 * input stream.
	 * 
	 * @param hz Hazelcast instance
	 * @param is Input stream providing HQL query
	 * @param fetchSize Result set page size
	 * @throws IOException Thrown if the specified input stream is invalid
	 */
	CompiledQuery(HazelcastInstance hz, InputStream is, int fetchSize) throws IOException {
		this.hz = hz;
		HqlEvalDriver driver = new HqlEvalDriver(is);
		init(driver, fetchSize);
	}

	private void init(HqlEvalDriver driver, int fetchSize) {
		driver.execute();
		hqlContext = driver.getHqlContext();
		Predicate queryPredicate;
		if (hqlContext.isWhereClause()) {
			queryPredicate = new SqlPredicate(hqlContext.getWhereClause());
		} else {
			queryPredicate = Predicates.alwaysTrue();
		}
		OrderBy orderBy = hqlContext.getOrderBy();
		if (orderBy == null || orderBy.getFieldList() == null || orderBy.getFieldList().size() == 0) {
			orderBy = new OrderBy();
			// Add a null field so that PagingPredicate works for the objects that do not
			// implement Comparable.
			orderBy.addField(null, SearchType.VALUE_FIELD, false);
		}
		pagingPredicate = new PagingPredicate(queryPredicate, orderBy, fetchSize);

	}

	/**
	 * Executes the compiled query and returns the result set.
	 */
	public IPageResults<T> execute() {
		IMap map = hz.getMap(hqlContext.getPath());
		Collection<T> results;
		switch (hqlContext.getResultType()) {
		case KEYS_VALUES:
			results = map.entrySet(pagingPredicate);
			break;
		case KEYS:
			results = map.keySet(pagingPredicate);
			break;
		default:
			results = map.values(pagingPredicate);
			break;
		}

		return new PageResultsImpl(map, results, pagingPredicate, hqlContext.getResultType());
	}

	/**
	 * Returns the map name found in the query.
	 */
	public String getMapName() {
		return hqlContext.getPath();
	}

	/**
	 * 
	 */
	public void dump() {
		hqlContext.dump();
	}

	@Override
	public String toString() {
		return "CompiledQuery [hqlContext=" + hqlContext + "]";
	}
}