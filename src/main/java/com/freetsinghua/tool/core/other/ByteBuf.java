package com.freetsinghua.tool.core.other;

/**
 * @author z.tsinghua
 * @date 2019/1/25
 */
public abstract class ByteBuf implements ReferenceCounted, Comparable<ByteBuf> {

    /** @return 此缓冲区可包含的字节数（八位字节） */
    public abstract int capacity();

    /**
     * 调整此缓冲区的容量。 如果{@code newCapacity}小于当前容量，则会截断此缓冲区的内容。 如果{@code
     * newCapacity}大于当前容量，则缓冲区附加未指定的数据，其长度为（newCapacity - currentCapacity）
     */
    public abstract ByteBuf capacity(int newCapacity);

    /**
     * 返回此缓冲区允许的最大容量。 如果用户尝试使用{@link #capacity（int）}将此缓冲区的*容量增加到超出最大容量，则这些方法将引发 {@link
     * IllegalArgumentException}。
     */
    public abstract int maxCapacity();

    /** 返回创建此缓冲区的{@link ByteBufAllocator} */
    public abstract ByteBufAllocator alloc();

    /** 如果此缓冲区是另一个缓冲区的包装，则返回底层缓冲区实例 */
    public abstract ByteBuf unwrap();

    /** 当且仅当此缓冲区由NIO直接缓冲区支持时，返回{@code true}。 */
    public abstract boolean isDirect();

    /** 当且仅当此缓冲区只读时，返回{@code true} */
    public abstract boolean isReadOnly();

    /** 返回此缓冲区的只读版本 */
    public abstract ByteBuf asReadOnly();

    /** 返回此缓冲区的{@code readerIndex} */
    public abstract int readerIndex();

    /**
     * 设置此缓冲区的{@code readerIndex}
     *
     * @throws IndexOutOfBoundsException 当{@code readerIndex} 小于0，或者大于 {@code writerIndex}时，会抛出异常
     */
    public abstract ByteBuf readerIndex(int readerIndex);

    /** 获取此缓冲区的{@code writerIndex} */
    public abstract int writerIndex();

    /**
     * 设置此缓冲区的{@code writerIndex}
     *
     * @throws IndexOutOfBoundsException 当{@code writerIndex}, 小于{@code readerIndex}， 或者大于{@code
     *     capacity}时，会抛出越界异常
     */
    public abstract ByteBuf writerIndex(int writerIndex);

    /**
     * 设置此缓冲区的{@code readerIndex} 和 {@code writerIndex}
     *
     * <p>{@code readerIndex <= writerIndex <= capacity}
     */
    public abstract ByteBuf setIndex(int readerIndex, int writerIndex);

    /** 返回缓冲区可读的字节数 */
    public abstract int readableBytes();

    /** 返回缓冲区可写的字节数 */
    public abstract int writableBytes();

    /** 返回可写字节的最大可能数，等于* {@code this.maxCapacity - this.writerIndex}。 */
    public abstract int maxWritableBytes();

    /** 当且仅当{@code writerIndex - readerIndex > 0}时才会返回true，否则返回false */
    public abstract boolean isReadable();

    /** 当且仅当{@code capacity - writerIndex > 0}时，才会返回{@code true}，否则返回{@code false} */
    public abstract boolean isWritable();

    /** 当且仅当此缓冲区有足够的空间允许写入指定数量的*元素时，才返回{@code true}。 */
    public abstract boolean isWritable(int size);

    /** 将{@code readerIndex} 和 {@code writerIndex} 同时设为0，等价于 {@code setIndex(0, 0)} */
    public abstract ByteBuf clear();

    /** 标记此缓冲区的{@code readerIndex}，通过调用{@code resetReaderIndex}来复位 */
    public abstract ByteBuf markReaderIndex();

    /**
     * 将{@code readerIndex}复位到{@code markReaderIndex()}方法标记的{@code readerIndex}
     *
     * @throws IndexOutOfBoundsException 如果当前的{@code readerIndex}大于标记的{@code writerIndex}，会抛出异常
     */
    public abstract ByteBuf resetReaderIndex();

    /** 标记此缓冲区的{@code writerIndex} */
    public abstract ByteBuf markWriterIndex();

