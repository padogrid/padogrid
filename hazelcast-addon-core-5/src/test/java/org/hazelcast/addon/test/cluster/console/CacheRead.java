package org.hazelcast.addon.test.cluster.console;

import java.util.Map;

import org.hazelcast.addon.cluster.ClusterUtil;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

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
		writeLine("   " + executableName + " [-create-map] map_name [-?]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Dumps the values of the specified IMap. To create the specified map,");
		writeLine("   specify the '-create-map' option.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -create-map");
		writeLine("             If specified, then creates the specified map in the cluster. If unspecified");
		writeLine("             and the map does not exist in the cluster, then it aborts the command.");
		writeLine();
		writeLine("   map_name   IMap name.");
		writeLine();
		writeLine("EXAMPLES");
		writeLine("   # Read my_map if it exists");
		writeLine("   ./" + executableName + " my_map");
		writeLine();
		writeLine("   # Create my_map if it does not exist");
		writeLine("   ./" + executableName + " -create-map my_map");
		writeLine();
	}

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	public static void main(String[] args) {
		boolean isCreateMap = false;
		String mapName = null;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
				System.exit(0);
			} else if (arg.startsWith("-create-map")) {
				isCreateMap = true;
			} else if (arg.startsWith("-") == false) {
				mapName = arg;
			}
		}

		if (mapName == null) {
			System.err.println("ERROR: IMap name not specified. Command aborted.");
			System.exit(1);
		}

		if (mapName.equals("-?")) {
			usage();
			System.exit(0);
		}

		HazelcastInstance instance = HazelcastClient.newHazelcastClient();

		Map<String, IMap> mapMap = ClusterUtil.getAllMaps(instance);
		IMap map = mapMap.get(mapName);
		if (isCreateMap == false && map == null) {
			System.err.println("ERROR: Map does not exist in the cluster: [" + mapName + "]. To create the map in the cluster,");
			System.err.println("       specify the '-create-map' option. Command aborted.");
			instance.shutdown();
			System.exit(1);
		}

		instance.getMap(mapName).values().forEach(c -> System.out.println("\t" + c));
		instance.shutdown();
	}
}
