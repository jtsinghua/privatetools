package com.freetsinghua.tool.cache;

import com.freetsinghua.tool.anotation.NotNull;
import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.common.CommonConstant;
import com.freetsinghua.tool.util.ThreadPool;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 简单缓存
 * <li>若是需要监听过期事件，或者驱逐事件，则需要传入监听器
 * <li>若是需要设置过期时间，则调用相应的构造器，或者方法
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public class SimpleCache<K, V> implements Cache<K, V> {

    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>(CommonConstant.K);
    private EventListener<V> mEventListener;
    private long expireTime;

    public SimpleCache() {
        mEventListener = null;
        expireTime = 0L;
    }

    public SimpleCache(EventListener<V> mEventListener) {
        this.mEventListener = mEventListener;
        this.expireTime = 0L;
    }

    public SimpleCache(EventListener<V> mEventListener, long expireTime) {
        this.mEventListener = mEventListener;
        this.expireTime = expireTime;
    }

    public void put(@NotNull K key, @NotNull V value, long expireTime) {
        cache.put(key, value);
        if (mEventListener != null) {
            ThreadPool.getInstance()
                    .scheduleWithFixedDelay(
                            () -> {
                                if (cache.containsKey(key)) {
                                    cache.remove(key);
                                    mEventListener.onEvent(value);
                                }
                            },
                            expireTime);
        }
    }

    @Override
    public void put(@NotNull K key, @NotNull V value) {
        cache.put(key, value);
        if (mEventListener != null) {
            ThreadPool.getInstance()
                    .scheduleWithFixedDelay(
                            () -> {
                                if (cache.containsKey(key)) {
                                    cache.remove(key);
                                    mEventListener.onEvent(value);
                                }
                            },
                            this.expireTime);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {}

    public V get(K key) {
        return cache.get(key);
    }

    public V getOrDefault(K key, V def) {
        return cache.getOrDefault(key, def);
    }

    @Override
    @Nullable
    public V getIfPresent(K key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        return null;
    }

    @Override
    @Nullable
    public V get(K key, Callable<? extends V> loader) {

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        if (loader != null) {
            try {
                return loader.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public Map<K, V> getAllPresent(Iterable<K> keys) {
        Map<K, V> map = new HashMap<>();

        keys.forEach(
                key -> {
                    V value = getIfPresent(key);
                    if (value != null) {
                        map.put(key, value);
                    }
                });

        return map;
    }

    @Override
    public void invalidate(K key) {
        cache.remove(key);
    }

    @Override
    public void invalidateAll(Iterable<K> keys) {
        keys.forEach(cache::remove);
    }

    @Override
    public void invalidateAll() {
        cache.clear();
    }

    @Override
    public long size() {
        return cache.size();
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        ConcurrentMap<K, V> map = new ConcurrentHashMap<>();
        for (Map.Entry<K, V> entry : cache.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }
}
