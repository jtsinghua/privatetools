package com.freetsinghua.tool.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author z.tsinghua
 * @date 2019/2/15
 */
public class NumberUtils {
    public static boolean isByte(Number number){
        return number instanceof Byte;
    }

    public static boolean isInteger(Number number) {
        return number instanceof Integer;
    }

    public static boolean isLong(Number number){
        return number instanceof Long;
    }

    public static boolean isFloat(Number number){
        return number instanceof Float;
    }

    public static boolean isDouble(Number number){
        return number instanceof Double;
    }

    public static boolean isShort(Number number){
        return number instanceof Short;
    }
}
