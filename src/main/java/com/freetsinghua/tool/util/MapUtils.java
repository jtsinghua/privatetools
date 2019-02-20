package com.freetsinghua.tool.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

/**
 * @author z.tsinghua
 * @date 2019/2/15
 */
public class MapUtils {

    public static Optional<String> getStringValue(Map map, Object key) {
        if (map.containsKey(key)) {
            Object objValue = map.get(key);
            if (objValue instanceof String) {
                String stringValue = (String) objValue;
                return Optional.of(stringValue);
            }
        }
        return Optional.empty();
    }

    public static Optional<Number> getNumberValue(Map map, Object key) {
        if (map.containsKey(key)) {
            Object objValue = map.get(key);
            if (objValue instanceof Number) {
                Number number = (Number) objValue;
                return Optional.of(number);
            }
        }
        return Optional.empty();
    }

    public static Optional<BigDecimal> getBigDecimalValue(Map map, Object key) {
        if (map.containsKey(key)) {
            Object objValue = map.get(key);
            if (objValue instanceof BigDecimal) {
                BigDecimal bigDecimalValue = (BigDecimal) objValue;
                return Optional.of(bigDecimalValue);
            }
        }
        return Optional.empty();
    }

    public static Optional<BigInteger> getBigIntegerValue(Map map, Object key) {
        if (map.containsKey(key)) {
            Object objValue = map.get(key);
            if (objValue instanceof BigInteger) {
                BigInteger bigIntegerValue = (BigInteger) objValue;
                return Optional.of(bigIntegerValue);
            }
        }
        return Optional.empty();
    }

    public static Optional<Double> getDoubleValue(Map map, Object key) {
        if (map.containsKey(key)) {
            Object objValue = map.get(key);
            if (objValue instanceof Double) {
                double doubleValue = (double) objValue;
                return Optional.of(doubleValue);
            }
        }
        return Optional.empty();
    }

    public static Optional<Float> getFloatValue(Map map, Object key) {
        if (map.containsKey(key)) {
            Object objValue = map.get(key);
            if (objValue instanceof Float) {
                float floatValue = (float) objValue;
                return Optional.of(floatValue);
            }
        }
        return Optional.empty();
    }

    public static Optional<Long> getLongValue(Map map, Object key) {
        if (map.containsKey(key)) {
            Object objValue = map.get(key);
            if (objValue instanceof Long) {
                long longValue = (long) objValue;
                return Optional.of(longValue);
            }
        }
        return Optional.empty();
    }

    public static Optional<Short> getShortValue(Map map, Object key) {
        if (map.containsKey(key)) {
            Object objValue = map.get(key);
            if (objValue instanceof Short) {
                short shortValue = (short) objValue;
                return Optional.of(shortValue);
            }
        }
        return Optional.empty();
    }

    public static Optional<Integer> getIntValue(Map map, Object key) {
        if (map.containsKey(key)) {
            Object objValue = map.get(key);
            if (objValue instanceof Integer) {
                return Optional.of((Integer) objValue);
            }
        }
        return Optional.empty();
    }

    public static Optional<Boolean> getBooleanValue(Map map, Object key) {
        if (map.containsKey(key)) {
            Object objValue = map.get(key);
            if (objValue instanceof Boolean) {
                return Optional.of((boolean) objValue);
            }
        }
        return Optional.empty();
    }
}
