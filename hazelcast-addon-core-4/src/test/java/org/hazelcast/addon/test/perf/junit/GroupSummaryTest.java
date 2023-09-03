package org.hazelcast.addon.test.perf.junit;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.hazelcast.addon.test.perf.DataIngestionTest;
import org.hazelcast.addon.test.perf.data.EligCallable;
import org.hazelcast.addon.test.perf.data.GroupSummary;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

import org.junit.Assert;

/**
 * GroupSummaryTest tests the {@linkplain EligCallable} task executed in the
 * cluster. It requires the user to first run {@linkplain DataIngestionTest} to
 * ingest data into the cluster.
 * 
 * @author dpark
 *
 */
public class GroupSummaryTest {
	private static HazelcastInstance hz;

	@BeforeClass
	public static void setUp() throws Exception {
		setUpClient();
	}

	private static void setUpClient() {
		System.setProperty("hazelcast.logging.type", "none");
		hz = HazelcastClient.newHazelcastClient();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		HazelcastClient.shutdownAll();
	}

	/**
	 * Tests the group number "x1".
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testGroupNumberX1() throws InterruptedException, ExecutionException {
		IExecutorService executorService = hz.getExecutorService("elig-executor");
		String groupNumber = "x1";
		EligCallable eligTask = new EligCallable(groupNumber);
		Future<GroupSummary> future = executorService.submit(eligTask);
		GroupSummary summary = future.get();
		Assert.assertNotNull(summary);
		System.out.println(summary);
	}
}
