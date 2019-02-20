package com.freetsinghua.tool.util;

import com.freetsinghua.tool.anotation.Nullable;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 唯一ID
 *
 * @author z.tsinghua
 * @date 2019/2/11
 */
public class IdUtils {
    /**
     * generate an {@link UUID}
     *
     * @return an {@link UUID} string
     */
    public static String generateLocalObjectUUID() {
        return UUID.randomUUID().toString();
    }

    /** 比{{@link #generateLocalObjectUUID()}}有更好的性能 */
    public static String fastUUID() {
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        return new UUID(threadLocalRandom.nextLong(), threadLocalRandom.nextLong()).toString();
    }

    /**
     * 有序id，long型, Twitter算法
     *
     * @return 返回id
     */
    public static long twitterLongId() {
        return TwitterIdGenerator.getInstance().nextId();
    }

    /**
     * 使用MySQL的uuid_short函数获取id
     *
     * @return 返回id
     */
    @Nullable
    public static String uuidShort() {
        return MySqlUUIDShortGenerator.getInstance().getUUid();
    }
}
