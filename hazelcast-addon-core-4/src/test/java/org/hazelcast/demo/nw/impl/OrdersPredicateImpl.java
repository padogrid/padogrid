package org.hazelcast.demo.nw.impl;

import org.hazelcast.addon.test.perf.query.IPredicate;
import org.hazelcast.demo.nw.data.Order;

import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder.EntryObject;
import com.hazelcast.query.Predicates;

public class OrdersPredicateImpl implements IPredicate<String, Order> {
	IMap<String, Order> map;
	
	@Override
	public void init(IMap<String, Order> map) {
		this.map = map;
	}

	@Override
	public Predicate<String, Order> getPredicate() {
		EntryObject e = Predicates.newPredicateBuilder().getEntryObject();
		Predicate<String, Order> predicate = e.get("freight").lessThan(20);
		return predicate;
	}
}