package com.freetsinghua.tool.task;

import java.util.concurrent.Callable;

/**
 * @author z.tsinghua
 * @date 2019/1/28
 */
public interface AsyncListenableTaskExecutor extends AsyncTaskExecutor {

    /**
     * 提交一个{@link Runnable}，返回{@link ListenableFuture}，如果执行成功，则返回{@code null}
     *
     * @param task 任务
     * @return 返回结果
     */
    ListenableFuture<?> submitListenable(Runnable task);

    /**
     * 提交一个{@link Callable},返回{@link ListenableFuture},如果执行成功，则返回null
     *
     * @param task 任务
     * @param <T> 泛型，表征返回类型
     * @return 返回结果
     */
    <T> ListenableFuture<T> submitListenable(Callable<T> task);
}
