package com.freetsinghua.tool.task;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author z.tsinghua
 * @date 2019/1/28
 */
public interface AsyncTaskExecutor extends TaskExecutor {

    /** 任务立即执行 */
    long TIMEOUT_IMMEDIATE = 0L;
    /** 任务不定时执行 */
    long TIMEOUT_INDEFINITE = Long.MAX_VALUE;

    /**
     * 指定给定的任务{@code task}
     *
     * @param task 要执行的任务
     * @param startTime 推迟多少时间执行
     */
    void execute(Runnable task, long startTime);

    /**
     * 提交一个任务执行，并接收一个{@link Future}，如果任务执行成功，{@link Future}为{@code null}
     *
     * @param task 任务
     * @return 返回执行任务结果
     */
    Future<?> submit(Runnable task);

    /**
     * 提交一个任务执行，返回执行结果
     *
     * @param task 任务
     * @param <T> 泛型，返回对象的类型
     * @return 返回结果
     */
    <T> Future<T> submit(Callable<T> task);
}
