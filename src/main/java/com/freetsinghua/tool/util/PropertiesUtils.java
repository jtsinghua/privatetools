package com.freetsinghua.tool.util;

import com.freetsinghua.tool.anotation.Nullable;

import java.util.Properties;

/**
 * {@link Properties}对象操作工具类
 *
 * @author z.tsinghua
 * @date 2019/2/12
 */
public class PropertiesUtils {

    /**
     * 获取{@code key}对应的属性值，若是对应的配置属性不存在，则返回默认值{@code def}
     *
     * @param properties {@link Properties}对象
     * @param key 键
     * @param def 默认值
     * @return 返回对应的属性值或者默认值
     */
    public static Object getObject(Properties properties, String key, Object def) {
        return properties.getOrDefault(key, def);
    }

    /**
     * 获取{@code key}对应的属性值——布尔型，若是对应的配置属性不存在，则返回默认值{@code def}
     *
     * @param properties {@link Properties}对象
     * @param key 键
     * @param def 默认值
     * @return 返回对应的属性值或者默认值
     */
    public static boolean getBooleanValue(Properties properties, String key, boolean def) {
        String value = properties.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return def;
        }

        try {
            return Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return def;
        }
    }

    /**
     * 获取{@code key}对应的属性值——双精度浮点型，若是对应的配置属性不存在，则返回默认值{@code def}
     *
     * @param properties {@link Properties}对象
     * @param key 键
     * @param def 默认值
     * @return 返回对应的属性值或者默认值
     */
    public static double getDoubleValue(Properties properties, String key, double def) {
        String value = properties.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return def;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return def;
        }
    }

    /**
     * 获取{@code key}对应的属性值——单精度浮点型，若是对应的配置属性不存在，则返回默认值{@code def}
     *
     * @param properties {@link Properties}对象
     * @param key 键
     * @param def 默认值
     * @return 返回对应的属性值或者默认值
     */
    public static float getFloatValue(Properties properties, String key, float def) {
        String value = properties.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return def;
        }

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return def;
        }
    }

    /**
     * 获取{@code key}对应的属性值——long格式，若是对应的配置属性不存在，则返回默认值{@code def}
     *
     * @param properties {@link Properties}对象
     * @param key 键
     * @param def 默认值
     * @return 返回对应的属性值或者默认值
     */
    public static long getLongValue(Properties properties, String key, long def) {
        String value = properties.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return def;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return def;
        }
    }

    /**
     * 获取{@code key}对应的属性值——short格式，若是对应的配置属性不存在，则返回默认值{@code def}
     *
     * @param properties {@link Properties}对象
     * @param key 键
     * @param def 默认值
     * @return 返回对应的属性值或者默认值
     */
    public static short getShortValue(Properties properties, String key, short def) {
        String value = properties.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return def;
        }

        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return def;
        }
    }

    /**
     * 获取{@code key}对应的属性值——字符串格式，若是对应的配置属性不存在，则返回默认值{@code def}
     *
     * @param properties {@link Properties}对象
     * @param key 键
     * @param def 默认值
     * @return 返回结果
     */
    public static String getStringValue(Properties properties, String key, @Nullable String def) {
        return properties.getProperty(key, def);
    }

    /**
     * 获取{@code key}对应的属性值，如果没有配置，则返回{@code def}
     *
     * @param properties {@link Properties}对象
     * @param key 键
     * @param def 默认值
     * @return 返回属性值，或者默认值
     */
    public static int getIntValue(Properties properties, String key, int def) {
        String value = properties.getProperty(key);
        if (StringUtils.isBlank(value)) {
            return def;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return def;
        }
    }
}
