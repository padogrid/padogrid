package org.apache.geode.addon.kafka.debezium;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netcrest.pado.internal.util.ObjectConverter;

/**
 * DebeziumKafkaSinkTask is a Kafka sink connector for receiving Debezium change
 * events. Use it for demo only until further notice.
 * <p>
 * <b>Known Issues:</b>
 * <p>
 * The {@link SinkRecord} argument of the {@link #put(Collection)} method
 * includes only the key record and does not include delete event information
 * needed to properly delete the entries in Geode/GemFire. Without the "before"
 * Struct data, we are left to construct the Geode/GemFire key object solely based
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
public class DebeziumKafkaSinkTask extends SinkTask {

	private static final Logger logger = LoggerFactory.getLogger(DebeziumKafkaSinkTask.class);
	
	/**
	 * Source time factor. Date long values are divided by this number. 
	 */
	private static int TIME_FACTOR = Integer.getInteger("padogrid.data.time.factor", 1);

	private ClientCache clientCache;
	private boolean isDebugEnabled = false;

	private String gemfirePropertyFile;
	private String gemfireClientFile;
	private String regionPath;
	private Region regon;
	private boolean isSmtEnabled = true;
	private boolean isDeleteEnabled = true;
	private String keyClassName;
	private String valueClassName;
	private String[] keyColumnNames;
	private String[] keyFieldNames;
	private String[] valueColumnNames;
	private String[] valueFieldNames;
	private ObjectConverter objConverter;
	private boolean isDelete = true;

	@Override
	public String version() {
		return new DebeziumKafkaSinkConnector().version();
	}

	@Override
	public void start(Map<String, String> props) {

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
		String isDebugStr = props.get(DebeziumKafkaSinkConnector.DEBUG_ENABLED);
		isDebugEnabled = isDebugStr != null && isDebugStr.equalsIgnoreCase("true") ? true : isDebugEnabled;
		String isSmtStr = props.get(DebeziumKafkaSinkConnector.SMT_ENABLED);
		isSmtEnabled = isSmtStr != null && isSmtStr.equalsIgnoreCase("false") ? false : isSmtEnabled;
		String isDeleteStr = props.get(DebeziumKafkaSinkConnector.DELETE_ENABLED);
		isDeleteEnabled = isDeleteStr != null && isDeleteStr.equalsIgnoreCase("false") ? false : isDeleteEnabled;
		keyClassName = props.get(DebeziumKafkaSinkConnector.KEY_CLASS_NAME_CONFIG);
		valueClassName = props.get(DebeziumKafkaSinkConnector.VALUE_CLASS_NAME_CONFIG);

		// Key
		String cnames = props.get(DebeziumKafkaSinkConnector.KEY_COLUMN_NAMES_CONFIG);
		String fnames = props.get(DebeziumKafkaSinkConnector.KEY_FIELD_NAMES_CONFIG);
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
		cnames = props.get(DebeziumKafkaSinkConnector.VALUE_COLUMN_NAMES_CONFIG);
		fnames = props.get(DebeziumKafkaSinkConnector.VALUE_FIELD_NAMES_CONFIG);
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
			System.out.println("====================================================================================");
			String classpathStr = System.getProperty("java.class.path");
			System.out.println("CLASSPATH=" + classpathStr);
			
			System.out.println(props);
			System.out.println("gemfirePropertyFile = " + gemfirePropertyFile);
			System.out.println("gemfireClientFile = " + gemfireClientFile);
			System.out.println("region = " + regionPath);
			System.out.println("smtEnabled = " + isSmtEnabled);
			System.out.println("deleteEnabled = " + isDeleteEnabled);
			System.out.println("keyClassName = " + keyClassName);
			System.out.println("keyColumnNames");
			for (int i = 0; i < keyColumnNames.length; i++) {
				System.out.println("   [" + i + "] " + keyColumnNames[i]);
			}
			System.out.println("keyFieldNames");
			for (int i = 0; i < keyFieldNames.length; i++) {
				System.out.println("   [" + i + "] " + keyFieldNames[i]);
			}
			System.out.println("valueClassName = " + valueClassName);
			System.out.println("valueColumnNames");
			for (int i = 0; i < valueColumnNames.length; i++) {
				System.out.println("   [" + i + "] " + valueColumnNames[i]);
			}
			System.out.println("valueFieldNames");
			for (int i = 0; i < valueFieldNames.length; i++) {
				System.out.println("   [" + i + "] " + valueFieldNames[i]);
			}
			System.out.println("====================================================================================");
			System.out.flush();
		}
		try {
			objConverter = new ObjectConverter(keyClassName, keyFieldNames, valueClassName, valueFieldNames);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		System.setProperty(DebeziumKafkaSinkConnector.GEMFIRE_PROPERTY_FILE_CONFIG, gemfirePropertyFile);
		System.setProperty(DebeziumKafkaSinkConnector.GEMFIRE_CLIENT_CONFIG_FILE_CONFIG, gemfireClientFile);

		clientCache = new ClientCacheFactory().create();
		regon = clientCache.getRegion(regionPath);
	}
	
	private Object[] getFieldFromMap(Map keyMap) {
		// Determine the key column names.
		if (keyColumnNames == null) {
			keyColumnNames = (String[]) keyMap.keySet().toArray();
		}
		Object keyFieldValues[] = null;
		if (keyColumnNames != null) {
			keyFieldValues = new Object[keyColumnNames.length];
			for (int j = 0; j < keyColumnNames.length; j++) {
				keyFieldValues[j] = keyMap.get(keyColumnNames[j]);
			}
		}
		return keyFieldValues;
	}

	private Object[] getValueFieldsFromMap(Map valueMap) {
		// Determine the value column names.
		if (valueColumnNames == null) {
			valueColumnNames = (String[]) valueMap.keySet().toArray();
		}
		Object valueFieldValues[] = null;
		if (valueColumnNames != null) {
			valueFieldValues = new Object[valueColumnNames.length];
			Class<?> valueFieldTypes[] = objConverter.getValueFielTypes();
			for (int j = 0; j < valueColumnNames.length; j++) {
				valueFieldValues[j] = valueMap.get(valueColumnNames[j]);
				// TODO: This is a hack. Support other types also.
				if (valueFieldTypes[j] != null && valueFieldTypes[j] == Date.class) {
					if (valueFieldValues[j] instanceof Number) {
						valueFieldValues[j] = new Date((long) valueFieldValues[j] / TIME_FACTOR);
					}
				}
			}
		}
		return valueFieldValues;
	}

	@Override
	public void put(Collection<SinkRecord> records) {
//		final Serde<String> serde = DebeziumSerdes.payloadJson(String.class);

		HashMap keyValueMap = new HashMap();
		int count = 0;
		for (SinkRecord sinkRecord : records) {
			Schema keySchema = sinkRecord.keySchema();
			Schema valueSchema = sinkRecord.valueSchema();

			if (isDebugEnabled) {
				System.out.println("sinkRecord=" + sinkRecord);
				System.out.println("keySchema=" + keySchema);
				System.out.println("valueSchema=" + valueSchema);
				if (keySchema != null) {
					System.out.println("keyFields=" + keySchema.fields());
				}
				if (valueSchema != null) {
					System.out.println("valueFields=" + valueSchema.fields());
				}
			}

			// Struct objects expected
			System.out.println("*************************key:" + sinkRecord.key().getClass().toString());
			System.out.println("*************************value:" + sinkRecord.value().getClass().toString());
			System.out.println("sinkeRecord.key()=" + sinkRecord.key().toString());
			System.out.println("sinkRecord.value()=" + sinkRecord.value().toString());

			Object keyFieldValues[] = null;
			Object valueFieldValues[] = null;
			boolean isDelete;
			if (sinkRecord.key() instanceof Map) {
				isDelete = sinkRecord.value() == null;
				if (isDelete) {
					keyFieldValues = getFieldFromMap((Map) sinkRecord.key());
				} else {
					keyFieldValues = getFieldFromMap((Map) sinkRecord.value());
				}
				if (sinkRecord.value() != null) {
					valueFieldValues = getValueFieldsFromMap((Map) sinkRecord.value());
				}
			} else {
				Struct keyStruct = (Struct) sinkRecord.key();
				Struct valueStruct = (Struct) sinkRecord.value();

				isDelete = valueStruct == null;
				Struct afterStruct = null;
				Object op = null;
				if (isSmtEnabled) {
					afterStruct = valueStruct;
				} else if (valueStruct != null) {
					op = valueStruct.get("op");
					isDelete = op != null && op.toString().equals("d");
					afterStruct = (Struct) valueStruct.get("after");
				}
				if (isDebugEnabled) {
					System.out.println("op=" + op);
					System.out.println("isDelete = " + isDelete);
					System.out.println("keyStruct = " + keyStruct);
					System.out.println("afterStruct = " + afterStruct);
					System.out.flush();
				}

				// Key
				// Determine the key column names.
				if (keyColumnNames == null) {
					keyColumnNames = getColumnNames(keyStruct);
				}
				if (keyColumnNames != null) {
					keyFieldValues = new Object[keyColumnNames.length];
					if (isDelete) {
						for (int j = 0; j < keyColumnNames.length; j++) {
							keyFieldValues[j] = keyStruct.get(keyColumnNames[j]);
						}
					} else {
						// If not delete (in that case, only the keys are sent), then
						// get key field values from the value, i.e., afterStruct.
						// Note deletes may not be possible if non-primary keys are used
						// to construct the Geode/GemFire keys since there are no value
						// payloads.
						for (int j = 0; j < keyColumnNames.length; j++) {
							keyFieldValues[j] = afterStruct.get(keyColumnNames[j]);
						}
					}
				}

				// Value
				// Determine the value column names.
				if (valueColumnNames == null) {
					valueColumnNames = getColumnNames(valueStruct);
				}
				if (valueColumnNames != null) {
					valueFieldValues = new Object[valueColumnNames.length];
					Class<?> valueFieldTypes[] = objConverter.getValueFielTypes();
					for (int j = 0; j < valueColumnNames.length; j++) {
						valueFieldValues[j] = afterStruct.get(valueColumnNames[j]);
						// TODO: This is a hack. Support other types also.
						if (valueFieldTypes[j] != null && valueFieldTypes[j] == Date.class) {
							if (valueFieldValues[j] instanceof Number) {
								valueFieldValues[j] = new Date((long) valueFieldValues[j] / TIME_FACTOR);
							}
						}
					}
				}
			}

			// Key
			Object key = null;
			// If the key column names are not defined or cannot be determined then
			// assign UUID for the key value
			if (keyColumnNames == null) {
				key = UUID.randomUUID().toString();
			} else {
				try {
					key = objConverter.createKeyObject(keyFieldValues);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | ParseException e) {
					throw new RuntimeException(e);
				}
			}
			if (isDebugEnabled) {
				System.out.println("key = " + key);
			}
			if (isDeleteEnabled && isDelete) {
				regon.destroy(key);
				continue;
			}

			// Value
			Object value = null;
			try {
				value = objConverter.createValueObject(valueFieldValues);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | ParseException e) {
				throw new RuntimeException(e);
			}
			if (isDebugEnabled) {
				for (int j = 0; j < valueColumnNames.length; j++) {
					System.out.println(
							"valueColumnNames[" + j + "] = " + valueColumnNames[j] + ": " + valueFieldValues[j]);
				}
				System.out.println("value = " + value);
			}

			keyValueMap.put(key, value);
			count++;
			if (count % 100 == 0) {
				regon.putAll(keyValueMap);
				keyValueMap.clear();
			}
		}
		if (count % 100 > 0) {
			regon.putAll(keyValueMap);
			keyValueMap.clear();
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

	@Override
	public void flush(Map<TopicPartition, OffsetAndMetadata> offsets) {
		logger.trace("Flushing map for {}", logRegionPath());
	}

	@Override
	public void stop() {
		if (clientCache != null && clientCache.isClosed() == false) {
			clientCache.close();
			logger.info("DebeziumKafkaAvroSinkTask.stop() invoked. clientCache closed.");
		} else {
			logger.info("DebeziumKafkaAvroSinkTask.stop() invoked.");
		}
	}

	private String logRegionPath() {
		return regionPath == null ? "stdout" : regionPath;
	}
}
