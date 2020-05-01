package org.apache.geode.addon.kafka.debezium;

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
 * DebeziumKafkaSinkConnector registers the Geode/GemFire connector for Kafka.
 * 
 * @author dpark
 *
 */
public class DebeziumKafkaSinkConnector extends SinkConnector {

	public static final String REGION_CONFIG = "region";
	public static final String DEBUG_ENABLED = "debug.enabled";
	public static final String SMT_ENABLED = "smt.enabled";
	public static final String DELETE_ENABLED = "delete.enabled";
	public static final String KEY_CLASS_NAME_CONFIG = "key.class";
	public static final String KEY_COLUMN_NAMES_CONFIG = "key.column.names";
	public static final String KEY_FIELD_NAMES_CONFIG = "key.field.names";
	public static final String VALUE_CLASS_NAME_CONFIG = "value.class";
	public static final String VALUE_COLUMN_NAMES_CONFIG = "value.column.names";
	public static final String VALUE_FIELD_NAMES_CONFIG = "value.field.names";
	public static final String GEMFIRE_PROPERTY_FILE_CONFIG = "gemfirePropertyFile";
	public static final String GEMFIRE_CLIENT_CONFIG_FILE_CONFIG = "gemfire.cache-xml-file";

	private static final ConfigDef CONFIG_DEF = new ConfigDef()
			.define(GEMFIRE_PROPERTY_FILE_CONFIG, Type.STRING, "/geode-addon/etc/client-gemfire.properties",
					Importance.MEDIUM, "Geode/GemFire client properties file path.")
			.define(GEMFIRE_CLIENT_CONFIG_FILE_CONFIG, Type.STRING, "/geode-addon/etc/client-cache.xml",
					Importance.MEDIUM, "Geode/GemFire client xml configuration file path.")
			.define(REGION_CONFIG, Type.STRING, "myregion", Importance.HIGH,
					"Destination region path. If not specified, then 'myregion' is assigned.")
			.define(DEBUG_ENABLED, Type.BOOLEAN, false, Importance.LOW,
					"Debug flag. If true, then debug information is printed.")
			.define(SMT_ENABLED, Type.BOOLEAN, true, Importance.HIGH,
					"Single Message Transform flag. If true, then SMT messages are expected.")
			.define(DELETE_ENABLED, Type.BOOLEAN, true, Importance.HIGH,
					"Single Message Transform flag. If true, then SMT messages are expected.")
			.define(KEY_CLASS_NAME_CONFIG, Type.STRING, null, Importance.HIGH,
					"Key class name. Name of serializable class for transforming table key columns to key objects into Geode/GemFire. "
							+ "If not specified but the key column names are specified, then the column values are concatenated with the delimiter '.'. "
							+ "If the key column names are also not specified, then the Kafka key is used.")
			.define(KEY_COLUMN_NAMES_CONFIG, Type.STRING, null, Importance.HIGH,
					"Comma separated key column names. An ordered list of table column names to be mapped to the key object field (setter) names.")
			.define(KEY_FIELD_NAMES_CONFIG, Type.STRING, null, Importance.HIGH,
					"Comma separated key object field (setter) names. An ordered list of key object field (setter) names to be mapped to the table column names.")
			.define(VALUE_CLASS_NAME_CONFIG, Type.STRING, null, Importance.HIGH,
					"Value class name. Name of serializable class for transforming table rows to value objects into Geode/GemFire. "
							+ "If not specified, then table rows are transformed to JSON objects.")
			.define(VALUE_COLUMN_NAMES_CONFIG, Type.STRING, null, Importance.HIGH,
					"Comma separated value column names. An ordered list of table column names to be mapped to the value object field (setter) names.")
			.define(VALUE_FIELD_NAMES_CONFIG, Type.STRING, null, Importance.HIGH,
					"Comma separated value object field (setter) names. An ordered list of value object field (setter) names to be mapped to the table column names.");

	private static final Logger logger = LoggerFactory.getLogger(DebeziumKafkaSinkConnector.class);

	private String gemfireClientConfigFile;
	private String regionPath;
	private boolean isDebugEnabled = false;
	private boolean isSmtEnabled = true;
	private boolean isDeleteEnabled = true;
	private String keyClassName;
	private String keyColumnNames;
	private String keyFieldNames;
	private String valueClassName;
	private String valueColumnNames;
	private String valueFieldNames;

	@Override
	public String version() {
		return AppInfoParser.getVersion();
	}

	@Override
	public void start(Map<String, String> props) {
		AbstractConfig parsedConfig = new AbstractConfig(CONFIG_DEF, props);
		gemfireClientConfigFile = parsedConfig.getString(GEMFIRE_CLIENT_CONFIG_FILE_CONFIG);
		regionPath = parsedConfig.getString(REGION_CONFIG);
		String isDebugStr = props.get(DEBUG_ENABLED);
		isDebugEnabled = isDebugStr != null && isDebugStr.equalsIgnoreCase("true") ? true : isDebugEnabled;
		String isSmtStr = props.get(SMT_ENABLED);
		isSmtEnabled = isSmtStr != null && isSmtStr.equalsIgnoreCase("false") ? false : isSmtEnabled;
		String isDeleteStr = props.get(DELETE_ENABLED);
		isDeleteEnabled = isDeleteStr != null && isDeleteStr.equalsIgnoreCase("false") ? false : isDeleteEnabled;
		keyClassName = props.get(KEY_CLASS_NAME_CONFIG);
		keyColumnNames = props.get(KEY_COLUMN_NAMES_CONFIG);
		keyFieldNames = props.get(KEY_FIELD_NAMES_CONFIG);
		valueClassName = props.get(VALUE_CLASS_NAME_CONFIG);
		valueColumnNames = props.get(VALUE_COLUMN_NAMES_CONFIG);
		valueFieldNames = props.get(VALUE_FIELD_NAMES_CONFIG);

		logger.info("====================================================================================");
		try {
			Class<?> valueClass = Class.forName(valueClassName);
			Object valueObj = valueClass.newInstance();
			logger.info("valueObj = " + valueObj);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		logger.info(props.toString());
		logger.info("regionPath = " + regionPath);
		logger.info("isSmtEnabled = " + isSmtEnabled);
		logger.info("isDeleteEnabled = " + isDeleteEnabled);
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
			if (gemfireClientConfigFile != null) {
				config.put(GEMFIRE_CLIENT_CONFIG_FILE_CONFIG, gemfireClientConfigFile);
			}
			if (regionPath != null) {
				config.put(REGION_CONFIG, regionPath);
			}
			config.put(DEBUG_ENABLED, Boolean.toString(isDebugEnabled));
			config.put(SMT_ENABLED, Boolean.toString(isSmtEnabled));
			config.put(DELETE_ENABLED, Boolean.toString(isDeleteEnabled));
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
