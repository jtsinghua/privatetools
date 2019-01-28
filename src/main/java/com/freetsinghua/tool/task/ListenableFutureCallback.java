package com.freetsinghua.tool.task;

/**
 * {@link ListenableFuture}回调
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public interface ListenableFutureCallback<T> extends SuccessCallback<T>, FailureCallback {}
