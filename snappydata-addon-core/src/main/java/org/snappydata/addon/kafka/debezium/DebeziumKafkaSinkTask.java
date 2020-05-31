package org.snappydata.addon.kafka.debezium;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netcrest.pado.internal.util.QueueDispatcher;
import com.netcrest.pado.internal.util.QueueDispatcherListener;

/**
 * DebeziumKafkaSinkTask is a Kafka sink connector for receiving Debezium change
 * events. Use it for demo only until further notice.
 * <p>
 * <b>Known Issues:</b>
 * <p>
 * The {@link SinkRecord} argument of the {@link #put(Collection)} method
 * includes only the key record and does not include delete event information
 * needed to properly delete the entries in SnappyData. Without the "before"
 * Struct data, we are left to construct the SnappyData key object solely based
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
public class DebeziumKafkaSinkTask extends SinkTask implements QueueDispatcherListener {

	private static final Logger logger = LoggerFactory.getLogger(DebeziumKafkaSinkTask.class);

	private boolean isDebugEnabled = false;

	private String connectionUrl;
	private String connectionDriverClassName;
	private String userName;
	private String pwd;
	private String tableName;
	private boolean isSmtEnabled = true;
	private boolean isDeleteEnabled = true;
	private String targetNamesStr;
	private String[] sourceColumnNames;
	private String[] targetColumnNames;
	private int[] targetColumnTypes;
	private String[] targetColumnTypeNames;
	private int queueBatchSize = DebeziumKafkaSinkConnector.DEFAULT_QUEUE_BATCH_SIZE;
	private long queueBatchIntervalInMsec = DebeziumKafkaSinkConnector.DEFAULT_QUEUE_BATCH_INTERVAL_IN_MSEC;

	private QueueDispatcher queueDispatcher;

	private Connection conn;
	private Statement stmt;

	@Override
	public String version() {
		return new DebeziumKafkaSinkConnector().version();
	}

	@Override
	public void start(Map<String, String> props) {
		connectionUrl = props.get(DebeziumKafkaSinkConnector.CONFIG_CONNECTION_URL);
		if (connectionUrl == null) {
			connectionUrl = DebeziumKafkaSinkConnector.DEFAULT_CONNECTION_URL;
		}
		connectionDriverClassName = props.get(DebeziumKafkaSinkConnector.CONFIG_CONNECTION_DRIVER_CLASS);
		if (connectionDriverClassName == null) {
			connectionDriverClassName = DebeziumKafkaSinkConnector.DEFAULT_CONNECTION_DRIVER_CLASS;
		}
		userName = props.get(DebeziumKafkaSinkConnector.CONFIG_CONNECTION_USER);
		if (userName == null) {
			userName = DebeziumKafkaSinkConnector.DEFAULT_USER;
		}
		pwd = props.get(DebeziumKafkaSinkConnector.CONFIG_CONNECTION_PASSWORD);
		if (pwd == null) {
			pwd = DebeziumKafkaSinkConnector.DEFAULT_PASSWORD;
		}
		tableName = props.get(DebeziumKafkaSinkConnector.CONFIG_TABLE);
		if (tableName == null) {
			tableName = "mytable";
		}
		String isDebugStr = props.get(DebeziumKafkaSinkConnector.CONFIG_DEBUG_ENABLED);
		isDebugEnabled = isDebugStr != null && isDebugStr.equalsIgnoreCase("true") ? true : isDebugEnabled;
		String isSmtStr = props.get(DebeziumKafkaSinkConnector.CONFIG_SMT_ENABLED);
		isSmtEnabled = isSmtStr != null && isSmtStr.equalsIgnoreCase("false") ? false : isSmtEnabled;
		String isDeleteStr = props.get(DebeziumKafkaSinkConnector.CONFIG_DELETE_ENABLED);
		isDeleteEnabled = isDeleteStr != null && isDeleteStr.equalsIgnoreCase("false") ? false : isDeleteEnabled;

		// Columns
		String scNames = props.get(DebeziumKafkaSinkConnector.CONFIG_SOURCE_COLUMN_NAMES);
		targetNamesStr = props.get(DebeziumKafkaSinkConnector.CONFIG_TARGET_COLUMN_NAMES);
		String[] tokens;
		if (scNames != null) {
			tokens = scNames.split(",");
			sourceColumnNames = new String[tokens.length];
			for (int j = 0; j < tokens.length; j++) {
				sourceColumnNames[j] = tokens[j].trim();
			}
		}
		if (targetNamesStr != null) {
			tokens = targetNamesStr.split(",");
			targetColumnNames = new String[tokens.length];
			for (int j = 0; j < tokens.length; j++) {
				targetColumnNames[j] = tokens[j].trim();
			}
		}
		String intVal = props.get(DebeziumKafkaSinkConnector.CONFIG_QUEUE_BATCH_SIZE);
		if (intVal != null) {
			try {
				queueBatchSize = Integer.parseInt(intVal);
			} catch (Exception ex) {
				// ignore. use the default value.
			}
		}
		String longVal = props.get(DebeziumKafkaSinkConnector.CONFIG_QUEUE_BATCH_INTERVAL_IN_MSEC);
		if (longVal != null) {
			try {
				queueBatchIntervalInMsec = Long.parseLong(longVal);
			} catch (Exception ex) {
				// ignore. use the default value.
			}
		}

		if (isDebugEnabled) {
			logger.info("====================================================================================");
			logger.info(props.toString());
			logger.info("table = " + tableName);
			logger.info("smtEnabled = " + isSmtEnabled);
			logger.info("deleteEnabled = " + isDeleteEnabled);
			logger.info("sourceColumnNames");
			for (int i = 0; i < sourceColumnNames.length; i++) {
				logger.info("   [" + i + "] " + sourceColumnNames[i]);
			}
			logger.info("targetColumnNames");
			for (int i = 0; i < targetColumnNames.length; i++) {
				logger.info("   [" + i + "] " + targetColumnNames[i]);
			}
			logger.info("queueBatchSize = " + queueBatchSize);
			logger.info("queueBatchIntervalInMsec = " + queueBatchIntervalInMsec);
			logger.info("====================================================================================");
		}

		try {
			Class.forName(connectionDriverClassName);
			conn = DriverManager.getConnection(connectionUrl, userName, pwd);
			stmt = conn.createStatement();
			initSnappy();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		queueDispatcher = new QueueDispatcher(queueBatchSize, queueBatchIntervalInMsec);
		queueDispatcher.setQueueDispatcherListener(this);
		queueDispatcher.start();
	}

	private void initSnappy() {
		targetColumnTypes = new int[targetColumnNames.length];
		targetColumnTypeNames = new String[targetColumnNames.length];
		DatabaseMetaData dbMetaData;
		try {
			dbMetaData = conn.getMetaData();
			ResultSet columns = dbMetaData.getColumns(null, "inventory", "customers", null);
			while (columns.next()) {
				String columnName = columns.getString("COLUMN_NAME");
				int dataType = columns.getInt("DATA_TYPE");
				String typeName = columns.getString("TYPE_NAME");
				System.out.println(columnName + ": " + dataType + ", " + typeName);
				for (int i = 0; i < targetColumnNames.length; i++) {
					if (columnName.equals(targetColumnNames[i])) {
						targetColumnTypes[i] = dataType;
						targetColumnTypeNames[i] = typeName;
						break;
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void put(Collection<SinkRecord> records) {
//		final Serde<String> serde = DebeziumSerdes.payloadJson(String.class);
		for (SinkRecord sinkRecord : records) {
			queueDispatcher.enqueue(sinkRecord);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void objectDispatched(Object obj) {
		Collection<SinkRecord> records = (Collection<SinkRecord>) obj;
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
			Struct keyStruct = (Struct) sinkRecord.key();
			Struct valueStruct = (Struct) sinkRecord.value();

			boolean isDelete = valueStruct == null;
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
				logger.info("op=" + op);
				logger.info("isDelete = " + isDelete);
				logger.info("afterStruct = " + afterStruct);
			}

			/*
			 * Columns
			 */
			Object value;

			// Determine the value column names.
			if (sourceColumnNames == null) {
				sourceColumnNames = getColumnNames(valueStruct);
			}

			Object values[] = new Object[sourceColumnNames.length];
			for (int j = 0; j < sourceColumnNames.length; j++) {
				values[j] = afterStruct.get(sourceColumnNames[j]);
			}
			if (isDebugEnabled) {
				for (int j = 0; j < sourceColumnNames.length; j++) {
					logger.info("valueColumnNames[" + j + "] = " + sourceColumnNames[j] + ": " + values[j]);
				}
			}

			StringBuffer buffer = new StringBuffer(100);
			buffer.append("put into ");
			buffer.append(tableName);
			buffer.append(" (");
			buffer.append(targetNamesStr);
			buffer.append(") values (");
			for (int i = 0; i < values.length; i++) {
				if (i > 0) {
					buffer.append(",");
				}
				// STRING comes in as CLOB
				switch (targetColumnTypes[i]) {
				case Types.VARCHAR:
				case Types.CLOB:
				case Types.DATE:
				case Types.TIMESTAMP:
				case Types.BINARY:
				case Types.VARBINARY:
					buffer.append("'");
					buffer.append(values[i]);
					buffer.append("'");
					break;
				default:
					buffer.append(values[i]);
					break;
				}
			}
			buffer.append(")");
			String sql = buffer.toString();
			if (isDebugEnabled) {
				logger.info(sql);
			}
			try {
				stmt.execute(sql);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
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
		queueDispatcher.flush();
	}

	@Override
	public void stop() {
		try {
			if (conn != null && conn.isClosed() == false) {
				conn.close();
			}
			queueDispatcher.stop();
		} catch (SQLException e) {
			// ignore
		}
		
		logger.info("DebeziumKafkaSinkTask stopped.");
	}
}
