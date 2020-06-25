package org.hazelcast.addon.test.cluster.console;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

/**
 * CacheRead dumps the specified IMap values.
 * 
 * @author dpark
 *
 */
public class CacheRead {

	public final static String PROPERTY_executableName = "executable.name";

	private static void usage() {
		String executableName = System.getProperty(PROPERTY_executableName, CacheRead.class.getName());
		writeLine();
		writeLine("Usage:");
		writeLine("   " + executableName + " map_name [-?]");
		writeLine();
		writeLine("   Dumps the values of the specified map.");
		writeLine();
		writeLine("       map_name   IMap name.");
		writeLine();
	}

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	public static void main(String[] args) {
		HazelcastInstance instance = HazelcastClient.newHazelcastClient();

		if (args.length == 0) {
			System.err.println("IMap name not specified. Command aborted.");
			System.exit(1);
		}

		String mapName = args[0];
		if (mapName.equals("-?")) {
			usage();
			System.exit(0);
		}
		instance.getMap(mapName).values().forEach(c -> System.out.println("\t" + c));
		instance.shutdown();
	}
}
