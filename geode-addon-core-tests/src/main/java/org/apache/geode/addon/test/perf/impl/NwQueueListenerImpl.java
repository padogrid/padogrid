package org.apache.geode.addon.test.perf.impl;

import java.util.List;

import org.apache.geode.cache.asyncqueue.AsyncEvent;
import org.apache.geode.cache.asyncqueue.AsyncEventListener;

/**
 * This class consumes async events and does nothing else. It is used to capture
 * GemFire statistics.
 * 
 * @author dpark
 *
 */
public class NwQueueListenerImpl implements AsyncEventListener {

	@Override
	public boolean processEvents(List<AsyncEvent> events) {
		// Do nothing`
		return true;
	}

}
