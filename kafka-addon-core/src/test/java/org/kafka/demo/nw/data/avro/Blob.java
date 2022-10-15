package org.kafka.demo.nw.data.avro;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Transient;

/**
 * Blob takes an array of bytes and key/value pairs of data.
 * 
 * @author dpark
 *
 */
public class Blob {

	private org.kafka.demo.nw.data.avro.generated.__Blob avro;

	public Blob() {
		avro = new org.kafka.demo.nw.data.avro.generated.__Blob(null,
				new HashMap<java.lang.CharSequence, java.lang.CharSequence>(4));
	}

	public Blob(byte[] blob) {
		avro = new org.kafka.demo.nw.data.avro.generated.__Blob(ByteBuffer.wrap(blob),
				new HashMap<java.lang.CharSequence, java.lang.CharSequence>(4));
	}

	public Blob(org.kafka.demo.nw.data.avro.generated.__Blob avro) {
		setAvro(avro);
	}

	public void setAvro(org.kafka.demo.nw.data.avro.generated.__Blob avro) {
		this.avro = avro;
	}

	@Transient
	public org.kafka.demo.nw.data.avro.generated.__Blob getAvro() {
		return avro;
	}

	public void setBlob(byte[] blob) {
		avro.setBlob(ByteBuffer.wrap(blob));
	}

	public byte[] getBlob() {
		if (avro.getBlob() == null) {
			return null;
		}
		return avro.getBlob().array();
	}

	public void put(String key, String value) {
		avro.getMap().put(key, value);
	}

	public Object get(String key) {
		return avro.getMap().get(key);
	}

	public Map<CharSequence, CharSequence> getMap() {
		return avro.getMap();
	}

	@Override
	public String toString() {
		if (getBlob() == null) {
			return "Blob [getBlob()=null" + ", getMap()" + getMap() + "]";
		} else {
			return "Blob [getBlob().length=" + getBlob().length + ", getMap()=" + getMap() + "]";
		}
	}
}
