package com.freetsinghua.tool.task;

/**
 * @author z.tsinghua
 * @date 2019/1/28
 */
public interface SchedulingTaskExecutor extends AsyncTaskExecutor {
    default boolean prefersShortLivedTasks() {
        return true;
    }
}
