package org.mqtt.addon.test.client.cluster.junit;

import java.lang.reflect.Field;
import java.util.Map;

public class TestUtil {
	/**
	 * Sets the specified environment variable.
	 * 
	 * @param envvar Environment variable
	 * @param value  Environment variable value
	 */
	@SuppressWarnings("unchecked")
	public static void setEnv(String envvar, String value) {
		try {
			Map<String, String> env = System.getenv();
			Class<?> cl = env.getClass();
			Field field = cl.getDeclaredField("m");
			field.setAccessible(true);
			Map<String, String> writableEnv = (Map<String, String>) field.get(env);
			writableEnv.put(envvar, value);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to set environment variable", e);
		}
	}
}
