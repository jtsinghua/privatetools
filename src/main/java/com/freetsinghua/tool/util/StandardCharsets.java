package com.freetsinghua.tool.util;

import com.freetsinghua.tool.common.CommonConstant;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * 标准{@link Charset Charsets}的常量定义。这些字符集保证在JDK每一个版本中都可用。
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public class StandardCharsets {

    /** Seven-bit ASCII */
    public static final Charset US_ASCII = getUsAscii();
    /** ISO Latin Alphabet */
    public static final Charset ISO_8859_1 = getIso88591();
    /** Eight-bit UCS Transformation Format */
    public static final Charset UTF_8 = getUtf8();
    /** Sixteen-bit UCS Transformation Format, big-endian byte order */
    public static final Charset UTF_16BE = getUtf16be();
    /** Sixteen-bit UCS Transformation Format, little-endian byte order */
    public static final Charset UTF_16LE = getUtf16le();
    /**
     * Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark
     */
    public static final Charset UTF_16 = getUtf16();

    private static final boolean CAN_USE;

    static {
        String javaVersion = System.getProperty("java.version").substring(0, 3);
        float v = Float.parseFloat(javaVersion);
        CAN_USE = ObjectUtils.floatCompare(v, CommonConstant.JAVA_VERSION_7) >= 0;
    }

    public static Charset getDefaultCharset(){
        return Charset.defaultCharset();
    }

    private static Charset getUtf16le() {
        return get("UTF-16LE");
    }

    private static Charset getUtf16be() {
        return get("UTF-16BE");
    }

    private static Charset getUtf16() {
        return get("UTF-16");
    }

    private static Charset getUsAscii() {
        return get("US-ASCII");
    }

    private static Charset getUtf8() {
        return get("UTF-8");
    }

    private static Charset getIso88591() {
        return get("ISO-8859-1");
    }

    private static Charset get(String charsetName) {
        if (CAN_USE) {
            switch (charsetName) {
                case "UTF-8":
                    {
                        return java.nio.charset.StandardCharsets.UTF_8;
                    }
                case "ISO-8859-1":
                    {
                        return java.nio.charset.StandardCharsets.ISO_8859_1;
                    }
                case "UTF-16BE":
                    {
                        return java.nio.charset.StandardCharsets.UTF_16BE;
                    }
                case "UTF-16LE":
                    {
                        return java.nio.charset.StandardCharsets.UTF_16LE;
                    }
                case "US-ASCII":
                    {
                        return java.nio.charset.StandardCharsets.US_ASCII;
                    }
                case "UTF-16":
                    {
                        return java.nio.charset.StandardCharsets.UTF_16;
                    }
                default:
                    throw new UnsupportedCharsetException(
                            "not support Charset[" + charsetName + "]");
            }
        }

        return Charset.forName(charsetName);
    }
}
