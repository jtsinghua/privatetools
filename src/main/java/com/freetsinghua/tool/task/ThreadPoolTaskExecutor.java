package com.freetsinghua.tool.task;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.util.Assert;
import com.freetsinghua.tool.util.ConcurrentReferenceHashMap;

/**
 *
 *
 * @author z.tsinghua
 * @date 2019/1/30
 */
public class ThreadPoolTaskExecutor extends ExecutorConfigurationSupport
		implements AsyncListenableTaskExecutor, SchedulingTaskExecutor {
	private final Object poolSizeMonitor = new Object();
	private int corePoolSize;
	private int maxPoolSize = Integer.MAX_VALUE;
	private int keepAliveSeconds = 60;
	private int queueCapacity = Integer.MAX_VALUE;
	private boolean allowCoreThreadTimeout = false;
	@Nullable
	private TaskDecorator taskDecorator;
	@Nullable
	private ThreadPoolExecutor threadPoolExecutor;
	private final Map<Runnable, Object> decoratedTaskMap = new ConcurrentReferenceHashMap<>(16,
			ConcurrentReferenceHashMap.ReferenceType.WEAK);


	/**
	 * 返回底层的ThreadPoolExecutor
	 * @return 返回结果
	 */
	public ThreadPoolExecutor getThreadPoolExecutor() {
		Assert.state(this.threadPoolExecutor != null, "ThreadPoolExecutor not initialized");
		return this.threadPoolExecutor;
	}

	public int getPoolSize() {
		if (this.threadPoolExecutor == null) {
			return this.corePoolSize;
		}

		return this.threadPoolExecutor.getPoolSize();
	}


	public int getActiveCount() {
		if (this.threadPoolExecutor == null) {
			return 0;
		}

		return this.threadPoolExecutor.getActiveCount();
	}

	@Override
	protected ExecutorService initializeExecutor(ThreadFactory threadFactory,
			RejectedExecutionHandler rejectedExecutionHandler) {

		BlockingQueue<Runnable> queue = createQueue(this.queueCapacity);
		ThreadPoolExecutor executor;
		if (this.taskDecorator != null) {
			executor = new ThreadPoolExecutor(this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds,
					TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler) {
				@Override
				public void execute(Runnable command) {
					Runnable decorate = taskDecorator.decorate(command);
					if (decorate != command) {
						decoratedTaskMap.put(decorate, command);
					}
					super.execute(command);
				}
			};
		} else {
			executor = new ThreadPoolExecutor(this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds,
					TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler);
		}

		if (this.allowCoreThreadTimeout) {
			executor.allowCoreThreadTimeOut(true);
		}

		this.threadPoolExecutor = executor;
		return executor;
	}

	private BlockingQueue<Runnable> createQueue(int queueCapacity) {
		if (queueCapacity > 0) {
			return new LinkedBlockingDeque<>(queueCapacity);
		} else {
			return new SynchronousQueue<>();
		}
	}

	public void setTaskDecorator(TaskDecorator taskDecorator) {
		this.taskDecorator = taskDecorator;
	}

	public void setAllowCoreThreadTimeout(boolean allowCoreThreadTimeout) {
		this.allowCoreThreadTimeout = allowCoreThreadTimeout;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public int getKeepAliveSeconds() {
		synchronized (poolSizeMonitor) {
			return keepAliveSeconds;
		}
	}

	public void setKeepAliveSeconds(int keepAliveSeconds) {
		synchronized (poolSizeMonitor) {
			this.keepAliveSeconds = keepAliveSeconds;
			if (this.threadPoolExecutor != null) {
				this.threadPoolExecutor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
			}
		}
	}

	public void setCorePoolSize(int corePoolSize) {
		synchronized (poolSizeMonitor) {
			this.corePoolSize = corePoolSize;
			if (this.threadPoolExecutor != null) {
				this.threadPoolExecutor.setCorePoolSize(corePoolSize);
			}
		}
	}

	public int getCorePoolSize() {
		synchronized (poolSizeMonitor) {
			return corePoolSize;
		}
	}

	public int getMaxPoolSize() {
		synchronized (poolSizeMonitor) {
			return maxPoolSize;
		}
	}

	public void setMaxPoolSize(int maxPoolSize) {
		synchronized (poolSizeMonitor) {
			this.maxPoolSize = maxPoolSize;
			if (this.threadPoolExecutor != null) {
				this.threadPoolExecutor.setMaximumPoolSize(maxPoolSize);
			}
		}
	}

	/**
	 * 提交一个{@link Runnable}，返回{@link ListenableFuture}，如果执行成功，则返回{@code null}
	 *
	 * @param task 任务
	 * @return 返回结果
	 */
	@Override
	public ListenableFuture<?> submitListenable(Runnable task) {
		ThreadPoolExecutor executor = getThreadPoolExecutor();
		try {
			ListenableFutureTask<Object> future = new ListenableFutureTask<>(task, null);
			executor.execute(future);
			return future;
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	/**
	 * 提交一个{@link Callable},返回{@link ListenableFuture},如果执行成功，则返回null
	 *
	 * @param task 任务
	 * @return 返回结果
	 */
	@Override
	public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
		ThreadPoolExecutor executor = getThreadPoolExecutor();
		try {
			ListenableFutureTask<T> future = new ListenableFutureTask<>(task);
			executor.execute(future);
			return future;
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	/**
	 * 指定给定的任务{@code task}
	 *
	 * @param task      要执行的任务
	 * @param startTime 推迟多少时间执行
	 */
	@Override
	public void execute(Runnable task, long startTime) {
		execute(task);
	}

	/**
	 * 提交一个任务执行，并接收一个{@link Future}，如果任务执行成功，{@link Future}为{@code null}
	 *
	 * @param task 任务
	 * @return 返回执行任务结果
	 */
	@Override
	public Future<?> submit(Runnable task) {
		Object invoke = new RunnableOrCallable<>(null, task).invoke();
		return getFuture(invoke);
	}

	/**
	 * 提交一个任务执行，返回执行结果
	 *
	 * @param task 任务
	 * @return 返回结果
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> Future<T> submit(Callable<T> task) {
		Object invoke = new RunnableOrCallable<T>(task, null).invoke();
		return (Future<T>) getFuture(invoke);
	}

	@SuppressWarnings("unchecked")
	private Future<?> getFuture(@Nullable Object task) {
		Assert.notNull(task, "Task must not be null");
		ThreadPoolExecutor executor = getThreadPoolExecutor();
		try {
			if (task instanceof Callable) {
				Callable callable = (Callable) task;
				return executor.submit(callable);
			}

			if (task instanceof Runnable) {
				Runnable runnable = (Runnable) task;
				return executor.submit(runnable);
			}
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}

		return null;
	}

	/**
	 * 指定给定的任务{@code task}
	 *
	 * @param task 要执行的任务
	 * @throws TaskRejectedException 如果任务被拒绝执行
	 */
	@Override
	public void execute(Runnable task) {
		ThreadPoolExecutor executor = getThreadPoolExecutor();
		try {
			executor.execute(task);
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	/**
	 * 运行类型，是callable，还是runnable
	 */
	private enum ExecutionType {
		/**
		 * runnable
		 */
		EXECUTION_TYPE_RUNNABLE,
		/**
		 * callable
		 */
		EXECUTION_TYPE_CALLABLE
	}

	private class RunnableOrCallable<T> {
		private Callable<T> cTask;
		private Runnable rTask;

		RunnableOrCallable(Callable<T> cTask, Runnable rTask) {
			this.cTask = cTask;
			this.rTask = rTask;
		}

		@Nullable
		Object invoke() {
			if (this.cTask != null) {
				return cTask;
			}

			if (this.rTask != null) {
				return rTask;
			}

			return null;
		}
	}
}
