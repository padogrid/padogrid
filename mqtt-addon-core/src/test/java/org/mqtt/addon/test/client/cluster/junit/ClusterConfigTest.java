package org.mqtt.addon.test.client.cluster.junit;

import java.io.File;
import java.io.FileReader;

import org.junit.Assert;
import org.junit.Test;
import org.mqtt.addon.client.cluster.IClusterConfig;
import org.mqtt.addon.client.cluster.config.ClusterConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * ClusterConfigTest tests {@linkplain ClusterConfig}.
 * 
 * @author dpark
 *
 */
public class ClusterConfigTest implements IClusterConfig {
	@Test
	public void testConfig() {
		System.setProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE, "etc/mqttv5-publisher.yaml");
		String configFile = System.getProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE);
		File file = new File(configFile);
		if (file.exists() == false) {
			Assert.fail(String.format("Config file not found [%s]", configFile));
		}
		try {
			Yaml yaml = new Yaml(new Constructor(ClusterConfig.class));
			FileReader reader = new FileReader(file);
			ClusterConfig config = yaml.load(reader);
			System.out.println(config);
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}
}
