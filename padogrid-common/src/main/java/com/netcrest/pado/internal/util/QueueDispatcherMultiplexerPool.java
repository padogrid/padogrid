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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * QueueDispatcherMultiplexerPool is a singleton class that provides a pool of
 * QueueDispatcherMultiplexer objects that are used as concurrent multiplexers
 * to consume and dispatch events. It must first be initialized with the maximum
 * number of threads by invoking {@link #initialize(int)}, which is typically
 * done at the server startup time.
 * 
 * @author dpark
 * 
 */
public class QueueDispatcherMultiplexerPool
{
	private static QueueDispatcherMultiplexerPool multiplexerPool = initialize(Runtime.getRuntime().availableProcessors() * 2);

	private int maxThreadCount = (int) (Runtime.getRuntime().availableProcessors() * 2);
	private Map<Object, QueueDispatcherMultiplexer> multiplexerMap = new HashMap<Object, QueueDispatcherMultiplexer>(20);
	private List<QueueDispatcherMultiplexer> multiplexerList = new ArrayList<QueueDispatcherMultiplexer>(20);
	private ThreadGroup threadGroup = new ThreadGroup("QueueDispatcherMultiplexers");

	private QueueDispatcherMultiplexerPool(int maxThreadCount)
	{
		this.maxThreadCount = maxThreadCount;
		this.threadGroup.setDaemon(true);
	}

	/**
	 * Initializes QueueDispatcherMultiplexerPool by creating a pool of the
	 * specified max number of QueueDispatcherMultiplexer objects. This method
	 * must be invoked once to create a QueueDispatcherMultiplexerPool instance.
	 * {@link #getMultiplexer(String)} returns null otherwise.
	 * 
	 * @param maxThreadCount
	 *            Max number of QueueDispatcherMultiplexer objects.
	 * @return QueueDispatcherMultiplexerPool instance.
	 */
	private static QueueDispatcherMultiplexerPool initialize(int maxThreadCount)
	{
		if (multiplexerPool == null) {
			multiplexerPool = new QueueDispatcherMultiplexerPool(maxThreadCount);
		}
		return multiplexerPool;
	}

	/**
	 * Returns the QueueDispatcherMultiplexerPool instance created by
	 * {@link #initialize(int)} which must be invoked once beforehand.
	 */
	public static QueueDispatcherMultiplexerPool getQueueDispatcherMultiplexerPool()
	{
		return multiplexerPool;
	}

	/**
	 * Adds the specified {@link QueueDispatcherListener} that belongs to the
	 * specified tag. The specified listener receives {@link List} of events.
	 * 
	 * @param tag
	 *            Unique tag representing the specified listener
	 * @param listener
	 *            Queue dispatcher listener
	 * @return {@link QueueDispatcherMultiplexer} object responsible for
	 *         consuming and dispatching lists of events.
	 */
	public QueueDispatcherMultiplexer addQueueDispatcherListener(Object tag, QueueDispatcherListener listener)
	{
		QueueDispatcherMultiplexer multiplexer = createMultiplexerIfNotExist(tag);
		multiplexer.addQueueDispatcherListener(tag, listener);
		return multiplexer;
	}

	/**
	 * Returns the multiplexer responsible for the specified tag.
	 * 
	 * @param tag
	 *            Unique tag representing the listener
	 * @return null if the specified tag was not added by invoking
	 *         {@link #addQueueDispatcherListener(String, QueueDispatcherListener)}
	 */
	public QueueDispatcherMultiplexer getMultiplexer(Object tag)
	{
		return multiplexerMap.get(tag);
	}

	/**
	 * Returns the QueueDispatcherMultiplexer object that owns the specified
	 * tag. It creates a new QueueDispatcherMultiplexer object if it does not
	 * exist.
	 * 
	 * @param tag
	 *            Unique tag representing the specified listener
	 */
	private QueueDispatcherMultiplexer createMultiplexerIfNotExist(Object tag)
	{
		QueueDispatcherMultiplexer multiplexer = multiplexerMap.get(tag);
		if (multiplexer == null) {
			if (multiplexerList.size() < maxThreadCount) {
				multiplexer = new QueueDispatcherMultiplexer(threadGroup, "Pado-QueueDispatcherMultiplexer-"
						+ (multiplexerList.size() + 1));
				multiplexer.start();
				multiplexerList.add(multiplexer);

			} else {
				int smallestSize = Integer.MAX_VALUE;
				for (QueueDispatcherMultiplexer tt : multiplexerList) {
					if (tt.getQueueSize() < smallestSize) {
						smallestSize = tt.getQueueSize();
						multiplexer = tt;
					}
				}
			}
			multiplexerMap.put(tag, multiplexer);
		}
		return multiplexer;
	}

	/**
	 * Removes the specified tag. Once removed, invoking
	 * {@link #getMultiplexer(String)} with the specified tag returns null and
	 * invoking
	 * {@link QueueDispatcherMultiplexer#enqueue(String, com.gemstone.gemfire.cache.EntryEvent)}
	 * has no effects.
	 * 
	 * @param tag
	 *            Unique tag representing the specified listener
	 * @return Removed listener if exists; null otherwise.
	 */
	public QueueDispatcherListener removeQueueDispatcherListener(Object tag)
	{
		QueueDispatcherMultiplexer multiplexer = getMultiplexer(tag);
		if (multiplexer == null) {
			return null;
		}
		return multiplexer.removeQueueDispatcherListener(tag);
	}
}
