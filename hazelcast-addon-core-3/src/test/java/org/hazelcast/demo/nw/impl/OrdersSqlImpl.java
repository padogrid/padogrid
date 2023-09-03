package org.hazelcast.demo.nw.impl;

import org.hazelcast.addon.test.perf.query.ISql;

public class OrdersSqlImpl implements ISql {
	private String mapName = "\"nw/orders\"";
	
	@Override
	public void init(String arg) {
		if (arg != null) {
			mapName = arg;
		}
	}

	@Override
	public String getSql() {
		return String.format("select * from %s where freight>20", mapName);
	}
}
