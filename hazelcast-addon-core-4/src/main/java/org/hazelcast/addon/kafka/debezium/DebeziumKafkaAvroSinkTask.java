package org.hazelcast.addon.kafka.debezium;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.avro.generic.GenericData;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.specific.SpecificData;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyAccessorFactory;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import io.confluent.connect.avro.AvroData;

/**
 * DebeziumKafkaAvroSinkTask is a Kafka sink connector for receiving Debezium
 * change events. Use it for demo only until further notice.
 * <p>
 * <b>Known Issues:</b>
 * <p>
 * The {@link SinkRecord} argument of the {@link #put(Collection)} method
 * includes only the key record and does not include delete event information
 * needed to properly delete the entries in Hazelcast. Without the "before"
 * Struct data, we are left to construct the Hazelcast key object solely based
 * on the key record. For tables with the primary key, this should be sufficient
 * since the key record holds the the primary key. For those tables without the
 * primary key, however, the "before" Struct data is needed in order to
 * construct the key object, which is typically comprised of a combination of
 * the table column values and a unique value such as timestamp introduced by
 * the application.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DebeziumKafkaAvroSinkTask extends SinkTask {

	private static final Logger logger = LoggerFactory.getLogger(DebeziumKafkaAvroSinkTask.class);

	private static final int MICRO_IN_MILLI = 1000;

	private boolean isDebugEnabled = false;

	private HazelcastInstance hzInstance;
	private String mapName;
	private IMap map;
	private boolean isSmtEnabled = true;
	private boolean isDeleteEnabled = true;
	private boolean isHazelcastEnabled = true;
	private boolean isAvroDeepCopyEnabled = false;
	private String keyClassName;
	private String valueClassName;
	private String[] keyColumnNames;
	private String[] keyFieldNames;
	private String[] valueColumnNames;
	private String[] valueFieldNames;
	private ObjectConverter objConverter;
	private org.apache.avro.Schema avroSchema;

	@Override
	public String version() {
		return new DebeziumKafkaAvroSinkConnector().version();
	}

	@Override
	public void start(Map<String, String> props) {

		String hazelcastConfigFile = props.get(DebeziumKafkaAvroSinkConnector.HAZELCAST_CLIENT_CONFIG_FILE_CONFIG);
		if (hazelcastConfigFile == null) {
			hazelcastConfigFile = "/hazelcast-addon/etc/hazelcast-client.xml";
		}
		mapName = props.get(DebeziumKafkaAvroSinkConnector.MAP_CONFIG);
		if (mapName == null) {
			mapName = "map";
		}
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
		keyClassName = props.get(DebeziumKafkaAvroSinkConnector.KEY_CLASS_NAME_CONFIG);
		valueClassName = props.get(DebeziumKafkaAvroSinkConnector.VALUE_CLASS_NAME_CONFIG);

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

		if (isDebugEnabled) {
			logger.info("====================================================================================");
			String classpathStr = System.getProperty("java.class.path");
			System.out.print("classpath=" + classpathStr);

			logger.info(props.toString());
			logger.info("mapName = " + mapName);
			logger.info("smtEnabled = " + isSmtEnabled);
			logger.info("deleteEnabled = " + isDeleteEnabled);
			logger.info("keyClassName = " + keyClassName);
			logger.info("keyColumnNames");
			for (int i = 0; i < keyColumnNames.length; i++) {
				logger.info("   [" + i + "] " + keyColumnNames[i]);
			}
			logger.info("keyFieldNames");
			for (int i = 0; i < keyFieldNames.length; i++) {
				logger.info("   [" + i + "] " + keyFieldNames[i]);
			}
			logger.info("valueClassName = " + valueClassName);
			logger.info("valueColumnNames");
			for (int i = 0; i < valueColumnNames.length; i++) {
				logger.info("   [" + i + "] " + valueColumnNames[i]);
			}
			logger.info("valueFieldNames");
			for (int i = 0; i < valueFieldNames.length; i++) {
				logger.info("   [" + i + "] " + valueFieldNames[i]);
			}
			logger.info("====================================================================================");
		}
		try {
			objConverter = new ObjectConverter(keyClassName, keyFieldNames, valueClassName, valueFieldNames);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		Class<?> clazz;
		try {
			clazz = Class.forName(valueClassName);
			java.lang.reflect.Field field = (java.lang.reflect.Field) clazz.getField("SCHEMA$");
			avroSchema = (org.apache.avro.Schema) field.get(null);

			// Create a new schema in case the specified class is a wrapper class.
			JSONObject jo = new JSONObject(avroSchema.toString());
			jo.put("name", clazz.getSimpleName());
			jo.put("namespace", clazz.getPackage().getName());
			avroSchema = new org.apache.avro.Schema.Parser().parse(jo.toString());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (isHazelcastEnabled) {
			System.setProperty("hazelcast.client.config", hazelcastConfigFile);
			HazelcastInstance hzInstance = HazelcastClient.newHazelcastClient();
			map = hzInstance.getMap(mapName);
		}
	}

	@Override
	public void put(Collection<SinkRecord> records) {
		HashMap keyValueMap = new HashMap();
		int count = 0;
		for (SinkRecord sinkRecord : records) {

			Schema keySchema = sinkRecord.keySchema();
			Schema valueSchema = sinkRecord.valueSchema();

			if (isDebugEnabled) {
				logger.info("sinkRecord=" + sinkRecord);
				logger.info("keySchema=" + keySchema);
				logger.info("valueSchema=" + valueSchema);
				logger.info("keyFields=" + keySchema.fields());
				if (valueSchema == null) {
					logger.info("valueSchema=null");
				} else {
					logger.info("valueFields=" + valueSchema.fields());
				}
			}

			// Struct objects expected
			Object keyObj = sinkRecord.key();
			Object valueObj = sinkRecord.value();

			if (isDebugEnabled) {
				logger.info("keyObjType = " + keyObj.getClass().getName());
				logger.info("valueObj = " + valueObj.getClass().getName());
				logger.info("keyObjType=" + keyObj.getClass().getName());
				logger.info(keyObj.toString());
				logger.info("valueObj=" + valueObj.getClass().getName());
				logger.info(valueObj.toString());
				logger.info("keyObj=" + keyObj);
				logger.info("valueObj=" + valueObj);
			}

			Struct keyStruct = (Struct) sinkRecord.key();
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
			Object key;

			// Determine the key column names.
			if (keyColumnNames == null) {
				keyColumnNames = getColumnNames(keyStruct);
			}

			// If the key column names are not defined or cannot be determined then
			// assign UUID for the key value
			if (keyColumnNames == null) {
				key = UUID.randomUUID().toString();
			} else {
				Object keyFieldValues[] = new Object[keyColumnNames.length];
				for (int j = 0; j < keyColumnNames.length; j++) {
					keyFieldValues[j] = keyStruct.get(keyColumnNames[j]);
				}
				try {
					key = objConverter.createKeyObject(keyFieldValues);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | ParseException e) {
					throw new RuntimeException(e);
				}
			}
			if (isDebugEnabled) {
				logger.info("key=" + key);
			}
			
			/*
			 * Value
			 */
			Object value;

			if (isAvroDeepCopyEnabled) {
				AvroData avroData = new AvroData(1);
				final GenericData.Record avro = (GenericData.Record) avroData.fromConnectData(valueSchema, valueStruct);
				if (isDebugEnabled) {
					logger.info("*******avro class=" + avro.getClass().getName());
					logger.info("*******avro=" + avro);
				}

				GenericData.Record after = (GenericData.Record) avro.get("after");

				if (isDebugEnabled) {
					if (after == null) {
						logger.info("after=null");
					} else {
						logger.info("afterClass=" + after.getClass().getName());
						logger.info("after=" + after);
					}
				}

				value = SpecificData.get().deepCopy(avroSchema, after);
				if (isDebugEnabled) {
					logger.info("************value=" + value);
				}

			} else {

				
				Struct afterStruct = null;
				if (isSmtEnabled) {
					afterStruct = valueStruct;
				} else if (valueStruct != null) {
					afterStruct = (Struct) valueStruct.get("after");
				}
				if (isDebugEnabled) {
					logger.info("op=" + op);
					logger.info("isDelete=" + isDelete);
					logger.info("afterStruct=" + afterStruct);
					System.out.flush();
				}
				
				// Determine the value column names.
				if (valueColumnNames == null) {
					valueColumnNames = getColumnNames(valueStruct);
				}
				Object valueFieldValues[] = new Object[valueColumnNames.length];
				Class<?> valueFieldTypes[] = objConverter.getValueFielTypes();
				for (int j = 0; j < valueColumnNames.length; j++) {
					valueFieldValues[j] = afterStruct.get(valueColumnNames[j]);
					// TODO: This is a hack. Support other types also.
//				if (valueFieldTypes[j] != null && valueFieldTypes[j] == Date.class) {
//					if (valueFieldValues[j] instanceof Number) {
//						valueFieldValues[j] = new Date((long) valueFieldValues[j] / MICRO_IN_MILLI);
//					}
//				}
				}
				try {
					value = objConverter.createValueObject(valueFieldValues);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | ParseException e) {
					throw new RuntimeException(e);
				}

				if (isDebugEnabled) {
					for (int j = 0; j < valueColumnNames.length; j++) {
						logger.info(
								"valueColumnNames[" + j + "] = " + valueColumnNames[j] + ": " + valueFieldValues[j]);
					}
					logger.info("value=" + value);
				}
			}

			if (isDeleteEnabled && isDelete) {
				if (isHazelcastEnabled) {
					map.delete(key);
				}
				continue;
			}

			keyValueMap.put(key, value);
			count++;
			if (count % 100 == 0) {
				if (isHazelcastEnabled) {
					map.putAll(keyValueMap);
				}
				keyValueMap.clear();
			}
		}
		if (count % 100 > 0) {
			if (isHazelcastEnabled) {
				map.putAll(keyValueMap);
			}
			keyValueMap.clear();
		}
	}

	/**
	 * Spring object conversion - not used
	 * @param <T>
	 * @param record
	 * @param object
	 * @return
	 */
	private static <T> T mapRecordToObject(GenericData.Record record, T object) {
		final org.apache.avro.Schema schema = ReflectData.get().getSchema(object.getClass());
		record.getSchema().getFields()
				.forEach(d -> PropertyAccessorFactory.forDirectFieldAccess(object).setPropertyValue(d.name(),
						record.get(d.name()) == null ? record.get(d.name()) : record.get(d.name()).toString()));
		return object;
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

	@Override
	public void flush(Map<TopicPartition, OffsetAndMetadata> offsets) {
		if (map != null) {
			logger.trace("Flushing map for {}", logMapName());
			map.flush();
		}
	}

	@Override
	public void stop() {
		if (hzInstance != null) {
			hzInstance.shutdown();
		}
	}

	private String logMapName() {
		return mapName == null ? "stdout" : mapName;
	}
}
