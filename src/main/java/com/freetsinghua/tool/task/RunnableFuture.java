package com.freetsinghua.tool.task;

import java.util.concurrent.Future;

/**
 * @author z.tsinghua
 * @date 2019/1/28
 */
public interface RunnableFuture<V> extends Runnable, Future<V> {

    /**
     * 将此Future设置为其计算结果，除非它已被取消
     */
    void run();
}
