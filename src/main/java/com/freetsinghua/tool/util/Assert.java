package com.freetsinghua.tool.util;

import com.freetsinghua.tool.anotation.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 断言实用程序类，它有助于验证参数
 *
 * @author z.tsinghua
 * @date 2019/1/26
 */
public abstract class Assert {

    /**
     * 状态判断方法
     *
     * @param expression 布尔表达式
     * @param message 断言失败时要使用的异常消息
     * @throws IllegalStateException 若是表达式不成立，则抛出异常
     */
    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * 状态判断方法
     *
     * @param expression 断言表达式
     * @param messageSupplier 断言失败，使用的异常信息
     * @throws IllegalStateException 如果表达式为{@code false} 则抛出异常
     */
    public static void state(boolean expression, @Nullable Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalStateException(safeGet(messageSupplier));
        }
    }

    /**
     * 断言一个布尔表达式，若是为{@code false}，则抛出{@link IllegalArgumentException}
     *
     * @param expression 布尔表达式
     * @param message 若是布尔表达式为{@code false}，则作为异常信息
     * @throws IllegalArgumentException 布尔表达式为{@code false},抛出异常
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言一个布尔表达式，若是为{@code false}，则抛出{@link IllegalArgumentException}
     *
     * @param expression 布尔表达式
     * @param messageSupplier 若是布尔表达式为{@code false}，则作为异常信息
     * @throws IllegalArgumentException 布尔表达式为{@code false},抛出异常
     */
    public static void isTrue(boolean expression, @Nullable Supplier<String> messageSupplier) {
        if (!expression) {
            throw new IllegalArgumentException(safeGet(messageSupplier));
        }
    }

    /**
     * 断言一个对象时是否是{@code null}
     *
     * @param object 对象
     * @param message 断言失败，用作异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void isNull(@Nullable Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言一个对象时是否是{@code null}
     *
     * @param object 对象
     * @param messageSupplier 断言失败，用作异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void isNull(Object object, Supplier<String> messageSupplier) {
        if (object != null) {
            throw new IllegalArgumentException(safeGet(messageSupplier));
        }
    }

    /**
     * 断言一个对象不是{@code null}
     *
     * @param object 对象
     * @param message 断言失败，异常消息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言一个对象不是{@code null}
     *
     * @param object 对象
     * @param messageSupplier 断言失败，异常消息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void notNull(
            @Nullable Object object, @Nullable Supplier<String> messageSupplier) {
        if (object == null) {
            throw new IllegalArgumentException(safeGet(messageSupplier));
        }
    }

    /**
     * 断言一个字符串是否有长度
     *
     * @param text 字符串
     * @param message 断言失败，异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void hasLength(@Nullable String text, String message) {
        if (!StringUtils.hasLength(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言一个字符串是否有长度
     *
     * @param text 字符串
     * @param messageSupplier 断言失败，异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void hasLength(
            @Nullable String text, @Nullable Supplier<String> messageSupplier) {
        if (!StringUtils.hasLength(text)) {
            throw new IllegalArgumentException(safeGet(messageSupplier));
        }
    }

    /**
     * 断言一个字符串是否有非空白字符
     *
     * @param text 字符串
     * @param message 断言失败，异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void hasText(@Nullable String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言一个字符串是否有非空白字符
     *
     * @param text 字符串
     * @param messageSupplier 断言失败，异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void hasText(@Nullable String text, @Nullable Supplier<String> messageSupplier) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(safeGet(messageSupplier));
        }
    }

    /**
     * 断言字符串{@code textToSearch}不包含{@code subString}
     *
     * @param message 断言失败，异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void doesNotContain(
            @Nullable String textToSearch, @Nullable String subString, String message) {
        if (StringUtils.hasLength(textToSearch)
                && StringUtils.hasLength(subString)
                && textToSearch.contains(subString)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言字符串{@code textToSearch}不包含{@code subString}
     *
     * @param messageSupplier 断言失败，异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void doesNotContain(
            @Nullable String textToSearch,
            @Nullable String subString,
            @Nullable Supplier<String> messageSupplier) {
        if (StringUtils.hasLength(textToSearch)
                && StringUtils.hasLength(subString)
                && textToSearch.contains(subString)) {
            throw new IllegalArgumentException(safeGet(messageSupplier));
        }
    }

    /**
     * 断言一个数组不为空
     *
     * @param array 数组
     * @param message 断言失败，异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void notEmpty(@Nullable Object[] array, String message) {
        if (ObjectUtils.isArrayEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言一个数组不为空
     *
     * @param array 数组
     * @param messageSupplier 断言失败，异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void notEmpty(
            @Nullable Object[] array, @Nullable Supplier<String> messageSupplier) {
        if (ObjectUtils.isArrayEmpty(array)) {
            throw new IllegalArgumentException(safeGet(messageSupplier));
        }
    }

    /**
     * 断言数组{@code array}中没有{@code null}
     *
     * @param message 断言失败时异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void nonNullElements(@Nullable Object[] array, String message) {
        if (array != null) {
            for (Object obj : array) {
                if (obj == null) {
                    throw new IllegalArgumentException(message);
                }
            }
        }
    }

    /**
     * 断言数组{@code array}中没有{@code null}
     *
     * @param messageSupplier 断言失败时异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void nonNullElements(
            @Nullable Object[] array, @Nullable Supplier<String> messageSupplier) {
        if (array != null) {
            for (Object obj : array) {
                if (obj == null) {
                    throw new IllegalArgumentException(safeGet(messageSupplier));
                }
            }
        }
    }

    /**
     * 断言集合{@code collection}不为空
     *
     * @param message 断言失败时，异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void notEmpty(@Nullable Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言集合{@code collection}不为空
     *
     * @param messageSupplier 断言失败时，异常信息
     * @throws IllegalArgumentException 断言失败，抛出异常
     */
    public static void notEmpty(
            @Nullable Collection<?> collection, Supplier<String> messageSupplier) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(safeGet(messageSupplier));
        }
    }

    @Nullable
    private static String safeGet(@Nullable Supplier<String> stringSupplier) {
        if (stringSupplier == null) {
            return null;
        }

        return stringSupplier.get();
    }
}
