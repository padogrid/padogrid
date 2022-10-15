package org.kafka.addon.cluster;

public interface ClusterConstants {
	public final String PROPERTY_PRODUCER_CONFIG_FILE = "org.kafka.addon.client.producer.config.file";
	public final String PROPERTY_CONSUMER_CONFIG_FILE = "org.kafka.addon.client.consumer.config.file";

	public final String DEFAULT_PRODUCER_CONFIG_FILE = "etc/kafka-producer.properties";
	public final String DEFAULT_CONSUMER_CONFIG_FILE = "etc/kafka-consumer.properties";

	public final String DEFAULT_BROKER_ADDRESS = "localhost:9092";
}
