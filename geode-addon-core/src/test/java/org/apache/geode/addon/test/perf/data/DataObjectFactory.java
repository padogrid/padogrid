package org.apache.geode.addon.test.perf.data;

import java.util.Properties;

public interface DataObjectFactory {
	
	public void initialize(Properties props);
	
	public Entry createEntry(int idNum);
	
	public Object getKey(int idNum);

	public static class Entry {
		public Object key;
		public Object value;

		public Entry(Object key, Object value) {
			this.key = key;
			this.value = value;
		}
	}
}
