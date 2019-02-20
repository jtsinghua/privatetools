package com.freetsinghua.tool.util;

import java.util.concurrent.TimeUnit;

/**
 * 线程睡眠处理工具
 *
 * @author z.tsinghua
 * @date 2019/2/13
 */
public class ThreadUtils {

    /**
     * 捕捉并处理{@link InterruptedException}
     *
     * @param sleepTimeMillis 睡眠时间，单位毫秒
     */
    public static void sleep(long sleepTimeMillis) {
        try {
            Thread.sleep(sleepTimeMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 捕捉并处理{@link InterruptedException}
     *
     * @param sleepTime 睡眠时间
     * @param timeUnit 单位
     */
    public static void sleep(long sleepTime, TimeUnit timeUnit) {
        try {
            Thread.sleep(timeUnit.toMillis(sleepTime));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
