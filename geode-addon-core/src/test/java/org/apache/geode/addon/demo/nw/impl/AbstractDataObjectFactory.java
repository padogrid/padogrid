package org.apache.geode.addon.demo.nw.impl;

import java.util.Properties;
import java.util.Random;

import org.apache.geode.addon.test.perf.data.DataObjectFactory;

public abstract class AbstractDataObjectFactory implements DataObjectFactory {

	protected String keyPrefix;
	protected int keyPrefixLength;
	protected boolean isKeyRandom;
	protected int keyLength = 10;
	protected boolean isKeyLeadingZeros;
	protected Random random = new Random();

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

	@Override
	public Object getKey(int idNum) {
		return createKey(idNum);
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
	
	protected String createForeignKey(String foreignKeyPrefix, int foreignKeyMax) {
		int val = random.nextInt(foreignKeyMax);
		if (isKeyLeadingZeros) {
			String str = Integer.toString(val);
			int leadingZeroCount = keyLength - foreignKeyPrefix.length() - str.length();
			for (int i = 0; i < leadingZeroCount; i++) {
				str = "0" + str;
			}
			return foreignKeyPrefix + str;
		} else {
			return foreignKeyPrefix + val;
		}
	}
}