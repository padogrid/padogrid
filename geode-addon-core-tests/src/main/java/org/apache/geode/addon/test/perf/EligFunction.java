package org.apache.geode.addon.test.perf;

import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.geode.addon.test.perf.data.Blob;
import org.apache.geode.addon.test.perf.data.ClientProfileKey;
import org.apache.geode.addon.test.perf.data.EligKey;
import org.apache.geode.addon.test.perf.data.GroupSummary;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.partition.PartitionRegionHelper;
import org.apache.geode.cache.query.FunctionDomainException;
import org.apache.geode.cache.query.NameResolutionException;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryInvocationTargetException;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.cache.query.Struct;
import org.apache.geode.cache.query.TypeMismatchException;
import org.apache.geode.cache.query.internal.DefaultQuery;
import org.apache.geode.cache.query.internal.ExecutionContext;
import org.apache.geode.cache.query.internal.QueryExecutionContext;
import org.apache.geode.internal.cache.LocalDataSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * EligFunction is executed in the cluster to insert {@linkplain GroupSummary}
 * objects in the "summary" map.
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EligFunction implements Function, Declarable {
	private static final long serialVersionUID = 1L;

	public final static String ID = "addon.EligFunction";

	private Logger logger = LogManager.getLogger(this.getClass());
	private String ELIG_QUERY = "select e.key,e.value from /eligibility.entrySet e where e.key.groupNumber=$1";

	/**
	 * Finds eligibility entries that match the specified group number. Invoking
	 * this method is expensive as it queries the entire cluster using the normal
	 * query API. The
	 * {@link #getEligibilityByGroupNumberInternalAPI(RegionFunctionContext, String)} method is
	 * preferred.
	 * 
	 * @param groupNumber
	 * @return
	 * @throws FunctionDomainException
	 * @throws TypeMismatchException
	 * @throws NameResolutionException
	 * @throws QueryInvocationTargetException
	 */
	@SuppressWarnings("unused")
	private SelectResults getEligibilityByGroupNumberPublicAPI(RegionFunctionContext context, String groupNumber) throws FunctionDomainException,
			TypeMismatchException, NameResolutionException, QueryInvocationTargetException {
		QueryService queryService = context.getCache().getQueryService();
		Query query = queryService.newQuery(ELIG_QUERY);
		Object[] params = new Object[] { groupNumber };
		SelectResults sr = (SelectResults) query.execute(params);
		return sr;
	}

	/**
	 * Finds eligibility entries that match the specified group number. This method
	 * invokes Geode internal API. Geode does not provide API for querying local
	 * data sets.
	 * 
	 * @param context     "eligibility" region context
	 * @param groupNumber Group number
	 * @return SelectResults containing a collection of Struct objects with "key"
	 *         and "value" entries.
	 * @throws FunctionDomainException
	 * @throws TypeMismatchException
	 * @throws NameResolutionException
	 * @throws QueryInvocationTargetException
	 */
	private SelectResults getEligibilityByGroupNumberInternalAPI(RegionFunctionContext context, String groupNumber)
			throws FunctionDomainException, TypeMismatchException, NameResolutionException,
			QueryInvocationTargetException {
		Region<EligKey, Blob> localData = PartitionRegionHelper.getLocalDataForContext(context);
		LocalDataSet localDataSet = (LocalDataSet) localData;
		QueryService queryService = localDataSet.getCache().getLocalQueryService();
		DefaultQuery query = (DefaultQuery) queryService.newQuery(ELIG_QUERY);
		final ExecutionContext executionContext = new QueryExecutionContext(null, localDataSet.getCache(), query);
		Object[] params = new Object[] { groupNumber };
		SelectResults sr = (SelectResults) localDataSet.executeQuery(query, executionContext, params,
				localDataSet.getBucketSet());
		return sr;
	}

	private Entry<ClientProfileKey, Blob> getClientProfileByGroupNumber(RegionFunctionContext context,
			String groupNumber) {
		Region<ClientProfileKey, Blob> region = context.getCache().getRegion("profile");
		ClientProfileKey key = new ClientProfileKey(groupNumber, "", "");
		Entry<ClientProfileKey, Blob> entry = region.getEntry(key);
		return entry;
	}

	@Override
	public void execute(FunctionContext fc) {
		RegionFunctionContext context = (RegionFunctionContext) fc;
		Set<String> filter = (Set<String>) context.getFilter();
		if (filter == null || filter.size() == 0) {
			context.getResultSender().lastResult(null);
			return;
		}
		String groupNumber = null;
		for (String str : filter) {
			groupNumber = str;
			break;
		}

		Entry<ClientProfileKey, Blob> clientProfile = getClientProfileByGroupNumber(context, groupNumber);
		if (clientProfile == null) {
			context.getResultSender().lastResult(null);
			return;
		}
		ClientProfileKey profileKey = clientProfile.getKey();

		GroupSummary summary;
		SelectResults sr = null;
		try {
			sr = getEligibilityByGroupNumberInternalAPI(context, groupNumber);
		} catch (FunctionDomainException | TypeMismatchException | NameResolutionException
				| QueryInvocationTargetException e) {
			logger.warn(e.getMessage(), e);
		}
		if (sr == null) {
			summary = new GroupSummary(profileKey, new Date());
		} else {
			long totalBlobSize = 0;
			for (Iterator iter = sr.iterator(); iter.hasNext();) {
				Struct struct = (Struct) iter.next();
				Blob blob = (Blob) struct.get("value");
				totalBlobSize += blob.getBlob().length;
			}
			Region<String, GroupSummary> summaryMap = fc.getCache().getRegion(RegionNameEnum.summary.name());
			summary = new GroupSummary(profileKey, sr.size(), totalBlobSize, new Date());
			summaryMap.put(groupNumber, summary);
		}
		context.getResultSender().lastResult(summary);
	}

	@Override
	public String getId() {
		return ID;
	}
}
