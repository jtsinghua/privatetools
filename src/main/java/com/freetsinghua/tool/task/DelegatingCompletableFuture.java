package com.freetsinghua.tool.task;

import com.freetsinghua.tool.util.Assert;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author z.tsinghua
 * @date 2019/1/28
 */
public class DelegatingCompletableFuture<T> extends CompletableFuture<T> {

    private final Future<T> delegate;

    public DelegatingCompletableFuture(Future<T> delegate) {
        Assert.notNull(delegate, "Delegate must not be null");
        this.delegate = delegate;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean result = this.delegate.cancel(mayInterruptIfRunning);
        super.cancel(mayInterruptIfRunning);
        return result;
    }
}
