package com.freetsinghua.tool.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freetsinghua.tool.anotation.Nullable;

/**
 * @author z.tsinghua
 * @date 2019/1/30
 */
public abstract class ExecutorConfigurationSupport extends CustomizableThreadFactory {
	protected Logger log = LoggerFactory.getLogger(getClass());
	private ThreadFactory threadFactory = this;
	private boolean setThreadNamePrefix = false;
	private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
	private boolean awaitForTaskCompleteOnShutdown = false;
	private int awaitTerminationSecond = 0;
	private ExecutorService executor;

	public void setThreadFactory(@Nullable ThreadFactory threadFactory) {
		this.threadFactory = threadFactory == null ? this : threadFactory;
	}

	@Override
	public void setThreadNamePrefix(@Nullable String threadNamePrefix) {
		super.setThreadNamePrefix(threadNamePrefix);
		this.setThreadNamePrefix = true;
	}

	public void setRejectedExecutionHandler(@Nullable RejectedExecutionHandler rejectedExecutionHandler) {
		this.rejectedExecutionHandler = rejectedExecutionHandler == null ? new ThreadPoolExecutor.AbortPolicy()
				: rejectedExecutionHandler;
	}

	public void setAwaitForTaskCompleteOnShutdown(boolean awaitForTaskCompleteOnShutdown) {
		this.awaitForTaskCompleteOnShutdown = awaitForTaskCompleteOnShutdown;
	}

	public void setAwaitTerminationSecond(int awaitTerminationSecond) {
		this.awaitTerminationSecond = awaitTerminationSecond;
	}

	public void afterPropertiesSet() {
		initialize();
	}

	private void initialize() {
		if (log.isInfoEnabled()) {
			log.info("Initializing ExecutorService ");
		}

		if (!setThreadNamePrefix) {
			setThreadNamePrefix(getDefaultThreadNamePrefix());
		}

		this.executor = initializeExecutor(this.threadFactory, this.rejectedExecutionHandler);
	}

	public void shutdown() {
		if (log.isInfoEnabled()) {
			log.info("Shutting down ExecutorService");
		}

		if (this.executor != null) {
			if (this.awaitForTaskCompleteOnShutdown) {
				this.executor.shutdown();
			} else {
				for (Runnable runnable : this.executor.shutdownNow()) {
					if (runnable instanceof Future) {
						((Future) runnable).cancel(true);
					}
				}
			}
			awaitTerminationIfNecessary(this.executor);
		}
	}

	private void awaitTerminationIfNecessary(ExecutorService executorService) {
		if (this.awaitTerminationSecond > 0) {
			try {
				if (!executorService.awaitTermination(this.awaitTerminationSecond, TimeUnit.SECONDS)) {
					if (log.isWarnEnabled()) {
						log.warn("Timed out when awaiting for executor to terminate");
					}
				}
			} catch (InterruptedException e) {
				if (log.isWarnEnabled()) {
					log.warn("Interrupted when awaiting for executor to terminate");
				}

				Thread.currentThread().interrupt();
			}
		}
	}

	protected void cancelRemainingTask(Runnable task) {
		if (task instanceof Future) {
			((Future) task).cancel(true);
		}
	}

	/**
	 * create the target {@link ExecutorService} instance. It called by {@link #initialize()}
	 *
	 * @param threadFactory            线程工厂
	 * @param rejectedExecutionHandler 拒绝策略
	 * @return 返回结果
	 */
	protected abstract ExecutorService initializeExecutor(ThreadFactory threadFactory,
			RejectedExecutionHandler rejectedExecutionHandler);
}
