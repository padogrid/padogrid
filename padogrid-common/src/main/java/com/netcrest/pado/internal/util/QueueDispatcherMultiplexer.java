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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * QueueDispatcherMultiplexer multiplexes event dispatchers by event tags. A
 * unique tag represents a unique dispatcher. Dispatcher listeners always
 * receive collections ({@link Collection}) of events.
 * 
 * @author dpark
 *
 */
public class QueueDispatcherMultiplexer extends Thread {
	private Map<Object, TemporalThreadState> stateMap = new ConcurrentHashMap<Object, TemporalThreadState>();
	private int batchSize = 500;
	private long timeIntervalInMsec = 500;
	private boolean shouldRun = true;
	private boolean terminated = false;

	private Object lock = new Object();

	public QueueDispatcherMultiplexer(ThreadGroup group, String name) {
		super(group, name);
		setDaemon(true);
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public int getBatchSize() {
		return this.batchSize;
	}

	public void setTimeInterval(long timeIntervalInMsec) {
		this.timeIntervalInMsec = timeIntervalInMsec;
	}

	public long getTimeInterval() {
		return this.timeIntervalInMsec;
	}

	public int getQueueSize() {
		return stateMap.size();
	}

	public void addQueueDispatcherListener(Object tag, QueueDispatcherListener listener) {
		TemporalThreadState state = stateMap.get(tag);
		if (state == null) {
			state = new TemporalThreadState();
			state.batch = new ConcurrentLinkedQueue<Object>();
			state.listener = listener;
			stateMap.put(tag, state);
		}
	}

	public QueueDispatcherListener removeQueueDispatcherListener(Object tag) {
		TemporalThreadState state = stateMap.remove(tag);
		return state.listener;
	}

	public void enqueue(String tag, Object event) {
		TemporalThreadState state = stateMap.get(tag);
		if (state == null) {
			return;
		}
		state.batch.add(event);
		if (state.paused == false && state.batch.size() >= batchSize) {
			synchronized (lock) {
				lock.notify();
			}
		}
	}

	public void clear(Object tag) {
		TemporalThreadState state = stateMap.get(tag);
		if (state != null) {
			state.batch.clear();
		}
	}

	public void pause(Object tag) {
		TemporalThreadState state = stateMap.get(tag);
		if (state != null) {
			state.paused = true;
		}
	}

	public boolean isPaused(Object tag) {
		TemporalThreadState state = stateMap.get(tag);
		if (state == null) {
			return false;
		} else {
			return state.paused;
		}
	}

	public void resume(Object tag) {
		TemporalThreadState state = stateMap.get(tag);
		if (state != null) {
			state.paused = false;
		}
	}

	private void dispatch() {
		Set<Map.Entry<Object, TemporalThreadState>> set = stateMap.entrySet();
		for (Map.Entry<Object, TemporalThreadState> entry : set) {
			TemporalThreadState state = entry.getValue();
			int count = state.batch.size();
			if (state.paused || count == 0) {
				continue;
			}
			List<Object> batch = new ArrayList<Object>(count);
			Iterator<Object> iterator = state.batch.iterator();
			int i = 1;
			while (i <= count && iterator.hasNext()) {
				batch.add(iterator.next());
				iterator.remove();
				i++;
			}
			state.listener.objectDispatched(batch);
		}
	}

	public void flush(Object tag) {
		TemporalThreadState state = stateMap.get(tag);
		if (state != null) {
			int count = state.batch.size();
			if (count > 0) {
				List<Object> batch = new ArrayList<Object>(count);
				Iterator<Object> iterator = state.batch.iterator();
				int i = 1;
				while (i <= count && iterator.hasNext()) {
					batch.add(iterator.next());
					iterator.remove();
					i++;
				}
				state.listener.objectDispatched(batch);
			}
		}
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void run() {
		while (shouldRun) {
			synchronized (lock) {
				try {
					lock.wait(timeIntervalInMsec);
					dispatch();
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		dispatch();
		terminated = true;
	}

	class TemporalThreadState {
		ConcurrentLinkedQueue<Object> batch;
		QueueDispatcherListener listener;
		boolean paused;
	}
}
