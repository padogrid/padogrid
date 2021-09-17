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

public enum SearchType {
	/**
	 * Search by key field (fields are object members)
	 */
	KEY_FIELD, 
	
	/**
	 * Search by value field (fields are object members)
	 */
	VALUE_FIELD, 
	
	/**
	 * Search by key object (must be Comparable)
	 */
	KEY_OBJECT,
	
	/**
	 * Search by value object (must be Comparable)
	 */
	VALUE_OBJECT
}
