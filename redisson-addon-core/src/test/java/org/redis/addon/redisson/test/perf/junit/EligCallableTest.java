package org.redis.addon.redisson.test.perf.junit;

import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.redis.addon.redisson.cluster.ClusterUtil;
import org.redis.addon.redisson.test.perf.EligCallable;
import org.redis.addon.test.perf.data.GroupSummary;
import org.redisson.api.RExecutorFuture;
import org.redisson.api.RExecutorService;
import org.redisson.api.RedissonClient;

public class EligCallableTest {

	@Test
	public void testEligCallable() {
		// Client
        RedissonClient client = ClusterUtil.createRedissonClient();
        RExecutorService e = client.getExecutorService("elig-executor");
        if (e.isShutdown()) {
			e.delete();
		}
        RExecutorFuture<GroupSummary> future = e.submit(new EligCallable("11111"));
        try {
			GroupSummary summary = future.get();
			System.out.println(summary);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        e.shutdown();
	}

}
