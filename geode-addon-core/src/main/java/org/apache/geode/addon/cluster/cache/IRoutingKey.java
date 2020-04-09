/*
 * Copyright (c) 2020 Netcrest Technologies, LLC. All rights reserved.
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
package org.apache.geode.addon.cluster.cache;

/**
 * IRoutingKey is implemented by a composite key class for colocating data. If
 * provides the identity key (or the entry key) and the routing key.
 * 
 * @author dpark
 *
 */
public interface IRoutingKey {
	Object getIdentityKey();

	Object getRoutingKey();
}
