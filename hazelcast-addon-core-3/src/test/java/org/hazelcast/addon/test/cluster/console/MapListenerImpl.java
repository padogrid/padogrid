package org.hazelcast.addon.test.cluster.console;

import java.util.Map;

import org.hazelcast.addon.cluster.ClusterUtil;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.hazelcast.map.listener.EntryLoadedListener;
import com.hazelcast.map.listener.EntryMergedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;

/**
 * CacheRead dumps the specified IMap values.
 * 
 * @author dpark
 *
 */
public class MapListenerImpl {

	public final static String PROPERTY_executableName = "executable.name";

	public final static String executableName = System.getProperty(PROPERTY_executableName,
			MapListenerImpl.class.getName());

	private static void usage() {

		writeLine();
		writeLine("NAME");
		writeLine("   " + executableName + " - Listen on a map and print received messages");
		writeLine();
		writeLine("SYNOPSIS");
		writeLine("   " + executableName + " [-create-map] -type map|rmap map_name [-?]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Listens on the specified IMap or ReplicatedMap and prints received events. To create the,");
		writeLine("   specified map, specify the '-create-map' option.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -type map|rmap");
		writeLine("             Map type. Specify 'map' for IMap, 'rmap' for ReplicatedMap.");
		writeLine();
		writeLine("   -create-map");
		writeLine("             If specified, then creates the specified map in the cluster. If unspecified");
		writeLine("             and the map does not exist in the cluster, then it aborts the command.");
		writeLine();
		writeLine("   map_name");
		writeLine("              Map name.");
		writeLine();
		writeLine("EXAMPLES");
		writeLine("   # Listen on my_map if it exists");
		writeLine("   ./" + executableName + " -type map my_map");
		writeLine();
		writeLine("   # Create my_replicated_map if it does not exist");
		writeLine("   ./" + executableName + " -create-map -type rmap my_replicated_map");
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

		String mapType = null;
		String mapName = null;
		boolean isCreateMap = false;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
				System.exit(0);
			} else if (arg.equals("-type")) {
				if (i < args.length - 1) {
					mapType = args[++i].trim();
				}
			} else if (arg.startsWith("-create-map")) {
				isCreateMap = true;
			} else if (arg.startsWith("-") == false) {
				mapName = arg;
			}
		}

		if (mapType == null) {
			System.err.println(
					"ERROR: Map type not specified. See usage (" + executableName + " -?). Command aborted.");
			System.exit(1);
		}
		boolean isRMap = false;
		if (mapType.equalsIgnoreCase("map")) {
			isRMap = false;
		} else if (mapType.equalsIgnoreCase("rmap")) {
			isRMap = true;
		} else {
			System.err.println("ERROR: Invalid map type [" + mapType + "]. See usage (" + executableName
					+ " -?). Command aborted.");
			System.exit(1);
		}
		if (mapName == null) {
			System.err.println("Map name not specified. See usage (" + executableName + " -?). Command aborted.");
			System.exit(1);
		}

		final HazelcastInstance instance = HazelcastClient.newHazelcastClient();

		Map<String, IMap> mapOfMaps;
		Map<String, ReplicatedMap> mapOfRMaps;

		IMap map = null;
		ReplicatedMap rmap = null;
		
		if (isRMap) {
			mapOfRMaps = ClusterUtil.getAllReplicatedMaps(instance);
			rmap = mapOfRMaps.get(mapName);
		} else {
			mapOfMaps = ClusterUtil.getAllMaps(instance);
			map = mapOfMaps.get(mapName);
		}
		
		if (isCreateMap == false) {
			if ((isRMap && rmap == null) || (isRMap == false && map == null)) {
				System.err.println(
						"ERROR: Map does not exist in the cluster: [" + mapName + "]. To create the map in the cluster,");
				System.err.println("       specify the '-create-map' option. Command aborted.");
				instance.shutdown();
				System.exit(1);
			}
		}

		if (isRMap) {
			rmap = instance.getReplicatedMap(mapName);
			writeLine("Listening on reliable map: " + mapName);
			writeLine("Ctrl-C to exit.");
			rmap.addEntryListener(new EntryListener() {
				@Override
				public void entryAdded(EntryEvent event) {
					Object key = event.getKey();
					Object obj = event.getValue();
					if (event.getSource() == null) {
						System.out.println("Added: key=" + key + ", value=" + obj);
					} else {
						System.out.println("Added: " + event.getSource() + " - key=" + key + ", value=" + obj);
					}
				}

				@Override
				public void entryUpdated(EntryEvent event) {
					Object key = event.getKey();
					Object obj = event.getValue();
					if (event.getSource() == null) {
						System.out.println("Updated: key=" + key + ", value=" + obj);
					} else {
						System.out.println("Updated: " + event.getSource() + " - key=" + key + ", value=" + obj);
					}
				}

				@Override
				public void entryRemoved(EntryEvent event) {
					Object key = event.getKey();
					Object obj = event.getValue();
					if (event.getSource() == null) {
						System.out.println("Removed: key=" + key + ", value=" + obj);
					} else {
						System.out.println("Removed: " + event.getSource() + " - key=" + key + ", value=" + obj);
					}
				}

				@Override
				public void entryEvicted(EntryEvent event) {
					Object key = event.getKey();
					Object obj = event.getValue();
					if (event.getSource() == null) {
						System.out.println("Evicted: key=" + key + ", value=" + obj);
					} else {
						System.out.println("Evicted: " + event.getSource() + " - key=" + key + ", value=" + obj);
					}
				}

				@Override
				public void mapCleared(MapEvent event) {
					if (event.getSource() == null) {
						System.out.println("Map Cleared: " + event.getNumberOfEntriesAffected());
					} else {
						System.out.println(
								"Map Cleared: " + event.getSource() + " - " + event.getNumberOfEntriesAffected());
					}
				}

				@Override
				public void mapEvicted(MapEvent event) {
					if (event.getSource() == null) {
						System.out.println("Map Evicted: " + event.getNumberOfEntriesAffected());
					} else {
						System.out.println(
								"Map Evicted: " + event.getSource() + " - " + event.getNumberOfEntriesAffected());
					}
				}

			});
		} else {
			map = instance.getMap(mapName);
			writeLine("Listening on map: " + mapName);
			writeLine("Ctrl-C to exit.");
			map.addEntryListener(new MyMapListenerImpl(), true);
		}

		// Shutdown hook for gracefully closing client
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				instance.shutdown();
			}
		});
	}

	@SuppressWarnings("rawtypes")
	static class MyMapListenerImpl implements MapClearedListener, MapEvictedListener, EntryAddedListener, EntryEvictedListener,
			EntryExpiredListener, EntryRemovedListener, EntryMergedListener, EntryUpdatedListener, EntryLoadedListener {

		@Override
		public void entryLoaded(EntryEvent event) {
			Object key = event.getKey();
			Object obj = event.getValue();
			if (event.getSource() == null) {
				System.out.println("Loaded: key=" + key + ", value=" + obj);
			} else {
				System.out.println("Loaded: " + event.getSource() + " - key=" + key + ", value=" + obj);
			}
		}

		@Override
		public void entryUpdated(EntryEvent event) {
			Object key = event.getKey();
			Object obj = event.getValue();
			if (event.getSource() == null) {
				System.out.println("Updated: key=" + key + ", value=" + obj);
			} else {
				System.out.println("Updated: " + event.getSource() + " - key=" + key + ", value=" + obj);
			}
		}

		@Override
		public void entryMerged(EntryEvent event) {
			Object key = event.getKey();
			Object obj = event.getValue();
			if (event.getSource() == null) {
				System.out.println("Merged: key=" + key + ", value=" + obj);
			} else {
				System.out.println("Merged: " + event.getSource() + " - key=" + key + ", value=" + obj);
			}
		}

		@Override
		public void entryRemoved(EntryEvent event) {
			Object key = event.getKey();
			Object obj = event.getValue();
			if (event.getSource() == null) {
				System.out.println("Removed: key=" + key + ", value=" + obj);
			} else {
				System.out.println("Removed: " + event.getSource() + " - key=" + key + ", value=" + obj);
			}
		}

		@Override
		public void entryExpired(EntryEvent event) {
			Object key = event.getKey();
			Object obj = event.getValue();
			if (event.getSource() == null) {
				System.out.println("Expired: key=" + key + ", value=" + obj);
			} else {
				System.out.println("Expired: " + event.getSource() + " - key=" + key + ", value=" + obj);
			}
		}

		@Override
		public void entryEvicted(EntryEvent event) {
			Object key = event.getKey();
			Object obj = event.getValue();
			if (event.getSource() == null) {
				System.out.println("Evicted: key=" + key + ", value=" + obj);
			} else {
				System.out.println("Evicted: " + event.getSource() + " - key=" + key + ", value=" + obj);
			}
		}

		@Override
		public void entryAdded(EntryEvent event) {
			Object key = event.getKey();
			Object obj = event.getValue();
			if (event.getSource() == null) {
				System.out.println("Added: key=" + key + ", value=" + obj);
			} else {
				System.out.println("Added: " + event.getSource() + " - key=" + key + ", value=" + obj);
			}
		}

		@Override
		public void mapEvicted(MapEvent event) {
			if (event.getSource() == null) {
				System.out.println("Map Evicted: " + event.getNumberOfEntriesAffected());
			} else {
				System.out.println("Map Evicted: " + event.getSource() + " - " + event.getNumberOfEntriesAffected());
			}
		}

		@Override
		public void mapCleared(MapEvent event) {
			if (event.getSource() == null) {
				System.out.println("Map Cleared: " + event.getNumberOfEntriesAffected());
			} else {
				System.out.println("Map Cleared: " + event.getSource() + " - " + event.getNumberOfEntriesAffected());
			}
		}

	}
}
