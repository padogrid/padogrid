package org.hazelcast.jet.addon.kafka.debezium;

import java.util.Map;

/**
 * Transformer is a callback invoked by {@linkplain KafkaTransformerSinks} after
 * the raw data has been converted to objects, providing the implementing class
 * an opportunity to perform additional object transformation and load.
 * 
 * @author dpark
 *
 * @param <K> Converted key object
 * @param <V> Converted value object
 * 
 * @return Object returned by the callback
 */
public interface Transformer<K, V> {
	Object transform(TransformerContext context, Map.Entry<K, V> entry);
}
