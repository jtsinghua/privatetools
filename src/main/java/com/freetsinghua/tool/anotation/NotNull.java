package com.freetsinghua.tool.anotation;

import java.lang.annotation.*;

/**
 * 不是null的注解，只是一个标记，并没有实际检查功能
 *
 * @author z.tsinghua
 * @date 2019/1/25
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface NotNull {
}
