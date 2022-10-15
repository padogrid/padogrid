package org.kafka.addon.test.cluster.console;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.kafka.addon.cluster.ClusterUtil;

/**
 * TopicSubscriber dumps the specified topic values.
 * 
 * @author dpark
 *
 */
public class TopicSubscriber {

	public final static String PROPERTY_executableName = "executable.name";

	private static void usage() {
		String executableName = System.getProperty(PROPERTY_executableName, TopicSubscriber.class.getName());
		writeLine();
		writeLine("NAME");
		writeLine("   " + executableName + " - Listen on the specified topics and print received messages");
		writeLine();
		writeLine("SYNOPSIS");
		writeLine("   " + executableName + " [-list] topic_name [-?]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Listens on the specified topics and prints received messages. To list the existing topics, specify '-list'.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -list");
		writeLine("             Lists all existing topics in the cluster.");
		writeLine();
		writeLine("   topic_name");
		writeLine("             Topic name.");
		writeLine();
		writeLine("EXAMPLES");
		writeLine("   # Read my_topic if it exists");
		writeLine("   ./" + executableName + " my_topic");
		writeLine();
	}

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	private static void listTopics(Map<String, List<PartitionInfo>> topicMap) {
		writeLine("Existing Topics:");
		for (String key : topicMap.keySet()) {
			writeLine("   " + key);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws IOException {
		boolean isListMaps = false;
		String topicName = null;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
				System.exit(0);
			} else if (arg.startsWith("-list")) {
				isListMaps = true;
			} else if (arg.startsWith("-") == false) {
				topicName = arg;
			}
		}

		if (isListMaps == false && topicName == null) {
			System.err.println("ERROR: Topic not specified. Command aborted.");
			System.exit(1);
		}

		if (topicName != null && topicName.equals("-?")) {
			usage();
			System.exit(0);
		}

		KafkaConsumer consumer = ClusterUtil.createConsumer();
		Map<String, List<PartitionInfo>> topicMap = consumer.listTopics();
		
		writeLine();
		if (isListMaps) {
			listTopics(topicMap);
		}

		if (topicName != null) {
			List<PartitionInfo> list = topicMap.get(topicName);
			if (list == null) {
				System.err.println("ERROR: Topic does not exist in the cluster: [" + topicName + "]");
				System.err.println("       If the topic name has characters such as '$' then you might need to precede");
				System.err.println("       them with the escape charater, '\'. Command aborted.");
				writeLine();
				if (isListMaps == false) {
					listTopics(topicMap);
					writeLine();
				}
				consumer.close();
				System.exit(1);
			}
			writeLine("Topic: " + topicName);
			writeLine("Key: Value, Class");
			writeLine("---------------------------------------------------------------------------------");
			consumer.subscribe(Collections.singletonList(topicName));
			consumer.seekToBeginning(Collections.EMPTY_LIST);
			while (true) {
				final ConsumerRecords<?, ?> records = consumer.poll(Duration.ofMillis(1000));
				for (final ConsumerRecord<?, ?> record : records) {
					writeLine(record.key() + ": " + record.value() + ", " + record.value().getClass().getCanonicalName());
				}
			}
			
		}
		writeLine();
		consumer.close();
	}
}
