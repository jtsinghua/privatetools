package com.freetsinghua.tool.util;

import java.lang.reflect.Array;
import java.util.Collections;

/**
 * create by @author z.tsinghua at 2018/9/15
 */
public class ArrayUtils {
    
    public static final Object[] EMPTY_ARRAY = Collections.EMPTY_LIST.toArray();

    /**
     * 创建数组
     * @param clazz 类型
     * @param length 数组长度
     * @param <T> 数组类型
     * @return 返回数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(Class<T> clazz, int length) {
        return (T[]) Array.newInstance(clazz, length);
    }
}
