package org.apache.geode.addon.kafka.debezium;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.avro.generic.GenericData;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.specific.SpecificData;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyAccessorFactory;

import com.netcrest.pado.internal.util.ObjectConverter;

import io.confluent.connect.avro.AvroData;

/**
 * DebeziumKafkaAvroSinkTask is a Kafka sink connector for receiving Debezium
 * change events. Use it for demo only until further notice.
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
public class DebeziumKafkaAvroSinkTask extends SinkTask {

	private static final Logger logger = LoggerFactory.getLogger(DebeziumKafkaAvroSinkTask.class);

	private DataConverter dataConverter;

	@Override
	public String version() {
		return new DebeziumKafkaAvroSinkConnector().version();
	}

	@Override
	public void start(Map<String, String> props) {
		dataConverter = new DataConverter(props);
	}

	@Override
	public void put(Collection<SinkRecord> records) {
		dataConverter.put(records);
	}

	/**
	 * Spring object conversion - not used
	 * 
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
	
	@Override
	public void flush(Map<TopicPartition, OffsetAndMetadata> offsets) {
		logger.trace("Flushing map for {}", logRegionPath());
	}

	@Override
	public void stop() {
		dataConverter.close();
	}

	private String logRegionPath() {
		return dataConverter.getRegionPath() == null ? "stdout" : dataConverter.getRegionPath();
	}
}
