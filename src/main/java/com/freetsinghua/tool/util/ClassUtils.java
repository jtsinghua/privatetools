package com.freetsinghua.tool.util;

import java.io.Closeable;
import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.util.*;

import com.freetsinghua.tool.anotation.Nullable;

/**
 * 从spring-core移植，用于获取ClassLoader实例
 *
 * <p>create by @author z.tsinghua at 2018/9/15
 */
public class ClassUtils {
	/**
	 * 包装器类型-原始类型map， 包装器类型为键，原始类型为值
	 */
	private static final Map<Class<?>, Class<?>> WRAPPER_TYPE_TO_PRIMITIVE_MAP = new IdentityHashMap<>(8);
	/**
	 * 原始类型-包装器类型map，原始类型为键，包装器类型值
	 */
	private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_TO_WRAPPER_MAP = new IdentityHashMap<>();
	/**
	 * 原始类型map，原始数据类型类名为键，类为值
	 */
	private static final Map<String, Class<?>> PRIMITIVE_TYPE_NAME_MAP = new HashMap<>();
	/**
	 * 常见类map，类名为键，类为值
	 */
	private static final Map<String, Class<?>> COMMON_CLASS_CACHE = new HashMap<>();

	/**
	 * java常见接口集合
	 */
	private static final Set<Class<?>> JAVA_LANGUAGE_INTERFACES;
	/**
	 * 包名分隔符
	 */
	private static final String PACKAGE_SEPARATOR = ".";
	/**
	 * 资源文件目录分隔符
	 */
	private static final String PATH_SEPARATOR = "/";
	/**
	 * 数组类型后缀
	 */
	private static final String ARRAY_SUFFIX = "[]";
	private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";
	private static final String INTERNAL_ARRAY_PREFIX = "[";
	private static final String INNER_CLASS_SEPARATOR = "$";

