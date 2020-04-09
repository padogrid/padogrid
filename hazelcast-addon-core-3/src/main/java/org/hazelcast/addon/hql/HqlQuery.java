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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.hazelcast.addon.exception.HqlException;
import org.hazelcast.addon.exception.MapNotFoundException;
import org.hazelcast.addon.hql.impl.PortableFactoryImpl;

import com.hazelcast.config.Config;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.SqlPredicate;

/**
 * HqlQuery is the entry point class for executing HQL queries. HQL supports the
 * following:
 * <ul>
 * <li>Where-clause predicate supported by {@link SqlPredicate}.</li>
 * <li>"order by" clause on keys, values, and their fields (properties).</li>
 * <li>Result set pagination</li>
 * </ul>
 * <b>Note:</b> The underlying querying mechanism embeds Hazelcast's
 * {@link PagingPredicate} to return {@link IPageResults} to provide pagination
 * support. If the order-by clause includes key or value objects as instead of
 * their fields then those objects must implement {@link Comparable}. Otherwise,
 * the query execution will fail.
 * <p>
 * <b>HQL Syntax:</b>
 * 
 * <pre>
 * select * &lt;from-clause&gt; [&lt;where-clause&gt;] [&lt;order-by-clause&gt;] [;]
 * &lt;from-clause&gt;: from &lt;map-name&gt;[.keys|.values|.entries] [&lt;alias&gt;] 
 * &lt;where-clause&gt;: where [&lt;alias&gt;-name&gt][.value.]&lt;field-name&gt;...
 * &lt;order-by-clause&gt;: order by [&lt;alias&gt;[.key.|.value.]]&lt;field-name&gt; [asc|desc]
 * </pre>
 * 
 * <b>Examples:</b>
 * 
 * <pre>
 * -- Query values (if keys or entries are not specified in the map name then
 * -- it defaults to values):
 * select *
 * from nw/orders
 * where customerId='123' or quantity>10 and quantity<100
 * order by customerId asc, orderId desc;
 * 
 * -- Query entries (keys and values) alias:
 * select * 
 * from nw/orders.entries e
 * where e.customerId='123' or quantity>10 and quantity<100 
 * order by e.value.customerId asc, e.value.orderId desc;
 * 
 * -- Query keys (returns keys only):
 * --    Note that at the time of writing, Hazelcast does not support query
 * --    executions on composite keys (Hazelcast version 3.12)
 * select *
 * from nw/orders.keys k
 * where k>1000;
 * 
 * -- Query keys sorted by objects themselves in ascending order.
 * -- The key objects must implement {@link Comparable}.
 * select *
 * from nw/orders.keys k order by k desc;
 * 
 * -- Query values sorted by objects themselves in descending order. 
 * -- The value objects must implement {@link Comparable}.
 * select *
 * from nw/orders v order by v desc;
 * </pre>
 * 
 * @author dpark
 *
 * @param <T>
 */
@SuppressWarnings("rawtypes")
public class HqlQuery<T> {

	private HazelcastInstance hz;
	private TreeMap<String, IMap> mapOfMaps = new TreeMap<String, IMap>();;

	/**
	 * Returns a new instance of HqlQuery. The returned instance can be used for any
	 * number of queries targeted for the specified {@link HazelcastInstance}.
	 * 
	 * @param <T> Returned result set object type
	 * @param hz  {@link HazelcastInstance}
	 */
	public final static <T> HqlQuery<T> newHqlQueryInstance(HazelcastInstance hz) {
		return new HqlQuery<T>(hz);
	}

	private HqlQuery(HazelcastInstance hz) {
		Config config = hz.getConfig();
		Map<Integer, PortableFactory> map = config.getSerializationConfig().getPortableFactories();
		Set<Map.Entry<Integer, PortableFactory>> set = map.entrySet();
		boolean isFactoryRegistered = false;
		for (Map.Entry<Integer, PortableFactory> entry : set) {
			PortableFactory factory = entry.getValue();
			if (factory instanceof PortableFactoryImpl) {
				isFactoryRegistered = true;
				break;
			}
		}
		if (isFactoryRegistered == false) {
			config.getSerializationConfig().addPortableFactory(PortableFactoryImpl.FACTORY_ID,
					new PortableFactoryImpl());
		}
		this.hz = hz;
		refresh();
	}

	/**
	 * Refreshes HqlQuery including the map of maps. This method should be invoked
	 * when new maps are created since instantiating this HqlQuery to update the map
	 * of maps. Note that it is invoked by
	 * {@link #newHqlQueryInstance(HazelcastInstance)}.
	 */
	public void refresh() {
		mapOfMaps.clear();
		Collection<DistributedObject> col = hz.getDistributedObjects();
		for (DistributedObject dobj : col) {
			if (dobj instanceof IMap) {
				mapOfMaps.put(((IMap) dobj).getName(), (IMap) dobj);
			}
		}
	}

