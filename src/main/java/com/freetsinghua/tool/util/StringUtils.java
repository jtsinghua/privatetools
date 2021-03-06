package com.freetsinghua.tool.util;

import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.common.CommonConstant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/** create by @author z.tsinghua at 2018/9/14 */
public class StringUtils {

    public static final String EMPTY_STRING = "";

    /** 判断一个字符串{@code str}是否有长度，即非空，若是非空，则返回true，否则返回false */
    public static boolean hasLength(@Nullable String str) {
        return null != str && !str.isEmpty();
    }

    /**
     * 判断指定的{@link CharSequence}不为空
     *
     * @param str 指定的{@link CharSequence}
     * @return 返回结果
     */
    public static boolean hasLength(CharSequence str) {
        return str != null && str.length() != 0;
    }

    public static String replace(String inString, String oldPattern, @Nullable String newPattern) {
        if (hasLength(inString) && hasLength(oldPattern) && null != newPattern) {
            int index = inString.indexOf(oldPattern);
            if (-1 == index) {
                return inString;
            } else {
                int capacity = inString.length();

                if (oldPattern.length() < newPattern.length()) {
                    capacity += 16;
                }

                StringBuilder builder = new StringBuilder(capacity);

                int pos = 0;
                for (int pathLen = oldPattern.length();
                        index >= 0;
                        index = inString.indexOf(oldPattern, pos)) {
                    builder.append(inString, pos, index);
                    builder.append(newPattern);
                    pos = index + pathLen;
                }

                builder.append(inString.substring(pos));

                return builder.toString();
            }

        } else {
            return inString;
        }
    }

    public static String cleanPath(String path) {
        if (!hasLength(path)) {
            return path;
        } else {
            String pathToUse =
                    replace(
                            path,
                            CommonConstant.WINDOWS_PATH_SEPARATOR,
                            CommonConstant.UNIX_PATH_SEPARATOR);
            // 查找‘：’
            int prefixIndex = pathToUse.indexOf(58);
            String prefix = "";
            if (prefixIndex != -1) {
                // 若是类似e:/uuids.txt这样的路径，则截取'e:'
                prefix = pathToUse.substring(0, prefixIndex + 1);
                if (prefix.contains(CommonConstant.UNIX_PATH_SEPARATOR)) {
                    prefix = "";
                } else {
                    pathToUse = pathToUse.substring(prefixIndex + 1);
                }
            }

            // 路径类似/home/tsinghua/downloads/
            if (pathToUse.startsWith(CommonConstant.UNIX_PATH_SEPARATOR)) {
                prefix = prefix + CommonConstant.UNIX_PATH_SEPARATOR;
                pathToUse = pathToUse.substring(1);
            }

            String[] pathArray =
                    delimitedListToStringArray(pathToUse, CommonConstant.UNIX_PATH_SEPARATOR);
            LinkedList<String> pathElements = new LinkedList<>();
            int tops = 0;

            int i;
            for (i = pathArray.length - 1; i >= 0; --i) {
                String element = pathArray[i];
                if (!".".equals(element)) {
                    if ("..".equals(element)) {
                        ++tops;
                    } else if (tops > 0) {
                        --tops;
                    } else {
                        pathElements.add(0, element);
                    }
                }
            }

            for (i = 0; i < tops; ++i) {
                pathElements.add(0, "..");
            }

            if (pathElements.size() == 1
                    && "".equals(pathElements.getLast())
                    && !prefix.endsWith(CommonConstant.WINDOWS_PATH_SEPARATOR)) {
                pathElements.add(0, ".");
            }

            return prefix
                    + collectionToDelimitedString(pathElements, CommonConstant.UNIX_PATH_SEPARATOR);
        }
    }

