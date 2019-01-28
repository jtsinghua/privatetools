package com.freetsinghua.tool.task;

/**
 * 执行失败回调
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
@FunctionalInterface
public interface FailureCallback {
    /**
     * 当{@link ListenableFuture}执行失败时回调
     *
     * @param thr 失败异常
     */
    void onFailure(Throwable thr);
}
