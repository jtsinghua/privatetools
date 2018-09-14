package com.freetsinghua.tool.core.io;

import com.sun.istack.internal.Nullable;

import java.io.IOException;

/**
 * create by @author z.tsinghua at 2018/9/14
 */
public class NestedIOException extends IOException {
    public NestedIOException(String message) {
        super(message);
    }
    
    public NestedIOException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
