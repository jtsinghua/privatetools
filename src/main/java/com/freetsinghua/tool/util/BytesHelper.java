package com.freetsinghua.tool.util;

import com.freetsinghua.tool.common.CommonConstant;

/**
 * @author z.tsinghua
 * @date 2019/2/11
 */
public final class BytesHelper {

	private BytesHelper() {
	}

    public static String toBinaryString(long value){
        String formatted = Long.toBinaryString(value);
        StringBuilder buf = new StringBuilder(StringUtils.repeat('0', 64));
        buf.replace(64 - formatted.length(), 64, formatted);

        return buf.toString();
    }

	/**
	 * 用二进制字符串表示指定的整数
	 * @param value 指定的整数
	 * @return 返回结果
	 */
	public static String toBinaryString(int value) {
		String formatted = Integer.toBinaryString(value);
		StringBuilder buf = new StringBuilder(StringUtils.repeat('0', 32));
		buf.replace(32 - formatted.length(), 32, formatted);

		return buf.toString();
	}

	/**
	 * Byte的二进制表示
	 * @param value 指定的数据
	 * @return 返回结果
	 */
	public static String toBinaryString(byte value) {
		String formatted = Integer.toBinaryString(value);
		if (formatted.length() > CommonConstant.BYTES) {
			formatted = formatted.substring(formatted.length() - 8);
		}

		StringBuilder buf = new StringBuilder("00000000");
		buf.replace(8 - formatted.length(), 8, formatted);

		return buf.toString();
	}

	/**
	 * 解释long的二进制表示
	 * @param bytes 指定的字节数组
	 * @return 返回结果
	 */
	public static long toLong(byte[] bytes) {
		return toLong(bytes, 0);
	}

	/**
	 * 将指定的二进制字节数组，转化为长整型
	 * @param bytes 指定的二进制字节数组
	 * @param srcPos 起始位置
	 * @return 返回结果
	 */
	public static long toLong(byte[] bytes, int srcPos) {
		if (bytes == null) {
			return 0L;
		}

		final int size = srcPos + 8;
		if (bytes.length < size) {
			throw new IllegalStateException("Excepting 8 byte value to construct a long value");
		}

		long result = 0L;
		for (int i = srcPos; i < size; i++) {
			result = (result << 8) | (bytes[i] & 0xff);
		}

		return result;
	}

	/**
	 * 将一个{@code long}解释成二进制
	 * @param longValue 指定的长整型
	 * @return 返回结果
	 */
	public static byte[] fromLong(long longValue) {
		byte[] bytes = new byte[8];
		fromlong(longValue, bytes, 0);
		return bytes;
	}

	/**
	 * 将指定的长整型解释为二进制，并缓存在一个字节数组中
	 * @param longValue 指定的长整型
	 * @param destBytes 指定的缓存字节数组
	 * @param destPos 起始位置
	 */
	public static void fromlong(long longValue, byte[] destBytes, int destPos) {
		destBytes[destPos] = (byte) (longValue >> 56);
		destBytes[destPos + 1] = (byte) ((longValue << 8) >> 56);
		destBytes[destPos + 2] = (byte) ((longValue << 16) >> 56);
		destBytes[destPos + 3] = (byte) ((longValue << 24) >> 56);
		destBytes[destPos + 4] = (byte) ((longValue << 32) >> 56);
		destBytes[destPos + 5] = (byte) ((longValue << 40) >> 56);
		destBytes[destPos + 6] = (byte) ((longValue << 48) >> 56);
		destBytes[destPos + 7] = (byte) ((longValue << 56) >> 56);
	}

	/**
	 * 将一个{@code int}解释为二进制形式
	 * @param intValue 指定的整形数据
	 * @return 返回结果
	 */
	public static byte[] fromInt(int intValue) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (intValue >> 24);
		bytes[1] = (byte) ((intValue << 8) >> 24);
		bytes[2] = (byte) ((intValue << 16) >> 24);
		bytes[3] = (byte) ((intValue << 24) >> 24);

		return bytes;
	}

	/**
	 * 将{@code short}解释为二进制形式
	 * @param shortValue 指定的短整型数据
	 * @return 返回结果
	 */
	public static byte[] fromShort(int shortValue) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (shortValue >> 8);
		bytes[1] = (byte) ((shortValue << 8) >> 8);

		return bytes;
	}

	/**
	 * 用于从一系列字节生成int的自定义算法
	 * @param bytes 指定的字节数组
	 * @return 返回结果
	 */
	public static int toInt(byte[] bytes) {
		int result = 0;
		for (int i = 0; i < CommonConstant.BYTES_INTEGER; i++) {
			result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
		}
		return result;
	}
}
