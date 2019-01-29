package com.freetsinghua.tool.task;

import java.util.Date;

import com.freetsinghua.tool.anotation.Nullable;

/**
 * Context对象封装给定任务的最后执行时间和最后完成时间。
 * @author z.tsinghua
 * @date 2019/1/29
 */
public interface TriggerContext {
	/**
	 * return the last schedule execution time or {@code null} if not schedule before
	 */
	@Nullable
	Date lastScheduledExecutionTime();

	/**
	 * return the last actual execution time or {@code null} if not schedule before
	 */
	@Nullable
	Date lastActualExecutionTime();

	/**
	 * return the last completion time or {@code null} if not schedule before
	 */
	@Nullable
	Date lastCompletionTime();
}
