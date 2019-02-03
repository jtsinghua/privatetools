package com.freetsinghua.tool.pool;

import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

import org.apache.commons.pool2.impl.BaseObjectPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.freetsinghua.tool.common.CommonConstant;

/**
 * 简单对象池，无法配置对象池
 *
 * @author z.tsinghua
 * @date 2019/2/2
 */
public class SimpleObjectPool<T> implements org.apache.commons.pool2.ObjectPool<T> {
	/**
	 * 代理对象池
	 */
	private final GenericObjectPool<T> innerObjectPool;
	/**
	 * 持有对象池
	 */
	private static final ThreadLocal<SimpleObjectPool<?>> POOL_THREAD_LOCAL = new ThreadLocal<>();

	private SimpleObjectPool(Class<T> type) {
		GenericObjectPoolConfig<T> objectPoolConfig = new GenericObjectPoolConfig<>();
		// 最小驱逐空闲时间，默认半小时
		objectPoolConfig.setMinEvictableIdleTimeMillis(BaseObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
		objectPoolConfig.setTestOnBorrow(BaseObjectPoolConfig.DEFAULT_TEST_ON_BORROW);
		objectPoolConfig.setTestOnCreate(BaseObjectPoolConfig.DEFAULT_TEST_ON_CREATE);
		// 当设置了BlockWhenExhausted属性，若是为负值，无限阻塞
		// 当等待1秒还没有获取对象，则抛出异常
		objectPoolConfig.setMaxWaitMillis(1000L);
		objectPoolConfig.setBlockWhenExhausted(true);
		// LAST IN FIRST OUT
		objectPoolConfig.setLifo(true);
		// 是否为等待公平借用对象的线程提供服务
		// True表示等待线程的服务就像在FIFO队列中等待一样
		objectPoolConfig.setFairness(true);
		objectPoolConfig.setMinIdle(CommonConstant.AVAILABLE_PROCESSORS * 128);
		objectPoolConfig.setMaxIdle(CommonConstant.AVAILABLE_PROCESSORS * 1024);
		// 对象创建时上限
		objectPoolConfig.setMaxTotal(CommonConstant.AVAILABLE_PROCESSORS * 1024 * CommonConstant.AVAILABLE_PROCESSORS);
		// 设置驱逐策略
		objectPoolConfig.setEvictionPolicy((config, underTest,
				idleCount) -> config.getIdleSoftEvictTime() < underTest.getIdleTimeMillis()
						&& config.getMinIdle() < idleCount
						|| config.getIdleEvictTime() < underTest.getIdleTimeMillis());
		// 驱逐器沉睡间隔
		objectPoolConfig.setTimeBetweenEvictionRunsMillis(1000 * 60 * 15);
		// 驱逐器关闭超时时间, 10秒
		objectPoolConfig.setEvictorShutdownTimeoutMillis(BaseObjectPoolConfig.DEFAULT_EVICTOR_SHUTDOWN_TIMEOUT_MILLIS);
		this.innerObjectPool = new GenericObjectPool<>(new DefaultPooledObjectFactory<>(type), objectPoolConfig);
	}

	/**
	 * 根据对象类型，获取一个对象池
	 * @param type 对象池中对象的类型
	 * @param <T> 对象类型
	 * @return 返回一个对象池
	 */
	@SuppressWarnings("unchecked")
	public static <T> SimpleObjectPool<T> getInstance(Class<T> type) {
		if (POOL_THREAD_LOCAL.get() == null) {
			POOL_THREAD_LOCAL.set(new SimpleObjectPool<>(type));
		}

		return (SimpleObjectPool<T>) POOL_THREAD_LOCAL.get();
	}

	/**
	 * Obtains an instance from this pool.
	 * <p>
	 * Instances returned from this method will have been either newly created with {@link
	 * PooledObjectFactory#makeObject} or will be a previously idle object and have been activated
	 * with {@link PooledObjectFactory#activateObject} and then validated with {@link
	 * PooledObjectFactory#validateObject}.
	 * </p>
	 * <p>
	 * By contract, clients <strong>must</strong> return the borrowed instance using {@link
	 * #returnObject}, {@link #invalidateObject}, or a related method as defined in an
	 * implementation or sub-interface.
	 * </p>
	 * <p>
	 * The behaviour of this method when the pool has been exhausted is not strictly specified
	 * (although it may be specified by implementations).
	 * </p>
	 *
	 * @return an instance from this pool.
	 * @throws IllegalStateException  after {@link #close close} has been called on this pool.
	 * @throws Exception              when {@link PooledObjectFactory#makeObject} throws an
	 *                                exception.
	 * @throws NoSuchElementException when the pool is exhausted and cannot or will not return
	 *                                another instance.
	 */
	@Override
	public T borrowObject() throws Exception, NoSuchElementException, IllegalStateException {
		return this.innerObjectPool.borrowObject();
	}

	/**
	 * Returns an instance to the pool. By contract, <code>obj</code>
	 * <strong>must</strong> have been obtained using {@link #borrowObject()} or
	 * a related method as defined in an implementation or sub-interface.
	 *
	 * @param obj a {@link #borrowObject borrowed} instance to be returned.
	 * @throws IllegalStateException if an attempt is made to return an object to the pool that is
	 *                               in any state other than allocated (i.e. borrowed). Attempting
	 *                               to return an object more than once or attempting to return an
	 *                               object that was never borrowed from the pool will trigger this
	 *                               exception.
	 * @throws Exception             if an instance cannot be returned to the pool
	 */
	@Override
	public void returnObject(T obj) throws Exception {
		this.innerObjectPool.returnObject(obj);
	}

	/**
	 * Invalidates an object from the pool.
	 * <p>
	 * By contract, <code>obj</code> <strong>must</strong> have been obtained using {@link
	 * #borrowObject} or a related method as defined in an implementation or sub-interface.
	 * </p>
	 * <p>
	 * This method should be used when an object that has been borrowed is determined (due to an
	 * exception or other problem) to be invalid.
	 * </p>
	 *
	 * @param obj a {@link #borrowObject borrowed} instance to be disposed.
	 * @throws Exception if the instance cannot be invalidated
	 */
	@Override
	public void invalidateObject(T obj) throws Exception {
		this.innerObjectPool.invalidateObject(obj);
	}

	/**
	 * Creates an object using the {@link PooledObjectFactory factory} or other implementation
	 * dependent mechanism, passivate it, and then place it in the idle object pool.
	 * <code>addObject</code> is useful for "pre-loading" a pool with idle objects. (Optional
	 * operation).
	 *
	 * @throws Exception                     when {@link PooledObjectFactory#makeObject} fails.
	 * @throws IllegalStateException         after {@link #close} has been called on this pool.
	 * @throws UnsupportedOperationException when this pool cannot add new idle objects.
	 */
	@Override
	public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
		this.innerObjectPool.addObject();
	}

