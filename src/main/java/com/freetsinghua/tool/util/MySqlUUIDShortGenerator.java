package com.freetsinghua.tool.util;

import com.freetsinghua.tool.core.io.ClassPathResource;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

/**
 * 使用MySQL的uuid_short函数获取id
 *
 * <p>配置文件：db.properties文件
 *
 * @apiNote 频繁的开启和关闭了连接，严重影响性能,使用对象池来创建连接池
 * @author z.tsinghua
 * @date 2019/2/12
 */
@Slf4j
final class MySqlUUIDShortGenerator {
    /** 连接池 */
    private static ConnectionPool connectionPool = new ConnectionPool();
    /** 连接池最大连接数 */
    private static final int MAX_CONNECTIONS = 25;
    /** 计数器 */
    private static AtomicInteger count = new AtomicInteger(0);
    /** 最大空闲次数 */
    private static final int MAX_TIMES = 5;
    /** 定时器延迟启动时间 */
    private static final long INITIAL_DELAY = 5;
    /** 定时器周期 */
    private static final long PERIOD = 5;

    private MySqlUUIDShortGenerator() {
        /*
         启动一个定时器，每{@link PERIOD}秒检查一次
         若是{@link MAX_TIMES}次后依旧没有活动链接，则关闭连接池
        */
        SimpleThreadPool.scheduleAtFixedRate(
                () -> {
                    if (connectionPool == null) {
                        // 通过触发异常，取消定时器执行
                        throw new RuntimeException();
                    }
                    // 如果所有对象空闲，则关闭连接池
                    if (connectionPool.getNumActive() == 0) {
                        if (count.get() >= MAX_TIMES) {
                            try {
                                connectionPool.close();
                                connectionPool = null;
                                log.debug("释放连接，关闭连接池");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            // 没有活动连接，增加计数
                            count.incrementAndGet();
                        }
                    } else {
                        // 有活动连接，减少计数
                        count.decrementAndGet();
                    }
                },
                INITIAL_DELAY,
                PERIOD,
                TimeUnit.SECONDS);
    }

    private static final MySqlUUIDShortGenerator GENERATOR = new MySqlUUIDShortGenerator();

    static MySqlUUIDShortGenerator getInstance() {
        // 若是没有空闲连接，则添加
        if (connectionPool.getNumIdle() == 0) {
            // 若是小于最大连接数，则创建新连接，否则抛出异常
            if (connectionPool.total() < MAX_CONNECTIONS) {
                log.debug("没有空闲连接，添加新连接");
                try {
                    connectionPool.addObject();
                } catch (Exception e) {
                    log.error("Add object failed: {}", e.getMessage(), e);
                }
            } else {
                throw new RuntimeException("连接池已耗尽");
            }
        }
        return GENERATOR;
    }

    /**
     * 获取数据库连接
     *
     * @throws ClassNotFoundException 若是加载类{@link com.mysql.jdbc.Driver}失败，则抛出异常
     * @throws IOException 若是加载配置文件失败，则抛出异常
     * @throws SQLException 若是获取连接失败，则抛出异常
     * @return 返回连接，或者抛出异常
     */
    private static Connection getConnection()
            throws ClassNotFoundException, IOException, SQLException {
        ClassPathResource classPathResource = new ClassPathResource("db.properties");
        Properties properties = new Properties();
        properties.load(classPathResource.getInputStream());

        String driverClass = PropertiesUtils.getStringValue(properties, "db.driverClass", null);
        Assert.state(driverClass != null, "DriverClass must not be null");

        Class.forName(driverClass);

        String url = PropertiesUtils.getStringValue(properties, "db.jdbcUrl", null);
        Assert.state(url != null, "Url must not be null");
        String user = PropertiesUtils.getStringValue(properties, "db.user", null);
        Assert.state(user != null, "User must not be null");
        String password = PropertiesUtils.getStringValue(properties, "db.password", null);
        Assert.state(password != null, "Password must not be null");

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * 查询数据库，获取结果
     *
     * @return 结果
     */
    String getUUid() {
        Connection connection = null;
        try {
            connection = connectionPool.borrowObject();
            String sql = "SELECT uuid_short()";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            String result = null;
            if (resultSet != null && resultSet.next()) {
                result = resultSet.getString(1);
            }
            if (resultSet != null) {
                resultSet.close();
            }
            return result;
        } catch (Exception e) {
            log.error("Get UUID_SHORT failed: {}", e.getMessage(), e);
            return null;
        } finally {
            try {
                // 归还对象
                if (connection != null) {
                    connectionPool.returnObject(connection);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** 连接工厂 */
    private static class ConnectionFactory implements PooledObjectFactory<Connection> {
        /**
         * Creates an instance that can be served by the pool and wrap it in a {@link PooledObject}
         * to be managed by the pool.
         *
         * @return a {@code PooledObject} wrapping an instance that can be served by the pool
         */
        @Override
        public PooledObject<Connection> makeObject()
                throws SQLException, IOException, ClassNotFoundException {
            return new DefaultPooledObject<>(getConnection());
        }

        /**
         * Destroys an instance no longer needed by the pool.
         *
         * <p>It is important for implementations of this method to be aware that there is no
         * guarantee about what state <code>obj</code> will be in and the implementation should be
         * prepared to handle unexpected errors.
         *
         * <p>Also, an implementation must take in to consideration that instances lost to the
         * garbage collector may never be destroyed.
         *
         * @param p a {@code PooledObject} wrapping the instance to be destroyed
         * @throws Exception should be avoided as it may be swallowed by the pool implementation.
         * @see #validateObject
         * @see ObjectPool#invalidateObject
         */
        @Override
        public void destroyObject(PooledObject<Connection> p) throws Exception {
            Connection connection = p.getObject();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }

        private static final int TIME_OUT = 5;
        /**
         * Ensures that the instance is safe to be returned by the pool.
         *
         * @param p a {@code PooledObject} wrapping the instance to be validated
         * @return <code>false</code> if <code>obj</code> is not valid and should be dropped from
         *     the pool, <code>true</code> otherwise.
         */
        @Override
        public boolean validateObject(PooledObject<Connection> p) {
            Connection connection = p.getObject();
            try {
                if (connection == null || connection.isClosed() || !connection.isValid(TIME_OUT)) {
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        /**
         * 重新初始化池返回的实例
         *
         * @param p a {@code PooledObject} wrapping the instance to be activated
         * @see #destroyObject
         */
        @Override
        public void activateObject(PooledObject<Connection> p)
                throws SQLException, IOException, ClassNotFoundException {
            if (!this.validateObject(p)) {
                p = new DefaultPooledObject<>(getConnection());
            }
        }

        /**
         * Uninitializes an instance to be returned to the idle object pool.
         *
         * @param p a {@code PooledObject} wrapping the instance to be passivated
         * @see #destroyObject
         */
        @Override
        public void passivateObject(PooledObject<Connection> p) {}
    }

    /** 连接池 */
    private static class ConnectionPool implements ObjectPool<Connection> {
        private final ObjectPool<Connection> connectionPool =
                new GenericObjectPool<>(new ConnectionFactory());
        /**
         * Obtains an instance from this pool.
         *
         * <p>Instances returned from this method will have been either newly created with {@link
         * PooledObjectFactory#makeObject} or will be a previously idle object and have been
         * activated with {@link PooledObjectFactory#activateObject} and then validated with {@link
         * PooledObjectFactory#validateObject}.
         *
         * <p>By contract, clients <strong>must</strong> return the borrowed instance using {@link
         * #returnObject}, {@link #invalidateObject}, or a related method as defined in an
         * implementation or sub-interface.
         *
         * <p>The behaviour of this method when the pool has been exhausted is not strictly
         * specified (although it may be specified by implementations).
         *
         * @return an instance from this pool.
         * @throws IllegalStateException after {@link #close close} has been called on this pool.
         * @throws Exception when {@link PooledObjectFactory#makeObject} throws an exception.
         * @throws NoSuchElementException when the pool is exhausted and cannot or will not return
         *     another instance.
         */
        @Override
        public Connection borrowObject()
                throws Exception, NoSuchElementException, IllegalStateException {
            return connectionPool.borrowObject();
        }

        /**
         * Returns an instance to the pool. By contract, <code>obj</code> <strong>must</strong> have
         * been obtained using {@link #borrowObject()} or a related method as defined in an
         * implementation or sub-interface.
         *
         * @param obj a {@link #borrowObject borrowed} instance to be returned.
         * @throws IllegalStateException if an attempt is made to return an object to the pool that
         *     is in any state other than allocated (i.e. borrowed). Attempting to return an object
         *     more than once or attempting to return an object that was never borrowed from the
         *     pool will trigger this exception.
         * @throws Exception if an instance cannot be returned to the pool
         */
        @Override
        public void returnObject(Connection obj) throws Exception {
            connectionPool.returnObject(obj);
        }

        /**
         * Invalidates an object from the pool.
         *
         * <p>By contract, <code>obj</code> <strong>must</strong> have been obtained using {@link
         * #borrowObject} or a related method as defined in an implementation or sub-interface.
         *
         * <p>This method should be used when an object that has been borrowed is determined (due to
         * an exception or other problem) to be invalid.
         *
         * @param obj a {@link #borrowObject borrowed} instance to be disposed.
         * @throws Exception if the instance cannot be invalidated
         */
        @Override
        public void invalidateObject(Connection obj) throws Exception {
            connectionPool.invalidateObject(obj);
        }

        /**
         * Creates an object using the {@link PooledObjectFactory factory} or other implementation
         * dependent mechanism, passivate it, and then place it in the idle object pool. <code>
         * addObject</code> is useful for "pre-loading" a pool with idle objects. (Optional
         * operation).
         *
         * @throws Exception when {@link PooledObjectFactory#makeObject} fails.
         * @throws IllegalStateException after {@link #close} has been called on this pool.
         * @throws UnsupportedOperationException when this pool cannot add new idle objects.
         */
        @Override
        public void addObject()
                throws Exception, IllegalStateException, UnsupportedOperationException {
            connectionPool.addObject();
        }

        /**
         * Returns the number of instances currently idle in this pool. This may be considered an
         * approximation of the number of objects that can be {@link #borrowObject borrowed} without
         * creating any new instances. Returns a negative value if this information is not
         * available.
         *
         * @return the number of instances currently idle in this pool.
         */
        @Override
        public int getNumIdle() {
            return connectionPool.getNumIdle();
        }

        /**
         * Returns the number of instances currently borrowed from this pool. Returns a negative
         * value if this information is not available.
         *
         * @return the number of instances currently borrowed from this pool.
         */
        @Override
        public int getNumActive() {
            return connectionPool.getNumActive();
        }

        /**
         * Clears any objects sitting idle in the pool, releasing any associated resources (optional
         * operation). Idle objects cleared must be {@link
         * PooledObjectFactory#destroyObject(PooledObject)}.
         *
         * @throws UnsupportedOperationException if this implementation does not support the
         *     operation
         * @throws Exception if the pool cannot be cleared
         */
        @Override
        public void clear() throws Exception, UnsupportedOperationException {
            connectionPool.clear();
        }

        /**
         * Closes this pool, and free any resources associated with it.
         *
         * <p>Calling {@link #addObject} or {@link #borrowObject} after invoking this method on a
         * pool will cause them to throw an {@link IllegalStateException}.
         *
         * <p>Implementations should silently fail if not all resources can be freed.
         */
        @Override
        public void close() {
            connectionPool.close();
        }

        /**
         * 获取全部对象数目
         *
         * @return 返回数目
         */
        int total() {
            return this.getNumActive() + this.getNumIdle();
        }
    }
}
