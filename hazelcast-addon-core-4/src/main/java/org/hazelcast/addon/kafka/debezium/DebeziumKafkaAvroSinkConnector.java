package org.hazelcast.addon.kafka.debezium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import com.hazelcast.client.HazelcastClient;

/**
 * DebeziumKafkaSinkConnector registers the Hazelcast connector for Kafka.
 * 
 * @author dpark
 *
 */
public class DebeziumKafkaAvroSinkConnector extends SinkConnector {

	public static final String MAP_CONFIG = "map";
	public static final String DEBUG_ENABLED = "debug.enabled";
	public static final String SMT_ENABLED = "smt.enabled";
	public static final String DELETE_ENABLED = "delete.enabled";
	public static final String HAZELCAST_ENABLED = "hazelcast.enabled";
	public static final String AVRO_DEEP_COPY_ENABLED = "avro.deep.copy.enabled";
	public static final String COLUMN_NAMES_CASE_SENSITVIE_ENABLED = "column.names.case.sensitive.enabled";
	public static final String KEY_STRUCT_ENABLED = "key.struct.enabled";
	public static final String KEY_CLASS_NAME_CONFIG = "key.class";
	public static final String KEY_COLUMN_NAMES_CONFIG = "key.column.names";
	public static final String KEY_FIELD_NAMES_CONFIG = "key.field.names";
	public static final String VALUE_CLASS_NAME_CONFIG = "value.class";
	public static final String VALUE_COLUMN_NAMES_CONFIG = "value.column.names";
	public static final String VALUE_FIELD_NAMES_CONFIG = "value.field.names";
	public static final String HAZELCAST_CLIENT_CONFIG_FILE_CONFIG = "hazelcast.client.config";
	public static final String PARTITION_AWARE_INDEXES_CONFIG = "key.partitionAware.indexes";
	public static final String MAP_REPLICATED_MAP_ENABLED = "map.replicatedMap.enabled";

	private static final ConfigDef CONFIG_DEF = new ConfigDef()
			.define(HAZELCAST_CLIENT_CONFIG_FILE_CONFIG, Type.STRING, "/hazelcast-addon/etc/hazelcast-client.xml",
					Importance.MEDIUM, "Hazelcast client configuration file path.")
			.define(MAP_CONFIG, Type.STRING, "map", Importance.HIGH,
					"Destination map name. If not specified, then 'map' is assigned.")
			.define(MAP_REPLICATED_MAP_ENABLED, Type.BOOLEAN, false, Importance.MEDIUM,
					"ReplicatedMap flag. If true, then data is stored in ReplicatedMap, otherwise, IMap.")
			.define(DEBUG_ENABLED, Type.BOOLEAN, false, Importance.LOW,
					"Debug flag. If true, then debug information is printed.")
			.define(SMT_ENABLED, Type.BOOLEAN, true, Importance.HIGH,
					"Single Message Transform flag. If true, then SMT messages are expected.")
			.define(DELETE_ENABLED, Type.BOOLEAN, true, Importance.HIGH,
					"Single Message Transform flag. If true, then SMT messages are expected.")
			.define(HAZELCAST_ENABLED, Type.BOOLEAN, true, Importance.HIGH,
					"Hazelcast flag. If true, then Hazelcast is enabled and it makes connection to Hazelcast. "
					+ "If false, Hazelcast is ignored and only displays consumed data.")
			.define(AVRO_DEEP_COPY_ENABLED, Type.BOOLEAN, false, Importance.MEDIUM,
					"AVRO deep copy flag. If true, then the payload is deep-copied from Struct to Avro. Note "
					+ "that deep-copy copies the entire payload to the Avro object and therefore filtering "
					+ "is not supported. If false, the payload is shallow-copied from Struct to data object. "
					+ "Filtering is supported for shallow copy. Shallow-copy is also supported for all object types "
					+ "including Avro.")
			.define(COLUMN_NAMES_CASE_SENSITVIE_ENABLED, Type.BOOLEAN, true, Importance.MEDIUM,
					"If false, then the both the column names and object field names are treated case insensitive.")
			.define(KEY_STRUCT_ENABLED, Type.BOOLEAN, false, Importance.HIGH,
					"By default, the key objects are constructed with the columns in the value records. If this option "
					+ "is set to true, then the key objects are constructed using the key records. Note that if the key "
					+ "records are missing or any of the listed key columns are missing then it defaults to the value records.")
			.define(KEY_CLASS_NAME_CONFIG, Type.STRING, null, Importance.HIGH,
					"Key class name. Name of serializable class for transforming table key columns to key objects into Hazelcast. "
							+ "If not specified but the key column names are specified, then the column values are concatenated with the delimiter '.'. "
							+ "If the key column names are also not specified, then the Kafka key is used.")
			.define(KEY_COLUMN_NAMES_CONFIG, Type.STRING, null, Importance.HIGH,
					"Comma separated key column names. An ordered list of table column names to be mapped to the key object field (setter) names. If key column names are not defined, i.e., null, then a random UUID is assigned to the key instead.")
			.define(KEY_FIELD_NAMES_CONFIG, Type.STRING, null, Importance.HIGH,
					"Comma separated key object field (setter) names. An ordered list of key object field (setter) names to be mapped to the table column names.")
			.define(VALUE_CLASS_NAME_CONFIG, Type.STRING, null, Importance.HIGH,
					"Value class name. Name of serializable class for transforming table rows to value objects into Hazelcast. "
							+ "If not specified, then table rows are transformed to JSON objects.")
			.define(VALUE_COLUMN_NAMES_CONFIG, Type.STRING, null, Importance.HIGH,
					"Comma separated value column names. An ordered list of table column names to be mapped to the value object field (setter) names.")
			.define(VALUE_FIELD_NAMES_CONFIG, Type.STRING, null, Importance.HIGH,
					"Comma separated value object field (setter) names. An ordered list of value object field (setter) names to be mapped to the table column names.")
			.define(PARTITION_AWARE_INDEXES_CONFIG, Type.STRING, null, Importance.MEDIUM,
					"Comma separated indexes of the value fields for creating the partition ID and co-locating data. The first index of the value field list is 0."
					+ " The Hazelcast map must be configured with ParititionStrategy set to StringPartitioningStrategy or StringAndPartitionAwarePartitioningStrategy.");
			
