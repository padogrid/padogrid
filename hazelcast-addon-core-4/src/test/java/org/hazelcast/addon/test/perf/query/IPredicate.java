package org.hazelcast.addon.test.perf.query;

import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;

public interface IPredicate<K, V> {
	void init(IMap<K, V> map);
	
	Predicate<K, V> getPredicate();
}
