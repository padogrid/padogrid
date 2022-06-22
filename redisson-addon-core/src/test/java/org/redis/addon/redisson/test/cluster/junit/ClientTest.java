package org.redis.addon.redisson.test.cluster.junit;

import java.io.IOException;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redis.addon.redisson.cluster.ClusterUtil;
import org.redis.addon.test.perf.data.Blob;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

public class ClientTest {
	
	private static RedissonClient redisson;
	
	@BeforeClass
	public static void setUp() throws Exception {
		 redisson = ClusterUtil.createRedissonClient();
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		redisson.shutdown();
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testGetAllMaps() throws IOException {
		RedissonClient redisson = ClusterUtil.createRedissonClient();

		System.out.println("Maps:");
		Map<String, RMap> maps = ClusterUtil.getAllMaps(redisson);
		for (Map.Entry<String, RMap> entry : maps.entrySet()) {
			System.out.println("   " + entry.getKey());
		}
	}

	@Test
	public void testBlobToMap() throws IOException {
		RMap<String, Blob> objMap = redisson.getMap("blob");
		objMap.put("blob1", new Blob(new byte[] { 0, 1, 2, 4 }));
		Blob blob = (Blob) objMap.get("blob1");
		System.out.println("blob=" + blob);

		redisson.shutdown();
	}

	@Test
	public void testStringToMap() throws IOException {
		RMap<String, String> map = redisson.getMap("map1");
		for (int i = 0; i < 100; i++) {
			map.put("key" + i, "value" + i);
		}

		for (int i = 0; i < 100; i++) {
			String value = map.get("key" + i);
			System.out.println("key1=" + value);
		}
	}
}