	/**
	 * Returns the number of instances currently idle in this pool. This may be considered an
	 * approximation of the number of objects that can be {@link #borrowObject borrowed} without
	 * creating any new instances. Returns a negative value if this information is not available.
	 *
	 * @return the number of instances currently idle in this pool.
	 */
	@Override
	public int getNumIdle() {
		return this.innerObjectPool.getNumIdle();
	}

	/**
	 * Returns the number of instances currently borrowed from this pool. Returns a negative value
	 * if this information is not available.
	 *
	 * @return the number of instances currently borrowed from this pool.
	 */
	@Override
	public int getNumActive() {
		return this.innerObjectPool.getNumActive();
	}

	/**
	 * Clears any objects sitting idle in the pool, releasing any associated resources (optional
	 * operation). Idle objects cleared must be {@link PooledObjectFactory#destroyObject(PooledObject)}.
	 *
	 * @throws UnsupportedOperationException if this implementation does not support the operation
	 * @throws Exception                     if the pool cannot be cleared
	 */
	@Override
	public void clear() throws Exception, UnsupportedOperationException {
		this.innerObjectPool.clear();
	}

	/**
	 * Closes this pool, and free any resources associated with it.
	 * <p>
	 * Calling {@link #addObject} or {@link #borrowObject} after invoking this method on a pool will
	 * cause them to throw an {@link IllegalStateException}.
	 * </p>
	 * <p>
	 * Implementations should silently fail if not all resources can be freed.
	 * </p>
	 */
	@Override
	public void close() {
		this.innerObjectPool.close();
	}

	@PreDestroy
	public void release() {
		POOL_THREAD_LOCAL.remove();
	}
}
