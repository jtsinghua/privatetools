package com.freetsinghua.tool.pool;

import org.apache.commons.pool2.impl.EvictionPolicy;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * 可定制对象池
 *
 * @author z.tsinghua
 * @date 2019/2/3
 */
public class ObjectPool {
	public static final class Builder {
		private GenericObjectPoolConfig config = new GenericObjectPoolConfig();

		/**
		 * Set the value for the {@code maxTotal} configuration attribute for pools created with
		 * this configuration instance.
		 *
		 * @param maxTotal The new setting of {@code maxTotal} for this configuration instance
		 * @see GenericObjectPool#setMaxTotal(int)
		 */
		public Builder setMaxTotal(int maxTotal) {
			config.setMaxTotal(maxTotal);
			return this;
		}

		/**
		 * Set the value for the {@code maxIdle} configuration attribute for pools created with this
		 * configuration instance.
		 *
		 * @param maxIdle The new setting of {@code maxIdle} for this configuration instance
		 * @see GenericObjectPool#setMaxIdle(int)
		 */
		public Builder setMaxIdle(int maxIdle) {
			config.setMaxIdle(maxIdle);
			return this;
		}

		/**
		 * Set the value for the {@code minIdle} configuration attribute for pools created with this
		 * configuration instance.
		 *
		 * @param minIdle The new setting of {@code minIdle} for this configuration instance
		 * @see GenericObjectPool#setMinIdle(int)
		 */
		public Builder setMinIdle(int minIdle) {
			config.setMinIdle(minIdle);
			return this;
		}

		/**
		 * Set the value for the {@code lifo} configuration attribute for pools created with this
		 * configuration instance.
		 *
		 * @param lifo The new setting of {@code lifo} for this configuration instance
		 * @see GenericObjectPool#getLifo()
		 * @see GenericKeyedObjectPool#getLifo()
		 */
		public Builder setLifo(boolean lifo) {
			config.setLifo(lifo);

			return this;
		}

		/**
		 * Set the value for the {@code fairness} configuration attribute for pools created with
		 * this configuration instance.
		 *
		 * @param fairness The new setting of {@code fairness} for this configuration instance
		 * @see GenericObjectPool#getFairness()
		 * @see GenericKeyedObjectPool#getFairness()
		 */
		public Builder setFairness(boolean fairness) {
			config.setFairness(fairness);
			return this;
		}

		/**
		 * Set the value for the {@code maxWait} configuration attribute for pools created with this
		 * configuration instance.
		 *
		 * @param maxWaitMillis The new setting of {@code maxWaitMillis} for this configuration
		 *                      instance
		 * @see GenericObjectPool#getMaxWaitMillis()
		 * @see GenericKeyedObjectPool#getMaxWaitMillis()
		 */
		public Builder setMaxWaitMillis(long maxWaitMillis) {
			config.setMaxWaitMillis(maxWaitMillis);
			return this;
		}

		/**
		 * Set the value for the {@code minEvictableIdleTimeMillis} configuration attribute for
		 * pools created with this configuration instance.
		 *
		 * @param minEvictableIdleTimeMillis The new setting of {@code minEvictableIdleTimeMillis}
		 *                                   for this configuration instance
		 * @see GenericObjectPool#getMinEvictableIdleTimeMillis()
		 * @see GenericKeyedObjectPool#getMinEvictableIdleTimeMillis()
		 */
		public Builder setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
			config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
			return this;
		}

