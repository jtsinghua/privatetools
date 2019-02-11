package com.freetsinghua.tool.common;

/**
 * @author z.tsinghua
 * @date 2019/1/25
 */
public class CommonConstant {
	public static final String STRING_YES = "yes";
	public static final String STRING_NO = "no";
	public static final String STRING_1 = "1";
	public static final String STRING_0 = "0";
	public static final String STRING_FALSE = "false";
	public static final String STRING_TRUE = "true";

	/** sha1散列算法 */
	public static final String STRING_ALGORITHM_SHA1 = "SHA-1";
	/** MD5散列算法 */
	public static final String STRING_ALGORITHM_MD5 = "MD5";

	/** jdk7 */
	public static final float JAVA_VERSION_7 = 1.7f;

	public static final int K = 1024;

	/** 内核数目 */
	public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	/**
	 * 字节数，整数
	 */
	public static final int BYTES_INTEGER = 4;
	/**
	 * 字节数，长整型
	 */
	public static final int BYTES_LONG = 8;
    public static final int BYTES = 8;
}
