package com.freetsinghua.tool.cache;

/**
 * 驱逐元素监听器
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public abstract class EvictEventListener<T> implements EventListener<T> {
    @Override
    public void onEvent(T obj) {
        onEvict(obj);
    }

    /**
     * 当将元素从缓存中移除，会调用这个方法
     *
     * @param obj 移除对象
     */
    public abstract void onEvict(T obj);
}
