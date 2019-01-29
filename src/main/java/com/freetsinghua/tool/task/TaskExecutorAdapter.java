package com.freetsinghua.tool.task;

import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.util.Assert;

import java.util.concurrent.*;

/**
 * @author z.tsinghua
 * @date 2019/1/28
 */
public class TaskExecutorAdapter implements AsyncListenableTaskExecutor {

    private final Executor currentExecutor;
    @Nullable private TaskDecorator taskDecorator;

    public TaskExecutorAdapter(Executor currentExecutor) {
        Assert.notNull(currentExecutor, "Executor must not be null");
        this.currentExecutor = currentExecutor;
    }

    public final void setTaskDecorator(TaskDecorator taskDecorator) {
        this.taskDecorator = taskDecorator;
    }

    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {

        try {
            ListenableFutureTask<Object> future = new ListenableFutureTask<>(task, null);
            doExecute(this.currentExecutor, this.taskDecorator, future);
            return future;
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException(
                    "Executor [" + this.currentExecutor + "] did not accept task " + task, ex);
        }
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        try {
            ListenableFutureTask<T> future = new ListenableFutureTask<>(task);
            doExecute(this.currentExecutor, this.taskDecorator, future);

            return future;
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException(
                    "Executor [ " + this.currentExecutor + "] did not accept task " + task, ex);
        }
    }

    @Override
    public void execute(Runnable task, long startTime) {
        execute(task);
    }

    @Override
    public Future<?> submit(Runnable task) {
        try {
            if (this.taskDecorator == null && this.currentExecutor instanceof ExecutorService) {
                return ((ExecutorService) this.currentExecutor).submit(task);
            } else {
                FutureTask<Object> futureTask = new FutureTask<>(task, null);
                doExecute(this.currentExecutor, this.taskDecorator, futureTask);

                return futureTask;
            }
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException(
                    "Executor [ " + this.currentExecutor + "] did not accept task " + task, ex);
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        try {
            if (this.taskDecorator == null && this.currentExecutor instanceof ExecutorService) {
                return ((ExecutorService) this.currentExecutor).submit(task);
            } else {
                FutureTask<T> futureTask = new FutureTask<>(task);
                doExecute(this.currentExecutor, this.taskDecorator, futureTask);
                return futureTask;
            }
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException(
                    "Executor [ " + this.currentExecutor + "] did not accept task " + task, ex);
        }
    }

    @Override
    public void execute(Runnable task) {
        try {
            doExecute(this.currentExecutor, this.taskDecorator, task);
        } catch (RejectedExecutionException e) {
            throw new TaskRejectedException(
                    "Executor [ " + this.currentExecutor + "] did not accept task " + task, e);
        }
    }

    /**
     * 实际的执行方法
     *
     * @param currentExecutor 要委托的基础JDK并发 {@link Executor}
     * @param taskDecorator 指定的装饰器
     * @param runnable 要执行的任务
     */
    protected void doExecute(
            Executor currentExecutor, @Nullable TaskDecorator taskDecorator, Runnable runnable)
            throws RejectedExecutionException {
        currentExecutor.execute(
                taskDecorator == null ? runnable : taskDecorator.decorate(runnable));
    }
}
