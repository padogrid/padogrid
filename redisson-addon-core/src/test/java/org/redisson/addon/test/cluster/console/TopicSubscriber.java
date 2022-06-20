package org.redisson.addon.test.cluster.console;

import org.redisson.addon.cluster.ClusterUtil;
import org.redisson.api.RReliableTopic;
import org.redisson.api.RShardedTopic;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

/**
 * CacheRead dumps the specified IMap values.
 * 
 * @author dpark
 *
 */
public class TopicSubscriber {

	public final static String PROPERTY_executableName = "executable.name";

	public final static String executableName = System.getProperty(PROPERTY_executableName,
			TopicSubscriber.class.getName());

	private static void usage() {

		writeLine();
		writeLine("NAME");
		writeLine("   " + executableName + " - Listen on a topic and print received messages");
		writeLine();
		writeLine("SYNOPSIS");
		writeLine("   " + executableName + " [-create-topic] -type topic|rtopic|stopic topic_name [-?]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Listens on the specified topic and prints received messages.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -type topic|rtopic|stopic");
		writeLine(
				"             Topic type. Specify 'topic' for Topic, 'rtopic' for ReliableTopic, 'stopic' for ShardedTopic");
		writeLine();
		writeLine("   topic_name");
		writeLine("              Topic name.");
		writeLine();
		writeLine("EXAMPLES");
		writeLine("   # Listen on my_topic if it exists");
		writeLine("   ./" + executableName + " -type topic my_topic");
		writeLine();
		writeLine("   # Create my_reliable_topic if it does not exist");
		writeLine("   ./" + executableName + " -type rtopic my_reliable_topic");
		writeLine();
		writeLine("   # Create my_sharded_topic if it does not exist");
		writeLine("   ./" + executableName + " -type rtopic my_sharded_topic");
		writeLine();
	}

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	enum TopicType {
		topic, rtopic, stopic;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {
		
		String topicTypeArg = null;
		String topicName = null;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
				System.exit(0);
			} else if (arg.equals("-type")) {
				if (i < args.length - 1) {
					topicTypeArg = args[++i].trim();
				}
			} else if (arg.startsWith("-") == false) {
				topicName = arg;
			}
		}

		if (topicTypeArg == null) {
			System.err.println("ERROR: Topic type not specified. See usage (" + executableName + " -?). Command aborted.");
			System.exit(1);
		}
		TopicType topicType = TopicType.topic;
		if (topicTypeArg.equalsIgnoreCase("topic")) {
			topicType = TopicType.topic;
		} else if (topicTypeArg.equalsIgnoreCase("rtopic")) {
			topicType = TopicType.rtopic;
		} else if (topicTypeArg.equalsIgnoreCase("stopic")) {
			topicType = TopicType.stopic;
		} else {
			System.err.println("ERROR: Invalid topic type [" + topicType + "]. See usage (" + executableName + " -?). Command aborted.");
			System.exit(1);
		}
		if (topicName == null) {
			System.err.println("Topic name not specified. See usage (" + executableName + " -?). Command aborted.");
			System.exit(1);
		}

		RedissonClient redisson = ClusterUtil.createRedissonClient();
		
		switch (topicType) {
		case rtopic:
			RReliableTopic rtopic = redisson.getReliableTopic(topicName);
			writeLine("Listening on ReliableTopic: " + topicName);
			writeLine("Ctrl-C to exit.");
			
			rtopic.addListener(Object.class, new MessageListener() {
				@Override
				public void onMessage(CharSequence channel, Object message) {
					System.out.println(channel + ": " + message);
				}
			});
			break;
		case stopic:
			RShardedTopic stopic = redisson.getShardedTopic(topicName);
			writeLine("Listening on ShardedTopic: " + topicName);
			writeLine("Ctrl-C to exit.");
			stopic.addListener(Object.class, new MessageListener() {
				@Override
				public void onMessage(CharSequence channel, Object message) {
					System.out.println(channel + ": " + message);
				}
			});
			break;
		case topic:
			default:
				RTopic topic = redisson.getTopic(topicName);
				writeLine("Listening on Topic: " + topicName);
				writeLine("Ctrl-C to exit.");
				topic.addListener(Object.class, new MessageListener() {
					@Override
					public void onMessage(CharSequence channel, Object message) {
						System.out.println(channel + ": " + message);
					}
				});
				break;
		}
		// Shutdown hook for gracefully closing client
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				redisson.shutdown();
			}
		});
	}
}
