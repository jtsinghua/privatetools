package com.freetsinghua.tool.cache;

/**
 * 时期事件监听器
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public abstract class ExpireEventListener<T> implements EventListener<T> {
    @Override
    public void onEvent(T obj) {
        onExpire(obj);
    }

    /**
     * 当监听到有过期事件被触发时，最终会调用这个方法
     *
     * @param obj 过期对象
     */
    public abstract void onExpire(T obj);
}
