package com.freetsinghua.tool.cache;

/**
 * 事件监听器接口
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public interface EventListener<T> {
    /**
     * 当监听事件被触发时，会调用这个方法
     */
    void onEvent(T obj);
}