    /**
     * 复位此缓冲区的{@code writerIndex}
     *
     * @throws IndexOutOfBoundsException 如果当前的{@code readerIndex} 大于标记的{@code writerIndex}时，会抛出异常
     */
    public abstract ByteBuf resetWriterIndex();

    /**
     * 丢弃{@code 0} 到 {@code readerIndex}之间的字节，同时将位于{@code readerIndex} 和 {@code
     * writerIndex}之间的字节到索引0开始。
     *
     * <p>将{@code readerIndex} 设置为0， 将{@code writerIndex}设置为{@code oldWriterIndex - oldReaderIndex}
     */
    public abstract ByteBuf discardReadBytes();

    /**
     * 与{@link ByteBuf#discardReadBytes()}类似
     *
     * <p>不同之处在于此方法可能会丢弃部分，全部或不丢弃读取的字节，具体取决于其内部实现，以减少总内存带宽消耗，但代价是可能额外的内存消耗
     */
    public abstract ByteBuf discardSomeReadBytes();

    /**
     * 保证{@link ByteBuf#writableBytes()}的返回值不小于{@code
     * minWritableBytes}，若是满足条件，则此方法的调用不会有副作用，否则会抛出异常{@link IllegalArgumentException}
     */
    public abstract ByteBuf ensureWritable(int minWritableBytes);

    /**
     * 试图确保{@link ByteBuf#writableBytes()}不小于{@code minWritableBytes}。
     *
     * <p>不同的是，这个方法不会抛出异常
     *
     * @param minWritableBytes 预期的最小可写字节数
     * @param force 当{@code writerIndex + minWritableBytes >= maxCapacity}时，
     *     <li>若是该参数为{@code true}，则将{@code capacity}设置为{@code maxCapacity}
     *     <li>若是该参数为{@code false}，那么不会设置
     * @return 返回一个状态码
     *     <li>如果空间足够，且容量没有改变，则返回0
     *     <li>如果没有足够的空间，且容量没有改变，则返回1
     *     <li>如果有足够的空间，且容量增加，则返回2
     *     <li>如果没有足够的空间，但是其容量{@code capacity} 设置为 {@code maxCapacity}， 则返回3
     */
    public abstract int ensureWritable(int minWritableBytes, boolean force);

    /**
     * 在此缓冲区的指定位置{@code index}获取一个布尔值
     *
     * <p>这个方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果{@code index} 小于0，或者{@code index + 1 > capacity}，则会抛出异常
     */
    public abstract boolean getBoolean(int index);

    /**
     * 在此缓冲区的指定位置{@code index}获取一个字节
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 1 >
     *     capacity}，则会抛出异常
     */
    public abstract byte getByte(int index);

    /**
     * 在此缓冲区的指定位置{@code index}获取一个无符号字节
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 1 >
     *     capacity}，则会抛出异常
     */
    public abstract short getUnsignedByte(int index);

    /**
     * 在此缓冲区的指定位置{@code index}获取一个短整型
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 2 >
     *     capacity}，则会抛出异常
     */
    public abstract short getShort(int index);

    /**
     * 获取Little Endian字节顺序中此缓冲区中指定绝对值{@code index}的16位短整数。
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 2 > *
     *     capacity}，则会抛出异常
     */
    public abstract short getShortLE(int index);

    /**
     * 在此缓冲区的指定位置{@code index}获取一个无符号短整型
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 2 >
     *     capacity}，则会抛出异常
     */
    public abstract int getUnsignedShort(int index);

    /**
     * 获取此缓冲区中指定绝对值{@code index}的16位无符号短整数。
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 2 > *
     *     capacity}，则会抛出异常
     */
    public abstract int getUnsignedShortLE(int index);

    /**
     * 获取此缓冲区中指定绝对值{@code index}的24位整数。
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 3 > *
     *     capacity}，则会抛出异常
     */
    public abstract int getMedium(int index);

    /**
     * 获取Little Endian字节顺序中此缓冲区中指定绝对值{@code index}的24位整数。
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 3 > *
     *     capacity}，则会抛出异常
     */
    public abstract int getMediumLE(int index);

    /**
     * 获取此缓冲区中指定绝对值{@code index}的24位无符号短整数。
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 3 > *
     *     capacity}，则会抛出异常
     */
    public abstract int getUnsignedMedium(int index);
}
