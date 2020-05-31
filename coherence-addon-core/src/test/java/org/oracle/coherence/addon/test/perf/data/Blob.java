package org.oracle.coherence.addon.test.perf.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class Blob implements PortableObject {
	private byte[] blob;
	private Map<Object, Object> map = new HashMap<Object, Object>(4);

	public Blob() {
	}

	public Blob(byte[] blob) {
		this.blob = blob;
	}

	public byte[] getBlob() {
		return blob;
	}

	public void setBlob(byte[] blob) {
		this.blob = blob;
	}

	public void put(Object key, Object value) {
		map.put(key, value);
	}

	public Object get(Object key) {
		return map.get(key);
	}

	public Map<Object, Object> getMap() {
		return map;
	}

	@Override
	public void readExternal(PofReader reader) throws IOException {
		int i = 0;
		blob = reader.readByteArray(i++);
		map = reader.readMap(i++, map);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		int i = 0;
		writer.writeByteArray(i++, blob);
		writer.writeMap(i++, map);
	}
}