    public static String collectionToDelimitedString(
            @Nullable Collection<?> coll, String delim, String prefix, String suffix) {
        if (CollectionUtils.isEmpty(coll)) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            Iterator it = coll.iterator();

            while (it.hasNext()) {
                sb.append(prefix).append(it.next()).append(suffix);
                if (it.hasNext()) {
                    sb.append(delim);
                }
            }

            return sb.toString();
        }
    }

    public static String collectionToDelimitedString(@Nullable Collection<?> coll, String delim) {
        return collectionToDelimitedString(coll, delim, "", "");
    }

    public static String collectionToCommaDelimitedString(Collection<?> coll) {
        return collectionToDelimitedString(coll, ",");
    }

    public static String[] delimitedListToStringArray(
            @Nullable String str, @Nullable String delimiter) {
        return delimitedListToStringArray(str, delimiter, null);
    }

    public static String[] delimitedListToStringArray(
            @Nullable String str, @Nullable String delimiter, @Nullable String charsToDelete) {
        if (str == null) {
            return new String[0];
        } else if (delimiter == null) {
            return new String[] {str};
        } else {
            List<String> result = new ArrayList<>();
            int pos;
            if ("".equals(delimiter)) {
                for (pos = 0; pos < str.length(); ++pos) {
                    result.add(deleteAny(str.substring(pos, pos + 1), charsToDelete));
                }
            } else {
                int delPos;
                for (pos = 0;
                        (delPos = str.indexOf(delimiter, pos)) != -1;
                        pos = delPos + delimiter.length()) {
                    result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                }

                if (str.length() > 0 && pos <= str.length()) {
                    result.add(deleteAny(str.substring(pos), charsToDelete));
                }
            }

            return toStringArray(result);
        }
    }

    public static String deleteAny(String inString, @Nullable String charsToDelete) {
        if (hasLength(inString) && hasLength(charsToDelete)) {
            StringBuilder sb = new StringBuilder(inString.length());

            for (int i = 0; i < inString.length(); ++i) {
                char c = inString.charAt(i);
                if (charsToDelete.indexOf(c) == -1) {
                    sb.append(c);
                }
            }

            return sb.toString();
        } else {
            return inString;
        }
    }

    public static String[] toStringArray(Collection<String> collection) {
        return collection.toArray(new String[0]);
    }

    public static String[] toStringArray(Enumeration<String> enumeration) {
        return toStringArray(Collections.list(enumeration));
    }

    @Nullable
    public static String getFilename(@Nullable String path) {
        if (path == null) {
            return null;
        } else {
            int separatorIndex = path.lastIndexOf("/");
            return separatorIndex != -1 ? path.substring(separatorIndex + 1) : path;
        }
    }

    public static String applyRelativePath(String path, String relativePath) {
        int separatorIndex = path.lastIndexOf("/");
        if (separatorIndex != -1) {
            String newPath = path.substring(0, separatorIndex);
            if (!relativePath.startsWith("/")) {
                newPath = newPath + "/";
            }

            return newPath + relativePath;
        } else {
            return relativePath;
        }
    }

    /**
     * 判断一个字符串是否为空
     *
     * @param value 要判断的字符串
     * @return 返回结果
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    /** 是否包含空白字符以外的 */
    public static boolean hasText(@Nullable String str) {
        return hasLength(str) && containsText(str);
    }

    /**
     * 构建一个字符串，重复指定的次数
     *
     * @param string 指定要重复的字符串
     * @param times 指定的重复次数
     * @return 返回结果
     */
    public static String repeat(String string, int times) {
        StringBuilder buf = new StringBuilder(string.length() * times);
        for (int i = 0; i < times; i++) {
            buf.append(string);
        }

        return buf.toString();
    }

    /**
     * 构造字符串，重复指定的字符指定的次数
     *
     * @param character 指定的字符
     * @param times 指定的重复次数
     * @return 返回结果
     */
    public static String repeat(Character character, int times) {
        char[] chars = new char[times];
        Arrays.fill(chars, character);

        return new String(chars);
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
