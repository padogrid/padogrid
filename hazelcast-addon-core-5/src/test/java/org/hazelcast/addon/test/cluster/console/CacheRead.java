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
		writeLine("NAME");
		writeLine("   " + executableName + " - Dump the values of the specified map");
		writeLine();
		writeLine("SYNOPSIS");
		writeLine("   " + executableName + " map_name [-?]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Dumps the values of the specified map.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   map_name   IMap name.");
		writeLine();
	}

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("IMap name not specified. Command aborted.");
			System.exit(1);
		}

		String mapName = args[0];
		if (mapName.equals("-?")) {
			usage();
			System.exit(0);
		}
		
		HazelcastInstance instance = HazelcastClient.newHazelcastClient();
		instance.getMap(mapName).values().forEach(c -> System.out.println("\t" + c));
		instance.shutdown();
	}
}
