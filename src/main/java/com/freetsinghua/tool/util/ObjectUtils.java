package com.freetsinghua.tool.util;

import com.freetsinghua.tool.anotation.Nullable;

import java.lang.reflect.Array;

/**
 * 通用对象工具
 *
 * <p>nullSafeEquals方法移植自spring-core
 *
 * <p>arrayEquals方法参考Junit的实现 create by @author z.tsinghua at 2018/9/15
 */
public class ObjectUtils {
    private static final int INITIAL_HASH = 7;
    private static final int MULTIPLIER = 31;
    /**
     * 比较两个对象是否相等
     *
     * @param o1 待比较对象
     * @param o2 待比较对象
     * @return 返回结果
     */
    public static boolean nullSafeEquals(@Nullable Object o1, @Nullable Object o2) {
        if (o1 == o2) {
            return true;
        } else if (o1 != null && o2 != null) {
            if (o1.equals(o2)) {
                return true;
            } else {
                // 若是数组
                return (o1.getClass().isArray() && o2.getClass().isArray()) && arrayEquals(o1, o2);
            }
        } else {
            return false;
        }
    }

    /**
     * 判断指定的对象是否是数组
     *
     * @param obj 要判断的对象
     * @return 返回结果
     */
    private static boolean isArray(Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    private static boolean equalsRegardingNull(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }

        return o1.equals(o2);
    }

    /**
     * 比较两个数组是否相等
     *
     * @param o1 数组
     * @param o2 数组
     * @return 返回结果
     */
    public static boolean arrayEquals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        // 长度
        int o1Len = Array.getLength(o1);
        int o2Len = Array.getLength(o2);
        // 若是长度不相等
        if (o1Len != o2Len) {
            return false;
        }
        // 逐个比较
        for (int i = 0; i < o1Len; i++) {
            Object oi1 = Array.get(o1, i);
            Object oi2 = Array.get(o2, i);

            if (isArray(oi1) && isArray(oi2)) {
                return arrayEquals(oi1, oi2);
            } else {
                if (!equalsRegardingNull(oi1, oi2)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static int floatCompare(float v1, float v2) {
        if (v1 - v2 < 1e7) {
            return 0;
        }

        if (v1 > v2) {
            return 1;
        }

        return -1;
    }

    /** 判断数组是否为空 */
    public static boolean isArrayEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static int nullSafeHashCode(@Nullable Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[]) {
                return nullSafeHashCode((Object[]) obj);
            }
            if (obj instanceof boolean[]) {
                return nullSafeHashCode((boolean[]) obj);
            }
            if (obj instanceof byte[]) {
                return nullSafeHashCode((byte[]) obj);
            }
            if (obj instanceof char[]) {
                return nullSafeHashCode((char[]) obj);
            }
            if (obj instanceof double[]) {
                return nullSafeHashCode((double[]) obj);
            }
            if (obj instanceof float[]) {
                return nullSafeHashCode((float[]) obj);
            }
            if (obj instanceof int[]) {
                return nullSafeHashCode((int[]) obj);
            }
            if (obj instanceof long[]) {
                return nullSafeHashCode((long[]) obj);
            }
            if (obj instanceof short[]) {
                return nullSafeHashCode((short[]) obj);
            }
        }
        return obj.hashCode();
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(@Nullable Object[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (Object element : array) {
            hash = MULTIPLIER * hash + nullSafeHashCode(element);
        }
        return hash;
    }
}
