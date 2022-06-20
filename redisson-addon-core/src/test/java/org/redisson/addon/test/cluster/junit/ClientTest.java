package org.redisson.addon.test.cluster.junit;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.addon.cluster.ClusterUtil;
import org.redisson.addon.test.perf.data.Blob;
import org.redisson.api.RExecutorService;
import org.redisson.api.RMap;
import org.redisson.api.RMapReactive;
import org.redisson.api.RMapRx;
import org.redisson.api.RQueue;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.RedissonRxClient;
import org.redisson.config.Config;

public class ClientTest {

	@Test
	public void testRedissonClient() throws IOException {
		// 1. Create config object
		Config config = new Config();
		config.useClusterServers()
				// use "rediss://" for SSL connection
				.addNodeAddress("redis://127.0.0.1:6379");

		// or read config from file
//		config = Config.fromYAML(new File("config-file.yaml")); 

		// 2. Create Redisson instance

		// Sync and Async API
		RedissonClient redisson = Redisson.create(config);

		// Reactive API
		RedissonReactiveClient redissonReactive = redisson.reactive();

		// RxJava3 API
		RedissonRxClient redissonRx = redisson.rxJava();
		
		System.out.println("Maps:");
		Map<String, RMap> maps = ClusterUtil.getAllMaps(redisson);
		for (Map.Entry<String, RMap> entry : maps.entrySet()) {
			System.out.println("   " + entry.getKey());
		}
		
		RMap<String, Blob> objMap = redisson.getMap("myObjMap");
		objMap.put("blob1", new Blob(new byte[] { 0, 1, 2, 4}));
		Blob blob = (Blob)objMap.get("blob1");
		System.out.println("blob=" + blob);

		// 3. Get Redis based implementation of java.util.concurrent.ConcurrentMap
		RMap<String, String> map = redisson.getMap("myMap");
		for (int i = 0; i < 100; i++) {
			map.put("key" + i, "value" + i);
		}
		String value = map.get("key1");
		System.out.println("key1=" + value);

		map = redisson.getMap("myMap2");
		map.put("1234567890", "1234567890");

		RMapReactive<String, String> mapReactive = redissonReactive.getMap("myMap");

		RMapRx<String, String> mapRx = redissonRx.getMap("myMap");

		// 4. Get Redis based implementation of java.util.concurrent.ExecutorService
		RExecutorService executor = redisson.getExecutorService("myExecutorService");

		// over 50 Redis based Java objects and services ...

		redisson.shutdown();
	}
}
