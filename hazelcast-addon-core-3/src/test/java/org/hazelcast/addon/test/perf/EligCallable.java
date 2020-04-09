package org.hazelcast.addon.test.perf;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.hazelcast.addon.test.perf.data.Blob;
import org.hazelcast.addon.test.perf.data.ClientProfileKey;
import org.hazelcast.addon.test.perf.data.EligKey;
import org.hazelcast.addon.test.perf.data.GroupSummary;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.PartitionPredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;

/**
 * EligCallable is executed in the cluster to insert {@linkplain GroupSummary}
 * objects in the "summary" map.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EligCallable
		implements Callable<GroupSummary>, Serializable, HazelcastInstanceAware, PartitionAware<String> {
	private static final long serialVersionUID = 1L;

	private HazelcastInstance hz;
	private String groupNumber;

	public EligCallable(String groupNumber) {
		this.groupNumber = groupNumber;
	}

	private Collection<Entry<EligKey, Blob>> getEligibilityByGroupNumber(String groupNumber) {
		IMap<EligKey, Blob> imap = hz.getMap("eligibility");
		EntryObject e = new PredicateBuilder().getEntryObject();
		Predicate predicate = e.key().get("groupNumber").equal(groupNumber);
		PartitionPredicate<EligKey, Blob> partitionPredicate = new PartitionPredicate<EligKey, Blob>(groupNumber,
				predicate);
		Collection<Entry<EligKey, Blob>> col = imap.entrySet(partitionPredicate);
		return col;
	}

	private Entry<ClientProfileKey, Blob> getClientProfileByGroupNumber(String groupNumber) {
		IMap<ClientProfileKey, Blob> imap = hz.getMap("profile");
		EntryObject e = new PredicateBuilder().getEntryObject();
		Predicate predicate = e.key().get("groupNumber").equal(groupNumber);
		PartitionPredicate<ClientProfileKey, Blob> partitionPredicate = new PartitionPredicate<ClientProfileKey, Blob>(
				groupNumber, predicate);
		Collection<Entry<ClientProfileKey, Blob>> col = imap.entrySet(partitionPredicate);
		if (col == null) {
			return null;
		}
		Entry<ClientProfileKey, Blob> entry = null;
		for (Entry<ClientProfileKey, Blob> entry2 : col) {
			entry = entry2;
		}
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
			IMap<String, GroupSummary> summaryMap = hz.getMap("summary");
			summary = new GroupSummary(profileKey, col.size(), totalBlobSize, new Date());
			summaryMap.put(groupNumber, summary);
		}
		return summary;
	}

	@Override
	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hz = hazelcastInstance;
	}

	/**
	 * Returns the group number as the partition key.
	 */
	@Override
	public String getPartitionKey() {
		return groupNumber;
	}
}
