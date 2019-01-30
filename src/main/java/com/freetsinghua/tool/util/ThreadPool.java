package com.freetsinghua.tool.util;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.freetsinghua.tool.common.CommonConstant;
import com.freetsinghua.tool.core.io.ClassPathResource;
import com.freetsinghua.tool.task.ConcurrentTaskExecutor;
import com.freetsinghua.tool.task.ConcurrentTaskScheduler;
import com.freetsinghua.tool.task.Trigger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 线程池
 *
 * <p>配置文件：threadpool.properties 使用guava的{@link ThreadFactoryBuilder}
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public final class ThreadPool {
	private final ConcurrentTaskExecutor executor;
	private final ConcurrentTaskScheduler scheduler;
	/**
	 * 核心线程数，用于配置{@link java.util.concurrent.ScheduledThreadPoolExecutor}
	 */
	private static int coreSize;

	private static ConcurrentTaskScheduler getScheduler() {
		try {
			ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("scheduled-%d").setDaemon(false)
					.build();
			ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(coreSize,
					threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
			return new ConcurrentTaskScheduler(scheduledThreadPoolExecutor);
		} catch (Throwable e) {
			return null;
		}
	}

	private static void setCoreSize(int core) {
		coreSize = core;
	}

	private static ConcurrentTaskExecutor getExecutor() {
		try {
			ClassPathResource resource = new ClassPathResource("threadpool.properties");
			Properties properties = new Properties();
			properties.load(resource.getInputStream());

			String coreSizeStr = properties.getProperty("threadpool.corePoolSize",
					String.valueOf(CommonConstant.AVAILABLE_PROCESSORS));
			String maximumPoolSizeStr = properties.getProperty("threadpool.maximumPoolSize",
					String.valueOf(CommonConstant.AVAILABLE_PROCESSORS * 8));
			String keepAliveTimeStr = properties.getProperty("threadpool.keepAliveTime", String.valueOf(10L));

			int size = Integer.parseInt(coreSizeStr);
			// 设置核心线程数
			setCoreSize(size);
			int maximumPoolSize = Integer.parseInt(maximumPoolSizeStr);
			long keepAliveTime = Long.parseLong(keepAliveTimeStr);

			ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("pool-%d").setDaemon(false).build();

			ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(size, maximumPoolSize, keepAliveTime,
					TimeUnit.SECONDS, new LinkedBlockingQueue<>(CommonConstant.K * 5), threadFactory,
					new ThreadPoolExecutor.CallerRunsPolicy());
			return new ConcurrentTaskExecutor(threadPoolExecutor);
		} catch (IOException e) {
			return null;
		}
	}

	private ThreadPool(ConcurrentTaskExecutor executor, ConcurrentTaskScheduler scheduler) {
		Assert.state(executor != null, "Executor must not be null");
		Assert.state(scheduler != null, "Executor must not be null");
		this.executor = executor;
		this.scheduler = scheduler;
	}

	private static final ThreadPool THREAD_POOL = new ThreadPool(getExecutor(), getScheduler());

	public static ThreadPool getInstance() {
		return THREAD_POOL;
	}

	public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
		return this.scheduler.schedule(task, trigger);
	}

	public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
		return this.scheduler.schedule(task, startTime);
	}

	public ScheduledFuture<?> schedule(Runnable task, long delay) {
		return this.scheduler.schedule(task, new Date(System.currentTimeMillis() + delay));
	}


	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
		return this.scheduler.scheduleAtFixedRate(task, startTime, period);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
		return this.scheduler.scheduleAtFixedRate(task, period);
	}

	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
		return this.scheduler.scheduleWithFixedDelay(task, startTime, delay);
	}

	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
		return this.scheduler.scheduleWithFixedDelay(task, delay);
	}

	public void execute(Runnable task) {
		this.executor.execute(task);
	}

	public void execute(Runnable task, long startTime) {
		this.executor.execute(task, startTime);
	}

	public Future<?> submit(Runnable task) {
		return this.executor.submit(task);
	}

	public <T> Future<T> submit(Callable<T> task) {
		return this.executor.submit(task);
	}
}
