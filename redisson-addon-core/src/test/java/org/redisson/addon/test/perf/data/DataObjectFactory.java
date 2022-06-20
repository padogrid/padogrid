package org.redisson.addon.test.perf.data;

import java.util.Properties;

/**
 * DataObjectFactory allows provides data ingestion properties for filling data
 * object contents.
 * 
 * @author dpark
 *
 */
public interface DataObjectFactory {

	/**
	 * Initializes the data object factory with the specified properties.
	 * 
	 * @param props Data object factory properties
	 */
	public void initialize(Properties props);

	/**
	 * @return the data object class
	 */
	public Class<?> getDataObjectClass();

	/**
	 * Creates a new key/value entry with the specified ID.
	 * 
	 * @param idNum ID for constructing the unique key
	 * @param erKey If null or {@link #isEr()} is false then there are no
	 *              relationship and this argument is ignored.
	 * @return an entry with the specified idNum as part of the primary key
	 */
	public Entry createEntry(int idNum, Object erKey);

	/**
	 * Returns the key associated with the specified id
	 * 
	 * @param idNum Unique ID
	 * @return the key associated with the specified id
	 */
	public Object getKey(int idNum);

	/**
	 * @return true if ER defined.
	 */
	public boolean isEr();

	/**
	 * @return Maximum number of ER keys. If 0, then no ER. If 1, then one-to-one
	 *         ER. If >1, then one-to-many ER.
	 */
	public int getMaxErKeys();

	/**
	 * @return true if {@link #getMaxErKeys()} is used as the upper bound to
	 *         generate the max number of ER keys, false if the
	 *         {@link #getMaxErKeys()} is used as the max number of ER keys.
	 */
	public boolean isErMaxRandom();

	/**
	 * @return ER child operation name.
	 */
	public String getErOperationName();

	public static class Entry {
		public Object key;
		public Object value;

		public Entry(Object key, Object value) {
			this.key = key;
			this.value = value;
		}
	}
}
