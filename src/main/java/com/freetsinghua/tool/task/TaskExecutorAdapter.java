package com.freetsinghua.tool.task;

import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.util.Assert;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

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

        try{


        }catch (RejectedExecutionException ex){
            throw new TaskRejectedException("Executor [" + this.currentExecutor + "] did not accept task " + task, ex);
        }

        return null;
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        return null;
    }

    @Override
    public void execute(Runnable task, long startTime) {}

    @Override
    public Future<?> submit(Runnable task) {
        return null;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return null;
    }

    @Override
    public void execute(Runnable task) {}

    /**
     * 实际的执行方法
     * @param currentExecutor 要委托的基础JDK并发 {@link Executor}
     * @param taskDecorator 指定的装饰器
     * @param runnable 要执行的任务
     */
    protected void doExecute(
            Executor currentExecutor, @Nullable TaskDecorator taskDecorator, Runnable runnable) throws RejectedExecutionException {
        currentExecutor.execute(
                taskDecorator == null ? runnable : taskDecorator.decorate(runnable));
    }
}
