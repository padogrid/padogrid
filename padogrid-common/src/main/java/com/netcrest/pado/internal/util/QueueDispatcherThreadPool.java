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
 * QueueDispatcherThreadPool maintains a pool of QueueDispatcher objects for
 * individual threads. It provides a dispatcher for the current thread.
 * 
 * @author dpark
 * 
 */
public class QueueDispatcherThreadPool
{
	private Map<Thread, QueueDispatcher> dispatcherMap = new HashMap<Thread, QueueDispatcher>();
	private String poolName;


	/**
	 * Creates a QueueDispatcherThreadPool object for the specified parameters.
	 * 
	 * @param poolName
	 *            Pool name uniquely identifying the dispatcher pool
	 */
	QueueDispatcherThreadPool(String poolName)
	{
		this.poolName = poolName;
		init();
	}

	/**
	 * Initializes the parameters.
	 */
	private void init()
	{
	}

	/**
	 * Returns the pool name.
	 */
	public String getPoolName()
	{
		return poolName;
	}

	/**
	 * Returns the dispatcher that belongs to the current thread.
	 */
	public QueueDispatcher getDispatcher()
	{
		QueueDispatcher dispatcher = dispatcherMap.get(Thread.currentThread());
		if (dispatcher == null || dispatcher.isTerminated()) {
			dispatcher = new QueueDispatcher();
			dispatcherMap.put(Thread.currentThread(), dispatcher);
		}
		return dispatcher;
	}

	/**
	 * Terminates and removes the dispatcher belonging to the current thread.
	 */
	public void terminateDispatcher()
	{
		QueueDispatcher dispatcher = dispatcherMap.remove(Thread.currentThread());
		dispatcher.stop();
	}

	/**
	 * Closes all dispatchers and clears the pool. This call should be made only
	 * after all threads have been stopped from accessing the pool.
	 */
	public synchronized void close()
	{
		Collection<QueueDispatcher> col = dispatcherMap.values();
		for (QueueDispatcher dispatcher : col) {
			dispatcher.stop();
		}
		dispatcherMap.clear();
	}
}