	static {
		WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Boolean.class, boolean.class);
		WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Byte.class, byte.class);
		WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Character.class, char.class);
		WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Short.class, short.class);
		WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Integer.class, int.class);
		WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Long.class, long.class);
		WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Float.class, float.class);
		WRAPPER_TYPE_TO_PRIMITIVE_MAP.put(Double.class, double.class);

		for (Map.Entry<Class<?>, Class<?>> entry : WRAPPER_TYPE_TO_PRIMITIVE_MAP.entrySet()) {
			PRIMITIVE_TYPE_TO_WRAPPER_MAP.put(entry.getValue(), entry.getKey());
			registerCommonClasses(entry.getKey());
		}

		Set<Class<?>> primitiveTypes = new HashSet<>(32);
		primitiveTypes.addAll(WRAPPER_TYPE_TO_PRIMITIVE_MAP.values());
		Collections.addAll(primitiveTypes, boolean[].class, byte[].class, char[].class, short[].class, int[].class,
				long[].class, float[].class, double[].class);
		primitiveTypes.add(void.class);
		for (Class<?> primitiveType : primitiveTypes) {
			PRIMITIVE_TYPE_NAME_MAP.put(primitiveType.getName(), primitiveType);
		}

		registerCommonClasses(Boolean[].class, Byte[].class, Integer[].class, Long[].class, Short[].class,
				Float[].class, Double[].class, Character[].class);
		registerCommonClasses(Number.class, Number[].class, String.class, String[].class, Class.class, Class[].class,
				Object.class, Object[].class);
		registerCommonClasses(Throwable.class, Exception.class, RuntimeException.class, Error.class,
				StackTraceElement.class, StackTraceElement[].class);
		registerCommonClasses(Enum.class, Iterable.class, Iterator.class, Enumeration.class, Collection.class,
				List.class, Set.class, Map.class, Map.Entry.class, Optional.class);

		Class<?>[] javaLanguageInterfaceArray = {Serializable.class, Externalizable.class, Closeable.class,
				AutoCloseable.class, Cloneable.class, Comparable.class};
		registerCommonClasses(javaLanguageInterfaceArray);
		JAVA_LANGUAGE_INTERFACES = new HashSet<>(Arrays.asList(javaLanguageInterfaceArray));
	}

	private static void registerCommonClasses(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			COMMON_CLASS_CACHE.put(clazz.getName(), clazz);
		}
	}

	/**
	 * 若是原始数据类型，则返回对应包装器类型
	 * @param name 类名
	 * @return 返回{@code null} 如果不是原始数据类型，否则返回对应的包装器类型
	 */
	@Nullable
	public static Class<?> resolvePrimitiveClassName(@Nullable String name) {
		Class<?> result = null;

		if (name != null && name.length() < 8) {
			result = PRIMITIVE_TYPE_NAME_MAP.get(name);
		}

		return result;
	}

	/**
	 * 检查指定的类是否是包装器类，如{@link Boolean}, {@link Character}, {@link Byte}, {@link Integer}, {@link Double}等等
	 * @param clazz 指定的类
	 * @return 返回结果
	 */
	public static boolean isPrimitiveWrapper(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return WRAPPER_TYPE_TO_PRIMITIVE_MAP.containsKey(clazz);
	}

	/**
	 * 检查指定的类是否是原始类型，或者是包装器类型
	 * @param clazz 指定的类
	 * @return 结果
	 */
	public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return clazz.isPrimitive() || isPrimitiveWrapper(clazz);
	}

	/**
	 * 检查指定的类是否是原始数据类型数组
	 * @param clazz 指定的类
	 * @return 结果
	 */
	public static boolean isPrimitiveArray(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return clazz.isArray() && clazz.getComponentType().isPrimitive();
	}

	/**
	 * 检查指定的类是否是包装器类数组
	 * @param clazz 指定的类
	 * @return 结果
	 */
	public static boolean isPrimitiveWrapperArray(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType());
	}

	/**
	 * 如果它是一个基本类，则解析指定的类，返回相应的原始包装类型。
	 * @param clazz 指定的类
	 * @return 结果
	 */
	public static Class<?> resolvePrimitiveIfNecessary(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return clazz.isPrimitive() && clazz != void.class ? PRIMITIVE_TYPE_TO_WRAPPER_MAP.get(clazz) : clazz;
	}

	/**
	 * 检查{@code rhsType}是否可以赋值给{@code lhsType}
	 * @param lhsType 目标类型
	 * @param rhsType 被赋值给{@code lhsType}的类型
	 * @return 若是可以赋值，返回{@code true}
	 */
	public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
		Assert.notNull(lhsType, "Left-hand side type must not be null");
		Assert.notNull(rhsType, "Right-hand side type must not be null");
		// 如果两者要么都是原始数据类型，要么都说普通类型
		if (lhsType.isAssignableFrom(rhsType)) {
			return true;
		}
		// 如果lhsType是原始数据类型， 意味着rhsType不是原始类型
		if (lhsType.isPrimitive()) {
			// 获取包rhsType的原始数据类型， 假设rhsType是包装器类型
			Class<?> resolvePrimitive = WRAPPER_TYPE_TO_PRIMITIVE_MAP.get(rhsType);
			return resolvePrimitive == lhsType;
		}
		// 如果lhsType不是原始数据类型, 同时意味着rhsType是原始数据类型
		else {
			Class<?> resolveWrapper = PRIMITIVE_TYPE_TO_WRAPPER_MAP.get(rhsType);
			return resolveWrapper != null && lhsType.isAssignableFrom(resolveWrapper);
		}
	}

	/**
	 * 检查是否可以把{@code value} 赋值给指定的类型{@code type}
	 * @param type 指定的类型
	 * @param value 值
	 * @return 返回结果
	 */
	public static boolean isAssignableValue(Class<?> type, @Nullable Object value) {
		Assert.notNull(type, "Class must not be null");
		return value != null ? isAssignable(type, value.getClass()) : !type.isPrimitive();
	}


	/**
	 * 将基于“.”的完全限定类名转换为基于“/”的资源路径
	 * @param className 指定的完全限定类名
	 * @return 返回结果
	 */
	public static String convertClassNameToResourcePath(String className) {
		Assert.notNull(className, "Class name must not be null");
		return className.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
	}

	/**
	 * 将基于'/'的资源路径，转化为基于'.'完全限定类名
	 * @param path 指定的资源路径名
	 * @return 返回结果
	 */
	public static String convertResourcePathToClassName(String path) {
		Assert.notNull(path, "Resource path must not be null");
		return path.replace(PATH_SEPARATOR, PACKAGE_SEPARATOR);
	}

	/**
	 * 返回适合与{@code ClassLoader.getResource}一起使用的路径（也适用于{@code Class.getResource}
	 * 方法是在返回值前加上斜杠（'/'）, 通过获取指定的类文件的包来构建，将所有点（'.'）转换为斜杠（'/'），必要时添加尾部斜杠，并将指定的资源名称连接到此。 
	 * 因此，此函数可用于构建适合加载与类文件位于同一包中的资源文件的路径，{@link com.freetsinghua.tool.core.io.ClassPathResource}通常更方便。
	 * @param clazz 将其包用作基础的类
	 * @param resourceName 要追加的资源名称。前导斜杠是可选的
	 * @return 建立的资源路径
	 */
	public static String addResourcePathToPackagePath(Class<?> clazz, String resourceName) {
		Assert.notNull(resourceName, "Resource name must not be null");
		if (!resourceName.startsWith(PATH_SEPARATOR)) {
			return classPackageAsResourcePath(clazz) + "/" + resourceName;
		}

		return classPackageAsResourcePath(clazz) + resourceName;
	}

	/**
	 * 构建一个String，该String由给定集合中的classes / interfaces的名称组成。
	 * 基本上像{@code AbstractCollection.toString（）}，但在每个类名之前剥去 "class"/"interface"前缀。
	 * @param classes a Collection of Class objects (may be {@code null})
	 * @return a String of form "[com.foo.Bar, com.foo.Baz]"
	 */
	public static String classNamesToString(@Nullable Collection<Class<?>> classes) {
		if (CollectionUtils.isEmpty(classes)) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (Iterator<Class<?>> it = classes.iterator(); it.hasNext();) {
			Class<?> clazz = it.next();
			sb.append(clazz.getName());
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Copy the given {@code Collection} into a {@code Class} array.
	 * @param classes 类的集合
	 * @return 返回结果
	 */
	public static Class<?>[] toClassArray(Collection<Class<?>> classes) {
		return classes.toArray(new Class<?>[]{});
	}

	/**
	 * 获取默认的{@link ClassLoader}
	 * @return 返回获取的类加载器，可能为null
	 */
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

	/**
	 * 完全限定类名转化为资源路径名
	 * @param clazz 指定的类
	 * @return 资源路径名
	 */
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
				return packageName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
			}
		}
	}

	/**
	 * check whether the given class is loadable in the give ClassLoader
	 * @param clazz the given class
	 * @param classLoader the given classloader
	 * @return {@code true} if is loadable
	 */
	private static boolean isLoadable(Class<?> clazz, ClassLoader classLoader) {
		try {
			return clazz == classLoader.loadClass(clazz.getName());
		} catch (ClassNotFoundException e) {
			//
			return false;
		}
	}

	/**
	 * check whether the given class is visible in the given ClassLoader
	 * @param clazz the given class
	 * @param classLoader the given classloader
	 * @return {@code true} if is visible
	 */
	private static boolean isVisible(Class<?> clazz, @Nullable ClassLoader classLoader) {
		if (classLoader == null) {
			return true;
		}

		if (classLoader == clazz.getClassLoader()) {
			return true;
		}

		return isLoadable(clazz, classLoader);
	}

	/**
	 * return all interfaces that the given class implements as set, include ones implements by supper class
	 * @param clazz the given class
	 * @param classLoader the given classloader
	 * @return return all interfaces
	 */
	public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz, @Nullable ClassLoader classLoader) {
		Assert.notNull(clazz, "Class must not be null");

		if (clazz.isInterface() && isVisible(clazz, classLoader)) {
			return Collections.singleton(clazz);
		}

		Set<Class<?>> interfaces = new LinkedHashSet<>();
		Class<?> currentClass = clazz;

		while (currentClass != null) {
			Class<?>[] interfaceArray = currentClass.getInterfaces();
			for (Class<?> cl : interfaceArray) {
				if (isVisible(cl, classLoader)) {
					interfaces.add(cl);
				}
			}
			currentClass = currentClass.getSuperclass();
		}

		return interfaces;
	}

	/**
	 * return all interfaces that the given class implements as set, include ones implements by supper class
	 * @param clazz the given class
	 * @return return all interfaces
	 */
	public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz) {
		return getAllInterfacesForClassAsSet(clazz, null);
	}

	/**
	 * return all interfaces that the given object implements as set
	 * @param instance the given object
	 * @return return all interfaces
	 */
	public static Set<Class<?>> getAllInterfacesAsSet(Object instance) {
		Assert.notNull(instance, "Instance must not be null");

		return getAllInterfacesForClassAsSet(instance.getClass());
	}

	/**
	 * return all interfaces that the given object implements as array
	 * @param instance the given object
	 * @return return all interfaces
	 */
	public static Class<?>[] getAllInterfaces(Object instance) {
		Assert.notNull(instance, "Instance must not be null");

		return getAllInterfacesForClass(instance.getClass());
	}

	/**
	 * return all interfaces that the given class implements
	 * @param clazz the given class
	 * @return return all interfaces
	 */
	private static Class<?>[] getAllInterfacesForClass(Class<?> clazz) {
		return getAllInterfacesForClass(clazz, null);
	}

	/**
	 * return all interfaces that the given class implements, and is visible in the given classloader
	 * @param clazz the given class
	 * @param classLoader the given classloader
	 * @return return all interfaces
	 */
	private static Class<?>[] getAllInterfacesForClass(Class<?> clazz, @Nullable ClassLoader classLoader) {
		return toClassArray(getAllInterfacesForClassAsSet(clazz, classLoader));
	}

	/**
	 * 为给定接口创建复合接口类，在单个类中实现给定接口
	 * @param interfaces the given interface array
	 * @param classLoader the given classloader
	 * @return return the class
	 */
	public static Class<?> createCompositeInterface(Class<?>[] interfaces, @Nullable ClassLoader classLoader) {
		Assert.notEmpty(interfaces, "Interfaces must not be empty");

		return Proxy.getProxyClass(classLoader, interfaces);
	}

	/**
	 * Replacement for {@code Class.forName()} that also returns Class instances for primitives (e.g. "int") and array class names (e.g. "String[]").
	 * Furthermore, it is also capable of resolving inner class names in Java source style (e.g. "java.lang.Thread.State" instead of "java.lang.Thread$State").
	 * @param name 完全限定类名
	 * @param classLoader 类加载器
	 * @return 返回结果
	 */
	public static Class<?> forName(String name, @Nullable ClassLoader classLoader) throws ClassNotFoundException {
		Assert.notNull(name, "Name must not be null");

		Class<?> clazz = resolvePrimitiveClassName(name);
		if (clazz == null) {
			// 常见的java类
			clazz = COMMON_CLASS_CACHE.get(name);
		}

		if (null != clazz) {
			return clazz;
		}
		// 如果是数组
		if (name.endsWith(ARRAY_SUFFIX)) {
			String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
			Class<?> elementClass = forName(elementClassName, classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}
		// 如果是 "[Ljava.lang.String;"
		if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
			String elementClassName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
			Class<?> elementClass = forName(elementClassName, classLoader);

			return Array.newInstance(elementClass, 0).getClass();
		}

		// "[[I"
		if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
			String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
			Class<?> elementClass = forName(elementName, classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}

		ClassLoader clToUser = classLoader;
		if (clToUser == null) {
			clToUser = getDefaultClassLoader();
		}

		try {
			return Class.forName(name, false, clToUser);
		} catch (ClassNotFoundException ex) {
			int lastDotIndex = name.lastIndexOf(PACKAGE_SEPARATOR);
			if (lastDotIndex != -1) {
				String innerClassName = name.substring(0, lastDotIndex) + INNER_CLASS_SEPARATOR
						+ name.substring(lastDotIndex + 1);
				try {
					return Class.forName(innerClassName, false, clToUser);
				} catch (ClassNotFoundException ex2) {
					// Swallow - let original exception get through
				}
			}
			throw ex;
		}
	}
}
