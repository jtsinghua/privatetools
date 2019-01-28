package com.freetsinghua.tool.task;

import com.freetsinghua.tool.anotation.NotNull;

import java.util.concurrent.Executor;

/**
 * 任务执行器
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public interface TaskExecutor extends Executor {

    /**
     * 指定给定的任务{@code task}
     *
     * @param task 要执行的任务
     * @throws TaskRejectedException 如果任务被拒绝执行
     */
    @Override
    void execute(@NotNull Runnable task);
}
