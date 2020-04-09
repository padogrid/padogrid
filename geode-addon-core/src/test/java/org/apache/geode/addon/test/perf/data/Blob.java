package org.apache.geode.addon.test.perf.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.geode.DataSerializable;
import org.apache.geode.DataSerializer;

/**
 * Blob takes an array of bytes and key/value pairs of data.
 * @author dpark
 *
 */
public class Blob implements DataSerializable
{
	private static final long serialVersionUID = 1L;
	private byte[] blob;
	private HashMap<Object, Object> map = new HashMap<Object, Object>(4);
	
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
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 */
	@Override
	public void toData(DataOutput out) throws IOException {
		DataSerializer.writeByteArray(blob, out);
		DataSerializer.writeHashMap(map, out);
	}

	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 */
	@Override
	public void fromData(DataInput in) throws IOException, ClassNotFoundException {
		blob = DataSerializer.readByteArray(in);
		map = DataSerializer.readHashMap(in);
	}
}
