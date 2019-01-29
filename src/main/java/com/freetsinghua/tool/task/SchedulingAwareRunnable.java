package com.freetsinghua.tool.task;

/**
 * @author z.tsinghua
 * @date 2019/1/29
 */
public interface SchedulingAwareRunnable extends Runnable {
    boolean isLongLived();
}
