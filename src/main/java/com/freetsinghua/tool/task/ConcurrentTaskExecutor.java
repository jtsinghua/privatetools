package com.freetsinghua.tool.task;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * @author z.tsinghua
 * @date 2019/1/28
 */
public class ConcurrentTaskExecutor implements AsyncListenableTaskExecutor, SchedulingTaskExecutor {

    private Executor currentExecutor;


    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        return null;
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        return null;
    }

    @Override
    public void execute(Runnable task, long startTime) {

    }

    @Override
    public Future<?> submit(Runnable task) {
        return null;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return null;
    }

    @Override
    public void execute(Runnable task) {

    }

    @Override
    public boolean prefersShortLivedTasks() {
        return false;
    }
}
