package com.freetsinghua.tool.task;

import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.*;

import javax.enterprise.concurrent.LastExecution;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;

import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.util.Assert;
import com.freetsinghua.tool.util.ClassUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author z.tsinghua
 * @date 2019/1/29
 */
public class ConcurrentTaskScheduler extends ConcurrentTaskExecutor implements TaskScheduler {
	private static final ErrorHandler LOG_AND_SUPPRESS_ERROR_HANDLER = new LoggingErrorHandler();
	private static final ErrorHandler LOG_AND_PROPAGATE_ERROR_HANDLER = new PropagatingErrorHandler();

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
	    try {
            if (this.enterpriseConcurrentScheduler){
                return new EnterpriseConcurrentTriggerScheduler().schedule(decorateTask(task, true), trigger);
            }else{
                ErrorHandler handler = this.errorHandler != null ? this.errorHandler : getDefaultErrorHandler(true);
                //TODO:
            }
        }catch (RejectedExecutionException ex){

        }
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

	private Runnable decorateTask(Runnable task, boolean isRepeatingTask) {
		Runnable result = decorateTaskWithErrorHandler(task, this.errorHandler, isRepeatingTask);
		if (this.enterpriseConcurrentScheduler) {
			result = ManagedTaskBuilder.buildManagedTask(result, task.toString());
		}
		return result;
	}

	private static DelegatingErrorHandlingRunnable decorateTaskWithErrorHandler(Runnable task,
			@Nullable ErrorHandler errorHandler, boolean isRepeatingTask) {

		if (task instanceof DelegatingErrorHandlingRunnable) {
			return (DelegatingErrorHandlingRunnable) task;
		}
		ErrorHandler eh = (errorHandler != null ? errorHandler : getDefaultErrorHandler(isRepeatingTask));
		return new DelegatingErrorHandlingRunnable(task, eh);
	}

	/**
	 * Return the default {@link ErrorHandler} implementation based on the boolean
	 * value indicating whether the task will be repeating or not. For repeating tasks
	 * it will suppress errors, but for one-time tasks it will propagate. In both
	 * cases, the error will be logged.
	 */
	public static ErrorHandler getDefaultErrorHandler(boolean isRepeatingTask) {
		return (isRepeatingTask ? LOG_AND_SUPPRESS_ERROR_HANDLER : LOG_AND_PROPAGATE_ERROR_HANDLER);
	}


	/**
	 * An {@link ErrorHandler} implementation that logs the Throwable at error
	 * level. It does not perform any additional error handling. This can be
	 * useful when suppression of errors is the intended behavior.
	 */
	@Slf4j
	private static class LoggingErrorHandler implements ErrorHandler {
		@Override
		public void handleError(Throwable t) {
			if (log.isErrorEnabled()) {
				log.error("Unexpected error occurred in scheduled task.", t);
			}
		}
	}


	/**
	 * An {@link ErrorHandler} implementation that logs the Throwable at error
	 * level and then propagates it.
	 */
	private static class PropagatingErrorHandler extends LoggingErrorHandler {

		@Override
		public void handleError(Throwable t) {
			super.handleError(t);
			rethrowRuntimeException(t);
		}

		private static void rethrowRuntimeException(Throwable ex) {
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			if (ex instanceof Error) {
				throw (Error) ex;
			}
			throw new UndeclaredThrowableException(ex);
		}
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
