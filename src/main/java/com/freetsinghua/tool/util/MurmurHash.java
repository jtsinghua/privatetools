package com.freetsinghua.tool.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * MurmurHash
 *
 * <p>来自jedis
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public class MurmurHash implements Hashing {
    /** byte数组散列 */
    public static int hashAsInt(byte[] data, int seed) {
        return hash(ByteBuffer.wrap(data), seed);
    }

    /** byte数组的部分散列 */
    public static int hash(byte[] data, int offset, int length, int seed) {
        return hash(ByteBuffer.wrap(data, offset, length), seed);
    }

    /** 将缓冲区中的字节从当前位置散列到限制. */
    private static int hash(ByteBuffer buf, int seed) {
        // save byte order for later restoration
        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        int m = 0x5bd1e995;
        int r = 24;

        int h = seed ^ buf.remaining();

        int k;
        while (buf.remaining() >= 4) {
            k = buf.getInt();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h *= m;
            h ^= k;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            finish.put(buf).rewind();
            h ^= finish.getInt();
            h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        buf.order(byteOrder);
        return h;
    }

    public static long hash64A(byte[] data, int seed) {
        return hash64A(ByteBuffer.wrap(data), seed);
    }

    public static long hash64A(byte[] data, int offset, int length, int seed) {
        return hash64A(ByteBuffer.wrap(data, offset, length), seed);
    }

    private static long hash64A(ByteBuffer buf, int seed) {
        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        long m = 0xc6a4a7935bd1e995L;
        int r = 47;

        long h = seed ^ (buf.remaining() * m);

        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        buf.order(byteOrder);
        return h;
    }

    @Override
    public long hash(byte[] key) {
        return hash64A(key, 0x1234ABCD);
    }

    @Override
    public long hash(String key) {
        return hash(key.getBytes(StandardCharsets.UTF_8));
    }
}
