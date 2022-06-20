package org.redisson.addon.test.perf.junit;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.addon.test.perf.DataIngestionTest;
import org.redisson.addon.test.perf.EligCallable;
import org.redisson.addon.test.perf.data.GroupSummary;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * GroupSummaryTest tests the {@linkplain EligCallable} task executed in the
 * cluster. It requires the user to first run {@linkplain DataIngestionTest} to
 * ingest data into the cluster.
 * 
 * @author dpark
 *
 */
public class GroupSummaryTest {
	private static RedissonClient redisson;

	@BeforeClass
	public static void setUp() throws Exception {
		setUpClient();
	}

	private static void setUpClient() {
		Config config = new Config();
		config.useClusterServers()
				// use "rediss://" for SSL connection
				.addNodeAddress("redis://127.0.0.1:6379");
		redisson = Redisson.create(config);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		redisson.shutdown();
	}

	/**
	 * Tests the group number "x1".
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testGroupNumberX1() throws InterruptedException, ExecutionException {
		RScheduledExecutorService executorService = redisson.getExecutorService("elig-executor");
		String groupNumber = "x1";
		EligCallable eligTask = new EligCallable(groupNumber);
		Future<GroupSummary> future = executorService.submit(eligTask);
		GroupSummary summary = future.get();
		Assert.assertNotNull(summary);
		System.out.println(summary);
	}
}
