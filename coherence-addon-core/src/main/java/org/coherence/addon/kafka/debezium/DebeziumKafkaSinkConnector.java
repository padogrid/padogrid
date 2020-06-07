package org.coherence.addon.kafka.debezium;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DebeziumKafkaSinkConnector registers the Coherence connector for Kafka.
 * 
 * @author dpark
 *
 */
public class DebeziumKafkaSinkConnector extends SinkConnector {

	public static final int DEFAULT_QUEUE_BATCH_SIZE = 100;
	public static final long DEFAULT_QUEUE_BATCH_INTERVAL_IN_MSEC = 500;
	
	public static final String CONFIG_CACHE = "cache";
	public static final String CONFIG_DEBUG_ENABLED = "debug.enabled";
	public static final String CONFIG_SMT_ENABLED = "smt.enabled";
	public static final String CONFIG_DELETE_ENABLED = "delete.enabled";
	public static final String CONFIG_KEY_CLASS_NAME = "key.class";
	public static final String CONFIG_KEY_COLUMN_NAMES = "key.column.names";
	public static final String CONFIG_KEY_FIELD_NAMES = "key.field.names";
	public static final String CONFIG_VALUE_CLASS_NAME = "value.class";
	public static final String CONFIG_VALUE_COLUMN_NAMES = "value.column.names";
	public static final String CONFIG_VALUE_FIELD_NAMES = "value.field.names";
	public static final String CONFIG_COHERENCE_CLIENT_CONFIG_FILE = "coherence.client-config";
	public static final String CONFIG_QUEUE_BATCH_SIZE = "queue.batch.size";
	public static final String CONFIG_QUEUE_BATCH_INTERVAL_IN_MSEC = "queue.batch.intervalInMsec";

	private static final ConfigDef CONFIG_DEF = new ConfigDef()
			.define(CONFIG_COHERENCE_CLIENT_CONFIG_FILE, Type.STRING, "/coherence-addon/etc/client-config.xml",
					Importance.MEDIUM, "Coherence client xml configuration file path.")
			.define(CONFIG_CACHE, Type.STRING, "mycache", Importance.HIGH,
					"Destination cache path. If not specified, then 'mycache' is assigned.")
			.define(CONFIG_DEBUG_ENABLED, Type.BOOLEAN, false, Importance.LOW,
					"Debug flag. If true, then debug information is printed.")
			.define(CONFIG_SMT_ENABLED, Type.BOOLEAN, true, Importance.HIGH,
					"Single Message Transform flag. If true, then SMT messages are expected.")
			.define(CONFIG_DELETE_ENABLED, Type.BOOLEAN, true, Importance.HIGH,
					"Single Message Transform flag. If true, then SMT messages are expected.")
			.define(CONFIG_KEY_CLASS_NAME, Type.STRING, null, Importance.HIGH,
					"Key class name. Name of serializable class for transforming table key columns to key objects into Coherence. "
							+ "If not specified but the key column names are specified, then the column values are concatenated with the delimiter '.'. "
							+ "If the key column names are also not specified, then the Kafka key is used.")
			.define(CONFIG_KEY_COLUMN_NAMES, Type.STRING, null, Importance.HIGH,
					"Comma separated key column names. An ordered list of table column names to be mapped to the key object field (setter) names.")
			.define(CONFIG_KEY_FIELD_NAMES, Type.STRING, null, Importance.HIGH,
					"Comma separated key object field (setter) names. An ordered list of key object field (setter) names to be mapped to the table column names.")
			.define(CONFIG_VALUE_CLASS_NAME, Type.STRING, null, Importance.HIGH,
					"Value class name. Name of serializable class for transforming table rows to value objects into Coherence. "
							+ "If not specified, then table rows are transformed to JSON objects.")
			.define(CONFIG_VALUE_COLUMN_NAMES, Type.STRING, null, Importance.HIGH,
					"Comma separated value column names. An ordered list of table column names to be mapped to the value object field (setter) names.")
			.define(CONFIG_VALUE_FIELD_NAMES, Type.STRING, null, Importance.HIGH,
					"Comma separated value object field (setter) names. An ordered list of value object field (setter) names to be mapped to the table column names.")
			.define(CONFIG_QUEUE_BATCH_SIZE, Type.INT, DEFAULT_QUEUE_BATCH_SIZE, Importance.MEDIUM,
					"Kafka sink consumer thread queue batch size.")
			.define(CONFIG_QUEUE_BATCH_INTERVAL_IN_MSEC, Type.LONG, DEFAULT_QUEUE_BATCH_INTERVAL_IN_MSEC,
					Importance.MEDIUM, "Kafka sink consumer thread queue batch interval in msec.");

	private static final Logger logger = LoggerFactory.getLogger(DebeziumKafkaSinkConnector.class);

	private String coherenceClientConfigFile;
	private String cacheName;
	private boolean isDebugEnabled = false;
	private boolean isSmtEnabled = true;
	private boolean isDeleteEnabled = true;
	private String keyClassName;
	private String keyColumnNames;
	private String keyFieldNames;
	private String valueClassName;
	private String valueColumnNames;
	private String valueFieldNames;
	private int queueBatchSize = DEFAULT_QUEUE_BATCH_SIZE;
	private long queueBatchIntervalInMsec = DEFAULT_QUEUE_BATCH_INTERVAL_IN_MSEC;

	@Override
	public String version() {
		return AppInfoParser.getVersion();
	}

