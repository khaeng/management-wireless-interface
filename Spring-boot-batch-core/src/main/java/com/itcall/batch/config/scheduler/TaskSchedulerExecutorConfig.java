package com.itcall.batch.config.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@DependsOn(value={"batchJobConfig"})
public class TaskSchedulerExecutorConfig {

	@Bean(name="taskScheduler", destroyMethod="destroy")
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(15);
		taskScheduler.initialize();
		taskScheduler.setThreadNamePrefix("Bat.Schd-");
		return taskScheduler;
	};

	@Bean(name= {"taskExecutor"}, destroyMethod="destroy")
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(10);
		taskExecutor.setMaxPoolSize(15);
		taskExecutor.setQueueCapacity(500);
		taskExecutor.initialize();
		taskExecutor.setThreadNamePrefix("Async-");
		return taskExecutor;
	}

	// @PostConstruct
	@Bean(name= {"batchTaskExecutor","threadPoolTaskExecutor"}, destroyMethod="destroy")
	public ThreadPoolTaskExecutor batchTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(10);
		taskExecutor.setMaxPoolSize(20);
		taskExecutor.setQueueCapacity(300);
		taskExecutor.initialize();
		taskExecutor.setThreadNamePrefix("Bat.Async-");
		return taskExecutor;
	}

	// @PostConstruct
	@Bean(name="batchShotTaskExecutor", destroyMethod="destroy")
	public ThreadPoolTaskExecutor batchShotTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(15);
		taskExecutor.setMaxPoolSize(20);
		taskExecutor.setQueueCapacity(30);
		return taskExecutor;
	}

}
