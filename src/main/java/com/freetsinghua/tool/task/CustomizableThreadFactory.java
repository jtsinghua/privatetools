package com.freetsinghua.tool.task;

import java.util.concurrent.ThreadFactory;

/**
 * 可扩展线程工厂
 * @author z.tsinghua
 * @date 2019/1/30
 */
public class CustomizableThreadFactory extends CustomizableThreadCreator implements ThreadFactory {

	public CustomizableThreadFactory() {
		super();
	}

	public CustomizableThreadFactory(String threadNamePrefix) {
		super(threadNamePrefix);
	}

	@Override
	public Thread newThread(Runnable runnable) {
		return createThread(runnable);
	}
}
