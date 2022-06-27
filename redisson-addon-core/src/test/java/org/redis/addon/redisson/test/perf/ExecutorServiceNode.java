package org.redis.addon.redisson.test.perf;

import java.util.Collections;

import org.redis.addon.redisson.cluster.ClusterUtil;
import org.redisson.RedissonNode;
import org.redisson.config.RedissonNodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor service ("elig-executor") for executing {@link EligCallable}.
 * 
 * @author dpark
 *
 */
public class ExecutorServiceNode {
	private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceNode.class);

	public static void main(String[] args) {
		RedissonNodeConfig nodeConfig = ClusterUtil.createRedissonNodeConfig();
		nodeConfig.setExecutorServiceWorkers(Collections.singletonMap("elig-executor", 1));
		RedissonNode node = RedissonNode.create(nodeConfig);
		node.start();

		// Gracefully shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("Shutting down [" + getClass().getCanonicalName() + "] ...");
				node.shutdown();
				logger.info("Shutdown complete [" + getClass().getCanonicalName() + "]");
			}
		});
	}
}
