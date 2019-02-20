package com.freetsinghua.tool.util;

/**
 * @author z.tsinghua
 * @date 2019/2/13
 */
public class SystemUtils {

    public static String getUserDir(){
        return System.getProperty("user.dir");
    }

    public static String getLineSeparator(){
        return System.getProperty("line.separator");
    }
    public static String getFileSeparator(){
        return System.getProperty("file.separator");
    }

}
