package com.freetsinghua.tool.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射工具类
 *
 * @author z.tsinghua
 * @date 2019/2/11
 */
public class ReflectUtils {

	/**
	 * 通过反射反初始化指定对象
	 * @return {@code true}如果反初始化成功
	 */
	public static <T> boolean passivate(T obj) {
		try {
			Class<?> objClass = obj.getClass();
			// 获取所有域
			Field[] classDeclaredFields = objClass.getDeclaredFields();
			for (Field field : classDeclaredFields) {
				String fieldName = field.getName();
				if (fieldName.contains("serialVersionUID")){
					continue;
				}
				String setterMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

				Method setterMethod = objClass.getDeclaredMethod(setterMethodName, field.getType());
				if (setterMethod != null) {
					Class<?> fieldTypeClass = ClassUtils.resolvePrimitiveIfNecessary(field.getType());
					// 如果是原始数据类型
					if (field.getType().isPrimitive()) {
						setterMethod.invoke(obj, ClassUtils.getPrimitiveWrapperObject(fieldTypeClass));
					} else {
						Object o = null;
						setterMethod.invoke(obj, o);
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
