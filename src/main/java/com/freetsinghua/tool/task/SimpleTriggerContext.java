package com.freetsinghua.tool.task;

import java.util.Date;

import com.freetsinghua.tool.anotation.Nullable;

import lombok.NoArgsConstructor;

/**
 * {@link TriggerContext}的简单实现类
 * @author z.tsinghua
 * @date 2019/1/29
 */
@NoArgsConstructor
public class SimpleTriggerContext implements TriggerContext {
	@Nullable
	private Date lastScheduleExecutionTime;
	@Nullable
	private Date lastActualExecutionTime;
	@Nullable
	private Date lastCompletionTime;


	public SimpleTriggerContext(Date lastScheduleExecutionTime, Date lastActualExecutionTime, Date lastCompletionTime) {
		this.lastScheduleExecutionTime = lastScheduleExecutionTime;
		this.lastActualExecutionTime = lastActualExecutionTime;
		this.lastCompletionTime = lastCompletionTime;
	}

	public void update(Date lastScheduleExecutionTime, Date lastActualExecutionTime, Date lastCompletionTime) {
		this.lastScheduleExecutionTime = lastScheduleExecutionTime;
		this.lastActualExecutionTime = lastActualExecutionTime;
		this.lastCompletionTime = lastCompletionTime;
	}

	@Override
	@Nullable
	public Date lastScheduledExecutionTime() {
		return lastScheduleExecutionTime;
	}

	@Override
	@Nullable
	public Date lastActualExecutionTime() {
		return lastActualExecutionTime;
	}

	@Override
	@Nullable
	public Date lastCompletionTime() {
		return lastCompletionTime;
	}
}
