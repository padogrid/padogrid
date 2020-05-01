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

/**
 * QueueDispatcherListener is invoked by QueueDispatcher to deliver dequeued
 * events.
 * 
 * @author dpark
 * @since 1.0
 */
public interface QueueDispatcherListener {
	/**
	 * Invoked when QueueDispatcher dispatches an object.
	 * 
	 * @param obj Dispatched object. Object may be {@link Collection} if the
	 *            dispatcher is configured to dispatch batches of objects. The
	 *            implementing class must properly type-cast this object with
	 *            {@link Collection}
	 */
	void objectDispatched(Object obj);
}