	/**
	 * Executes the specified query with the default fetch size of 100. If the query
	 * contains an undefined map then this method throws an exception. This done by
	 * first checking the map of maps (see {@link #getMapOfMaps()}, which may become
	 * stale over time. To update the map of maps, invoke the {@link #refresh()}
	 * method first before calling this method. Note that {@link #refresh()} makes a
	 * remote call which may be expensive.
	 * 
	 * @param query HQL query string
	 * @return Result set that can be scrolled by page at a time
	 * @throws HqlException Thrown if the query is invalid
	 */
	public IPageResults<T> execute(String query) throws HqlException {
		return execute(query, 100);
	}

	/**
	 * Executes the specified query. If the query contains an undefined map then
	 * this method throws an exception. This done by first checking the map of maps
	 * (see {@link #getMapOfMaps()}, which may become stale over time. To update the
	 * map of maps, invoke the {@link #refresh()} method first before calling this
	 * method. Note that {@link #refresh()} makes a remote call which may be
	 * expensive.
	 * 
	 * @param query     HQL query string
	 * @param fetchSize Page fetch size
	 * @return Result set that can be scrolled by page at a time
	 * @throws HqlException Thrown if the query is invalid
	 */
	public IPageResults<T> execute(String query, int fetchSize) throws HqlException {
		CompiledQuery<T> cq = compile(query, fetchSize);
		return execute(cq);
	}

	/**
	 * Executes the query text read from the specified path. If the query contains
	 * an undefined map then this method throws an exception. This done by first
	 * checking the map of maps (see {@link #getMapOfMaps()}, which may become stale
	 * over time. To update the map of maps, invoke the {@link #refresh()} method
	 * first before calling this method. Note that {@link #refresh()} makes a remote
	 * call which may be expensive.
	 * 
	 * @param path      File containing HQL query string
	 * @param fetchSize Page fetch size
	 * @return Result set that can be scrolled by page at a time
	 * @throws IOException  Thrown if the specified path is invalid
	 * @throws HqlException Thrown if the query is invalid
	 */
	public IPageResults<T> execute(Path path, int fetchSize) throws IOException, HqlException {
		CompiledQuery<T> cq = compile(path, fetchSize);
		return execute(cq);
	}

	/**
	 * Executes the query text read from the specified path with the default fetch
	 * size of 100. If the query contains an undefined map then this method throws
	 * an exception. This done by first checking the map of maps (see
	 * {@link #getMapOfMaps()}, which may become stale over time. To update the map
	 * of maps, invoke the {@link #refresh()} method first before calling this
	 * method. Note that {@link #refresh()} makes a remote call which may be
	 * expensive.
	 * 
	 * @param path File containing HQL query string
	 * @return Result set that can be scrolled by page at a time
	 * @throws IOException  Thrown if the specified path is invalid
	 * @throws HqlException Thrown if the query is invalid
	 */
	public IPageResults<T> execute(Path path) throws IOException, HqlException {
		CompiledQuery<T> cq = compile(path, 100);
		return execute(cq);
	}

	/**
	 * Executes the query text read from the specified input stream. If the query
	 * contains an undefined map then this method throws an exception. This done by
	 * first checking the map of maps (see {@link #getMapOfMaps()}, which may become
	 * stale over time. To update the map of maps, invoke the {@link #refresh()}
	 * method first before calling this method. Note that {@link #refresh()} makes a
	 * remote call which may be expensive.
	 * 
	 * @param path      File containing HQL query string
	 * @param fetchSize Page fetch size
	 * @return Result set that can be scrolled by page at a time
	 * @throws IOException  Thrown if the specified path is invalid
	 * @throws HqlException Thrown if the query is invalid
	 */
	public IPageResults<T> execute(InputStream is, int fetchSize) throws IOException, HqlException {
		CompiledQuery<T> cq = compile(is, fetchSize);
		return execute(cq);
	}

	/**
	 * Executes the query text read from the specified input stream with the default
	 * fetch size of 100. If the query contains an undefined map then this method
	 * throws an exception. This done by first checking the map of maps (see
	 * {@link #getMapOfMaps()}, which may become stale over time. To update the map
	 * of maps, invoke the {@link #refresh()} method first before calling this
	 * method. Note that {@link #refresh()} makes a remote call which may be
	 * expensive.
	 * 
	 * @param path File containing HQL query string
	 * @return Result set that can be scrolled by page at a time
	 * @throws IOException  Thrown if the specified path is invalid
	 * @throws HqlException Thrown if the query is invalid
	 */
	public IPageResults<T> execute(InputStream is) throws IOException, HqlException {
		CompiledQuery<T> cq = compile(is, 100);
		return execute(cq);
	}

