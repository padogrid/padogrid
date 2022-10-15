package org.kafka.addon.cluster;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

/**
 * ClusterUtil provides cluster/member specific convenience methods.
 * 
 * @author dpark
 *
 */
public class ClusterUtil {

	/**
	 * Returns the Kafka client producer config file defined by the system property
	 * {@link ClusterConstants#PROPERTY_PRODUCER_CONFIG_FILE}. If the property is
	 * not defined, then it returns
	 * {@link ClusterConstants#DEFAULT_PRODUCER_CONFIG_FILE}
	 */
	public static String getProducerConfigFile() {
		return System.getProperty(ClusterConstants.PROPERTY_PRODUCER_CONFIG_FILE,
				ClusterConstants.DEFAULT_PRODUCER_CONFIG_FILE);
	}

	/**
	 * Returns the Kafka client consumer config file defined by the system property
	 * {@link ClusterConstants#PROPERTY_CONSUMER_CONFIG_FILE}. If the property is
	 * not defined, then it returns
	 * {@link ClusterConstants#DEFAULT_CONSUMER_CONFIG_FILE}
	 */
	public static String getConsumerConfigFile() {
		return System.getProperty(ClusterConstants.PROPERTY_CONSUMER_CONFIG_FILE,
				ClusterConstants.DEFAULT_CONSUMER_CONFIG_FILE);
	}

	/**
	 * Creates a KafkaProducer instance configured with the properties read from the
	 * configuration file. The producer configuration file is specified by the
	 * system property, {@link ClusterConstants#DEFAULT_PRODUCER_CONFIG_FILE}.
	 * 
	 * @return KafkaProducer instance
	 * @throws IOException Thrown if an error occurs while reading the configuration
	 *                     file
	 */
	@SuppressWarnings("rawtypes")
	public static KafkaProducer createProducer() throws IOException {
		String configFile = getProducerConfigFile();
		if (!Files.exists(Paths.get(configFile))) {
			throw new IOException(configFile + " not found.");
		} else {
			try (InputStream inputStream = new FileInputStream(configFile)) {
				Properties props = new Properties();
				props.load(inputStream);
				return new KafkaProducer(props);
			}
		}
	}

	/**
	 * Creates a KafkaProducer instance configured with the specified properties. If
	 * overwriteConfigFile is true then it overwrites the configuration file
	 * settings; otherwise, it overrides (ignores) the configuration file.
	 * 
	 * @param producerProps       producer properties
	 * @param overwriteConfigFile true to overwrite configuration file, false to
	 *                            override (ignore) configuration file
	 * @return KafkaProducer instance
	 * @throws IOException Thrown if an error occurs while reading the configuration
	 *                     file
	 */
	@SuppressWarnings("rawtypes")
	public static KafkaProducer createProducer(Properties producerProps, boolean overwriteConfigFile)
			throws IOException {
		Properties props;
		if (overwriteConfigFile) {
			String configFile = getProducerConfigFile();
			if (!Files.exists(Paths.get(configFile))) {
				throw new IOException(configFile + " not found.");
			} else {
				try (InputStream inputStream = new FileInputStream(configFile)) {
					props = new Properties();
					props.load(inputStream);
					props.putAll(producerProps);
				}
			}
		} else {
			props = producerProps;
		}
		return new KafkaProducer(props);
	}

	/**
	 * Creates a KafkaConsumer instance configured with the properties read from the
	 * configuration file. The consumer configuration file is specified by the
	 * system property, {@link ClusterConstants#DEFAULT_CONSUMER_CONFIG_FILE}.
	 * 
	 * @return KafkaConsumer instance
	 * @throws IOException Thrown if an error occurs while reading the configuration
	 *                     file
	 */
	@SuppressWarnings("rawtypes")
	public static KafkaConsumer createConsumer() throws IOException {
		String configFile = getConsumerConfigFile();
		if (!Files.exists(Paths.get(configFile))) {
			throw new IOException(configFile + " not found.");
		} else {
			try (InputStream inputStream = new FileInputStream(configFile)) {
				Properties props = new Properties();
				props.load(inputStream);
				return new KafkaConsumer(props);
			}
		}
	}

	/**
	 * Creates a KafkaConsumer instance configured with the specified properties. If
	 * overwriteConfigFile is true then it overwrites the configuration file
	 * settings; otherwise, it overrides (ignores) the configuration file.
	 * 
	 * @param consumerProps       consumer properties
	 * @param overwriteConfigFile true to overwrite configuration file, false to
	 *                            override (ignore) configuration file
	 * @return KafkaConsumer instance
	 * @throws IOException Thrown if an error occurs while reading the configuration
	 *                     file
	 */
	@SuppressWarnings("rawtypes")
	public static KafkaConsumer createConsumer(Properties consumerProps, boolean overwriteConfigFile)
			throws IOException {
		Properties props;
		if (overwriteConfigFile) {
			String configFile = getConsumerConfigFile();
			if (!Files.exists(Paths.get(configFile))) {
				throw new IOException(configFile + " not found.");
			} else {
				try (InputStream inputStream = new FileInputStream(configFile)) {
					props = new Properties();
					props.load(inputStream);
					props.putAll(consumerProps);
				}
			}

		} else {
			props = consumerProps;
		}
		return new KafkaConsumer(props);
	}

}
