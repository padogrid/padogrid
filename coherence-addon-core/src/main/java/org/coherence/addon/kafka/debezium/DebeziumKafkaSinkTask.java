package org.coherence.addon.kafka.debezium;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;

/**
 * DebeziumKafkaSinkTask is a Kafka sink connector for receiving Debezium change
 * events. Use it for demo only until further notice.
 * <p>
 * <b>Known Issues:</b>
 * <p>
 * The {@link SinkRecord} argument of the {@link #put(Collection)} method
 * includes only the key record and does not include delete event information
 * needed to properly delete the entries in Coherence. Without the "before"
 * Struct data, we are left to construct the Coherence key object solely based
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
public class DebeziumKafkaSinkTask extends SinkTask implements QueueDispatcherListener {

	private static final Logger logger = LoggerFactory.getLogger(DebeziumKafkaSinkTask.class);

	private Session session;
	private boolean isDebugEnabled = false;

	private String coherenceClientFile;
	private String cacheName;
	private NamedCache cache;
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
	private int queueBatchSize = DebeziumKafkaSinkConnector.DEFAULT_QUEUE_BATCH_SIZE;
	private long queueBatchIntervalInMsec = DebeziumKafkaSinkConnector.DEFAULT_QUEUE_BATCH_INTERVAL_IN_MSEC;

	
	private QueueDispatcher queueDispatcher;

	@Override
	public String version() {
		return new DebeziumKafkaSinkConnector().version();
	}

	@Override
	public void start(Map<String, String> props) {
		coherenceClientFile = props.get(DebeziumKafkaSinkConnector.CONFIG_COHERENCE_CLIENT_CONFIG_FILE);
		if (coherenceClientFile == null) {
			coherenceClientFile = "/coherence-addon/etc/client-config.xml";
		}
		cacheName = props.get(DebeziumKafkaSinkConnector.CONFIG_CACHE);
		if (cacheName == null) {
			cacheName = "mycache";
		}
		String isDebugStr = props.get(DebeziumKafkaSinkConnector.CONFIG_DEBUG_ENABLED);
		isDebugEnabled = isDebugStr != null && isDebugStr.equalsIgnoreCase("true") ? true : isDebugEnabled;
		String isSmtStr = props.get(DebeziumKafkaSinkConnector.CONFIG_SMT_ENABLED);
		isSmtEnabled = isSmtStr != null && isSmtStr.equalsIgnoreCase("false") ? false : isSmtEnabled;
		String isDeleteStr = props.get(DebeziumKafkaSinkConnector.CONFIG_DELETE_ENABLED);
		isDeleteEnabled = isDeleteStr != null && isDeleteStr.equalsIgnoreCase("false") ? false : isDeleteEnabled;
		keyClassName = props.get(DebeziumKafkaSinkConnector.CONFIG_KEY_CLASS_NAME);
		valueClassName = props.get(DebeziumKafkaSinkConnector.CONFIG_VALUE_CLASS_NAME);

		// Key
		String cnames = props.get(DebeziumKafkaSinkConnector.CONFIG_KEY_COLUMN_NAMES);
		String fnames = props.get(DebeziumKafkaSinkConnector.CONFIG_KEY_FIELD_NAMES);
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
		cnames = props.get(DebeziumKafkaSinkConnector.CONFIG_VALUE_COLUMN_NAMES);
		fnames = props.get(DebeziumKafkaSinkConnector.CONFIG_VALUE_FIELD_NAMES);
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

		String classpathStr = System.getProperty("java.class.path");
		logger.info("CLASSPATH=" + classpathStr);
		
		if (isDebugEnabled) {
			logger.info("====================================================================================");
			logger.info(props.toString());
			logger.info("coherenceClientFile = " + coherenceClientFile);
			logger.info("cache = " + cacheName);
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
			logger.info("queueBatchSize = " + queueBatchSize);
			logger.info("queueBatchIntervalInMsec = " + queueBatchIntervalInMsec);
			logger.info("====================================================================================");
		}
		try {
			objConverter = new ObjectConverter(keyClassName, keyFieldNames, valueClassName, valueFieldNames);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		System.setProperty(DebeziumKafkaSinkConnector.CONFIG_COHERENCE_CLIENT_CONFIG_FILE, coherenceClientFile);
		initCoherence();
		
		queueDispatcher = new QueueDispatcher(queueBatchSize, queueBatchIntervalInMsec);
		queueDispatcher.setQueueDispatcherListener(this);
		queueDispatcher.start();
	}
	
	private void initCoherence()
	{
		session = Session.create();
		cache = session.getCache(cacheName);
	}
	
	@Override
	public void put(Collection<SinkRecord> records) {
//		final Serde<String> serde = DebeziumSerdes.payloadJson(String.class);
		for (SinkRecord sinkRecord : records) {
			queueDispatcher.enqueue(sinkRecord);
		}
	}

	@Override
	public void objectDispatched(Object obj) {
		Collection<SinkRecord> records = (Collection<SinkRecord>) obj;
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
				logger.info("key = " + key);
			}
			if (isDeleteEnabled && isDelete) {
				cache.remove(key);
				continue;
			}

			/*
			 * Value
			 */
			Object value;

			// Determine the value column names.
			if (valueColumnNames == null) {
				valueColumnNames = getColumnNames(valueStruct);
			}
			Object valueFieldValues[] = new Object[valueColumnNames.length];
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
					logger.info("valueColumnNames[" + j + "] = " + valueColumnNames[j] + ": " + valueFieldValues[j]);
				}
				logger.info("value = " + value);
			}

			keyValueMap.put(key, value);
			count++;
			if (count % 100 == 0) {
				cache.putAll(keyValueMap);
				keyValueMap.clear();
			}
		}
		if (count % 100 > 0) {
			cache.putAll(keyValueMap);
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
		// Ignore
	}

	@Override
	public void stop() {
		if (session != null) {
			try {
				session.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
