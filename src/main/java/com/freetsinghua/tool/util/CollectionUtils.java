package com.freetsinghua.tool.util;

import com.freetsinghua.tool.anotation.Nullable;

import java.util.Collection;

/**
 * create by @author z.tsinghua at 2018/9/15
 */
public class CollectionUtils {
    
    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
}
