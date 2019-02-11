package com.freetsinghua.tool.util;

import java.lang.reflect.Field;

import com.freetsinghua.tool.anotation.Nullable;

import sun.misc.Unsafe;

/**
 * @author z.tsinghua
 * @date 2019/2/11
 */
public class UnsafeUtils {

	/**
	 * 获取{@link Unsafe}实例
	 * @return 返回实例，或者{@code null}
	 */
	@Nullable
	public static Unsafe getUnsafeInstance() {
		try {
			Class<Unsafe> unsafeClass = Unsafe.class;
			Field field = unsafeClass.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			return (Unsafe) field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
