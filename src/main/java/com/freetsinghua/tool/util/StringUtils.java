package com.freetsinghua.tool.util;

import com.sun.istack.internal.Nullable;

import java.util.*;

/** create by @author z.tsinghua at 2018/9/14 */
public class StringUtils {

    public static final String EMPTY_STRING = "";

    /** 判断一个字符串{@code str}是否有长度，即非空，若是非空，则返回true，否则返回false */
    public static boolean hasLength(@Nullable String str) {
        return null != str && !str.isEmpty();
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
            String pathToUse = replace(path, "\\", "/");
            int prefixIndex = pathToUse.indexOf(58);
            String prefix = "";
            if (prefixIndex != -1) {
                prefix = pathToUse.substring(0, prefixIndex + 1);
                if (prefix.contains("/")) {
                    prefix = "";
                } else {
                    pathToUse = pathToUse.substring(prefixIndex + 1);
                }
            }

            if (pathToUse.startsWith("/")) {
                prefix = prefix + "/";
                pathToUse = pathToUse.substring(1);
            }

            String[] pathArray = delimitedListToStringArray(pathToUse, "/");
            LinkedList<String> pathElements = new LinkedList();
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
                    && !prefix.endsWith("/")) {
                pathElements.add(0, ".");
            }

            return prefix + collectionToDelimitedString(pathElements, "/");
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
            List<String> result = new ArrayList();
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
