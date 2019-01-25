package com.freetsinghua.tool.util;

import com.sun.istack.internal.Nullable;

/**
 * 从spring-core移植，用于获取ClassLoader实例
 *
 * <p>create by @author z.tsinghua at 2018/9/15
 */
public class ClassUtils {

	@Nullable
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader classLoader = null;

		try {
			classLoader = Thread.currentThread().getContextClassLoader();
		} catch (Throwable throwable) {
			//
		}

		if (classLoader == null) {
			classLoader = ClassUtils.class.getClassLoader();
			if (classLoader == null) {
				try {
					classLoader = ClassLoader.getSystemClassLoader();
				} catch (Throwable throwable) {
					//
				}
			}
		}

		return classLoader;
	}

	public static String classPackageAsResourcePath(@Nullable Class<?> clazz) {
		if (clazz == null) {
			return "";
		} else {
			String className = clazz.getName();
			int packageEndIndex = className.lastIndexOf(46);
			if (packageEndIndex == -1) {
				return "";
			} else {
				String packageName = className.substring(0, packageEndIndex);
				return packageName.replace('.', '/');
			}
		}
	}
}