	@Override
	public void start(Map<String, String> props) {
		AbstractConfig parsedConfig = new AbstractConfig(CONFIG_DEF, props);
		coherenceClientConfigFile = parsedConfig.getString(CONFIG_COHERENCE_CLIENT_CONFIG_FILE);
		cacheName = parsedConfig.getString(CONFIG_CACHE);
		String isDebugStr = props.get(CONFIG_DEBUG_ENABLED);
		isDebugEnabled = isDebugStr != null && isDebugStr.equalsIgnoreCase("true") ? true : isDebugEnabled;
		String isSmtStr = props.get(CONFIG_SMT_ENABLED);
		isSmtEnabled = isSmtStr != null && isSmtStr.equalsIgnoreCase("false") ? false : isSmtEnabled;
		String isDeleteStr = props.get(CONFIG_DELETE_ENABLED);
		isDeleteEnabled = isDeleteStr != null && isDeleteStr.equalsIgnoreCase("false") ? false : isDeleteEnabled;
		keyClassName = props.get(CONFIG_KEY_CLASS_NAME);
		keyColumnNames = props.get(CONFIG_KEY_COLUMN_NAMES);
		keyFieldNames = props.get(CONFIG_KEY_FIELD_NAMES);
		valueClassName = props.get(CONFIG_VALUE_CLASS_NAME);
		valueColumnNames = props.get(CONFIG_VALUE_COLUMN_NAMES);
		valueFieldNames = props.get(CONFIG_VALUE_FIELD_NAMES);
		String intVal = props.get(CONFIG_QUEUE_BATCH_SIZE);
		if (intVal != null) {
			try {
				queueBatchSize = Integer.parseInt(intVal);
			} catch (Exception ex) {
				// ignore. use the default value.
			}
		}
		String longVal = props.get(CONFIG_QUEUE_BATCH_INTERVAL_IN_MSEC);
		if (longVal != null) {
			try {
				queueBatchIntervalInMsec = Long.parseLong(longVal);
			} catch (Exception ex) {
				// ignore. use the default value.
			}
		}

		logger.info("====================================================================================");
		try {
			Class<?> valueClass = Class.forName(valueClassName);
			Object valueObj = valueClass.newInstance();
			logger.info("valueObj = " + valueObj);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		logger.info(props.toString());
		logger.info(CONFIG_COHERENCE_CLIENT_CONFIG_FILE + " = " + coherenceClientConfigFile);
		logger.info(CONFIG_CACHE + " = " + cacheName);
		logger.info(CONFIG_DEBUG_ENABLED + " = " + isDebugEnabled);
		logger.info(CONFIG_SMT_ENABLED + " = " + isSmtEnabled);
		logger.info(CONFIG_DELETE_ENABLED + " = " + isDeleteEnabled);
		logger.info(CONFIG_KEY_CLASS_NAME + " = " + keyClassName);
		logger.info(CONFIG_KEY_COLUMN_NAMES + " = " + keyColumnNames);
		logger.info(CONFIG_KEY_FIELD_NAMES + " = " + keyFieldNames);
		logger.info(CONFIG_VALUE_CLASS_NAME + " = " + valueClassName);
		logger.info(CONFIG_VALUE_COLUMN_NAMES + " = " + valueColumnNames);
		logger.info(CONFIG_VALUE_FIELD_NAMES + " = " + valueFieldNames);
		logger.info(CONFIG_QUEUE_BATCH_SIZE + " = " + queueBatchSize);
		logger.info(CONFIG_QUEUE_BATCH_INTERVAL_IN_MSEC + " = " + queueBatchIntervalInMsec);
		logger.info("====================================================================================");
	}

	@Override
	public Class<? extends Task> taskClass() {
		return DebeziumKafkaSinkTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		ArrayList<Map<String, String>> configs = new ArrayList<>();
		for (int i = 0; i < maxTasks; i++) {
			Map<String, String> config = new HashMap<>();
			if (coherenceClientConfigFile != null) {
				config.put(CONFIG_COHERENCE_CLIENT_CONFIG_FILE, coherenceClientConfigFile);
			}
			if (cacheName != null) {
				config.put(CONFIG_CACHE, cacheName);
			}
			config.put(CONFIG_DEBUG_ENABLED, Boolean.toString(isDebugEnabled));
			config.put(CONFIG_SMT_ENABLED, Boolean.toString(isSmtEnabled));
			config.put(CONFIG_DELETE_ENABLED, Boolean.toString(isDeleteEnabled));
			if (keyClassName != null) {
				config.put(CONFIG_KEY_CLASS_NAME, keyClassName);
			}
			if (keyColumnNames != null) {
				config.put(CONFIG_KEY_COLUMN_NAMES, keyColumnNames);
			}
			if (keyFieldNames != null) {
				config.put(CONFIG_KEY_FIELD_NAMES, keyFieldNames);
			}
			if (valueClassName != null) {
				config.put(CONFIG_VALUE_CLASS_NAME, valueClassName);
			}
			if (valueColumnNames != null) {
				config.put(CONFIG_VALUE_COLUMN_NAMES, valueColumnNames);
			}
			if (valueFieldNames != null) {
				config.put(CONFIG_VALUE_FIELD_NAMES, valueFieldNames);
			}
			config.put(CONFIG_QUEUE_BATCH_SIZE, String.valueOf(queueBatchSize));
			config.put(CONFIG_QUEUE_BATCH_INTERVAL_IN_MSEC, String.valueOf(queueBatchIntervalInMsec));
			configs.add(config);
		}
		return configs;
	}

	@Override
	public void stop() {

	}

	@Override
	public ConfigDef config() {
		return CONFIG_DEF;
	}

}
