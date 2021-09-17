package org.hazelcast.addon.test.perf.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

/**
 * Blob takes an array of bytes and key/value pairs of data.
 * @author dpark
 *
 */
public class Blob implements DataSerializable
{
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
	 * Writes the state of this object to the given <code>DataOutput</code>.
	 */
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeByteArray(blob);
		out.writeObject(map);
	}

	/**
	 * Reads the state of this object from the given <code>DataInput</code>.
	 */
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		blob = in.readByteArray();
		map = (Map)in.readObject();
	}
}
