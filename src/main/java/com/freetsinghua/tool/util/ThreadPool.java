package com.freetsinghua.tool.util;

import com.freetsinghua.tool.common.CommonConstant;
import com.freetsinghua.tool.core.io.ClassPathResource;
import com.freetsinghua.tool.task.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * 线程池
 *
 * <p>配置文件：threadpool.properties 使用guava的{@link ThreadFactoryBuilder}
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public final class ThreadPool implements AsyncTaskExecutor, TaskScheduler {

    private final ConcurrentTaskExecutor executor;
    private final ConcurrentTaskScheduler scheduler;
    private static int coreSize;

    private static ConcurrentTaskScheduler getScheduler() {
        try {
            ScheduledThreadPoolExecutor scheduledThreadPoolExecutor =
                    new ScheduledThreadPoolExecutor(coreSize);
            return new ConcurrentTaskScheduler(scheduledThreadPoolExecutor);
        } catch (Throwable e) {
            throw new RuntimeException(e);
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

            String coreSizeStr =
                    properties.getProperty(
                            "threadpool.corePoolSize",
                            String.valueOf(CommonConstant.AVAILABLE_PROCESSORS));
            String maximumPoolSizeStr =
                    properties.getProperty(
                            "threadpool.maximumPoolSize",
                            String.valueOf(CommonConstant.AVAILABLE_PROCESSORS * 8));
            String keepAliveTimeStr =
                    properties.getProperty("threadpool.keepAliveTime", String.valueOf(10L));

            int size = Integer.parseInt(coreSizeStr);
            // 设置核心线程数
            setCoreSize(size);
            int maximumPoolSize = Integer.parseInt(maximumPoolSizeStr);
            long keepAliveTime = Long.parseLong(keepAliveTimeStr);

            ThreadFactory threadFactory =
                    new ThreadFactoryBuilder().setNameFormat("pool-%d").setDaemon(false).build();

            ThreadPoolExecutor threadPoolExecutor =
                    new ThreadPoolExecutor(
                            size,
                            maximumPoolSize,
                            keepAliveTime,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(CommonConstant.K * 5),
                            threadFactory);
            return new ConcurrentTaskExecutor(threadPoolExecutor);
        } catch (IOException e) {
            throw new RuntimeException("threadpool", e);
        }
    }

    private ThreadPool(ConcurrentTaskExecutor executor, ConcurrentTaskScheduler scheduler) {
        this.executor = executor;
        this.scheduler = scheduler;
    }

    private static final ThreadPool THREAD_POOL = new ThreadPool(getExecutor(), getScheduler());

    public static ThreadPool getInstance() {
        return THREAD_POOL;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        return this.scheduler.schedule(task, trigger);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
        return this.scheduler.schedule(task, startTime);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
        return this.scheduler.scheduleAtFixedRate(task, startTime, period);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
        return this.scheduler.scheduleAtFixedRate(task, period);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
        return this.scheduler.scheduleAtFixedRate(task, startTime, delay);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
        return this.scheduler.scheduleAtFixedRate(task, delay);
    }

    @Override
    public void execute(Runnable task) {
        this.executor.execute(task);
    }

    @Override
    public void execute(Runnable task, long startTime) {
        this.executor.execute(task, startTime);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return this.executor.submit(task);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return this.executor.submit(task);
    }
}
