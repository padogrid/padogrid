package org.apache.geode.addon.cluster.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Declarable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MultiInitializer handles multiple Geode/GemFire initializers.
 * 
 * @author dpark
 *
 */
public class MultiInitializer implements Declarable {

	private Logger logger = LogManager.getLogger(this.getClass());
	private Declarable initializers[];

	@SuppressWarnings({ "rawtypes" })
	public void initialize(Cache cache, Properties properties) {

		if (properties == null) {
			return;
		}

		// Gather prefixes
		// <className, Properties>
		Properties prefixClassNameProps = new Properties();
		HashSet<String> prefixSet = new HashSet<String>(3);
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			String k = (String) entry.getKey();
			int index = k.indexOf('.');
			if (index != -1) {
				String prefix = k.substring(0, index);
				prefixSet.add(prefix);
				if (k.equals(prefix + ".class-name")) {
					prefixClassNameProps.put(prefix, entry.getValue().toString());
				}
			}
		}

		HashMap<String, Properties> initializerMap = new HashMap<String, Properties>(3);
		Set<Map.Entry<Object, Object>> inputEntries = properties.entrySet();
		for (Map.Entry<Object, Object> entry : prefixClassNameProps.entrySet()) {
			String prefix = entry.getKey().toString();
			String prefixDot = prefix + ".";
			String className = entry.getValue().toString();
			Properties parameterProps = new Properties();
			initializerMap.put(className, parameterProps);
			for (Map.Entry<Object, Object> inputEntry : inputEntries) {
				String inputKey = inputEntry.getKey().toString();
				String inputValue = inputEntry.getValue().toString();
				if (inputKey.startsWith(prefixDot)) {
					int index = inputKey.indexOf('.');
					String parameterPart = inputKey.substring(index + 1);
					if (parameterPart.startsWith("parameter.")) {
						index = parameterPart.indexOf('.');
						String parameter = parameterPart.substring(index + 1);
						parameterProps.setProperty(parameter, inputValue);
					}
				}
			}
		}

		// Create Initializer instances
		initializers = new Declarable[initializerMap.size()];
		int i = 0;
		for (Map.Entry<String, Properties> entry : initializerMap.entrySet()) {
			String className = entry.getKey();
			Properties parameterProps = entry.getValue();
			try {
				Class clazz = Class.forName(className);
				Declarable initializer = (Declarable) clazz.newInstance();
				initializer.initialize(cache, parameterProps);
				// Must invoke the deprecated init(), also. Two entry points. Sigh..
				initializer.init(parameterProps);
				initializers[i++] = initializer;
			} catch (ClassNotFoundException e) {
				logger.error("Invalid initializer class name. Dicarded.", e);
			} catch (InstantiationException e) {
				logger.error(e);
			} catch (IllegalAccessException e) {
				logger.error(e);
			} catch (ClassCastException e) {
				logger.error("Invalid initializer. Declarable required. This class does not implement Declarable. ["
						+ className + "]. Discarded.");
			}
		}
	}
}
