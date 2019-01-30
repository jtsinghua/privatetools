package com.freetsinghua.tool.task;

import java.util.Date;
import java.util.concurrent.*;

import com.freetsinghua.tool.anotation.NotNull;
import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.util.Assert;

/**
 * 内部适配器，根据给定{@link Trigger}建议的下一个执行时间，将基础{@link Runnable}重新安排。
 * @author z.tsinghua
 * @date 2019/1/30
 */
public class ReschedulingRunnable extends DelegatingErrorHandlingRunnable implements ScheduledFuture<Object> {

	private final Trigger trigger;
	private final SimpleTriggerContext triggerContext = new SimpleTriggerContext();
	private final ScheduledExecutorService executor;
	@Nullable
	private ScheduledFuture<?> currentFuture;
	@Nullable
	private Date scheduledExecutionTime;
	private final Object triggerContextMonitor = new Object();


	public ReschedulingRunnable(Runnable delegate, Trigger trigger, ScheduledExecutorService executor,
			ErrorHandler errorHandler) {
		super(delegate, errorHandler);
		this.trigger = trigger;
		this.executor = executor;
	}

	@Nullable
	public ScheduledFuture<?> schedule() {
		synchronized (triggerContextMonitor) {
			this.scheduledExecutionTime = this.trigger.nextExecutionTime(this.triggerContext);
			if (this.scheduledExecutionTime == null) {
				return null;
			}

			long initialDelay = this.scheduledExecutionTime.getTime() - System.currentTimeMillis();
			this.currentFuture = this.executor.schedule(this, initialDelay, TimeUnit.MILLISECONDS);
			return this;
		}
	}

	private ScheduledFuture<?> obtainCurrentFuture() {
		Assert.state(this.currentFuture != null, "No scheduled future");
		return this.currentFuture;
	}

	@Override
	public void run() {
		Date actualExecutionTime = new Date();
		super.run();
		Date completionTime = new Date();

		synchronized (triggerContextMonitor) {
			Assert.state(this.currentFuture != null, "NO scheduled execution");
			this.triggerContext.update(this.scheduledExecutionTime, actualExecutionTime, completionTime);
			if (!obtainCurrentFuture().isCancelled()) {
				schedule();
			}
		}
	}

	@Override
	public long getDelay(@NotNull TimeUnit unit) {
		ScheduledFuture<?> curr;
		synchronized (triggerContextMonitor) {
			curr = obtainCurrentFuture();
		}
		return curr.getDelay(unit);
	}

	@Override
	public int compareTo(Delayed o) {
		if (this == o) {
			return 0;
		}
		long diff = this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS);
		return Long.compare(diff, 0L);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		synchronized (triggerContextMonitor) {
			return obtainCurrentFuture().cancel(mayInterruptIfRunning);
		}
	}

	@Override
	public boolean isCancelled() {
		synchronized (triggerContextMonitor) {
			return obtainCurrentFuture().isCancelled();
		}
	}

	@Override
	public boolean isDone() {
		synchronized (triggerContextMonitor) {
			return obtainCurrentFuture().isDone();
		}
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		ScheduledFuture<?> curr;
		synchronized (triggerContextMonitor) {
			curr = obtainCurrentFuture();
		}
		return curr.get();
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		ScheduledFuture<?> curr;
		synchronized (triggerContextMonitor) {
			curr = obtainCurrentFuture();
		}
		return curr.get(timeout, unit);
	}
}
