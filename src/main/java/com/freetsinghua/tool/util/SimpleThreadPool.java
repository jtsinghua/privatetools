package com.freetsinghua.tool.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import com.freetsinghua.tool.common.CommonConstant;
import com.freetsinghua.tool.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author z.tsinghua
 * @date 2019/2/12
 */
public class SimpleThreadPool {

    private static final ExecutorService EXECUTOR_SERVICE;
    private static final ScheduledThreadPoolExecutor SCHEDULING_EXECUTOR_SERVICE;

    static {
        ExecutorService executorService;
        ScheduledThreadPoolExecutor schedulingExecutorService;
        try {
            ClassPathResource classPathResource = new ClassPathResource("threadpool.properties");
            Properties properties = new Properties();
            properties.load(classPathResource.getInputStream());

            int corePoolSize =
                    PropertiesUtils.getIntValue(
                            properties,
                            "threadpool.corePoolSize",
                            CommonConstant.AVAILABLE_PROCESSORS);
            int keepAliveTime =
                    PropertiesUtils.getIntValue(properties, "threadpool.keepAliveTime", 10);
            int maximumPoolSize =
                    PropertiesUtils.getIntValue(
                            properties,
                            "threadpool.maximumPoolSize",
                            CommonConstant.AVAILABLE_PROCESSORS * 8);

            ThreadFactory threadFactory =
                    new ThreadFactoryBuilder().setNameFormat("simple-%d").setDaemon(true).build();

            executorService =
                    new ThreadPoolExecutor(
                            corePoolSize,
                            maximumPoolSize,
                            keepAliveTime,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(1024 * 4),
                            threadFactory,
                            new ThreadPoolExecutor.CallerRunsPolicy());
            schedulingExecutorService =
                    new ScheduledThreadPoolExecutor(
                            corePoolSize, threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
        } catch (IOException e) {
            e.printStackTrace();
            executorService = null;
            schedulingExecutorService = null;
        }

        EXECUTOR_SERVICE = executorService;
        SCHEDULING_EXECUTOR_SERVICE = schedulingExecutorService;
    }

    public static void schedued(Runnable task, long delay, TimeUnit timeUnit) {
        SCHEDULING_EXECUTOR_SERVICE.schedule(task, delay, timeUnit);
    }

    /**
     * 创建并执行一个周期性操作，该操作在给定的初始延迟后首先启用，然后在给定的周期内启用;执行将在 {@code initialDelay}之后开始，然后是{@code
     * initialDelay + period}，然后是 {@code initialDelay + 2 * period}，依此类推。
     * 如果任务的任何执行遇到异常，则后续执行被禁止。否则，任务仅通过取消或终止执行人终止。 如果执行此任务的时间超过其周期，则后续执行可能会延迟，但不会同时执行。
     *
     * @param task 要执行的任务
     * @param initialDelay 初始延迟时间
     * @param period 周期
     * @param timeUnit 时间单位
     */
    public static void scheduleAtFixedRate(
            Runnable task, long initialDelay, long period, TimeUnit timeUnit) {
        SCHEDULING_EXECUTOR_SERVICE.scheduleAtFixedRate(task, initialDelay, period, timeUnit);
        //        SCHEDULING_EXECUTOR_SERVICE.scheduleWithFixedDelay()
    }

    /**
     * 创建并执行一个周期性动作，该动作在给定的初始延迟之后首先被启用，并且随后在一次执行的终止和下一次执行的开始之间具有给定的延迟。
     * 如果任务的任何执行遇到异常，则后续执行被禁止。否则，任务仅通过取消或终止执行人终止。
     *
     * @param task 任务
     * @param initialDelay 初始延迟时间
     * @param delay 延迟执行时间
     * @param timeUnit 时间单位
     */
    public static void scheduleWithFixedDelay(
            Runnable task, long initialDelay, long delay, TimeUnit timeUnit) {
        SCHEDULING_EXECUTOR_SERVICE.scheduleWithFixedDelay(task, initialDelay, delay, timeUnit);
    }

    /**
     * 在将来的某个时间执行给定的命令。命令可以在{@code Executor}实现的判断下,在新线程，池化线程或调用线程中执行。
     *
     * @param task 任务
     */
    public static void execute(Runnable task) {
        EXECUTOR_SERVICE.execute(task);
    }

    /**
     * Submits a value-returning task for execution and returns a Future representing the pending
     * results of the task. The Future's {@code get} method will return the task's result upon
     * successful completion.
     *
     * <p>If you would like to immediately block waiting for a task, you can use constructions of
     * the form {@code result = exec.submit(aCallable).get();}
     *
     * @param task 任务
     * @param <T> 返回值类型
     * @return 返回{@link Future}
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return EXECUTOR_SERVICE.submit(task);
    }
}
