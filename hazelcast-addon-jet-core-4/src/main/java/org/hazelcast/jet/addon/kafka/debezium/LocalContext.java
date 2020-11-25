package org.hazelcast.jet.addon.kafka.debezium;

import java.util.Properties;
import java.util.Set;

import org.json.JSONObject;

import com.hazelcast.jet.JetInstance;

/**
 * LocalContext provides Jet job configuration information needed in creating
 * pipelines. It should be used by clients that submit jobs to Jet.
 * 
 * @author dpark
 *
 */
public class LocalContext {
	private String name;
	private Properties jetProps;
	private JetInstance jet;

	/**
	 * Constructs LocalContext instance.
	 * 
	 * @param jet  Jet instance
	 * @param json Job configuration
	 * @throws ClassNotFoundException
	 */
	public LocalContext(JetInstance jet, JSONObject json) throws ClassNotFoundException {
		this.jet = jet;

		name = json.getString("name");
		if (name == null) {
			name = "jet-connector";
		}

		JSONObject config = json.getJSONObject("config");

		// Kafka props for Jet
		JSONObject jetConfig = json.getJSONObject("jet");
		jetProps = defaultKafkaProps();
		Set<String> keys = jetConfig.keySet();
		for (String key : keys) {
			jetProps.put(key, jetConfig.get(key).toString());
		}
		jetProps.put("topics", config.get("topics"));
	}

	/**
	 * @return Job name. If it is not defined in the configuration file, then
	 *         returns the default name, "jet-connector".
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Properties for connecting to the Kafka source.
	 */
	public Properties getKafkaProps() {
		return jetProps;
	}

	/**
	 * @return Jet instance
	 */
	public JetInstance getJet() {
		return jet;
	}

	/**
	 * @return All Jet properties including Kafka properties.
	 */
	public Properties getJetProps() {
		return jetProps;
	}

	/**
	 * @return Default Kakfa properties set with localhost and Apicurio Avro
	 *         serializer.
	 */
	private Properties defaultKafkaProps() {
		Properties props = new Properties();
		props.setProperty("bootstrap.servers", "localhost:9092");
		props.put("key.deserializer", "io.apicurio.registry.utils.serde.AvroKafkaDeserializer");
		props.put("value.deserializer", "io.apicurio.registry.utils.serde.AvroKafkaDeserializer");
		props.put("specific.avro.reader", true);
		props.put("schema.registry.url", "http://localhost:8080/api");
		props.put("apicurio.registry.url", "http://localhost:8080/api");
		props.setProperty("auto.offset.reset", "earliest");
		return props;
	}
}
