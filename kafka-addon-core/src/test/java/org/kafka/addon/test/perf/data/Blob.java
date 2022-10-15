package org.kafka.addon.test.perf.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

/**
 * Blob takes an array of bytes and key/value pairs of data.
 * @author dpark
 *
 */
public class Blob implements Externalizable
{
	private static final long serialVersionUID = 1L;
	
	private byte[] blob;
	private Map<Object, Object> map = new HashMap<Object, Object>(4);
	
	public Blob()
	{		
	}
	
	public Blob(byte[] blob)
	{
		this.blob = blob;
	}
	
	public byte[] getBlob()
	{
		return blob;
	}
	
	public void setBlob(byte[] blob)
	{
		this.blob = blob;
	}
	
	public void put(Object key, Object value)
	{
		map.put(key, value);
	}
	
	public Object get(Object key)
	{
		return map.get(key);
	}
	
	public Map<Object, Object> getMap()
	{
		return map;
	}
	
	/**
	 * Writes the state of this object to the given <code>ObjectOutput</code>.
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(blob);
		out.writeObject(map);
	}

	/**
	 * Reads the state of this object from the given <code>ObjectInput</code>.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		blob = (byte[])in.readObject();
		map = (Map)in.readObject();
	}
}
