package org.apache.geode.addon.function;

import java.util.Properties;
import java.util.Set;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.internal.cache.BucketRegion;
import org.apache.geode.internal.cache.PartitionedRegion;

/**
 * ClearFunction clears the specified region. Geode does not permit clearing
 * partitioned regions. Furthermore, clients can only clear local regions such
 * that they cannot clear replicated regions. This function allows clearing
 * both partitioned and replicated regions.
 * <p>
 * <b>Arguments:</b>
 * <ul><li><b>fullPath</b> String Region full path </li></ul>
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes"})
public class ClearFunction implements Function, Declarable {
	private static final long serialVersionUID = 1L;

	public final static String ID = "addon.ClearFunction";

	private Cache cache;
	
	public enum ClearStatus { SUCCESS, ERROR_REGION_PATH_UNDEFINED, ERROR_REGION_PATH_NOT_FOUND };
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(FunctionContext context) {
		String fullPath = (String)context.getArguments();
		if (fullPath == null || fullPath.length() == 0) {
			context.getResultSender().lastResult(ClearStatus.ERROR_REGION_PATH_UNDEFINED);
			return;
		}
		Region region = cache.getRegion(fullPath);
		if (region == null) {
			context.getResultSender().lastResult(ClearStatus.ERROR_REGION_PATH_NOT_FOUND);
			return;
		}

		// TODO: ExecutorService to use a thread pool for concurrent
		// removes.
		if (region instanceof PartitionedRegion) {
			PartitionedRegion pr = (PartitionedRegion) region;
			Set<BucketRegion> set = pr.getDataStore().getAllLocalPrimaryBucketRegions();
			for (BucketRegion bucketRegion : set) {
				final BucketRegion br = bucketRegion;
				Set keySet = br.keySet();
				for (Object key : keySet) {
					br.remove(key);
				}
			}
		} else {
			region.clear();
		}
		context.getResultSender().lastResult(ClearStatus.SUCCESS);
	}
	
	@Override
	public void initialize(Cache cache, Properties properties) {
		this.cache = cache;
	}
	
	@Override
	public String getId() {
		return ID;
	}
}