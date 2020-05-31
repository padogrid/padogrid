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
 * DebeziumKafkaSinkConnector registers the SnappyData connector for Kafka.
 * 
 * @author dpark
 *
 */
public class DebeziumKafkaSinkConnector extends SinkConnector {

	public static final String DEFAULT_CONNECTION_URL = "jdbc:snappydata://localhost:1527/";
	public static final String DEFAULT_CONNECTION_DRIVER_CLASS = "io.snappydata.jdbc.ClientDriver";
	public static final String DEFAULT_USER = "app";
	public static final String DEFAULT_PASSWORD = "app";
	public static final String DEFAULT_TABLE = "mytable";
	public static final int DEFAULT_QUEUE_BATCH_SIZE = 100;
	public static final long DEFAULT_QUEUE_BATCH_INTERVAL_IN_MSEC = 500;

	public static final String CONFIG_TABLE = "table";
	public static final String CONFIG_DEBUG_ENABLED = "debug.enabled";
	public static final String CONFIG_SMT_ENABLED = "smt.enabled";
	public static final String CONFIG_DELETE_ENABLED = "delete.enabled";
	public static final String CONFIG_CONNECTION_URL = "connection.url";
	public static final String CONFIG_CONNECTION_DRIVER_CLASS = "connection.driver.class";
	public static final String CONFIG_CONNECTION_USER = "connection.user";
	public static final String CONFIG_CONNECTION_PASSWORD = "connection.password";
	public static final String CONFIG_SOURCE_COLUMN_NAMES = "source.column.names";
	public static final String CONFIG_TARGET_COLUMN_NAMES = "target.column.names";
	public static final String CONFIG_QUEUE_BATCH_SIZE = "queue.batch.size";
	public static final String CONFIG_QUEUE_BATCH_INTERVAL_IN_MSEC = "queue.batch.intervalInMsec";

	private static final ConfigDef CONFIG_DEF = new ConfigDef()
			.define(CONFIG_TABLE, Type.STRING, DEFAULT_TABLE, Importance.HIGH,
					"Destination table. If not specified, then 'mytable' is assigned.")
			.define(CONFIG_DEBUG_ENABLED, Type.BOOLEAN, false, Importance.LOW,
					"Debug flag. If true, then debug information is printed.")
			.define(CONFIG_SMT_ENABLED, Type.BOOLEAN, true, Importance.HIGH,
					"Single Message Transform flag. If true, then SMT messages are expected.")
			.define(CONFIG_DELETE_ENABLED, Type.BOOLEAN, true, Importance.HIGH,
					"Single Message Transform flag. If true, then SMT messages are expected.")
			.define(CONFIG_CONNECTION_URL, Type.STRING, DEFAULT_CONNECTION_URL, Importance.HIGH,
					"SnappyData JDBC Connection URL.")
			.define(CONFIG_CONNECTION_DRIVER_CLASS, Type.STRING, DEFAULT_CONNECTION_DRIVER_CLASS, Importance.HIGH,
					"SnappyData JDBC driver class.")
			.define(CONFIG_CONNECTION_USER, Type.STRING, DEFAULT_USER, Importance.HIGH, "SnappyData connection user.")
			.define(CONFIG_CONNECTION_PASSWORD, Type.STRING, DEFAULT_PASSWORD, Importance.HIGH,
					"SnappyData conenction password.")
			.define(CONFIG_SOURCE_COLUMN_NAMES, Type.STRING, null, Importance.HIGH,
					"An ordered list of comma separated source table column names to be mapped to the target SnappyData table column names.")
			.define(CONFIG_TARGET_COLUMN_NAMES, Type.STRING, null, Importance.HIGH,
					"An ordered list of comma separated target SnappyData table column names.")
			.define(CONFIG_QUEUE_BATCH_SIZE, Type.INT, DEFAULT_QUEUE_BATCH_SIZE, Importance.MEDIUM,
					"Kafka sink consumer thread queue batch size.")
			.define(CONFIG_QUEUE_BATCH_INTERVAL_IN_MSEC, Type.LONG, DEFAULT_QUEUE_BATCH_INTERVAL_IN_MSEC,
					Importance.MEDIUM, "Kafka sink consumer thread queue batch interval in msec.");

