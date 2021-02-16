package org.apache.geode.addon.test.cluster.console;

import java.util.Set;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;

/**
 * CacheRead dumps the specified region values.
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
		writeLine("   " + executableName + " region_path [-?]");
		writeLine();
		writeLine("   Dumps the values of the specified region.");
		writeLine();
		writeLine("       region_path   Region path.");
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
			System.err.println("Region path not specified. Command aborted.");
			System.exit(1);
		}

		String regionPath = args[0];
		if (regionPath.equals("-?")) {
			usage();
			System.exit(0);
		}

		ClientCache clientCache = new ClientCacheFactory().create();
		Region<?, ?> region = clientCache.getRegion(regionPath);
		if (region == null) {
			System.err.println("ERROR: Specified region path does not exist: [" + regionPath + "]. Command aborted.");
			clientCache.close();
			System.exit(1);
		}
		Set<?> keys = region.keySetOnServer();
		region.getAll(keys).forEach((k, v) -> {
			System.out.println(k + " " + v);
		});
		clientCache.close();
	}
}