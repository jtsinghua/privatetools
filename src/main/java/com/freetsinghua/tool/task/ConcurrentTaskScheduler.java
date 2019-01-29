package com.freetsinghua.tool.task;

import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.util.Assert;
import com.freetsinghua.tool.util.ClassUtils;

import javax.enterprise.concurrent.LastExecution;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author z.tsinghua
 * @date 2019/1/29
 */
public class ConcurrentTaskScheduler extends ConcurrentTaskExecutor implements TaskScheduler {
	@Nullable
	private static Class<?> managedScheduledExecutorServiceClass;

	static {
		try {
			managedScheduledExecutorServiceClass = ClassUtils.forName(
					"javax.enterprise.concurrent.ManagedScheduledExecutorService",
					ConcurrentTaskScheduler.class.getClassLoader());
		} catch (ClassNotFoundException ex) {
			//
			managedScheduledExecutorServiceClass = null;
		}
	}


	private ScheduledExecutorService scheduledExecutor;
	private ErrorHandler errorHandler;
	private boolean enterpriseConcurrentScheduler = false;

	public ConcurrentTaskScheduler() {
		super();
		this.scheduledExecutor = initScheduledExecutor(null);
	}

	public ConcurrentTaskScheduler(ScheduledExecutorService scheduledExecutor) {
		super(scheduledExecutor);
		this.scheduledExecutor = initScheduledExecutor(scheduledExecutor);
	}

	public ConcurrentTaskScheduler(Executor executor, ScheduledExecutorService scheduledExecutor) {
		super(executor);
		this.scheduledExecutor = initScheduledExecutor(scheduledExecutor);
	}

	private ScheduledExecutorService initScheduledExecutor(@Nullable ScheduledExecutorService scheduledExecutor) {
		if (scheduledExecutor != null) {
			this.scheduledExecutor = scheduledExecutor;
			this.enterpriseConcurrentScheduler = (managedScheduledExecutorServiceClass != null)
					&& managedScheduledExecutorServiceClass.isInstance(scheduledExecutor);
		} else {
			this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
			this.enterpriseConcurrentScheduler = false;
		}
		return this.scheduledExecutor;
	}

	public void setScheduledExecutor(@Nullable ScheduledExecutorService scheduledExecutor) {
		this.scheduledExecutor = scheduledExecutor;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		Assert.notNull(errorHandler, "'errorHandler' must not be null");
		this.errorHandler = errorHandler;
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
		return null;
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
		return null;
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
		return null;
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
		return null;
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
		return null;
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
		return null;
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
		return null;
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
		return null;
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
		return null;
	}

	private class EnterpriseConcurrentTriggerScheduler {
		public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
			ManagedScheduledExecutorService executor = (ManagedScheduledExecutorService) scheduledExecutor;
			return executor.schedule(task, new javax.enterprise.concurrent.Trigger() {
				@Override
				public Date getNextRunTime(LastExecution lastExecutionInfo, Date taskScheduledTime) {
					return trigger.nextExecutionTime(lastExecutionInfo != null
							? new SimpleTriggerContext(lastExecutionInfo.getScheduledStart(),
									lastExecutionInfo.getRunStart(), lastExecutionInfo.getRunEnd())
							: new SimpleTriggerContext());
				}

				@Override
				public boolean skipRun(LastExecution lastExecutionInfo, Date scheduledRunTime) {
					return false;
				}
			});
		}
	}
}
