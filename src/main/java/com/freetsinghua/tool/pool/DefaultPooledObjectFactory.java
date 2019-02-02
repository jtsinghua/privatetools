package com.freetsinghua.tool.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author z.tsinghua
 * @date 2019/2/2
 */
public class DefaultPooledObjectFactory<T> extends BasePooledObjectFactory<T> {
    private final Class<T> type;

    public DefaultPooledObjectFactory(Class<T> type) {
        this.type = type;
    }

    @Override
    public T create() throws Exception {
        return type.newInstance();
    }

    @Override
    public PooledObject<T> wrap(T obj) {
        return new DefaultPooledObject<>(obj);
    }
}
