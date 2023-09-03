package org.hazelcast.demo.nw.impl;

import org.hazelcast.addon.test.perf.query.IPredicate;
import org.hazelcast.demo.nw.data.Order;

import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class OrdersPredicateImpl implements IPredicate<String, Order> {
	IMap<String, Order> map;
	
	@Override
	public void init(IMap<String, Order> map) {
		this.map = map;
	}

	@Override
	public Predicate<String, Order> getPredicate() {
		Predicate<String, Order> predicate = Predicates.lessThan("freight", 20);
		return predicate;
	}
}