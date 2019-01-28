package com.freetsinghua.tool.task;

/**
 * 执行成功回调
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
@FunctionalInterface
public interface SuccessCallback<T> {

    /**
     * 当{@link ListenableFuture}执行成功时调用
     *
     * @param obj 执行结果
     */
    void onSuccess(T obj);
}