	private String hazelcastClientConfigFile;
	private String mapName;
	private boolean isReplicatedMapEnabled = false;
	private boolean isDebugEnabled = false;
	private boolean isSmtEnabled = true;
	private boolean isDeleteEnabled = true;
	private boolean isHazelcastEnabled = true;
	private boolean isAvroDeepCopyEnabled = false;
	private boolean isColumnNamesCaseSensitiveEnabled = true;
	private boolean isKeyStructEnabled = false;
	private String keyClassName;
	private String keyColumnNames;
	private String keyFieldNames;
	private String valueClassName;
	private String valueColumnNames;
	private String valueFieldNames;
	private String partitionAwareIndexes;

	@Override
	public String version() {
		return AppInfoParser.getVersion();
	}
	
	@Override
	public void start(Map<String, String> props) {
		AbstractConfig parsedConfig = new AbstractConfig(CONFIG_DEF, props);
		hazelcastClientConfigFile = parsedConfig.getString(HAZELCAST_CLIENT_CONFIG_FILE_CONFIG);
		mapName = parsedConfig.getString(MAP_CONFIG);
		String isReplicatedMapEnabledStr = props.get(MAP_REPLICATED_MAP_ENABLED);
		isReplicatedMapEnabled = isReplicatedMapEnabledStr != null && isReplicatedMapEnabledStr.equalsIgnoreCase("true") ? true : isReplicatedMapEnabled;
		String isDebugStr = props.get(DEBUG_ENABLED);
		isDebugEnabled = isDebugStr != null && isDebugStr.equalsIgnoreCase("true") ? true : isDebugEnabled;
		String isSmtStr = props.get(SMT_ENABLED);
		isSmtEnabled = isSmtStr != null && isSmtStr.equalsIgnoreCase("false") ? false : isSmtEnabled;
		String isDeleteStr = props.get(DELETE_ENABLED);
		isDeleteEnabled = isDeleteStr != null && isDeleteStr.equalsIgnoreCase("false") ? false : isDeleteEnabled;
		String isHazelcastStr = props.get(HAZELCAST_ENABLED);
		isHazelcastEnabled = isHazelcastStr != null && isHazelcastStr.equalsIgnoreCase("false") ? false : isHazelcastEnabled;
		String isAvroDeepCopyStr = props.get(AVRO_DEEP_COPY_ENABLED);
		isAvroDeepCopyEnabled = isAvroDeepCopyStr != null && isAvroDeepCopyStr.equalsIgnoreCase("true") ? true : isAvroDeepCopyEnabled;
		String isColumnNamesCaseSensitiveStr = props.get(COLUMN_NAMES_CASE_SENSITVIE_ENABLED);
		isColumnNamesCaseSensitiveEnabled = isColumnNamesCaseSensitiveStr != null && isHazelcastStr.equalsIgnoreCase("false") ? false : isColumnNamesCaseSensitiveEnabled;
		String isKeyStructStr = props.get(KEY_STRUCT_ENABLED);
		isKeyStructEnabled = isKeyStructStr != null && isKeyStructStr.equalsIgnoreCase("false") ? false : isKeyStructEnabled;
		keyClassName = props.get(KEY_CLASS_NAME_CONFIG);
		keyColumnNames = props.get(KEY_COLUMN_NAMES_CONFIG);
		keyFieldNames = props.get(KEY_FIELD_NAMES_CONFIG);
		valueClassName = props.get(VALUE_CLASS_NAME_CONFIG);
		valueColumnNames = props.get(VALUE_COLUMN_NAMES_CONFIG);
		valueFieldNames = props.get(VALUE_FIELD_NAMES_CONFIG);
		partitionAwareIndexes = props.get(PARTITION_AWARE_INDEXES_CONFIG);

		System.out.println(
				"connector: ====================================================================================");
		try {
			Class<?> valueClass = Class.forName(valueClassName);
			Object valueObj = valueClass.newInstance();
			System.out.println("valueObj = " + valueObj);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(props);
		System.out.println("connector: mapName = " + mapName);
		System.out.println("connector: isReplicatedMapEnabled = " + isReplicatedMapEnabled);
		System.out.println("connector: isSmtEnabled = " + isSmtEnabled);
		System.out.println("connector: isDeleteEnabled = " + isDeleteEnabled);
		System.out.println("connector: isHazelcastEnabled = " + isHazelcastEnabled);
		System.out.println("connector: isAvroDeepCopyEnabled = " + isAvroDeepCopyEnabled);
		System.out.println("connector: isColumnNamesCaseSensitiveEnabled = " + isColumnNamesCaseSensitiveEnabled);
		System.out.println("connector: isKeyStructEnabled = " + isKeyStructEnabled);
		System.out.println(
				"connector: ====================================================================================");
		System.out.flush();
	}

	@Override
	public Class<? extends Task> taskClass() {
		return DebeziumKafkaAvroSinkTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		ArrayList<Map<String, String>> configs = new ArrayList<>();
		for (int i = 0; i < maxTasks; i++) {
			Map<String, String> config = new HashMap<>();
			if (hazelcastClientConfigFile != null) {
				config.put(HAZELCAST_CLIENT_CONFIG_FILE_CONFIG, hazelcastClientConfigFile);
			}
			if (mapName != null) {
				config.put(MAP_CONFIG, mapName);
			}
			config.put(MAP_REPLICATED_MAP_ENABLED, Boolean.toString(isReplicatedMapEnabled));
			config.put(DEBUG_ENABLED, Boolean.toString(isDebugEnabled));
			config.put(SMT_ENABLED, Boolean.toString(isSmtEnabled));
			config.put(DELETE_ENABLED, Boolean.toString(isDeleteEnabled));
			config.put(HAZELCAST_ENABLED, Boolean.toString(isHazelcastEnabled));
			config.put(AVRO_DEEP_COPY_ENABLED, Boolean.toString(isAvroDeepCopyEnabled));
			config.put(COLUMN_NAMES_CASE_SENSITVIE_ENABLED, Boolean.toString(isHazelcastEnabled));
			config.put(KEY_STRUCT_ENABLED, Boolean.toString(isKeyStructEnabled));
			if (keyClassName != null) {
				config.put(KEY_CLASS_NAME_CONFIG, keyClassName);
			}
			if (keyColumnNames != null) {
				config.put(KEY_COLUMN_NAMES_CONFIG, keyColumnNames);
			}
			if (keyFieldNames != null) {
				config.put(KEY_FIELD_NAMES_CONFIG, keyFieldNames);
			}
			if (valueClassName != null) {
				config.put(VALUE_CLASS_NAME_CONFIG, valueClassName);
			}
			if (valueColumnNames != null) {
				config.put(VALUE_COLUMN_NAMES_CONFIG, valueColumnNames);
			}
			if (valueFieldNames != null) {
				config.put(VALUE_FIELD_NAMES_CONFIG, valueFieldNames);
			}
			if (partitionAwareIndexes != null) {
				config.put(PARTITION_AWARE_INDEXES_CONFIG, partitionAwareIndexes);
			}
			configs.add(config);
		}
		return configs;
	}

	@Override
	public void stop() {
		// Do not shutdown Hazelcast here. Hazelcast shutdown is done on an individual task basis.
	}

	@Override
	public ConfigDef config() {
		return CONFIG_DEF;
	}

}
