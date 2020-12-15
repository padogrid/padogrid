package org.hazelcast.jet.addon.kafka.debezium;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hazelcast.addon.kafka.debezium.DebeziumKafkaAvroSinkConnector;
import org.json.JSONObject;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.replicatedmap.ReplicatedMap;

/**
 * TransformerContext is the server-side context object containing object
 * converter and transformation information.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TransformerContext {
	Map<String, String> props;
	Properties jetProps;
	JetInstance jet;
	HazelcastInstance imdg;
	DataConverter<?, ?> converter;
	boolean isHazelcastEnabled = true;
	boolean isJetEnabled = false;
	boolean isReplicatedMapEnabled = false;
	ReplicatedMap<?, ?> rmap;
	IMap<?, ?> map;
	Transformer<?, ?> transformer;

	TransformerContext(JetInstance jet, JSONObject json) throws ClassNotFoundException {

		// Connector props
		props = new HashMap<String, String>();
		setProperty(props, json, "name");
		JSONObject config = json.getJSONObject("config");
		Set<String> keys = config.keySet();
		for (String key : keys) {
			props.put(key, config.get(key).toString());
		}

		// Kafka props for Jet
		JSONObject jetConfig = json.getJSONObject("jet");
		jetProps = defaultKafkaProps();
		keys = jetConfig.keySet();
		for (String key : keys) {
			jetProps.put(key, jetConfig.get(key).toString());
		}
		if (props.get("value.converter.apicurio.registry.url") != null) {
			props.put("schema.registry.url", props.get("value.converter.apicurio.registry.url"));
			props.put("apicurio.registry.url", props.get("value.converter.apicurio.registry.url"));
		}
		if (jetProps.getProperty("transformer") != null) {
			try {
				Class cls = Class.forName(jetProps.getProperty("transformer"));
				transformer = (Transformer) cls.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(
						"Error encountered while instantiating transformer: " + jetProps.getProperty("transformer"), e);
			}
		}

		// Create IMDG client
		String isHazelcastStr = props.get(DebeziumKafkaAvroSinkConnector.HAZELCAST_ENABLED);
		isHazelcastEnabled = isHazelcastStr != null && isHazelcastStr.equalsIgnoreCase("false") ? false
				: isHazelcastEnabled;
		if (isHazelcastEnabled) {
//			ClientConfig clientConfig = createImdgClientConfig();
			this.imdg = HazelcastClient.newHazelcastClient();
		}

		this.jet = jet;
		this.converter = new DataConverter(props, imdg);
		String jetEnabledStr = jetProps.getProperty("jet.enabled");
		this.isJetEnabled = jetEnabledStr != null && jetEnabledStr.equalsIgnoreCase("true") ? true : this.isJetEnabled;
		if (isJetEnabled) {
			String mapName = props.get(DebeziumKafkaAvroSinkConnector.MAP_CONFIG);
			if (mapName == null) {
				mapName = "map";
			}
			String isReplicatedMapEnabledStr = props.get(DebeziumKafkaAvroSinkConnector.MAP_REPLICATED_MAP_ENABLED);
			this.isReplicatedMapEnabled = isReplicatedMapEnabledStr != null
					&& isReplicatedMapEnabledStr.equalsIgnoreCase("true") ? true : this.isReplicatedMapEnabled;
			if (this.isReplicatedMapEnabled) {
				rmap = jet.getReplicatedMap(mapName);
			} else {
				map = jet.getMap(mapName);
			}
		}
	}

	private ClientConfig createImdgClientConfig() {
		if (props.get("hazelcast.client.config") != null) {
			System.setProperty("hazelcast.client.config", props.get("hazelcast.client.config"));
		}
		return ClientConfig.load();
	}

	public String getName() {
		String name = props.get("name");
		if (name == null) {
			return "jet-connector";
		} else {
			return name;
		}
	}

	public Map<String, String> getProps() {
		return props;
	}

	public Properties getKafkaProps() {
		return jetProps;
	}

	public JetInstance getJet() {
		return jet;
	}

	public HazelcastInstance getImdg() {
		return imdg;
	}

	public DataConverter getConverter() {
		return converter;
	}

	public boolean isJetEnabled() {
		return isJetEnabled;
	}

	public Properties getJetProps() {
		return jetProps;
	}

	public boolean isHazelcastEnabled() {
		return isHazelcastEnabled;
	}

	public boolean isReplicatedMapEnabled() {
		return isReplicatedMapEnabled;
	}

	/**
	 * 
	 * @return ReplicatedMap instance. null if not defined.
	 */
	public ReplicatedMap getRmap() {
		return rmap;
	}

	/**
	 * @return Returns IMap instance. null if not defined.
	 */
	public IMap getMap() {
		return map;
	}

	/**
	 * Transforms the specified key/value object.
	 * 
	 * @param entry Key/value entry
	 * @return Object returned by the callback
	 */
	public Object transform(Map.Entry entry) {
		if (transformer != null) {
			return transformer.transform(this, entry);
		} else {
			return entry;
		}
	}

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

	private Map<String, String> setProperty(Map<String, String> props, JSONObject json, String key) {
		if (json.get(key) != null) {
			props.put(key, json.get(key).toString());
		}
		return props;
	}

}