		/**
		 * Set the value for the {@code softMinEvictableIdleTimeMillis} configuration attribute for
		 * pools created with this configuration instance.
		 *
		 * @param softMinEvictableIdleTimeMillis The new setting of {@code softMinEvictableIdleTimeMillis}
		 *                                       for this configuration instance
		 * @see GenericObjectPool#getSoftMinEvictableIdleTimeMillis()
		 * @see GenericKeyedObjectPool#getSoftMinEvictableIdleTimeMillis()
		 */
		public Builder setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
			config.setSoftMinEvictableIdleTimeMillis(softMinEvictableIdleTimeMillis);
			return this;
		}

		/**
		 * Set the value for the {@code numTestsPerEvictionRun} configuration attribute for pools
		 * created with this configuration instance.
		 *
		 * @param numTestsPerEvictionRun The new setting of {@code numTestsPerEvictionRun} for this
		 *                               configuration instance
		 * @see GenericObjectPool#getNumTestsPerEvictionRun()
		 * @see GenericKeyedObjectPool#getNumTestsPerEvictionRun()
		 */
		public Builder setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
			config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
			return this;
		}

		/**
		 * Set the value for the {@code evictorShutdownTimeoutMillis} configuration attribute for
		 * pools created with this configuration instance.
		 *
		 * @param evictorShutdownTimeoutMillis The new setting of {@code evictorShutdownTimeoutMillis}
		 *                                     for this configuration instance
		 * @see GenericObjectPool#getEvictorShutdownTimeoutMillis()
		 * @see GenericKeyedObjectPool#getEvictorShutdownTimeoutMillis()
		 */
		public Builder setEvictorShutdownTimeoutMillis(long evictorShutdownTimeoutMillis) {
			config.setEvictorShutdownTimeoutMillis(evictorShutdownTimeoutMillis);
			return this;
		}

		/**
		 * Set the value for the {@code testOnCreate} configuration attribute for pools created with
		 * this configuration instance.
		 *
		 * @param testOnCreate The new setting of {@code testOnCreate} for this configuration
		 *                     instance
		 * @see GenericObjectPool#getTestOnCreate()
		 * @see GenericKeyedObjectPool#getTestOnCreate()
		 * @since 2.2
		 */
		public Builder setTestOnCreate(boolean testOnCreate) {
			config.setTestOnCreate(testOnCreate);
			return this;
		}

		/**
		 * Set the value for the {@code testOnBorrow} configuration attribute for pools created with
		 * this configuration instance.
		 *
		 * @param testOnBorrow The new setting of {@code testOnBorrow} for this configuration
		 *                     instance
		 * @see GenericObjectPool#getTestOnBorrow()
		 * @see GenericKeyedObjectPool#getTestOnBorrow()
		 */
		public Builder setTestOnBorrow(boolean testOnBorrow) {
			config.setTestOnBorrow(testOnBorrow);
			return this;
		}

		/**
		 * Set the value for the {@code testOnReturn} configuration attribute for pools created with
		 * this configuration instance.
		 *
		 * @param testOnReturn The new setting of {@code testOnReturn} for this configuration
		 *                     instance
		 * @see GenericObjectPool#getTestOnReturn()
		 * @see GenericKeyedObjectPool#getTestOnReturn()
		 */
		public Builder setTestOnReturn(boolean testOnReturn) {
			config.setTestOnReturn(testOnReturn);
			return this;
		}

		/**
		 * Set the value for the {@code testWhileIdle} configuration attribute for pools created
		 * with this configuration instance.
		 *
		 * @param testWhileIdle The new setting of {@code testWhileIdle} for this configuration
		 *                      instance
		 * @see GenericObjectPool#getTestWhileIdle()
		 * @see GenericKeyedObjectPool#getTestWhileIdle()
		 */
		public Builder setTestWhileIdle(boolean testWhileIdle) {
			config.setTestWhileIdle(testWhileIdle);
			return this;
		}

		/**
		 * Set the value for the {@code timeBetweenEvictionRunsMillis} configuration attribute for
		 * pools created with this configuration instance.
		 *
		 * @param timeBetweenEvictionRunsMillis The new setting of {@code timeBetweenEvictionRunsMillis}
		 *                                      for this configuration instance
		 * @see GenericObjectPool#getTimeBetweenEvictionRunsMillis()
		 * @see GenericKeyedObjectPool#getTimeBetweenEvictionRunsMillis()
		 */
		public Builder setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
			config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
			return this;
		}

		/**
		 * Set the value for the {@code evictionPolicyClass} configuration attribute for pools
		 * created with this configuration instance.
		 *
		 * @param evictionPolicy The new setting of {@code evictionPolicyClass} for this
		 *                       configuration instance
		 * @see GenericObjectPool#getEvictionPolicy()
		 * @see GenericKeyedObjectPool#getEvictionPolicy()
		 * @since 2.6.0
		 */
		@SuppressWarnings("unchecked")
		public Builder setEvictionPolicy(EvictionPolicy<?> evictionPolicy) {
			config.setEvictionPolicy(evictionPolicy);

			return this;
		}

		/**
		 * Set the value for the {@code evictionPolicyClassName} configuration attribute for pools
		 * created with this configuration instance.
		 *
		 * @param evictionPolicyClassName The new setting of {@code evictionPolicyClassName} for
		 *                                this configuration instance
		 * @see GenericObjectPool#getEvictionPolicyClassName()
		 * @see GenericKeyedObjectPool#getEvictionPolicyClassName()
		 */
		public Builder setEvictionPolicyClassName(String evictionPolicyClassName) {
			config.setEvictionPolicyClassName(evictionPolicyClassName);
			return this;
		}

		/**
		 * Set the value for the {@code blockWhenExhausted} configuration attribute for pools
		 * created with this configuration instance.
		 *
		 * @param blockWhenExhausted The new setting of {@code blockWhenExhausted} for this
		 *                           configuration instance
		 * @see GenericObjectPool#getBlockWhenExhausted()
		 * @see GenericKeyedObjectPool#getBlockWhenExhausted()
		 */
		public Builder setBlockWhenExhausted(boolean blockWhenExhausted) {
			config.setBlockWhenExhausted(blockWhenExhausted);
			return this;
		}

		/**
		 * Sets the value of the flag that determines if JMX will be enabled for pools created with
		 * this configuration instance.
		 *
		 * @param jmxEnabled The new setting of {@code jmxEnabled} for this configuration instance
		 */
		public Builder setJmxEnabled(boolean jmxEnabled) {
			config.setJmxEnabled(jmxEnabled);
			return this;
		}

		/**
		 * Sets the value of the JMX name base that will be used as part of the name assigned to JMX
		 * enabled pools created with this configuration instance. A value of <code>null</code>
		 * means that the pool will define the JMX name base.
		 *
		 * @param jmxNameBase The new setting of {@code jmxNameBase} for this configuration
		 *                    instance
		 */
		public Builder setJmxNameBase(String jmxNameBase) {
			config.setJmxNameBase(jmxNameBase);
			return this;
		}

		/**
		 * Sets the value of the JMX name prefix that will be used as part of the name assigned to
		 * JMX enabled pools created with this configuration instance.
		 *
		 * @param jmxNamePrefix The new setting of {@code jmxNamePrefix} for this configuration
		 *                      instance
		 */
		public Builder setJmxNamePrefix(String jmxNamePrefix) {
			config.setJmxNamePrefix(jmxNamePrefix);
			return this;
		}

		@SuppressWarnings("unchecked")
		public GenericObjectPool build(Class<?> type) {
			return new GenericObjectPool(new DefaultPooledObjectFactory(type), config);
		}
	}
}
