/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado.internal.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * QueueDispatcherThreadPoolManager maintains named QueueDispatcherThreadPool
 * objects allowing access to multiple dispatcher pools in a single JVM. Note
 * that each instance of QueueDispatcher is backed by a consumer thread and
 * therefore the number of total dispatcher threads is directly proportional to
 * the number of threads accessing the dispatchers.
 * 
 * @author dpark
 * 
 */
public class QueueDispatcherThreadPoolManager
{
	private static QueueDispatcherThreadPoolManager manager = new QueueDispatcherThreadPoolManager();

	private Map<String, QueueDispatcherThreadPool> poolMap = new HashMap<String, QueueDispatcherThreadPool>();

	public static QueueDispatcherThreadPoolManager getQueueDispatcherThreadPoolManager()
	{
		return manager;
	}

	/**
	 * Creates a pool of QueueDispatcher objects. The returned pool is mapped by
	 * the specified pool name and contains dispatchers mapped by threads.
	 * 
	 * @param poolName
	 *            Pool name uniquely identifying the dispatcher pool.
	 */
	public synchronized QueueDispatcherThreadPool createPool(String poolName)
	{
		QueueDispatcherThreadPool pool = poolMap.get(poolName);
		if (pool == null) {
			pool = new QueueDispatcherThreadPool(poolName);
			poolMap.put(poolName, pool);
		}
		return pool;
	}

	/**
	 * Returns the dispatcher pool mapped by the specified pool name.
	 * 
	 * @param poolName
	 *            Pool name uniquely identifying a pool.
	 * @return Returns null if the pool does not exist. A pool must be created
	 *         first by invoking
	 *         {@link #createPool(String)}.
	 */
	public QueueDispatcherThreadPool getPool(String poolName)
	{
		return poolMap.get(poolName);
	}

	/**
	 * Closes all of the pools in the specified pool.
	 * 
	 * @param poolName
	 *            Pool name uniquely identifying a pool.
	 */
	public synchronized void closePool(String poolName)
	{
		QueueDispatcherThreadPool pool = poolMap.remove(poolName);
		if (pool != null) {
			pool.close();
		}
	}

	/**
	 * Closes all of the dispatchers in all pools.
	 */
	public synchronized void close()
	{
		Collection<QueueDispatcherThreadPool> col = poolMap.values();
		for (QueueDispatcherThreadPool pool : col) {
			pool.close();
		}
		poolMap.clear();
	}
}
