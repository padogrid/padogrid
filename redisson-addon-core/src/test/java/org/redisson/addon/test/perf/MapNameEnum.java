package org.redisson.addon.test.perf;

import java.util.HashSet;

/**
 * MapNameEnum lists the valid map names used by the perf tests.
 * @author dpark
 *
 */
public enum MapNameEnum {
	eligibility, profile;
	
	static MapNameEnum getMapNameEnum(String mapName) {
		if (eligibility.name().equalsIgnoreCase(mapName)) {
			return eligibility;
		} else if (profile.name().equalsIgnoreCase(mapName)) {
			return profile;
		} else {
			return null;
		}
	}
	
	static MapNameEnum[] getMapNameEnums(String mapNames) {
		if (mapNames == null) {
			return new MapNameEnum[0];
		}
		String split[] = mapNames.split(",");
		HashSet<MapNameEnum> mapNameSet = new HashSet<MapNameEnum>(split.length);
		for (String mapName : split) {
			MapNameEnum mapNameEnum = getMapNameEnum(mapName);
			if (mapNameEnum != null) {
				mapNameSet.add(mapNameEnum);
			}
		}
		return mapNameSet.toArray(new MapNameEnum[mapNameSet.size()]);
	}
}
