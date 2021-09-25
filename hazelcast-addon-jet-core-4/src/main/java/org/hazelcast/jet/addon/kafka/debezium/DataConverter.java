package org.hazelcast.jet.addon.kafka.debezium;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.specific.SpecificData;
import org.hazelcast.addon.kafka.debezium.DebeziumKafkaAvroSinkConnector;
import org.hazelcast.addon.kafka.debezium.ObjectConverter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Util;
import com.hazelcast.map.IMap;
import com.hazelcast.replicatedmap.ReplicatedMap;

/**
 * DataConverter converts raw entries to object entries and puts
 * them in Hazelcast IMDG if Hazelcast IMDG is enabled. An instance of
 * DataConverter is provided by {@linkplain TransformerContext} which
 * in turn is managed by {@linkplain JetConnector}.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DataConverter<K, V> {

	private static final Logger logger = LoggerFactory.getLogger(DataConverter.class);

	private boolean isDebugEnabled = false;

	private HazelcastInstance hzInstance;
	private String mapName;
	private Map map;
	private boolean isReplicatedMapEnabled = false;
	private boolean isSmtEnabled = true;
	private boolean isDeleteEnabled = true;
	private boolean isHazelcastEnabled = true;
	private boolean isAvroDeepCopyEnabled = false;
	private boolean isColumnNamesCaseSensitiveEnabled = true;
	private boolean isKeyStructEnabled = false;
	private String keyClassName;
	private String valueClassName;
	private Schema keyAvroSchema;
	private Schema valueAvroSchema;
	private String[] keyColumnNames;
	private String[] keyFieldNames;
	private String[] valueColumnNames;
	private String[] valueFieldNames;
	private ObjectConverter objConverter;
	private Schema avroSchema;
	int[] colocatedFieldIndexes = new int[0];

	DataConverter(Map<String, String> props, HazelcastInstance imdg) {
		this.hzInstance = imdg;
		init(props);
	}

	private void init(Map<String, String> props) {
		String hazelcastConfigFile = props.get(DebeziumKafkaAvroSinkConnector.HAZELCAST_CLIENT_CONFIG_FILE_CONFIG);
		if (hazelcastConfigFile == null) {
			hazelcastConfigFile = "/hazelcast-addon/etc/hazelcast-client.xml";
		}
		mapName = props.get(DebeziumKafkaAvroSinkConnector.MAP_CONFIG);
		if (mapName == null) {
			mapName = "map";
		}
		String isReplicatedMapEnabledStr = props.get(DebeziumKafkaAvroSinkConnector.MAP_REPLICATED_MAP_ENABLED);
		isReplicatedMapEnabled = isReplicatedMapEnabledStr != null && isReplicatedMapEnabledStr.equalsIgnoreCase("true")
				? true
				: isReplicatedMapEnabled;
		String isDebugStr = props.get(DebeziumKafkaAvroSinkConnector.DEBUG_ENABLED);
		isDebugEnabled = isDebugStr != null && isDebugStr.equalsIgnoreCase("true") ? true : isDebugEnabled;
		String isSmtStr = props.get(DebeziumKafkaAvroSinkConnector.SMT_ENABLED);
		isSmtEnabled = isSmtStr != null && isSmtStr.equalsIgnoreCase("false") ? false : isSmtEnabled;
		String isDeleteStr = props.get(DebeziumKafkaAvroSinkConnector.DELETE_ENABLED);
		isDeleteEnabled = isDeleteStr != null && isDeleteStr.equalsIgnoreCase("false") ? false : isDeleteEnabled;
		String isHazelcastStr = props.get(DebeziumKafkaAvroSinkConnector.HAZELCAST_ENABLED);
		isHazelcastEnabled = isHazelcastStr != null && isHazelcastStr.equalsIgnoreCase("false") ? false
				: isHazelcastEnabled;
		String isAvroDeepCopyStr = props.get(DebeziumKafkaAvroSinkConnector.AVRO_DEEP_COPY_ENABLED);
		isAvroDeepCopyEnabled = isAvroDeepCopyStr != null && isAvroDeepCopyStr.equalsIgnoreCase("true") ? true
				: isAvroDeepCopyEnabled;
		String isColumnNamesCaseSensitiveStr = props
				.get(DebeziumKafkaAvroSinkConnector.COLUMN_NAMES_CASE_SENSITVIE_ENABLED);
		isColumnNamesCaseSensitiveEnabled = isColumnNamesCaseSensitiveStr != null
				&& isColumnNamesCaseSensitiveStr.equalsIgnoreCase("false") ? false : isColumnNamesCaseSensitiveEnabled;
		String isKeyStructStr = props.get(DebeziumKafkaAvroSinkConnector.KEY_STRUCT_ENABLED);
		isKeyStructEnabled = isKeyStructStr != null && isKeyStructStr.equalsIgnoreCase("false") ? false
				: isKeyStructEnabled;
		keyClassName = props.get(DebeziumKafkaAvroSinkConnector.KEY_CLASS_NAME_CONFIG);
		valueClassName = props.get(DebeziumKafkaAvroSinkConnector.VALUE_CLASS_NAME_CONFIG);
		if (valueClassName != null) {
			try {
				valueAvroSchema = getAvroSchema(valueClassName);
			} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException
					| NoSuchFieldException ex) {
				throw new RuntimeException("Invalid Avro value class [" + valueClassName + "]", ex);
			}
		}

		// Key
		String cnames = props.get(DebeziumKafkaAvroSinkConnector.KEY_COLUMN_NAMES_CONFIG);
		String fnames = props.get(DebeziumKafkaAvroSinkConnector.KEY_FIELD_NAMES_CONFIG);
		String tokens[];

		if (cnames != null) {
			tokens = cnames.split(",");
			keyColumnNames = new String[tokens.length];
			for (int j = 0; j < tokens.length; j++) {
				keyColumnNames[j] = tokens[j].trim();
			}
		}
		if (fnames != null) {
			tokens = fnames.split(",");
			keyFieldNames = new String[tokens.length];
			for (int j = 0; j < tokens.length; j++) {
				keyFieldNames[j] = tokens[j].trim();
			}
		}

		// Value
		cnames = props.get(DebeziumKafkaAvroSinkConnector.VALUE_COLUMN_NAMES_CONFIG);
		fnames = props.get(DebeziumKafkaAvroSinkConnector.VALUE_FIELD_NAMES_CONFIG);
		if (cnames != null) {
			tokens = cnames.split(",");
			valueColumnNames = new String[tokens.length];
			for (int j = 0; j < tokens.length; j++) {
				valueColumnNames[j] = tokens[j].trim();
			}
		}
		if (fnames != null) {
			tokens = fnames.split(",");
			valueFieldNames = new String[tokens.length];
			for (int j = 0; j < tokens.length; j++) {
				valueFieldNames[j] = tokens[j].trim();
			}
		}

		// PartitionAware
		String pwIndexesStr = props.get(DebeziumKafkaAvroSinkConnector.PARTITION_AWARE_INDEXES_CONFIG);
		if (pwIndexesStr != null) {
			tokens = pwIndexesStr.split(",");
			colocatedFieldIndexes = new int[tokens.length];
			for (int i = 0; i < tokens.length; i++) {
				try {
					int index = Integer.parseInt(tokens[i]);
					if (index < 0 || index >= valueFieldNames.length) {
						throw new RuntimeException("Invalid PartitionAware index: " + index + ". "
								+ "It must be >=0 or less than the number of value field names. DebeziumKafkaAvroSinkConnector.PARTITION_AWARE_INDEXES_CONFIG=\"pwIndexesStr\"");
					}
					colocatedFieldIndexes[i] = index;

				} catch (NumberFormatException ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		if (isDebugEnabled) {
			logger.info("====================================================================================");
			String classpathStr = System.getProperty("java.class.path");
			System.out.print("classpath=" + classpathStr);

			logger.info(props.toString());
			logger.info("mapName=" + mapName);
			logger.info("isReplicatedMapEnabled=" + isReplicatedMapEnabled);
			logger.info("smtEnabled=" + isSmtEnabled);
			logger.info("deleteEnabled=" + isDeleteEnabled);
			logger.info("keyClassName=" + keyClassName);
			logger.info("keyColumnNames");
			if (keyColumnNames == null) {
				logger.info("keyColumnNames=null");
			} else {
				logger.info("keyColumnNames");
				for (int i = 0; i < keyColumnNames.length; i++) {
					logger.info("   [" + i + "] " + keyColumnNames[i]);
				}
			}
			if (keyFieldNames == null) {
				logger.info("keyFieldNames=null");
			} else {
				logger.info("keyFieldNames");
				for (int i = 0; i < keyFieldNames.length; i++) {
					logger.info("   [" + i + "] " + keyFieldNames[i]);
				}
			}
			logger.info("valueClassName=" + valueClassName);
			logger.info("valueColumnNames");
			for (int i = 0; i < valueColumnNames.length; i++) {
				logger.info("   [" + i + "] " + valueColumnNames[i]);
			}
			logger.info("valueFieldNames");
			for (int i = 0; i < valueFieldNames.length; i++) {
				logger.info("   [" + i + "] " + valueFieldNames[i]);
			}
			if (colocatedFieldIndexes.length == 0) {
				logger.info("partitionAwareIndexes: undefined");
			} else {
				logger.info("partitionAwareIndexes");
				for (int i = 0; i < colocatedFieldIndexes.length; i++) {
					logger.info("   [" + i + "] " + colocatedFieldIndexes[i] + ": "
							+ valueFieldNames[colocatedFieldIndexes[i]]);
				}
			}

			logger.info("====================================================================================");
		}
		try {
			objConverter = new ObjectConverter(keyClassName, keyFieldNames, valueClassName, valueFieldNames);
			objConverter.setColumnNamesCaseSensitive(isColumnNamesCaseSensitiveEnabled);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		Class<?> clazz;
		try {
			clazz = Class.forName(valueClassName);
			java.lang.reflect.Field field = (java.lang.reflect.Field) clazz.getField("SCHEMA$");
			avroSchema = (Schema) field.get(null);

			// Create a new schema in case the specified class is a wrapper class.
			JSONObject jo = new JSONObject(avroSchema.toString());
			jo.put("name", clazz.getSimpleName());
			jo.put("namespace", clazz.getPackage().getName());
			avroSchema = new Schema.Parser().parse(jo.toString());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (hzInstance == null) {
			System.setProperty("hazelcast.client.config", hazelcastConfigFile);
			hzInstance = HazelcastClient.newHazelcastClient();
		}
		if (isHazelcastEnabled) {
			if (isReplicatedMapEnabled) {
				map = hzInstance.getReplicatedMap(mapName);
			} else {
				map = hzInstance.getMap(mapName);
			}
		}
	}

	private String[] getColumnNames(GenericData.Record record) {
		if (record == null) {
			return null;
		}
		List<Schema.Field> fieldList = record.getSchema().getFields();
		String[] columnNames = new String[fieldList.size()];
		int j = 0;
		for (Schema.Field field : fieldList) {
			columnNames[j++] = field.name();
		}
		return columnNames;
	}

	private Schema getAvroSchema(String className) throws ClassNotFoundException, IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException, SecurityException {
		Class<?> clazz = Class.forName(className);
		java.lang.reflect.Field field = (java.lang.reflect.Field) clazz.getField("SCHEMA$");
		Schema schema = (Schema) field.get(null);

		// Create a new schema in case the specified class is a wrapper class.
		JSONObject jo = new JSONObject(schema.toString());
		jo.put("name", clazz.getSimpleName());
		jo.put("namespace", clazz.getPackage().getName());
		return new Schema.Parser().parse(jo.toString());
	}

	/**
	 * Converts the specified GenericData.Record entry in object form and puts it in Hazelcast
	 * IMDG if it is enabled.
	 * 
	 * @param entry Key/value entry of GenericData records.
	 * @return Converted key/value objects
	 */
	public Map.Entry<K, V> putEntry(Map.Entry<GenericData.Record, GenericData.Record> entry) {
		GenericData.Record keyRecord = (GenericData.Record) entry.getKey();
		GenericData.Record valueRecord = (GenericData.Record) entry.getValue();

		if (isDebugEnabled) {
			logger.info("keySchema=" + keyAvroSchema);
			logger.info("valueSchema=" + valueAvroSchema);
			if (keyAvroSchema != null) {
				logger.info("keyFields=" + keyAvroSchema.getFields());
			}
			if (valueAvroSchema != null) {
				logger.info("valueAvroFields=" + valueAvroSchema.getFields());
			}
		}

		boolean isDelete = valueRecord == null;
		Object op = null;
		if (valueRecord != null) {
			op = valueRecord.get("op");
			isDelete = op != null && op.toString().equals("d");
		}

		GenericData.Record keyAfter = null;
		if (keyRecord != null) {
			keyAfter = (GenericData.Record) keyRecord.get("after");
		}
		GenericData.Record valueAfter = (GenericData.Record) valueRecord.get("after");

		/*
		 * Key
		 */
		K key = null;

		// Determine the key column names.
		if (keyColumnNames == null) {
			keyColumnNames = getColumnNames(keyAfter);
		}

		// If the key column names are not defined or cannot be determined then
		// assign UUID for the key value
		if (keyColumnNames == null) {
			key = (K) UUID.randomUUID().toString();
		}

		/*
		 * Value
		 */
		V value = null;

		if (isAvroDeepCopyEnabled) {
			value = (V) SpecificData.get().deepCopy(avroSchema, valueAfter);

			if (key == null) {
				Object keyFieldValues[] = new Object[keyColumnNames.length];
				for (int j = 0; j < keyColumnNames.length; j++) {
					keyFieldValues[j] = valueAfter.get(keyColumnNames[j]);
				}
				try {
					key = (K) objConverter.createKeyObject(keyFieldValues);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | ParseException e) {
					throw new RuntimeException(e);
				}
			}

		} else {

			// Determine the value column names.
			if (valueColumnNames == null) {
				valueColumnNames = getColumnNames(valueAfter);
			}
			Object valueFieldValues[] = new Object[valueColumnNames.length];
			for (int j = 0; j < valueColumnNames.length; j++) {
				valueFieldValues[j] = valueAfter.get(valueColumnNames[j]);
			}
			try {
				value = (V) objConverter.createValueObject(valueFieldValues);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | ParseException e) {
				throw new RuntimeException(e);
			}

			if (isDebugEnabled) {
				for (int j = 0; j < valueColumnNames.length; j++) {
					logger.info("valueColumnNames[" + j + "] = " + valueColumnNames[j] + ": " + valueFieldValues[j]);
				}
			}

			if (key == null) {
				Object keyFieldValues[] = new Object[keyColumnNames.length];
				for (int j = 0; j < keyColumnNames.length; j++) {
					keyFieldValues[j] = valueAfter.get(keyColumnNames[j]);
				}
				try {
					key = (K) objConverter.createKeyObject(keyFieldValues);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | ParseException e) {
					throw new RuntimeException(e);
				}
			}

			if (colocatedFieldIndexes.length > 0) {
				String keyStr = key.toString() + "@";
				String dot = "";
				for (int index : colocatedFieldIndexes) {
					if (valueFieldValues[index] == null) {
						keyStr = keyStr + dot + "null";
					} else {
						keyStr = keyStr + dot + valueFieldValues[index].toString();
					}
					dot = ".";
				}
				key = (K) keyStr;
			}
		}

		if (isDebugEnabled) {
			logger.info("**** key class=" + key.getClass().getName());
			logger.info("**** key=" + key);
		}

		if (isDeleteEnabled && isDelete) {
			if (isHazelcastEnabled) {
				if (isReplicatedMapEnabled) {
					// ReplicatedMap has no support for delete that has void return type
					map.remove(key);
				} else {
					// Invoke delete() to avoid returning previous value
					((IMap) map).delete(key);
				}
			}
		}
		if (isHazelcastEnabled) {
			if (map instanceof ReplicatedMap) {
				map.put(key, value);
			} else {
				((IMap) map).set(key, value);
			}
		}

		if (isDebugEnabled) {
			logger.info("**** value class=" + value.getClass().getName());
			logger.info("**** value=" + value);
		}

		return Util.entry(key, value);
	}
}
