package com.freetsinghua.tool.task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.enterprise.concurrent.ManagedExecutors;
import javax.enterprise.concurrent.ManagedTask;

import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.util.ClassUtils;

/**
 * @author z.tsinghua
 * @date 2019/1/28
 */
public class ConcurrentTaskExecutor implements AsyncListenableTaskExecutor, SchedulingTaskExecutor {

	@Nullable
	private static Class<?> managedExecutorServiceClass;

	static {
		try {
			managedExecutorServiceClass = ClassUtils.forName("javax.enterprise.concurrent.ManagedExecutorService",
					ConcurrentTaskExecutor.class.getClassLoader());
		} catch (ClassNotFoundException ex) {
			//
			managedExecutorServiceClass = null;
		}
	}

	private Executor currentExecutor;
	private TaskExecutorAdapter adapterExecutor;

	public ConcurrentTaskExecutor() {
		this.currentExecutor = Executors.newSingleThreadExecutor();
		this.adapterExecutor = new TaskExecutorAdapter(this.currentExecutor);
	}

	public ConcurrentTaskExecutor(Executor executor) {
		this.currentExecutor = executor == null ? Executors.newSingleThreadExecutor() : executor;
		this.adapterExecutor = new TaskExecutorAdapter(this.currentExecutor);
	}

	public void setCurrentExecutor(@Nullable Executor executor) {
		this.currentExecutor = executor == null ? Executors.newSingleThreadExecutor() : executor;
		this.adapterExecutor = getAdapterExecutor(this.currentExecutor);
	}

	private TaskExecutorAdapter getAdapterExecutor(Executor executor) {
		if (managedExecutorServiceClass != null && managedExecutorServiceClass.isInstance(executor)) {
			return new ManagedTaskExecutorAdapter(executor);
		}

		return new TaskExecutorAdapter(executor);
	}

	@Override
	public ListenableFuture<?> submitListenable(Runnable task) {
		return this.adapterExecutor.submitListenable(task);
	}

	@Override
	public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
		return this.adapterExecutor.submitListenable(task);
	}

	@Override
	public void execute(Runnable task, long startTime) {
		this.adapterExecutor.execute(ManagedTaskBuilder.buildManagedTask(task, task.toString()), startTime);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return this.adapterExecutor.submit(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return this.adapterExecutor.submit(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
	}

	@Override
	public void execute(Runnable task) {
		this.adapterExecutor.execute(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
	}

	@Override
	public boolean prefersShortLivedTasks() {
		return false;
	}

	private static class ManagedTaskExecutorAdapter extends TaskExecutorAdapter {

		public ManagedTaskExecutorAdapter(Executor concurrentExecutor) {
			super(concurrentExecutor);
		}

		@Override
		public void execute(Runnable task) {
			super.execute(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}

		@Override
		public Future<?> submit(Runnable task) {
			return super.submit(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}

		@Override
		public <T> Future<T> submit(Callable<T> task) {
			return super.submit(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}

		@Override
		public ListenableFuture<?> submitListenable(Runnable task) {
			return super.submitListenable(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}

		@Override
		public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
			return super.submitListenable(ManagedTaskBuilder.buildManagedTask(task, task.toString()));
		}
	}

	protected static class ManagedTaskBuilder {

		public static Runnable buildManagedTask(Runnable task, String identityName) {
			Map<String, String> properties;
			if (task instanceof SchedulingAwareRunnable) {
				properties = new HashMap<>(4);
				properties.put(ManagedTask.LONGRUNNING_HINT,
						Boolean.toString(((SchedulingAwareRunnable) task).isLongLived()));
			} else {
				properties = new HashMap<>(2);
			}
			properties.put(ManagedTask.IDENTITY_NAME, identityName);
			return ManagedExecutors.managedTask(task, properties, null);
		}

		public static <T> Callable<T> buildManagedTask(Callable<T> task, String identityName) {
			Map<String, String> properties = new HashMap<>(2);
			properties.put(ManagedTask.IDENTITY_NAME, identityName);
			return ManagedExecutors.managedTask(task, properties, null);
		}
	}
}
