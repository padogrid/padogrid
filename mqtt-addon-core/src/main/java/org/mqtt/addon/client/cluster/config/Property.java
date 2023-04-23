package org.mqtt.addon.client.cluster.config;

public class Property {
	private String key;
	private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		if (value != null) {
			return ConfigUtil.parseStringValue(value);
		}
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
