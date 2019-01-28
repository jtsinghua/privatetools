package com.freetsinghua.tool.task;

/**
 * 装饰接口
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
@FunctionalInterface
public interface TaskDecorator {

    /** 装饰给定的{@code Runnable}，返回一个可能被包装的{@code Runnable}来实际执行。 */
    Runnable decorate(Runnable runnable);
}
