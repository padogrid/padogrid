package org.oracle.coherence.addon.test.perf.data;

import java.util.Properties;

public interface DataObjectFactory {

	public void initialize(Properties props);

	/**
	 * @return the data object class
	 */
	public Class<?> getDataObjectClass();

	/**
	 * @return an entry with the specified idNum as part of the primary key
	 */
	public Entry createEntry(int idNum);

	/**
	 * Returns the key associated with the specified id
	 * 
	 * @param idNum Unique ID
	 * @return the key associated with the specified id
	 */
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
