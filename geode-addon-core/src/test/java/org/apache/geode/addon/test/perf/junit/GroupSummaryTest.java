package org.apache.geode.addon.test.perf.junit;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.apache.geode.addon.test.perf.PdxDataIngestionTest;
import org.apache.geode.addon.test.perf.EligFunction;
import org.apache.geode.addon.test.perf.RegionNameEnum;
import org.apache.geode.addon.test.perf.data.GroupSummary;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.ResultCollector;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * GroupSummaryTest tests the {@linkplain EligFunction} task executed in the
 * cluster. It requires the user to first run {@linkplain PdxDataIngestionTest} to
 * ingest data into the cluster.
 * 
 * @author dpark
 *
 */
public class GroupSummaryTest {
	private static ClientCache clientCache;

	@BeforeClass
	public static void setUp() throws Exception {
		setUpClient();
	}

	private static void setUpClient() {
		clientCache = new ClientCacheFactory().create();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		clientCache.close();
	}

	/**
	 * Tests the group number "x1".
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testGroupNumberX1() throws InterruptedException, ExecutionException {
		Region region = clientCache.getRegion(RegionNameEnum.eligibility.name());
		String groupNumber = "x1";
		ResultCollector rc = FunctionService.onRegion(region)
				.withFilter(Collections.singleton(groupNumber))
				.execute(EligFunction.ID);
		Object summary = rc.getResult();
		Assert.assertNotNull(summary);
		System.out.println(summary);
	}
}