	/**
	 * Executes the specified CompiledQuery.
	 * 
	 * @param cq CompiedQuery
	 * @return Query results
	 * @throws HqlException Thrown if the query is invalid
	 */
	private IPageResults<T> execute(CompiledQuery<T> cq) throws HqlException {
		if (isMapExist(cq.getMapName()) == false) {
			throw new HqlException(new MapNotFoundException("The query contains map undefined in the cluster: " + cq.getMapName()));
		}
		return cq.execute();
	}

	/**
	 * Compiles the specified query but does not execute the query. The returned
	 * CompiledQuery can be executed later by invoking
	 * {@link CompiledQuery#execute()}. The fetch size defaults to 100.
	 * 
	 * @param query HQL query string
	 * @return Result set that can be scrolled by page at a time
	 */
	public CompiledQuery<T> compile(String query) {
		return compile(query, 100);
	}

	/**
	 * Compiles the specified query but does not execute the query. The returned
	 * CompiledQuery can be executed later by invoking
	 * {@link CompiledQuery#execute()}.
	 * 
	 * @param query     HQL query string
	 * @param fetchSize Page fetch size
	 * @return Result set that can be scrolled by page at a time
	 */
	public CompiledQuery<T> compile(String query, int fetchSize) {
		return new CompiledQuery<T>(hz, query, fetchSize);
	}
	
	/**
	 * Compiles the specified query but does not execute the query. The returned
	 * CompiledQuery can be executed later by invoking
	 * {@link CompiledQuery#execute()}. The fetch size defaults to 100.
	 * 
	 * @param path      File containing HQL query string
	 * @return Result set that can be scrolled by page at a time
	 * @throws IOException Thrown if the specified path is invalid
	 */
	public CompiledQuery<T> compile(Path path) throws IOException {
		return new CompiledQuery<T>(hz, path, 100);
	}

	/**
	 * Compiles the specified query but does not execute the query. The returned
	 * CompiledQuery can be executed later by invoking
	 * {@link CompiledQuery#execute()}.
	 * 
	 * @param path      File containing HQL query string
	 * @param fetchSize Page fetch size
	 * @return Result set that can be scrolled by page at a time
	 * @throws IOException Thrown if the specified path is invalid
	 */
	public CompiledQuery<T> compile(Path path, int fetchSize) throws IOException {
		return new CompiledQuery<T>(hz, path, fetchSize);
	}

	/**
	 * Compiles the query text read from the specified input stream but does not
	 * execute the query. The returned CompiledQuery can be executed later by
	 * invoking {@link CompiledQuery#execute()}. The fetch size defaults to 100.
	 * 
	 * @param is Input stream providing HQL query
	 * @return Result set that can be scrolled by page at a time
	 * @throws IOException Thrown if the specified path is invalid
	 */
	public CompiledQuery<T> compile(InputStream is) throws IOException {
		return new CompiledQuery<T>(hz, is, 100);
	}

	/**
	 * Compiles the query text read from the specified input stream but does not
	 * execute the query. The returned CompiledQuery can be executed later by
	 * invoking {@link CompiledQuery#execute()}.
	 * 
	 * @param is        Input stream providing HQL query
	 * @param fetchSize Page fetch size
	 * @return Result set that can be scrolled by page at a time
	 * @throws IOException Thrown if the specified path is invalid
	 */
	public CompiledQuery<T> compile(InputStream is, int fetchSize) throws IOException {
		return new CompiledQuery<T>(hz, is, fetchSize);
	}

	/**
	 * Returns true if the specified map name exists. Note that this method checks
	 * the map of maps which may become stale over time. Invoke the
	 * {@link #refresh()} method before calling this method to update map of maps.
	 * 
	 * @param mapName Map name
	 */
	public boolean isMapExist(String mapName) {
		return mapOfMaps.containsKey(mapName);
	}

	/**
	 * Returns the map found in the map of maps.
	 * 
	 * @param mapName Map name
	 */
	public IMap getMap(String mapName) {
		return mapOfMaps.get(mapName);
	}

	/**
	 * Returns the map containing the entire set of IMaps found in the cluster. To
	 * get the latest map of maps, invoke the {@link #refresh()} method first before
	 * calling this method.
	 */
	public TreeMap<String, IMap> getMapOfMaps() {
		return mapOfMaps;
	}
}
