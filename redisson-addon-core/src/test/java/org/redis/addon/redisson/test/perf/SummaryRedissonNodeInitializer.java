package org.redis.addon.redisson.test.perf;

import org.redisson.RedissonNode;
import org.redisson.api.RMap;
import org.redisson.api.RRemoteService;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonNodeInitializer;
import org.redisson.api.RemoteInvocationOptions;

public class SummaryRedissonNodeInitializer implements RedissonNodeInitializer {

	@Override
	public void onStartup(RedissonNode redissonNode) {
		String groupNumber = null;
		RMap<String, Integer> map = redissonNode.getRedisson().getMap("summary");
		
		
		RedissonClient redisson = redissonNode.getRedisson();
		RemoteInvocationOptions options = RemoteInvocationOptions.defaults();
		RRemoteService remoteService = redisson.getRemoteService("summary");
		
		EligCallable service = remoteService.get(EligCallable.class, options);
		remoteService.register(EligCallable.class, service);

		
//		RExecutorService executor = redisson.getExecutorService("summary");
//		executor.registerWorkers(options);

//        // ...
//        // or
//        redisson.getRemoteService("myRemoteService").register(MyRemoteService.class, new MyRemoteServiceImpl(...));
//        // or
//        reidsson.getTopic("myNotificationTopic").publish("New node has joined. id:" + redissonNode.getId() + " remote-server:" + redissonNode.getRemoteAddress());
	}

}