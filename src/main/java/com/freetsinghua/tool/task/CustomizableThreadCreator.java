package com.freetsinghua.tool.task;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import com.freetsinghua.tool.anotation.Nullable;

/**
 * @author z.tsinghua
 * @date 2019/1/30
 */
public class CustomizableThreadCreator implements Serializable {
	private String threadNamePrefix;
	private int threadPriority = Thread.NORM_PRIORITY;
	private boolean daemon;
	@Nullable
	private ThreadGroup threadGroup;
	private final AtomicLong threadCount = new AtomicLong(0L);

	public CustomizableThreadCreator() {
		this.threadNamePrefix = getDefaultThreadNamePrefix();
	}

	public CustomizableThreadCreator(@Nullable String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix == null ? getDefaultThreadNamePrefix() : threadNamePrefix;
	}

	public void setThreadNamePrefix(@Nullable String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix == null ? getDefaultThreadNamePrefix() : threadNamePrefix;
	}

	public String getThreadNamePrefix() {
		return threadNamePrefix;
	}

	public int getThreadPriority() {
		return threadPriority;
	}

	public void setThreadPriority(int threadPriority) {
		this.threadPriority = threadPriority;
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public boolean isDaemon() {
		return this.daemon;
	}

	public void setThreadGroupName(String groupName) {
		this.threadGroup = new ThreadGroup(groupName);
	}

	@Nullable
	public ThreadGroup getThreadGroup() {
		return threadGroup;
	}

	public void setThreadGroup(ThreadGroup threadGroup) {
		this.threadGroup = threadGroup;
	}

	protected String getNextThreadName() {
		return getThreadNamePrefix() + this.threadCount.incrementAndGet();
	}

	public Thread createThread(Runnable runnable) {
		Thread thread = new Thread(getThreadGroup(), runnable, getNextThreadName());
		thread.setPriority(getThreadPriority());
		thread.setDaemon(isDaemon());

		return thread;
	}

	protected String getDefaultThreadNamePrefix() {
		return getClass().getSimpleName() + "-";
	}
}
