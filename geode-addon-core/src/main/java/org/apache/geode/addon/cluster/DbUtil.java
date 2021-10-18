package org.apache.geode.addon.cluster;

import java.util.Set;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;

public class DbUtil {

	/**
	 * Recursively loads database tables to all the regions that are registered with
	 * {@linkplain CacheWriterLoaderPkDbImpl}. If the cache is not available or
	 * closed then it silently returns without taking any actions.
	 */
	public final static void loadAll() {
		Cache cache;
		try {
			cache = CacheFactory.getAnyInstance();
			if (cache.isClosed()) {
				return;
			}
		} catch (Exception ex) {
			// ignore
			return;
		}
		Set<Region<?, ?>> set = cache.rootRegions();
		for (Region<?, ?> region : set) {
			loadAll(region);
		}
	}

	/**
	 * Recursively loads database tables to the specified region and its sub-regions
	 * that are registered with {@linkplain CacheWriterLoaderPkDbImpl}.
	 * 
	 * @param region parent region
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final static void loadAll(Region region) {
		RegionAttributes attr = region.getAttributes();
		if (attr != null) {
			CacheLoader loader = attr.getCacheLoader();
			if (loader != null && loader instanceof CacheWriterLoaderPkDbImpl) {
				CacheWriterLoaderPkDbImpl pkDbLoader = (CacheWriterLoaderPkDbImpl) loader;
				pkDbLoader.loadAllFromDb();
			}
		}
		Set<Region> subRegionSet = region.subregions(false);
		for (Region<?, ?> subRegion : subRegionSet) {
			loadAll(subRegion);
		}
	}
}
