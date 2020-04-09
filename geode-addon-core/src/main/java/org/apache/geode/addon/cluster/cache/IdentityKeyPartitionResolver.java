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

import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.EntryOperation;
import org.apache.geode.cache.PartitionResolver;

/**
 * IdentityKeyPartitionResolver extracts the routing key from the string entry
 * key comprised of tokens separated by the default delimiter '.'. The routing
 * key constitutes one or more tokens in the entry key. The token indexes by
 * defined in an array by invoking {@link #setRoutingKeyIndexs(int[])} or a
 * comma separated string by defining the 'indexes' parameter in the
 * configuration (cache.xml) file. The index value begins from 0. If the token
 * indexes are not defined then the routing key is set to the second token,
 * i.e., index value of 1.
 * 
 * @author dpark
 *
 */
@SuppressWarnings("rawtypes")
public class IdentityKeyPartitionResolver implements Declarable, PartitionResolver {
	private int[] routingKeyIndexes;
	private String delimiter = ".";
	private int largestIndex = -1;
	private boolean routingKeyIndexesDefined = false;

	public IdentityKeyPartitionResolver() {
	}

	public Object getRoutingObject(EntryOperation opDetails) {
		Object key = opDetails.getKey();
		if (key instanceof IRoutingKey) {
			key = ((IRoutingKey) key).getRoutingKey();
		} else {
			key = getRoutingKey(key.toString());
		}
		return key;
	}

	private String getRoutingKey(String key) {
		if (routingKeyIndexesDefined == false) {
			return key;
		}

		// Non-regex delimiter
		String tokens[] = key.toString().split(Pattern.quote(delimiter));

		// If the number of tokens is less than the number of allowed tokens then
		// return the passed-in key. This enables any key to be used as a routing key.
		if (tokens.length <= routingKeyIndexes.length || tokens.length <= largestIndex) {
			return key;
		}
		StringBuffer buffer = new StringBuffer(20);
		for (int index : routingKeyIndexes) {
			buffer.append(tokens[index]);
		}
		key = buffer.toString();
		return key;
	}

	@Override
	public void initialize(Cache cache, Properties properties) {
		this.delimiter = properties.getProperty("delimiter", this.delimiter);
		String indexes = properties.getProperty("indexes");
		if (indexes != null) {
			String tokens[] = indexes.split(",");
			routingKeyIndexes = new int[tokens.length];
			for (int i = 0; i < tokens.length; i++) {
				routingKeyIndexes[i] = Integer.parseInt(tokens[i]);
			}
			setRoutingKeyIndexs(routingKeyIndexes);
		}
	}

	public void close() {
	}

	public String getName() {
		return "IdentityKeyPartitionResolver";
	}

	public void setCompositeKeyInfo(CompositeKeyInfo compositeKeyInfo) {
		if (compositeKeyInfo != null) {
			setRoutingKeyIndexs(compositeKeyInfo.getRoutingKeyIndexes());
			setCompositeKeyDelimiter(compositeKeyInfo.getCompositeKeyDelimiter());
		}
	}

	public CompositeKeyInfo getCompositeKeyInfo() {
		return new CompositeKeyInfo(this.routingKeyIndexes, this.delimiter);
	}

	public void setRoutingKeyIndexs(int[] routingKeyIndexes) {
		this.routingKeyIndexes = routingKeyIndexes;
		routingKeyIndexesDefined = routingKeyIndexes != null && routingKeyIndexes.length > 0;
		largestIndex = -1;
		if (routingKeyIndexesDefined) {
			for (int i : routingKeyIndexes) {
				if (largestIndex < i) {
					largestIndex = i;
				}
			}
		}
	}

	public int[] getRoutingKeyIndexes() {
		return this.routingKeyIndexes;
	}

	public void setCompositeKeyDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getCompositeKeyDelimiter() {
		return this.delimiter;
	}
}
