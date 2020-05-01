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
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QueueDispatcher dispatches enqueued events one at a time or in a batch at a time.
 * Each new instance of QeueuDispatcher is backed by a new daemon consumer thread. 
 * @author dpark
 *
 */
public class QueueDispatcher
{
	private static final Logger logger = LoggerFactory.getLogger(QueueDispatcher.class);
	
	private ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue<Object>();
	private QueueDispatcherListener queueDispatcherListener;
	private ConsumerThread consumerThread;
	private boolean terminated = true;
	private boolean paused = false;
	private int batchSize = 1;
	private long timeIntervalInMsec = 500;
	private String name = "Pado-QueueDispatcher.ConsumerThread";
	private Object lock = new Object();

	/**
	 * Constructs a single event dispatcher.
	 * {@link QueueDispatcherListener#objectDispatched(Object)} receives a
	 * single object at a time. Set batchSize to greater than 1 to receive
	 * {@link Collection} of objects.
	 */
	public QueueDispatcher()
	{
	}

	/**
	 * Constructs a dispatcher with the specified batch size. It defaults the
	 * time interval to 500 msec.
	 * 
	 * @param batchSize
	 *            If greater than 1 then a batch of objects is dispatched.
	 *            QueueDispatcherListener#objectDispatched(Object)} must
	 *            type-cast {@link Collection} the received object.
	 */
	public QueueDispatcher(int batchSize)
	{
		setBatchSize(batchSize);
	}

	/**
	 * Constructs a dispatcher with the specified batch size and time interval.
	 * 
	 * @param batchSize
	 *            If greater than 1 then a batch of objects is dispatched.
	 *            QueueDispatcherListener#objectDispatched(Object)} must
	 *            type-cast {@link Collection} the received object.
	 * @param timeIntervalInMsec
	 *            Time interval in msec. Objects are dispatched if the queue
	 *            reaches the batch size or the time interval, whichever comes
	 *            first.
	 */
	public QueueDispatcher(int batchSize, long timeIntervalInMsec)
	{
		setBatchSize(batchSize);
		setTimeInterval(timeIntervalInMsec);
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setTimeInterval(long timeIntervalInMsec)
	{
		this.timeIntervalInMsec = timeIntervalInMsec;
	}

	public long getTimeInterval()
	{
		return timeIntervalInMsec;
	}

	/**
	 * Sets the batch size. The {@link #start()} method determines the
	 * dispatcher type (single vs. batch) which is maintained throughout the
	 * dispatcher's life. In other words, changing the batch size to 1 after
	 * {@link #start} will not change the dispatcher type to single from batch.
	 * 
	 * @param batchSize
	 *            If greater than 1 then a batch of objects is dispatched.
	 *            QueueDispatcherListener#objectDispatched(Object)} must
	 *            type-cast {@link Collection} the received object. If less than
	 *            1 then defaults to 1.
	 */
	public void setBatchSize(int batchSize)
	{
		if (batchSize < 1) {
			this.batchSize = 1;
		} else {
			this.batchSize = batchSize;
		}
	}

	public int getBatchSize()
	{
		return this.batchSize;
	}

	public void start()
	{
		if (consumerThread == null) {
			consumerThread = new ConsumerThread(getName());
			consumerThread.setDaemon(true);
			consumerThread.start();
			terminated = false;
		}
	}

	public void stop()
	{
		if (consumerThread != null) {
			consumerThread.terminate();
			consumerThread = null;
		}
	}

	public void pause()
	{
		paused = true;
	}

	public void resume()
	{
		paused = false;
	}

	public void enqueue(Object obj)
	{
		// queue does not need to be synchronized for adding objects.
		// queue is synchronized only when the operation gets affected if
		// the queue size is reduced.
		queue.offer(obj);
		if (paused == false && queue.size() >= batchSize) {
			synchronized (lock) {
				lock.notify();
			}
		}
	}

	public Object dequeue() throws InterruptedException
	{
		return dequeue(0);
	}

	public Object dequeue(long timeoutInMsec) throws InterruptedException
	{
		while (paused || queue.size() == 0) {
			synchronized (lock) {
				lock.wait(timeoutInMsec);
			}
		}
		if (paused || queue.size() == 0) {
			return null;
		} else {
			return queue.poll();
		}
	}

	public List<Object> dequeueBatch() throws InterruptedException
	{
		return dequeueBatch(0);
	}

	public List<Object> dequeueBatch(long timeoutInMsec) throws InterruptedException
	{
		while (paused || queue.size() == 0) {
			synchronized (lock) {
				lock.wait(timeoutInMsec);
			}
		}
		int count = queue.size();
		if (paused || count == 0) {
			return null;
		} else {
			List<Object> batch = new ArrayList<Object>(count);
			try {
				Iterator<Object> iterator = queue.iterator();
				int i = 1;
				while (i <= count && iterator.hasNext()) {
					batch.add(iterator.next());
					iterator.remove();
					i++;
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			return batch;
		}
	}

	public int size()
	{
		return queue.size();
	}

	public boolean isEmpty()
	{
		return queue.size() == 0;
	}

	/**
	 * Clears the queue.
	 */
	public void clear()
	{
		queue.clear();
	}

	public boolean isTerminated()
	{
		return terminated;
	}

	public void setQueueDispatcherListener(QueueDispatcherListener listener)
	{
		this.queueDispatcherListener = listener;
	}

	public QueueDispatcherListener getQueueDispatcherListener()
	{
		return queueDispatcherListener;
	}

	class ConsumerThread extends Thread
	{
		private boolean shouldRun = true;
		private boolean isBatch = false;

		ConsumerThread(String name)
		{
			super(name);
			setDaemon(true);
		}

		public boolean isBatch()
		{
			return isBatch;
		}
		
		public void run()
		{
			isBatch = batchSize > 1;
			if (isBatch) {
				runBatch();
			} else {
				runSingle();
			}
		}

		private void runSingle()
		{
			while (shouldRun) {
				try {
					Object obj = dequeue(1000);
					if (obj != null && queueDispatcherListener != null) {
						queueDispatcherListener.objectDispatched(obj);
					}
				} catch (InterruptedException ex) {
					// ignore
				}
			}
			terminated = true;
			clear();
		}

		private void runBatch()
		{
			while (shouldRun) {
				try {
					List<Object> batch = dequeueBatch(timeIntervalInMsec);
					if (batch != null && queueDispatcherListener != null) {
						if (batch.size() > 0) {
							queueDispatcherListener.objectDispatched(batch);
						}
					}
				} catch (InterruptedException ex) {
					// ignore
				}
			}
			terminated = true;
			clear();
		}

		public void terminate()
		{
			shouldRun = false;
		}
	}

	/**
	 * Flushes the queue by dispatching all of the objects in the queue.
	 */
	public void flush()
	{
		if (queueDispatcherListener != null) {
			if (consumerThread != null && consumerThread.isBatch()) {
				if (queue.size() > 0) {
					Iterator<Object> iterator = queue.iterator();
					ArrayList<Object> list = new ArrayList<Object>(queue.size());
					while (iterator.hasNext()) {
						list.add(iterator.hasNext());
					}
					queueDispatcherListener.objectDispatched(list);
				}
			} else {
				Iterator<Object> iterator = queue.iterator();
				while (iterator.hasNext()) {
					queueDispatcherListener.objectDispatched(iterator.next());
				}
			}
		}
		queue.clear();
	}
}
