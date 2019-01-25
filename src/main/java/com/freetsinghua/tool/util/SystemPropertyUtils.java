package com.freetsinghua.tool.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.common.CommonConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * 用于检索和解析Java系统属性值的实用程序方法的集合
 * 
 * @author z.tsinghua
 * @date 2019/1/25
 */
@Slf4j
public class SystemPropertyUtils {

	/**
	 * 当且仅当存在具有指定{@code key}的系统属性时，才返回{@code true}.
	 */
	public static boolean contain(String key) {
		return get(key) != null;
	}

	/**
	 * 返回具有指定的{@code key}的Java系统属性的值
	 *
	 * @return 返回属性值，或者null
	 */
	@Nullable
	public static String get(String key) {
		return get(key, null);
	}

	/**
	 * 返回具有指定的{@code key}的Java系统属性的值，如果属性访问失败，则返回到指定的默认值{@code def}
	 *
	 * @return 返回属性值，或者返回如果没有对应的属性，或者无法访问，则返回{@code def},
	 */
	public static String get(String key, String def) {
		// 检查key
		checkKey(key);

		String value;
		if (System.getSecurityManager() == null) {
			value = System.getProperty(key);
		} else {
			value = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key));
		}

		if (value == null) {
			value = def;
		}

		return value;
	}

	private static void checkKey(String key) {
		if (key == null) {
			throw new NullPointerException("key 不能为null");
		}

		if (key.length() == 0) {
			throw new IllegalArgumentException("key 不能为空");
		}
	}

    /**
     * 返回具有指定的{@code key}的Java系统属性的值(浮点型类型)，如果属性访问失败，则返回到指定的默认值{@code def}
     *
     * @return 返回属性值，或者返回如果没有对应的属性，或者无法访问，则返回{@code def},
     */
	public static Float getFloat(String key, float def){
        String value = get(key);
        if (value == null){
            return def;
        }

        value = value.trim();
        try {
            return Float.parseFloat(value);
        }catch (NumberFormatException e){
            //
        }

        log.warn("无法解析浮点型系统属性 '{}': {} - 使用默认值: {}", key, value, def);

        return def;
    }

	/**
	 * 返回具有指定的{@code key}的Java系统属性的值(int类型)，如果属性访问失败，则返回到指定的默认值{@code def}
	 *
	 * @return 返回属性值，或者返回如果没有对应的属性，或者无法访问，则返回{@code def},
	 */
	public static Integer getInt(String key, int def) {
		String value = get(key);
		if (value == null) {
			return def;
		}

		value = value.trim();
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			//
		}

		log.warn("无法解析整数系统属性 '{}':{} - 使用默认值: {}", key, value, def);
		return def;
	}

	/**
	 * 返回具有指定的{@code key}的Java系统属性的值(Long类型)，如果属性访问失败，则返回到指定的默认值{@code def}
	 *
	 * @return 返回属性值，或者返回如果没有对应的属性，或者无法访问，则返回{@code def},
	 */
	public static Long getLong(String key, long def) {
		String value = get(key);
		if (value == null) {
			return def;
		}

		value = value.trim();
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			//
		}

		log.warn("无法解析长整数系统属性 '{}':{} - 使用默认值: {}", key, value, def);
		return def;
	}

	/**
	 * 返回具有指定的{@code key}的Java系统属性的值，如果属性访问失败，则返回到指定的默认值{@code def}。
	 *
	 * @return 返回对应的属性值，或者返回默认值{@code def}
	 */
	public static Boolean getBoolean(String key, boolean def) {
		String value = get(key);
		if (value == null) {
			return def;
		}

		value = value.trim().toLowerCase();
		if (value.isEmpty()) {
			return def;
		}

		if (CommonConstant.STRING_YES.equals(value) || CommonConstant.STRING_TRUE.equals(value)
				|| CommonConstant.STRING_1.equals(value)) {
			return true;
		}

		if (CommonConstant.STRING_NO.equals(value) || CommonConstant.STRING_FALSE.equals(value)
				|| CommonConstant.STRING_0.equals(value)) {
			return false;
		}

		log.warn("无法解析布尔系统属性 '{}':{} - 使用默认的值: {}", key, value, def);

		return false;
	}
}
