package com.freetsinghua.tool.task;

import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.util.Assert;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 管理{@link SuccessCallback} 和 {@link FailureCallback}
 *
 * @author z.tsinghua
 * @date 2019/1/29
 */
public class ListenableFutureCallbackRegistry<T> {
    private final Queue<SuccessCallback<? super T>> successCallbacks = new LinkedList<>();
    private final Queue<FailureCallback> failureCallbacks = new LinkedList<>();

    private State state = State.NEW;

    @Nullable private Object result;

    private final Object mutex = new Object();

    public void addCallback(ListenableFutureCallback<? super T> callback) {
        Assert.notNull(callback, "'callback' must not be null");

        synchronized (this.mutex) {
            switch (state) {
                case NEW:
                    {
                        this.successCallbacks.add(callback);
                        this.failureCallbacks.add(callback);
                        break;
                    }
                case SUCCESS:
                    {
                        notifySuccess(callback);
                        break;
                    }
                case FAILURE:
                    {
                        notifyFailure(callback);
                        break;
                    }
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    private void notifyFailure(FailureCallback callback) {
        Assert.isTrue(
                this.result instanceof Throwable, "no Throwable result set for failure state");
        try {
            callback.onFailure((Throwable) this.result);
        } catch (Throwable ex) {
            // 忽略
        }
    }

    @SuppressWarnings("unchecked")
    private void notifySuccess(SuccessCallback<? super T> callback) {
        try {
            callback.onSuccess((T) this.result);
        } catch (Throwable ex) {
            // 忽略
        }
    }

    public void addFailureCallback(FailureCallback failureCallback) {
        Assert.notNull(failureCallback, "'failureCallback' must not be null");
        synchronized (this.mutex) {
            switch (this.state) {
                case NEW:
                    {
                        this.failureCallbacks.add(failureCallback);
                        break;
                    }
                case FAILURE:
                    {
                        notifyFailure(failureCallback);
                        break;
                    }
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    public void addSuccessCallback(SuccessCallback<? super T> successCallback) {
        Assert.notNull(successCallback, "'successCallback' must not be null");
        synchronized (this.mutex) {
            switch (this.state) {
                case NEW:
                    {
                        this.successCallbacks.add(successCallback);
                        break;
                    }
                case SUCCESS:
                    {
                        notifySuccess(successCallback);
                        break;
                    }
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    public void success(@Nullable T result) {
        synchronized (this.mutex) {
            this.state = State.SUCCESS;
            this.result = result;
            SuccessCallback<? super T> callback;
            while ((callback = successCallbacks.poll()) != null) {
                notifySuccess(callback);
            }
        }
    }

    public void failure(Throwable thx) {
        synchronized (this.mutex) {
            this.state = State.FAILURE;
            this.result = thx;
            FailureCallback callback;
            while ((callback = failureCallbacks.poll()) != null) {
                notifyFailure(callback);
            }
        }
    }

    private enum State {
        NEW,
        SUCCESS,
        FAILURE
    }
}
