package org.redis.addon.redisson.test.cluster.console;

import java.util.Map;
import java.util.Set;

import org.hibernate.internal.build.AllowSysOut;
import org.redis.addon.redisson.cluster.ClusterUtil;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

/**
 * CacheRead dumps the specified RMap values.
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
		writeLine("   " + executableName + " [-list] map_name [-?]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Dumps the values of the specified RMap. To list the existing maps, specify '-list'.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -list");
		writeLine("             Lists all RMaps in the cluster.");
		writeLine();
		writeLine("   map_name   RMap name.");
		writeLine();
		writeLine("EXAMPLES");
		writeLine("   # Read my_map if it exists");
		writeLine("   ./" + executableName + " my_map");
		writeLine();
	}

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	@SuppressWarnings("rawtypes")
	private static void listMaps(Map<String, RMap> mapMap) {
		writeLine("Existing RMaps:");
		for (String key : mapMap.keySet()) {
			writeLine("   " + key);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {
		boolean isListMaps = false;
		String mapName = null;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
				System.exit(0);
			} else if (arg.startsWith("-list")) {
				isListMaps = true;
			} else if (arg.startsWith("-") == false) {
				mapName = arg;
			}
		}

		if (isListMaps == false && mapName == null) {
			System.err.println("ERROR: RMap name not specified. Command aborted.");
			System.exit(1);
		}

		if (mapName != null && mapName.equals("-?")) {
			usage();
			System.exit(0);
		}

		RedissonClient redisson = ClusterUtil.createRedissonClient();
		Map<String, RMap> mapMap = ClusterUtil.getAllMaps(redisson);

		writeLine();
		if (isListMaps) {
			listMaps(mapMap);
		}

		if (mapName != null) {
			RMap map = mapMap.get(mapName);
			if (map == null) {
				System.err.println("ERROR: Map does not exist in the cluster: [" + mapName + "]");
				System.err.println("       If the map name has characters such as '$' then you might need to precede");
				System.err.println("       them with the escape charater, '\'. Command aborted.");
				writeLine();
				if (isListMaps == false) {
					listMaps(mapMap);
					writeLine();
				}
				redisson.shutdown();
				System.exit(1);
			}
			writeLine("RMap: " + mapName);
			writeLine("Key: Value, Class");
			writeLine("---------------------------------------------------------------------------------");
			((Set<Map.Entry>) map.entrySet()).forEach(e -> writeLine(
					e.getKey() + ": " + e.getValue() + ", " + e.getValue().getClass().getCanonicalName()));
		}
		writeLine();
		redisson.shutdown();
	}
}
