package org.oracle.coherence.addon.test.perf;

import java.util.HashSet;

/**
 * MapNameEnum lists the valid map names used by the perf tests.
 * @author dpark
 *
 */
public enum CacheNameEnum {
	eligibility, profile, summary;
	
	static CacheNameEnum getMapNameEnum(String mapName) {
		if (eligibility.name().equalsIgnoreCase(mapName)) {
			return eligibility;
		} else if (profile.name().equalsIgnoreCase(mapName)) {
			return profile;
		} else if (summary.name().equalsIgnoreCase(mapName)) {
			return summary;
		} else {
			return null;
		}
	}
	
	static CacheNameEnum[] getMapNameEnums(String mapNames) {
		if (mapNames == null) {
			return new CacheNameEnum[0];
		}
		String split[] = mapNames.split(",");
		HashSet<CacheNameEnum> mapNameSet = new HashSet<CacheNameEnum>(split.length);
		for (String mapName : split) {
			CacheNameEnum mapNameEnum = getMapNameEnum(mapName);
			if (mapNameEnum != null) {
				mapNameSet.add(mapNameEnum);
			}
		}
		return mapNameSet.toArray(new CacheNameEnum[mapNameSet.size()]);
	}
}
