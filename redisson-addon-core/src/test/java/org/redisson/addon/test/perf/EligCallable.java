package org.redisson.addon.test.perf;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.redisson.addon.test.perf.data.Blob;
import org.redisson.addon.test.perf.data.ClientProfileKey;
import org.redisson.addon.test.perf.data.EligKey;
import org.redisson.addon.test.perf.data.GroupSummary;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;


/**
 * EligCallable is executed in the cluster to insert {@linkplain GroupSummary}
 * objects in the "summary" map.
 * 
 * @author dpark
 *
 */
public class EligCallable
		implements Callable<GroupSummary>, Serializable {
	private static final long serialVersionUID = 1L;

	private String groupNumber;
	
	@RInject
    RedissonClient redisson;

	public EligCallable(String groupNumber) {
		this.groupNumber = groupNumber;
	}

	/**
	 * Returns all the specified group number matching eligibility entries.
	 * @param groupNumber
	 */
	private Collection<Entry<EligKey, Blob>> getEligibilityByGroupNumber(String groupNumber) {
		RMap<EligKey, Blob> map = redisson.getMap("eligibility");
		Collection<Entry<EligKey, Blob>> col = null;
		return col;
	}

	/**
	 * Returns the specified group number matching client profile entry.
	 * @param groupNumber
	 */
	private Entry<ClientProfileKey, Blob> getClientProfileByGroupNumber(String groupNumber) {
		RMap<ClientProfileKey, Blob> map = redisson.getMap("profile");
		Entry<ClientProfileKey, Blob> entry = null;
		return entry;
	}

	@Override
	public GroupSummary call() {
		Entry<ClientProfileKey, Blob> clientProfile = getClientProfileByGroupNumber(groupNumber);
		if (clientProfile == null) {
			return null;
		}
		ClientProfileKey profileKey = clientProfile.getKey();

		GroupSummary summary;
		Collection<Entry<EligKey, Blob>> col = getEligibilityByGroupNumber(groupNumber);
		if (col == null) {
			summary = new GroupSummary(profileKey, new Date());
		} else {
			long totalBlobSize = 0;
			for (Entry<EligKey, Blob> entry : col) {
				totalBlobSize += entry.getValue().getBlob().length;
			}
			RMap<String, GroupSummary> summaryMap = redisson.getMap("summary");
			summary = new GroupSummary(profileKey, col.size(), totalBlobSize, new Date());
			summaryMap.put(groupNumber, summary);
		}
		return summary;
	}
}
