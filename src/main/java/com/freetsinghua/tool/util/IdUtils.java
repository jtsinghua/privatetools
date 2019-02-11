package com.freetsinghua.tool.util;

import org.omg.CORBA.ByteHolder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 唯一ID
 *
 * @author z.tsinghua
 * @date 2019/2/11
 */
public class IdUtils {

	private static AtomicLong idCounter = new AtomicLong(0L);
	private static final int IP;
	private static final int JVM = (int) (System.currentTimeMillis() >>> 8);
	private volatile static short count = (short) 0;

	static {
		int ipadd;
		try {
			ipadd = BytesHelper.toInt(InetAddress.getLocalHost().getAddress());
		} catch (UnknownHostException e) {
			ipadd = 0;
		}

		IP = ipadd;
	}

	private static int getIp() {
		return IP;
	}

	private static int getJVM() {
		return JVM;
	}

	private static short getCount() {
		synchronized (IdUtils.class) {
			if (count < 0) {
				count = 0;
			}
			return count++;
		}
	}

	private static short getHiTime() {
		return (short) (System.currentTimeMillis() >> 32);
	}

	private static int getLoTime() {
		return (int) System.currentTimeMillis();
	}

	private static String format(int value) {
		String formatted = Integer.toHexString(value);
		StringBuilder buf = new StringBuilder("00000000");
		buf.replace(8 - formatted.length(), 8, formatted);

		return buf.toString();
	}

	private static String format(short value) {
		String formatted = Integer.toHexString(value);
		StringBuilder buf = new StringBuilder("0000");
		buf.replace(4 - formatted.length(), 4, formatted);

		return buf.toString();
	}

	/**
	 * 简单id获取
	 * @return 返回id
	 */
	public static long simpleLongId() {
		return idCounter.incrementAndGet();
	}

	/**
	 *  returns a string of length 32, This string will consist of only hex digits.
	 * Optionally,  the string may be generated with separators between each component of the UUID.
	 * @apiNote 线程安全
	 * @return 返回字符串
	 */
	public static String uuidHexId() {
		return format(getIp()) + format(getJVM()) + format(getHiTime()) + format(getLoTime()) + format(getCount());
	}
}