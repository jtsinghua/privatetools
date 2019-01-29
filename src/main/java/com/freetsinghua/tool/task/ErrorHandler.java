package com.freetsinghua.tool.task;

/**
 * @author z.tsinghua
 * @date 2019/1/29
 */
@FunctionalInterface
public interface ErrorHandler {

    /**
     * Handle the given error, possibly rethrowing it as a fatal exception.
     */
    void handleError(Throwable t);

}
