package com.freetsinghua.tool.task;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import com.freetsinghua.tool.anotation.Nullable;

/**
 * @author z.tsinghua
 * @date 2019/1/29
 */
public interface TaskScheduler {

	@Nullable
	ScheduledFuture<?> schedule(Runnable task, Trigger trigger);

	ScheduledFuture<?> schedule(Runnable task, Date startTime);

	default ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
		return schedule(task, Date.from(startTime));
	}


	ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period);

	default ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
		return scheduleAtFixedRate(task, period.toMillis());
	}

	/**
	 * 安排给定的{@link Runnable}，尽快开始，并在给定的时间段内调用它。
	 *
	 * @param task 任务
	 * @param period 连续执行任务之间的间隔
	 * @return 表示待完成任务
	 */
	ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period);

	ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay);

	default ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
		return scheduleWithFixedDelay(task, delay.toMillis());
	}

	/**
	 * 安排给定的{@link Runnable}，在指定的执行时间调用它，然后在完成一次执行和下一次执行的开始之间给定延迟。
	 *
	 * @param task 指定任务
	 * @param delay 延迟时间
	 * @return 表示待完成的任务
	 */
	ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay);
}
