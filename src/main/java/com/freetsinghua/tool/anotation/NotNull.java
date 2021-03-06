package com.freetsinghua.tool.anotation;

import java.lang.annotation.*;

/**
 * 标记注解，表示不能为null
 *
 * @author z.tsinghua
 * @date 2019/1/25
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface NotNull {
}
