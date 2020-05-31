package org.oracle.coherence.addon.test.perf;

import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;

import org.oracle.coherence.addon.test.perf.data.Blob;
import org.oracle.coherence.addon.test.perf.data.ClientProfileKey;
import org.oracle.coherence.addon.test.perf.data.EligKey;
import org.oracle.coherence.addon.test.perf.data.GroupSummary;

import com.oracle.common.base.Predicate;
import com.tangosol.coherence.dslquery.FilterBuilder;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.util.Filter;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.filter.KeyAssociatedFilter;

/**
 * EligCallable is executed in the cluster to insert {@linkplain GroupSummary}
 * objects in the "summary" map.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EligProcessor implements InvocableMap.EntryProcessor {
	private static final long serialVersionUID = 1L;

	private Session session;
	private String groupNumber;

	public EligProcessor(String groupNumber) {
		this.groupNumber = groupNumber;
	}

//	private Collection<Entry<EligKey, Blob>> getEligibilityByGroupNumber(String groupNumber) {
//		NamedCache<EligKey, Blob> cache = session.getCache(CacheNameEnum.eligibility.name());
//		cache.getCacheService().
//		EntryObject e = new PredicateBuilder().getEntryObject();
//		Predicate predicate = e.key().get("groupNumber").equal(groupNumber);
//		PartitionPredicate<EligKey, Blob> partitionPredicate = new PartitionPredicate<EligKey, Blob>(groupNumber,
//				predicate);
//		Collection<Entry<EligKey, Blob>> col = cache.entrySet(partitionPredicate);
//		return col;
//	}
//
//	private Entry<ClientProfileKey, Blob> getClientProfileByGroupNumber(String groupNumber) {
//		NamedCache<ClientProfileKey, Blob> cache = session.getCache(CacheNameEnum.profile.name());
//		FilterBuilder filterBuilder = new FilterBuilder();
//		Filter filter = new KeyAssociatedFilter(groupNumber);
//		cache.stream(filter);
//		EntryObject e = new PredicateBuilder().getEntryObject();
//		Predicate predicate = e.key().get("groupNumber").equal(groupNumber);
//		PartitionPredicate<ClientProfileKey, Blob> partitionPredicate = new PartitionPredicate<ClientProfileKey, Blob>(
//				groupNumber, predicate);
//		Collection<Entry<ClientProfileKey, Blob>> col = imap.entrySet(partitionPredicate);
//		if (col == null) {
//			return null;
//		}
//		Entry<ClientProfileKey, Blob> entry = null;
//		for (Entry<ClientProfileKey, Blob> entry2 : col) {
//			entry = entry2;
//		}
//		return entry;
//	}
	
	private Collection<Entry<EligKey, Blob>> getEligibilityByGroupNumber(String groupNumber) {
		return null;
	}
	
	private Entry<ClientProfileKey, Blob> getClientProfileByGroupNumber(String groupNumber) {
		return null;
	}

	@Override
	public Object process(com.tangosol.util.InvocableMap.Entry entry) {
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
			for (Entry<EligKey, Blob> entry2 : col) {
				totalBlobSize += entry2.getValue().getBlob().length;
			}
			NamedCache<String, GroupSummary> summaryMap = session.getCache(CacheNameEnum.summary.name());
			summary = new GroupSummary(profileKey, col.size(), totalBlobSize, new Date());
			summaryMap.put(groupNumber, summary);
		}
		return summary;
	}

	@Override
	public java.util.Map processAll(java.util.Set setEntries) {
		return null;
	}

}
