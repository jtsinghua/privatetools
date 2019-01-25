package com.freetsinghua.tool.anotation;

import java.lang.annotation.*;

/**
 * @author z.tsinghua
 * @date 2019/1/25
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface Nullable {
}