	private static final Logger logger = LoggerFactory.getLogger(DebeziumKafkaSinkConnector.class);

	private String tableName;
	private boolean isDebugEnabled = false;
	private boolean isSmtEnabled = true;
	private boolean isDeleteEnabled = true;
	private String connectionUrl;
	private String connectionDriverClassName;
	private String connectionUser;
	private String connectionPwd;
	private String sourceColumnNames;
	private String targetColumnNames;
	private int queueBatchSize = DEFAULT_QUEUE_BATCH_SIZE;
	private long queueBatchIntervalInMsec = DEFAULT_QUEUE_BATCH_INTERVAL_IN_MSEC;

	@Override
	public String version() {
		return AppInfoParser.getVersion();
	}

	@Override
	public void start(Map<String, String> props) {
		AbstractConfig parsedConfig = new AbstractConfig(CONFIG_DEF, props);
		tableName = parsedConfig.getString(CONFIG_TABLE);
		String isDebugStr = props.get(CONFIG_DEBUG_ENABLED);
		isDebugEnabled = isDebugStr != null && isDebugStr.equalsIgnoreCase("true") ? true : isDebugEnabled;
		String isSmtStr = props.get(CONFIG_SMT_ENABLED);
		isSmtEnabled = isSmtStr != null && isSmtStr.equalsIgnoreCase("false") ? false : isSmtEnabled;
		String isDeleteStr = props.get(CONFIG_DELETE_ENABLED);
		isDeleteEnabled = isDeleteStr != null && isDeleteStr.equalsIgnoreCase("false") ? false : isDeleteEnabled;
		connectionUrl = props.get(CONFIG_CONNECTION_URL);
		connectionDriverClassName = props.get(CONFIG_CONNECTION_DRIVER_CLASS);
		connectionUser = props.get(CONFIG_CONNECTION_USER);
		connectionPwd = props.get(CONFIG_CONNECTION_PASSWORD);
		sourceColumnNames = props.get(CONFIG_SOURCE_COLUMN_NAMES);
		targetColumnNames = props.get(CONFIG_TARGET_COLUMN_NAMES);
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
		logger.info(CONFIG_CONNECTION_URL + " = " + connectionUrl);
		logger.info(CONFIG_CONNECTION_DRIVER_CLASS + " = " + connectionDriverClassName);
		logger.info(CONFIG_CONNECTION_USER + " = " + connectionUser);
		logger.info(CONFIG_CONNECTION_PASSWORD + " = ********");
		logger.info(CONFIG_SMT_ENABLED + " = " + isSmtEnabled);
		logger.info(CONFIG_DELETE_ENABLED + " = " + isDeleteEnabled);
		logger.info(CONFIG_DEBUG_ENABLED + " = " + isDebugEnabled);
		logger.info(CONFIG_TABLE + " = " + tableName);
		logger.info(CONFIG_SOURCE_COLUMN_NAMES + " = " + sourceColumnNames);
		logger.info(CONFIG_TARGET_COLUMN_NAMES + " = " + targetColumnNames);
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
			if (tableName != null) {
				config.put(CONFIG_TABLE, tableName);
			}
			config.put(CONFIG_DEBUG_ENABLED, Boolean.toString(isDebugEnabled));
			config.put(CONFIG_SMT_ENABLED, Boolean.toString(isSmtEnabled));
			config.put(CONFIG_DELETE_ENABLED, Boolean.toString(isDeleteEnabled));
			if (connectionUrl != null) {
				config.put(CONFIG_CONNECTION_URL, connectionUrl);
			}
			if (connectionDriverClassName != null) {
				config.put(CONFIG_CONNECTION_DRIVER_CLASS, connectionDriverClassName);
			}
			if (connectionUser != null) {
				config.put(CONFIG_CONNECTION_USER, connectionUser);
			}
			if (connectionPwd != null) {
				config.put(CONFIG_CONNECTION_USER, connectionPwd);
			}
			if (sourceColumnNames != null) {
				config.put(CONFIG_SOURCE_COLUMN_NAMES, sourceColumnNames);
			}
			if (targetColumnNames != null) {
				config.put(CONFIG_TARGET_COLUMN_NAMES, targetColumnNames);
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
