package org.hazelcast.demo.nw.impl;

import java.util.Properties;

import org.hazelcast.addon.test.perf.data.DataObjectFactory;

public abstract class AbstractDataObjectFactory implements DataObjectFactory {

	protected String keyPrefix;
	protected int keyPrefixLength;
	protected boolean isKeyRandom;
	protected int keyLength = 10;
	protected boolean isKeyLeadingZeros;
	protected int maxErKeys = 1;
	protected String erOperationName;
	protected boolean isErMaxRandom = true;

	@Override
	public void initialize(Properties props) {
		keyPrefix = props.getProperty("factory.key.prefix");
		if (keyPrefix == null) {
			keyPrefix = props.getProperty("key.prefix", "k");
		}
		keyPrefixLength = keyPrefix.length();
		keyLength = Integer.valueOf(props.getProperty("factory.key.length", "-1"));
		isKeyRandom = Boolean.valueOf(props.getProperty("factory.key.isRandom", "true"));
		isKeyLeadingZeros = Boolean.valueOf(props.getProperty("factory.key.isLeadingZeros", "false"));
		maxErKeys = Integer.valueOf(props.getProperty("factory.er.maxKeys", "1"));
		erOperationName = props.getProperty("factory.er.operation");
		isErMaxRandom = Boolean.valueOf(props.getProperty("factory.er.isRandom", "true"));		
	}

	public String getKeyPrefix() {
		return keyPrefix;
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public int getKeyPrefixLength() {
		return keyPrefixLength;
	}

	public void setKeyPrefixLength(int keyPrefixLength) {
		this.keyPrefixLength = keyPrefixLength;
	}

	public boolean isKeyRandom() {
		return isKeyRandom;
	}

	public void setKeyRandom(boolean isKeyRandom) {
		this.isKeyRandom = isKeyRandom;
	}

	public int getKeyLength() {
		return keyLength;
	}

	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}

	public boolean isKeyLeadingZeros() {
		return isKeyLeadingZeros;
	}

	public void setKeyLeadingZeros(boolean isKeyLeadingZeros) {
		this.isKeyLeadingZeros = isKeyLeadingZeros;
	}

	public boolean isEr() {
		return erOperationName != null;
	}

	public void setMaxErKeys(int maxErKeys) {
		this.maxErKeys = maxErKeys;
	}

	public void setErOperationName(String erChildOperationName) {
		this.erOperationName = erChildOperationName;
	}

	public int getMaxErKeys() {
		return maxErKeys;
	}

	public String getErOperationName()
	{
		return erOperationName;
	}

	public boolean isErMaxRandom() {
		return isErMaxRandom;
	}

	public void setErMaxRandom(boolean isErMaxRandom) {
		this.isErMaxRandom = isErMaxRandom;
	}

	@Override
	public Object getKey(int idNum) {
		return createKey(idNum);
	}

	public Entry[] createEntries(int[] idNums, Object[] erKeys) {
		return null;
	}

	protected String createKey(int idNum) {
		if (keyLength <= 0) {
			return Integer.toString(idNum);
		}
		if (isKeyLeadingZeros) {
			String str = Integer.toString(idNum);
			int leadingZeroCount = keyLength - keyPrefixLength - str.length();
			for (int i = 0; i < leadingZeroCount; i++) {
				str = "0" + str;
			}
			return keyPrefix + str;
		} else {
			return keyPrefix + idNum;
		}
	}
}