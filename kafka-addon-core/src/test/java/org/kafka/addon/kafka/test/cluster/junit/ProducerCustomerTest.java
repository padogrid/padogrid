package org.kafka.addon.kafka.test.cluster.junit;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kafka.demo.nw.data.avro.Customer;
import org.kafka.demo.nw.data.avro.generated.__Customer;
import org.kafka.demo.nw.impl.CustomerFactoryImpl;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

public class ProducerCustomerTest {

	private static final String TOPIC = "customers";
	private static final Properties props = new Properties();
	private static KafkaProducer<String, __Customer> producer;

	@BeforeClass
	public static void setUp() throws Exception {

		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

		producer = new KafkaProducer<String, __Customer>(props);
	}

	@Test
	public void testProducer() throws Exception {
		CustomerFactoryImpl customerFactory = new CustomerFactoryImpl();
		for (long i = 0; i < 10; i++) {
			final Customer customer = customerFactory.createCustomer();
			final String customerId = "id" + Long.toString(i);
			customer.setCustomerId(customerId);
			final ProducerRecord<String, __Customer> record = new ProducerRecord<String, __Customer>(TOPIC,
					customer.getCustomerId().toString(), customer.getAvro());
			producer.send(record);
			Thread.sleep(1000L);
		}

		producer.flush();
		System.out.printf("Successfully produced 10 messages to a topic called %s%n", TOPIC);

	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (producer != null) {
			producer.close();
		}
	}
}
