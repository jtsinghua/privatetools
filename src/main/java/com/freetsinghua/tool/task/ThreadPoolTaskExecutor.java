package com.freetsinghua.tool.task;

import com.google.common.collect.Queues;

import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.util.ConcurrentReferenceHashMap;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author z.tsinghua
 * @date 2019/1/30
 */
public class ThreadPoolTaskExecutor extends ExecutorConfigurationSupport {
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
    private final Map<Runnable, Object> decoratedTaskMap = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    @Override
    protected ExecutorService initializeExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        return null;
    }

    protected BlockingDeque<Runnable> createBlockingDeque(int queueCapacity) {
        if (queueCapacity > 0) {
            return new LinkedBlockingDeque<>(queueCapacity);
        } else {
            //TODO:
            return null;
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
}
