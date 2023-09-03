package org.hazelcast.addon.test.perf.query;

public interface ISql {
	void init(String arg);
	
	String getSql();
}
