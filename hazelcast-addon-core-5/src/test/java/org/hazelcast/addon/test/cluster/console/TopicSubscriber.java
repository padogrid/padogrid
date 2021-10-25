package org.hazelcast.addon.test.cluster.console;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import com.hazelcast.topic.ReliableMessageListener;

/**
 * CacheRead dumps the specified IMap values.
 * 
 * @author dpark
 *
 */
public class TopicSubscriber {

	public final static String PROPERTY_executableName = "executable.name";

	public final static String executableName = System.getProperty(PROPERTY_executableName, TopicSubscriber.class.getName());
	
	private static void usage() {
		
		writeLine();
		writeLine("NAME");
		writeLine("   " + executableName + " - Listen on a topic and print received messages");
		writeLine();
		writeLine("SYNOPSIS");
		writeLine("   " + executableName + " -type topic|rtopic topic_name [-?]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Listens on the specified topic and prints received messages.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -type topic|rtopic");
		writeLine("             Topic type. Specify 'topic' for non-reliable topic, 'rtopic' for reliable topic.");
		writeLine();
		writeLine("   topic_name");
		writeLine("              Topic name.");
		writeLine();
	}

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {
		
		String topicType = null;
		String topicName = null;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
				System.exit(0);
			} else if (arg.equals("-type")) {
				if (i < args.length - 1) {
					topicType = args[++i].trim();
				}
			} else if (arg.startsWith("-") == false) {
				topicName = arg;
			}
		}

		if (topicType == null) {
			System.err.println("ERROR: Topic type not specified. See usage (" + executableName + " -?). Command aborted.");
			System.exit(1);
		}
		boolean isRTopic = false;
		if (topicType.equalsIgnoreCase("topic")) {
			isRTopic = false;
		} else if (topicType.equalsIgnoreCase("rtopic")) {
			isRTopic = true;
		} else {
			System.err.println("ERROR: Invalid topic type [" + topicType + "]. See usage (" + executableName + " -?). Command aborted.");
			System.exit(1);
		}
		if (args.length == 0) {
			System.err.println("Topic name not specified. See usage (" + executableName + " -?). Command aborted.");
			System.exit(1);
		}

		final HazelcastInstance instance = HazelcastClient.newHazelcastClient();
		ITopic topic;
		if (isRTopic) {
			topic = instance.getReliableTopic(topicName);
			writeLine("Listening on reliable topic: " + topicName);
			writeLine("Ctrl-C to exit.");
			topic.addMessageListener(new ReliableMessageListener() {
				@Override
				public void onMessage(Message message) {
					Object obj = message.getMessageObject();
					if (message.getPublishingMember() == null) {
						System.out.println(obj);
					} else {
						System.out.println(message.getPublishingMember() + ": " + obj);
					}
				}

				@Override
				public long retrieveInitialSequence() {
					// -1 -> No initial sequence. Start from the next published message.
					return -1;
				}

				@Override
				public void storeSequence(long sequence) {
					// Ignore
				}

				@Override
				public boolean isLossTolerant() {
					return false;
				}

				@Override
				public boolean isTerminal(Throwable failure) {
					failure.printStackTrace();
					return true;
				}

			});
		} else {
			topic = instance.getTopic(topicName);
			writeLine("Listening on topic: " + topicName);
			writeLine("Ctrl-C to exit.");
			topic.addMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message message) {
					Object obj = message.getMessageObject();
					if (message.getPublishingMember() == null) {
						System.out.println(obj);
					} else {
						System.out.println(message.getPublishingMember() + ": " + obj);
					}
				}

			});
		}

		// Shutdown hook for gracefully closing client
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				instance.shutdown();
			}
		});
	}
}
