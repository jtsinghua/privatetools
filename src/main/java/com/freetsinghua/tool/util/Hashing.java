package com.freetsinghua.tool.util;

import com.freetsinghua.tool.common.CommonConstant;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author z.tsinghua
 * @date 2019/1/28
 */
public interface Hashing {
    long hash(String input);

    long hash(byte[] input);

    /* *********************** */

    ThreadLocal<MessageDigest> md5Holder = new ThreadLocal<>();

    MurmurHash MURMUR_HASH = new MurmurHash();

    Hashing MD5 =
            new Hashing() {
                @Override
                public long hash(String input) {
                    return hash(input.getBytes(StandardCharsets.UTF_8));
                }

                @Override
                public long hash(byte[] input) {

                    try {
                        if (md5Holder.get() == null) {
                            md5Holder.set(
                                    MessageDigest.getInstance(CommonConstant.STRING_ALGORITHM_MD5));
                        }
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException("no md5 algorithm found");
                    }

                    MessageDigest messageDigest = md5Holder.get();

                    messageDigest.reset();
                    messageDigest.update(input);
                    byte[] bValue = messageDigest.digest();

                    return ((long) (bValue[3] & 0xFF) << 24)
                            | ((long) (bValue[2] & 0xFF) << 16)
                            | ((long) (bValue[1] & 0xFF) << 8)
                            | (long) (bValue[0] & 0xFF);
                }
            };
}
