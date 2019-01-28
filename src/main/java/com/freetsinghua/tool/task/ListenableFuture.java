package com.freetsinghua.tool.task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 可监听的{@link Future}
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public interface ListenableFuture<T> extends Future<T> {

    /** 注册给定的{@link ListenableFutureCallback} */
    void addCallback(ListenableFutureCallback<? super T> callback);

    /** 注册给定的回调 */
    void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback);

    /** 暴露{@link ListenableFuture} */
    default CompletableFuture<T> completable() {
        CompletableFuture<T> completableFuture = new DelegatingCompletableFuture<>(this);
        addCallback(completableFuture::complete, completableFuture::completeExceptionally);
        return completableFuture;
    }
}
