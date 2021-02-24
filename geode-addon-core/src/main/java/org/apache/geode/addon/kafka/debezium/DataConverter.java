package org.apache.geode.addon.kafka.debezium;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.specific.SpecificData;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netcrest.pado.gemfire.GemfireEntryImpl;
import com.netcrest.pado.internal.util.ObjectConverter;

import io.confluent.connect.avro.AvroData;

/**
 * DataConverter converts raw entries to object entries and puts them in
 * Geode/GemFire if Gedoe/GemFire is enabled.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DataConverter<K, V> {

	private static final Logger logger = LoggerFactory.getLogger(DataConverter.class);

	private ClientCache clientCache;
	private boolean isDebugEnabled = false;

	private String gemfirePropertyFile;
	private String gemfireClientFile;
	private String regionPath;
	private Region region;
	private boolean isSmtEnabled = true;
	private boolean isDeleteEnabled = true;
	private boolean isGeodeEnabled = true;
	private boolean isAvroDeepCopyEnabled = false;
	private boolean isColumnNamesCaseSensitiveEnabled = true;
	private boolean isKeyStructEnabled = false;
	private String keyClassName;
	private String valueClassName;
//	private Schema keyAvroSchema;
//	private Schema valueAvroSchema;
	private String[] keyColumnNames;
	private String[] keyFieldNames;
	private String[] valueColumnNames;
	private String[] valueFieldNames;
	private ObjectConverter objConverter;
	private Schema avroSchema;
	int[] colocatedFieldIndexes = new int[0];

	public DataConverter(Map<String, String> props) {
		init(props);
	}
	
	private void log(String message) {
//		logger.info(message);
		System.out.println(message);
	}

	private void init(Map<String, String> props) {
		gemfirePropertyFile = props.get(DebeziumKafkaSinkConnector.GEMFIRE_PROPERTY_FILE_CONFIG);
		if (gemfirePropertyFile == null) {
			gemfirePropertyFile = "/padogrid/etc/client-gemfire.properties";
		}
		gemfireClientFile = props.get(DebeziumKafkaSinkConnector.GEMFIRE_CLIENT_CONFIG_FILE_CONFIG);
		if (gemfireClientFile == null) {
			gemfireClientFile = "/padogrid/etc/client-cache.xml";
		}
		regionPath = props.get(DebeziumKafkaSinkConnector.REGION_CONFIG);
		if (regionPath == null) {
			regionPath = "myregion";
		}
		String isDebugStr = props.get(DebeziumKafkaAvroSinkConnector.DEBUG_ENABLED);
		isDebugEnabled = isDebugStr != null && isDebugStr.equalsIgnoreCase("true") ? true : isDebugEnabled;
		String isSmtStr = props.get(DebeziumKafkaAvroSinkConnector.SMT_ENABLED);
		isSmtEnabled = isSmtStr != null && isSmtStr.equalsIgnoreCase("false") ? false : isSmtEnabled;
		String isDeleteStr = props.get(DebeziumKafkaAvroSinkConnector.DELETE_ENABLED);
		isDeleteEnabled = isDeleteStr != null && isDeleteStr.equalsIgnoreCase("false") ? false : isDeleteEnabled;
		String isGeodeStr = props.get(DebeziumKafkaAvroSinkConnector.GEODE_ENABLED);
		isGeodeEnabled = isGeodeStr != null && isGeodeStr.equalsIgnoreCase("false") ? false : isGeodeEnabled;
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
		
//		if (valueClassName != null) {
//			try {
//				valueAvroSchema = getAvroSchema(valueClassName);
//			} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException
//					| NoSuchFieldException ex) {
//				throw new RuntimeException("Invalid Avro value class [" + valueClassName + "]", ex);
//			}
//		}

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
			log("====================================================================================");
			String classpathStr = System.getProperty("java.class.path");
			System.out.print("classpath=" + classpathStr);

			log(props.toString());
			log("isDebugEnabled=" + isDebugEnabled);
			log("regionPath=" + regionPath);
			log("smtEnabled=" + isSmtEnabled);
			log("deleteEnabled=" + isDeleteEnabled);
			log("keyClassName=" + keyClassName);
			log("keyColumnNames");
			if (keyColumnNames == null) {
				log("keyColumnNames=null");
			} else {
				log("keyColumnNames");
				for (int i = 0; i < keyColumnNames.length; i++) {
					log("   [" + i + "] " + keyColumnNames[i]);
				}
			}
			if (keyFieldNames == null) {
				log("keyFieldNames=null");
			} else {
				log("keyFieldNames");
				for (int i = 0; i < keyFieldNames.length; i++) {
					log("   [" + i + "] " + keyFieldNames[i]);
				}
			}
			log("valueClassName=" + valueClassName);
			log("valueColumnNames");
			for (int i = 0; i < valueColumnNames.length; i++) {
				log("   [" + i + "] " + valueColumnNames[i]);
			}
			log("valueFieldNames");
			for (int i = 0; i < valueFieldNames.length; i++) {
				log("   [" + i + "] " + valueFieldNames[i]);
			}
			if (colocatedFieldIndexes.length == 0) {
				log("partitionAwareIndexes: undefined");
			} else {
				log("partitionAwareIndexes");
				for (int i = 0; i < colocatedFieldIndexes.length; i++) {
					log("   [" + i + "] " + colocatedFieldIndexes[i] + ": "
							+ valueFieldNames[colocatedFieldIndexes[i]]);
				}
			}

			log("====================================================================================");
		}
		try {
			objConverter = new ObjectConverter(keyClassName, keyFieldNames, valueClassName, valueFieldNames);
			objConverter.setColumnNamesCaseSensitive(isColumnNamesCaseSensitiveEnabled);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		if (isAvroDeepCopyEnabled) {
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
		}

		if (isGeodeEnabled) {
			System.setProperty(DebeziumKafkaSinkConnector.GEMFIRE_PROPERTY_FILE_CONFIG, gemfirePropertyFile);
			System.setProperty(DebeziumKafkaSinkConnector.GEMFIRE_CLIENT_CONFIG_FILE_CONFIG, gemfireClientFile);
			clientCache = new ClientCacheFactory().create();
			region = clientCache.getRegion(regionPath);
		}
	}
	
	private String[] getColumnNames(Struct structObj) {
		if (structObj == null) {
			return null;
		}
		List<Field> fieldList = structObj.schema().fields();
		String[] columnNames = new String[fieldList.size()];
		int j = 0;
		for (Field field : fieldList) {
			columnNames[j++] = field.name();
		}
		return columnNames;
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
	
	public void put(Collection<SinkRecord> records) {
		HashMap keyValueMap = new HashMap();
		int count = 0;
		for (SinkRecord sinkRecord : records) {

			org.apache.kafka.connect.data.Schema keySchema = sinkRecord.keySchema();
			org.apache.kafka.connect.data.Schema valueSchema = sinkRecord.valueSchema();

			if (isDebugEnabled) {
				log("sinkRecord=" + sinkRecord);
				log("keySchema=" + keySchema);
				log("valueSchema=" + valueSchema);
				if (keySchema == null) {
					log("keyFields=null");
				} else {
					log("keyFields=" + keySchema.fields());
				}
				if (valueSchema == null) {
					log("valueSchema=null");
				} else {
					log("valueFields=" + valueSchema.fields());
				}
			}

			// Struct objects expected
			Object keyObj = sinkRecord.key();
			Object valueObj = sinkRecord.value();

			if (isDebugEnabled) {
				if (keyObj == null) {
					log("keyObjType=null");
				} else {
					log("keyObjType=" + keyObj.getClass().getName());
				}				
				log("valueObj=" + valueObj.getClass().getName());
				log("keyObj=" + keyObj);
				log("valueObj=" + valueObj);
			}

			Struct keyStruct;
			if (keyObj == null) {
				keyStruct = null;
			} else {
				keyStruct = (Struct) keyObj;
			}
			Struct valueStruct = (Struct) sinkRecord.value();
			boolean isDelete = valueStruct == null;
			Object op = null;
			if (valueStruct != null) {
				op = valueStruct.get("op");
				isDelete = op != null && op.toString().equals("d");
			}

			/*
			 * Key
			 */
			Object key = null;

			// Determine the key column names.
			if (keyColumnNames == null) {
				keyColumnNames = getColumnNames(keyStruct);
			}

			// If the key column names are not defined or cannot be determined then
			// assign UUID for the key value
			if (keyColumnNames == null) {
				key = UUID.randomUUID().toString();
			}

			/*
			 * Value
			 */
			Object value;

			if (isAvroDeepCopyEnabled) {
				AvroData avroData = new AvroData(1);
				final GenericData.Record avro = (GenericData.Record) avroData.fromConnectData(valueSchema, valueStruct);
				if (isDebugEnabled) {
					log("*******avro class=" + avro.getClass().getName());
					log("*******avro=" + avro);
				}

				GenericData.Record after = (GenericData.Record) avro.get("after");

				if (isDebugEnabled) {
					if (after == null) {
						log("after=null");
					} else {
						log("afterClass=" + after.getClass().getName());
						log("after=" + after);
					}
				}

				value = SpecificData.get().deepCopy(avroSchema, after);
			
				if (key == null) {
					Object keyFieldValues[] = new Object[keyColumnNames.length];
					for (int j = 0; j < keyColumnNames.length; j++) {
						keyFieldValues[j] = after.get(keyColumnNames[j]);
					}
					try {
						key = objConverter.createKeyObject(keyFieldValues);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | ParseException e) {
						throw new RuntimeException(e);
					}
				}

			} else {

				Struct afterStruct = null;
				if (isSmtEnabled) {
					afterStruct = valueStruct;
				} else if (valueStruct != null) {
					afterStruct = (Struct) valueStruct.get("after");
				}
				if (isDebugEnabled) {
					log("op=" + op);
					log("isDelete=" + isDelete);
					log("afterStruct=" + afterStruct);
				}

				// Determine the value column names.
				if (valueColumnNames == null) {
					valueColumnNames = getColumnNames(valueStruct);
				}
				Object valueFieldValues[] = new Object[valueColumnNames.length];
				Class<?> valueFieldTypes[] = objConverter.getValueFielTypes();
				for (int j = 0; j < valueColumnNames.length; j++) {
					valueFieldValues[j] = afterStruct.get(valueColumnNames[j]);
				}
				try {
					value = objConverter.createValueObject(valueFieldValues);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | ParseException e) {
					throw new RuntimeException(e);
				}

				if (isDebugEnabled) {
					for (int j = 0; j < valueColumnNames.length; j++) {
						log(
								"valueColumnNames[" + j + "] = " + valueColumnNames[j] + ": " + valueFieldValues[j]);
					}
				}
				
				if (key == null) {
					Object keyFieldValues[] = new Object[keyColumnNames.length];
					for (int j = 0; j < keyColumnNames.length; j++) {
						keyFieldValues[j] = afterStruct.get(keyColumnNames[j]);
					}
					try {
						key = objConverter.createKeyObject(keyFieldValues);
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
					key = keyStr;
				}
			}

			if (isDebugEnabled) {
				log("**** key class=" + key.getClass().getName());
				log("**** key=" + key);
			}

			if (isDeleteEnabled && isDelete) {
				if (isGeodeEnabled) {
					// Invoke delete() to avoid returning previous value
					region.destroy(key);
				}
				continue;
			}
			
			if (isDebugEnabled) {
				log("**** value class=" + value.getClass().getName());
				log("**** value=" + value);
			}

			keyValueMap.put(key, value);
			count++;
			if (count % 100 == 0) {
				if (isGeodeEnabled) {
					region.putAll(keyValueMap);
				}
				keyValueMap.clear();
			}
		}
		if (count % 100 > 0) {
			if (isGeodeEnabled) {
				region.putAll(keyValueMap);
			}
			keyValueMap.clear();
		}
	}

	/**
	 * Converts the specified GenericData.Record entry in object form and puts it in
	 * Geode/GEmFire if it is enabled.
	 * 
	 * @param entry Key/value entry of GenericData records.
	 * @return Converted key/value objects
	 */
	public Map.Entry<K, V> putEntry(Map.Entry<GenericData.Record, GenericData.Record> entry) {
		GenericData.Record keyRecord = (GenericData.Record) entry.getKey();
		GenericData.Record valueRecord = (GenericData.Record) entry.getValue();

//		if (isDebugEnabled) {
//			log("keySchema=" + keyAvroSchema);
//			log("valueSchema=" + valueAvroSchema);
//			if (keyAvroSchema != null) {
//				log("keyFields=" + keyAvroSchema.getFields());
//			}
//			if (valueAvroSchema != null) {
//				log("valueAvroFields=" + valueAvroSchema.getFields());
//			}
//		}

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
					log("valueColumnNames[" + j + "] = " + valueColumnNames[j] + ": " + valueFieldValues[j]);
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
			log("**** key class=" + key.getClass().getName());
			log("**** key=" + key);
		}

		if (isGeodeEnabled) {
			if (isDeleteEnabled && isDelete) {
				region.remove(key);
			}
		} else {
			region.put(key, value);

		}

		if (isDebugEnabled) {
			log("**** value class=" + value.getClass().getName());
			log("**** value=" + value);
		}

		return new GemfireEntryImpl(key, value);
	}
	
	public String getRegionPath()
	{
		return regionPath;
	}
	
	public void close() {
		if (clientCache != null && clientCache.isClosed() == false) {
			clientCache.close();
			log("DebeziumKafkaAvroSinkTask.stop() invoked. clientCache closed.");
		} else {
			log("DebeziumKafkaAvroSinkTask.stop() invoked.");
		}
	}

}
