package org.redis.addon.redisson.test.perf;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.redisson.RedissonNode;
import org.redisson.api.RExecutorService;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonNodeInitializer;
import org.redisson.api.WorkerOptions;
import org.redisson.api.executor.TaskFailureListener;
import org.redisson.api.executor.TaskFinishedListener;
import org.redisson.api.executor.TaskStartedListener;
import org.redisson.api.executor.TaskSuccessListener;
import org.springframework.beans.factory.BeanFactory;

public class PerfTestRedissonNodeInitializer implements RedissonNodeInitializer {

	@Override
	public void onStartup(RedissonNode redissonNode) {
		
		RedissonClient redisson = redissonNode.getRedisson();
//		redisson.getRemoteService("summary").register(EligCallable.class, new EligCallable());

		BeanFactory beanFactory = null;
		ExecutorService executorService = null;
		WorkerOptions options = WorkerOptions.defaults()

				// Defines workers amount used to execute tasks.
				// Default is 1.
				.workers(1)
				// Defines Spring BeanFactory instance to execute tasks
				// with Spring's '@Autowired', '@Value' or JSR-330's '@Inject' annotation.
//				.beanFactory(beanFactory)

				// Defines custom ExecutorService to execute tasks
				// Config.executor is used by default.
//				.executorService(executorService)

				// Defines task timeout since task execution start moment
				.taskTimeout(60, TimeUnit.SECONDS)

				// add listener which is invoked on task success event
				.addListener(new TaskSuccessListener() {
					
					@Override
					public <GroupSummary> void onSucceeded(String taskId, GroupSummary result) {					
					}
					
				})

				// add listener which is invoked on task failed event
				.addListener(new TaskFailureListener() {
					public void onFailed(String taskId, Throwable exception) {
					}
				})

				// add listener which is invoked on task started event
				.addListener(new TaskStartedListener() {
					public void onStarted(String taskId) {
					}
				})

				// add listener which is invoked on task finished event
				.addListener(new TaskFinishedListener() {
					public void onFinished(String taskId) {
					}
				});

		RExecutorService executor = redisson.getExecutorService("elig-executor");
		executor.registerWorkers(options);

//        // ...
//        // or
//        redisson.getRemoteService("myRemoteService").register(MyRemoteService.class, new MyRemoteServiceImpl(...));
//        // or
//        reidsson.getTopic("myNotificationTopic").publish("New node has joined. id:" + redissonNode.getId() + " remote-server:" + redissonNode.getRemoteAddress());
	}

}