package com.freetsinghua.tool.task;

import java.util.concurrent.RejectedExecutionException;

/**
 * 任务被拒绝异常
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public class TaskRejectedException extends RejectedExecutionException {

    public TaskRejectedException(String message) {
        super(message);
    }

    public TaskRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
