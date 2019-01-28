package com.freetsinghua.tool.cache;

import com.freetsinghua.tool.anotation.NotNull;
import com.freetsinghua.tool.anotation.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

/**
 * @author z.tsinghua
 * @date 2019/1/28
 */
public interface Cache<K, V> {

    /**
     * 返回和{@code key}关联的缓存值， 若是不存在，返回{@code null}
     *
     * @param key 键
     */
    @Nullable
    V getIfPresent(K key);

    /**
     * 返回与此缓存中的{@code key}关联的值，如果需要，从{@code * loader}获取该值。
     *
     * @param key 键
     * @param loader 加载方法
     * @return 结果
     */
    V get(K key, Callable<? extends V> loader);

    /**
     * 返回与此缓存中的{@code keys}关联的值的映射。返回的map将仅包含缓存中已存在的条目。
     *
     * @param keys 键集合
     * @return 结果
     */
    Map<K, V> getAllPresent(Iterable<K> keys);

    /**
     * 在此缓存中将{@code value}与{@code key}关联。如果缓存先前包含与{@code key}关联的值，则旧值将被替换为{@code value}。
     *
     * <p>在使用传统的“如果存在缓存，则返回，否则创建”模式时，首选{@link #get(Object, Callable)}。
     *
     * @param key 键
     * @param value 值
     */
    void put(@NotNull K key, @NotNull V value);

    /**
     * 将指定map中的所有键值对复制到缓存。
     * <li>对于从指定映射中的键到值的每个映射，此调用的效果等同于在此映射上调用{@link #put(Object, Object)} 的效果。
     * <li>如果在操作正在进行时修改了指定的映射，则此操作的行为未定义
     */
    void putAll(Map<? extends K, ? extends V> map);

    /** 丢弃键{@code key}的任何缓存值. */
    void invalidate(K key);

    /** 丢弃所有和键们{@code keys}关联的值 */
    void invalidateAll(Iterable<K> keys);

    /** 清空缓存 */
    void invalidateAll();

    /** 返回此缓存中的近似条目数 */
    long size();

    /** 返回存储在此缓存中的条目视图作为线程安全的映射。对map所做的修改会直接不会影响缓存。 */
    ConcurrentMap<K, V> asMap();
}
