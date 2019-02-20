package com.freetsinghua.tool.util;

import com.google.common.io.BaseEncoding;

import java.util.Base64;

/**
 * 编码、解码
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public class SecurityUtils {

    /** byte编码为string */
    public static String encodeHex(byte[] input) {
        return BaseEncoding.base64().encode(input);
    }

    /** string解码为byte数组 */
    public static byte[] decodeHex(CharSequence input) {
        return BaseEncoding.base64().decode(input);
    }

    /** base64 编码 */
    public static String encodeBase64(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }

    /** base64解码 */
    public static byte[] decodeBase64(String input) {
        return Base64.getDecoder().decode(input);
    }
}