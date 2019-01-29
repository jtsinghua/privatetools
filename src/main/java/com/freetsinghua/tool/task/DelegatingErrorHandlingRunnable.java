package com.freetsinghua.tool.task;

import java.lang.reflect.UndeclaredThrowableException;

import com.freetsinghua.tool.util.Assert;

/**
 * @author z.tsinghua
 * @date 2019/1/29
 */
public class DelegatingErrorHandlingRunnable implements Runnable {

	private Runnable delegate;
	private ErrorHandler errorHandler;

	public DelegatingErrorHandlingRunnable(Runnable delegate, ErrorHandler errorHandler) {
		Assert.notNull(delegate, "Delegate must not be null");
		Assert.notNull(errorHandler, "ErrorHandler must not be null");
		this.delegate = delegate;
		this.errorHandler = errorHandler;
	}

	@Override
	public void run() {
		try {
			this.delegate.run();
		} catch (UndeclaredThrowableException e) {
			this.errorHandler.handleError(e.getUndeclaredThrowable());
		} catch (Throwable e) {
			this.errorHandler.handleError(e);
		}
	}

	@Override
	public String toString() {
		return "DelegatingErrorHandlingRunnable for " + this.delegate;
	}
}
