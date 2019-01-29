package com.freetsinghua.tool.task;

import com.freetsinghua.tool.anotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author z.tsinghua
 * @date 2019/1/29
 */
public class ListenableFutureTask<T> extends FutureTask<T> implements ListenableFuture<T> {
    private final ListenableFutureCallbackRegistry<T> registry =
            new ListenableFutureCallbackRegistry<>();

    public ListenableFutureTask(Callable<T> callable) {
        super(callable);
    }

    public ListenableFutureTask(Runnable runnable, @Nullable T result) {
        super(runnable, result);
    }

    @Override
    public void addCallback(ListenableFutureCallback<? super T> callback) {
        this.registry.addCallback(callback);
    }

    @Override
    public void addCallback(
            SuccessCallback<? super T> successCallback, FailureCallback failureCallback) {
        this.registry.addSuccessCallback(successCallback);
        this.registry.addFailureCallback(failureCallback);
    }

    @Override
    public CompletableFuture<T> completable() {
        CompletableFuture<T> completableFuture = new DelegatingCompletableFuture<>(this);
        this.registry.addSuccessCallback(completableFuture::complete);
        this.registry.addFailureCallback(completableFuture::completeExceptionally);
        return completableFuture;
    }

    @Override
    protected void done() {
        Throwable cause;

        try {
            T result = get();
            this.registry.success(result);
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        } catch (ExecutionException e) {
            cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
        } catch (Throwable ex) {
            cause = ex;
        }

        this.registry.failure(cause);
    }
}
