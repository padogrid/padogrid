package org.kafka.addon.kafka.test.cluster.junit;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kafka.demo.nw.data.avro.Customer;
import org.kafka.demo.nw.data.avro.generated.__Customer;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

public class ConsumerCustomerTest {

	private static final String TOPIC = "customers";
	private static final Properties props = new Properties();
	private static String configFile;
	private static KafkaConsumer<String, __Customer> consumer;

	@BeforeClass
	public static void setUp() throws Exception {

		// Backwards compatibility, assume localhost
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-customers");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
		props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

		consumer = new KafkaConsumer<>(props);

	}

	@Test
	public void testConsumer() throws Exception {

		consumer.subscribe(Collections.singletonList(TOPIC));
		while (true) {
			final ConsumerRecords<String, __Customer> records = consumer.poll(Duration.ofMillis(100));
			for (final ConsumerRecord<String, __Customer> record : records) {
				final String key = record.key();
				final Customer customer = new Customer(record.value());
				System.out.printf("key = %s, value = %s%n", key, customer);
			}
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		consumer.close();
	}

}
