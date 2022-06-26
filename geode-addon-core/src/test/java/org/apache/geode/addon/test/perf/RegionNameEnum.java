package org.apache.geode.addon.test.perf;

import java.util.HashSet;

/**
 * RegionNameEnum lists the valid region names used by the perf tests.
 * @author dpark
 *
 */
public enum RegionNameEnum {
eligibility, profile, summary;
	
	public static RegionNameEnum getMapNameEnum(String mapName) {
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
	
	public static RegionNameEnum[] getMapNameEnums(String mapNames) {
		if (mapNames == null) {
			return new RegionNameEnum[0];
		}
		String split[] = mapNames.split(",");
		HashSet<RegionNameEnum> mapNameSet = new HashSet<RegionNameEnum>(split.length);
		for (String mapName : split) {
			RegionNameEnum mapNameEnum = getMapNameEnum(mapName);
			if (mapNameEnum != null) {
				mapNameSet.add(mapNameEnum);
			}
		}
		return mapNameSet.toArray(new RegionNameEnum[mapNameSet.size()]);
	}
}
