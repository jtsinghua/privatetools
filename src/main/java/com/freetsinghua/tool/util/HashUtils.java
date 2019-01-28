package com.freetsinghua.tool.util;

import com.freetsinghua.tool.anotation.NotNull;
import com.freetsinghua.tool.anotation.Nullable;
import com.freetsinghua.tool.common.CommonConstant;
import com.google.common.hash.Hashing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.zip.CRC32;

/**
 * 散列工具类
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public class HashUtils {

    private static SecureRandom secureRandom = new SecureRandom();
    private static final ThreadLocal<MessageDigest> SHA1_DIGEST =
            createMessageDigestThreadLocal(CommonConstant.STRING_ALGORITHM_SHA1);
    private static final ThreadLocal<MessageDigest> MD5_DIGEST =
            createMessageDigestThreadLocal(CommonConstant.STRING_ALGORITHM_MD5);

    /* *************SHA1*************** */

    /** sha1散列算法 */
    public static byte[] sha1(@NotNull byte[] input) {
        return digest(input, null, 1, get(SHA1_DIGEST));
    }

    /** sha1散列算法 */
    public static byte[] sha1(@NotNull String input, @Nullable Charset charset) {
        charset = charset == null ? StandardCharsets.UTF_8 : charset;
        return digest(input.getBytes(charset), null, 1, get(SHA1_DIGEST));
    }

    /** sha1散列算法, 使用salt */
    public static byte[] sha1(@NotNull byte[] input, @Nullable byte[] salt) {
        return digest(input, salt, 1, get(SHA1_DIGEST));
    }

    /** sha1散列算法, 使用salt */
    public static byte[] sha1(
            @NotNull String input, @Nullable byte[] salt, @Nullable Charset charset) {
        charset = charset == null ? StandardCharsets.UTF_8 : charset;
        return digest(input.getBytes(charset), salt, 1, get(SHA1_DIGEST));
    }

    /** sha1散列算法, 使用salt,迭代 */
    public static byte[] sha1(@NotNull byte[] input, @Nullable byte[] salt, int iterationCount) {
        return digest(input, salt, iterationCount, get(SHA1_DIGEST));
    }

    /* ******************MD5******************* */

    /** 对文件进行md5散列 */
    public static byte[] md5File(InputStream input) {
        try {
            return digestFile(input, get(MD5_DIGEST));
        } catch (IOException e) {
            throw new RuntimeException("md5File: Failed to encode file", e);
        }
    }

    /* *****************基于JDK的CRC32******************** */

    public static int crc32AsInt(@NotNull String input, @Nullable Charset charset) {
        charset = charset == null ? StandardCharsets.UTF_8 : charset;
        return crc32AsInt(input.getBytes(charset));
    }

    public static int crc32AsInt(@NotNull byte[] input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input);

        return Math.toIntExact(crc32.getValue());
    }

    public static long crc32AsLong(@NotNull String input, @Nullable Charset charset) {
        charset = charset == null ? StandardCharsets.UTF_8 : charset;
        return crc32AsLong(input.getBytes(charset));
    }

    public static long crc32AsLong(@NotNull byte[] input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input);

        return crc32.getValue();
    }

    /* ******************基于Guava的MurMurHash******************** */

    private static final int MURMUR_SEED = 1_318_007_700;

    /** 对输入字符串进行murmur32散列, 返回值可能是负数 */
    public static int murmur32AsInt(@NotNull byte[] input) {
        return Hashing.murmur3_32(MURMUR_SEED).hashBytes(input).asInt();
    }

    /** 对输入字符串进行murmur32散列, 返回值可能是负数 */
    public static int murmur32AsInt(@NotNull String input, @Nullable Charset charset) {
        charset = charset == null ? StandardCharsets.UTF_8 : charset;
        return Hashing.murmur3_32(MURMUR_SEED).hashString(input, charset).asInt();
    }

    /** 对输入字符串进行murmur128散列, 返回值可能是负数 */
    public static long murmur128AsLong(@NotNull byte[] input) {
        return Hashing.murmur3_128(MURMUR_SEED).hashBytes(input).asLong();
    }

    /** 对输入字符串进行murmur128散列, 返回值可能是负数 */
    public static long murmur128AsLong(@NotNull String input, @Nullable Charset charset) {
        charset = charset == null ? StandardCharsets.UTF_8 : charset;
        return Hashing.murmur3_128(MURMUR_SEED).hashString(input, charset).asLong();
    }

    /* **************辅助********************  */

    private static byte[] digestFile(InputStream inputStream, MessageDigest messageDigest)
            throws IOException {
        int bufferLen = 16 * 1024;
        byte[] buffer = new byte[bufferLen];

        int read = inputStream.read(buffer, 0, bufferLen);
        while (read != -1) {
            messageDigest.update(buffer, 0, read);
            read = inputStream.read(buffer, 0, bufferLen);
        }

        return messageDigest.digest();
    }

    private static MessageDigest get(ThreadLocal<MessageDigest> messageDigestThreadLocal) {
        MessageDigest messageDigest = messageDigestThreadLocal.get();

        Assert.notNull(messageDigest, "get: this object[messageDigest] should not null");

        messageDigest.reset();

        return messageDigest;
    }

    /**
     * 创建线程安全的MessageDigest
     *
     * @param algorithm 算法
     */
    private static ThreadLocal<MessageDigest> createMessageDigestThreadLocal(String algorithm) {
        return ThreadLocal.withInitial(
                () -> {
                    try {
                        return MessageDigest.getInstance(algorithm);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(
                                "unexpected exception when create MessageDigest for ["
                                        + algorithm
                                        + "]",
                                e);
                    }
                });
    }

    private static byte[] digest(
            byte[] input, byte[] salt, int iterationCount, MessageDigest messageDigest) {

        if (salt != null) {
            messageDigest.update(salt);
        }

        byte[] result = messageDigest.digest(input);

        for (int i = 0; i < iterationCount - 1; i++) {
            messageDigest.reset();
            result = messageDigest.digest(result);
        }

        return result;
    }

    /**
     * 使用SecureRandom产生随机byte数组
     *
     * @param numBytes byte数组的长度
     */
    public static byte[] generateSalt(int numBytes) {
        Assert.isTrue(numBytes > 0, "argument numBytes should bigger than 0");

        byte[] bytes = new byte[numBytes];
        // 随机
        secureRandom.nextBytes(bytes);

        return bytes;
    }
}
