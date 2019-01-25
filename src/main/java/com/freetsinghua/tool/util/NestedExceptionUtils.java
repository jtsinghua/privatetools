package com.freetsinghua.tool.util;

import com.sun.istack.internal.Nullable;

/**
 * create by @author z.tsinghua at 2018/9/14
 */
public abstract class NestedExceptionUtils {
    public NestedExceptionUtils() {
    }
    
    @Nullable
    public static String buildMessage(@Nullable String message, @Nullable Throwable cause){
        if (null == cause){
            return message;
        }else {
            StringBuilder builder = new StringBuilder(64);
            
            if (null != message){
                builder.append(message).append("; ");
            }
            
            builder.append("nested exception is ").append(cause);
            
            return builder.toString();
        }
    }
    
    
    @Nullable
    public static Throwable getRootCause(@Nullable Throwable original){
        if (null == original){
            return null;
        }else {
            Throwable rootCause = null;
            
            for (Throwable cause = original.getCause(); null != cause && cause != rootCause; cause = cause.getCause()){
                rootCause = cause;
            }
            
            return rootCause;
        }
    }
    
    public static Throwable getMostSpecificCause(Throwable original){
        Throwable rootCause = getRootCause(original);
        
        return null != rootCause ? rootCause : original;
    }
}
