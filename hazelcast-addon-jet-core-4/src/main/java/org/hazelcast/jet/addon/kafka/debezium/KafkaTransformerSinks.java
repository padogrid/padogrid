package org.hazelcast.jet.addon.kafka.debezium;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.generic.GenericData;

import com.hazelcast.jet.JetInstance;

/**
 * KafkaTransformerSinks provides sink entry points for transforming
 * GenericData.Record entries received from Kafka topics.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "unchecked" })
public class KafkaTransformerSinks {
	static HashMap<String, JetAggregator> map = new HashMap<String, JetAggregator>(10);

	public static JetAggregator getJetAggregator(ClassLoader classLoader, JetInstance jet, String connectorConfigFile) {
		JetAggregator ja = map.get(connectorConfigFile);
		if (ja == null) {
			ja = new JetAggregator(classLoader, jet, connectorConfigFile);
			map.put(connectorConfigFile, ja);
		}
		return ja;
	}

	/**
	 * Transforms the GeneralData.Record entries as follows.
	 * <ol>
	 * <li>Convert to key/value objects</li>
	 * <li>If IMDG is enabled then put the converted key/value objects into
	 * IMDG</li>
	 * <li>Invoke {@linkplain Transformer} object if enabled</li>
	 * </ol>
	 * <b>
	 * 
	 * @param jet                 Jet instance.
	 * @param item                Map.Entry<GenericData.Record, GenericData.Record>
	 *                            entry to be converted to key/value objects.
	 * @param connectorConfigFile Connector configuration file name.
	 */
	public static Object transform(ClassLoader classLoader, JetInstance jet, Object item, String connectorConfigFile) {
		JetAggregator ja = getJetAggregator(classLoader, jet, connectorConfigFile);
		Map.Entry<GenericData.Record, GenericData.Record> entry = (Map.Entry<GenericData.Record, GenericData.Record>) item;
		TransformerContext context = ja.getTransformerContext(jet);
		Map.Entry<?, ?> pentry = context.getConverter().putEntry(entry);
		if (context.isJetEnabled()) {
			if (context.isReplicatedMapEnabled()) {
				context.getRmap().put(pentry.getKey(), pentry.getValue());
			} else {
				context.getMap().set(pentry.getKey(), pentry.getValue());
			}
		}
		return context.transform(pentry);
	}
}
