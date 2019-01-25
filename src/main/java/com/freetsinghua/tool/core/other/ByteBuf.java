package com.freetsinghua.tool.core.other;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

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

    /**
     * 获取此缓冲区中指定绝对值{@code index}的32位整数
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 4 >
     *     capacity}，则会抛出异常
     */
    public abstract int getInt(int index);

    /**
     * 以Little Endian字节序获取此缓冲区中指定绝对值{@code index}的32位整数
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 4 >
     *     capacity}，则会抛出异常
     */
    public abstract int getIntLE(int index);

    /**
     * 获取此缓冲区中指定绝对值{@code index}的32位无符号整数
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 4 >
     *     capacity}，则会抛出异常
     */
    public abstract long getUnsignedInt(int index);

    /**
     * 以Little Endian字节序获取此缓冲区中指定绝对值{@code index}的32位无符号整数
     *
     * <p>此方法不会改变{@code readerIndex} 和 {@code writerIndex}的值
     *
     * @throws IndexOutOfBoundsException 如果指定的{@code index}小于0， 或者{@code index + 4 >
     *     capacity}，则会抛出异常
     */
    public abstract long getUnsignedIntLE(int index);

    /**
     * Gets a 64-bit long integer at the specified absolute {@code index} in this buffer. This
     * method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 8} is greater than {@code this.capacity}
     */
    public abstract long getLong(int index);

    /**
     * Gets a 64-bit long integer at the specified absolute {@code index} in this buffer in Little
     * Endian Byte Order. This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 8} is greater than {@code this.capacity}
     */
    public abstract long getLongLE(int index);

    /**
     * Gets a 2-byte UTF-16 character at the specified absolute {@code index} in this buffer. This
     * method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 2} is greater than {@code this.capacity}
     */
    public abstract char getChar(int index);

    /**
     * Gets a 32-bit floating point number at the specified absolute {@code index} in this buffer.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 4} is greater than {@code this.capacity}
     */
    public abstract float getFloat(int index);

    /**
     * Gets a 32-bit floating point number at the specified absolute {@code index} in this buffer in
     * Little Endian Byte Order. This method does not modify {@code readerIndex} or {@code
     * writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 4} is greater than {@code this.capacity}
     */
    public float getFloatLE(int index) {
        return Float.intBitsToFloat(getIntLE(index));
    }

    /**
     * Gets a 64-bit floating point number at the specified absolute {@code index} in this buffer.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 8} is greater than {@code this.capacity}
     */
    public abstract double getDouble(int index);

    /**
     * Gets a 64-bit floating point number at the specified absolute {@code index} in this buffer in
     * Little Endian Byte Order. This method does not modify {@code readerIndex} or {@code
     * writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 8} is greater than {@code this.capacity}
     */
    public double getDoubleLE(int index) {
        return Double.longBitsToDouble(getLongLE(index));
    }

    /**
     * Transfers this buffer's data to the specified destination starting at the specified absolute
     * {@code index} until the destination becomes non-writable. This method is basically same with
     * {@link #getBytes(int, ByteBuf, int, int)}, except that this method increases the {@code
     * writerIndex} of the destination by the number of the transferred bytes while {@link
     * #getBytes(int, ByteBuf, int, int)} does not. This method does not modify {@code readerIndex}
     * or {@code writerIndex} of the source buffer (i.e. {@code this}).
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + dst.writableBytes} is greater than {@code this.capacity}
     */
    public abstract ByteBuf getBytes(int index, ByteBuf dst);

    /**
     * Transfers this buffer's data to the specified destination starting at the specified absolute
     * {@code index}. This method is basically same with {@link #getBytes(int, ByteBuf, int, int)},
     * except that this method increases the {@code writerIndex} of the destination by the number of
     * the transferred bytes while {@link #getBytes(int, ByteBuf, int, int)} does not. This method
     * does not modify {@code readerIndex} or {@code writerIndex} of the source buffer (i.e. {@code
     * this}).
     *
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0}, if
     *     {@code index + length} is greater than {@code this.capacity}, or if {@code length} is
     *     greater than {@code dst.writableBytes}
     */
    public abstract ByteBuf getBytes(int index, ByteBuf dst, int length);

    /**
     * Transfers this buffer's data to the specified destination starting at the specified absolute
     * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of both
     * the source (i.e. {@code this}) and the destination.
     *
     * @param dstIndex the first index of the destination
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0}, if
     *     the specified {@code dstIndex} is less than {@code 0}, if {@code index + length} is
     *     greater than {@code this.capacity}, or if {@code dstIndex + length} is greater than
     *     {@code dst.capacity}
     */
    public abstract ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length);

    /**
     * Transfers this buffer's data to the specified destination starting at the specified absolute
     * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of this
     * buffer
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + dst.length} is greater than {@code this.capacity}
     */
    public abstract ByteBuf getBytes(int index, byte[] dst);

    /**
     * Transfers this buffer's data to the specified destination starting at the specified absolute
     * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of this
     * buffer.
     *
     * @param dstIndex the first index of the destination
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0}, if
     *     the specified {@code dstIndex} is less than {@code 0}, if {@code index + length} is
     *     greater than {@code this.capacity}, or if {@code dstIndex + length} is greater than
     *     {@code dst.length}
     */
    public abstract ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length);

    /**
     * Transfers this buffer's data to the specified destination starting at the specified absolute
     * {@code index} until the destination's position reaches its limit. This method does not modify
     * {@code readerIndex} or {@code writerIndex} of this buffer while the destination's {@code
     * position} will be increased.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + dst.remaining()} is greater than {@code this.capacity}
     */
    public abstract ByteBuf getBytes(int index, ByteBuffer dst);

    /**
     * Transfers this buffer's data to the specified stream starting at the specified absolute
     * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of this
     * buffer.
     *
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + length} is greater than {@code this.capacity}
     * @throws IOException if the specified stream threw an exception during I/O
     */
    public abstract ByteBuf getBytes(int index, OutputStream out, int length) throws IOException;

    /**
     * Transfers this buffer's data to the specified channel starting at the specified absolute
     * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of this
     * buffer.
     *
     * @param length the maximum number of bytes to transfer
     * @return the actual number of bytes written out to the specified channel
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + length} is greater than {@code this.capacity}
     * @throws IOException if the specified channel threw an exception during I/O
     */
    public abstract int getBytes(int index, GatheringByteChannel out, int length)
            throws IOException;

    /**
     * Transfers this buffer's data starting at the specified absolute {@code index} to the
     * specified channel starting at the given file position. This method does not modify {@code
     * readerIndex} or {@code writerIndex} of this buffer. This method does not modify the channel's
     * position.
     *
     * @param position the file position at which the transfer is to begin
     * @param length the maximum number of bytes to transfer
     * @return the actual number of bytes written out to the specified channel
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + length} is greater than {@code this.capacity}
     * @throws IOException if the specified channel threw an exception during I/O
     */
    public abstract int getBytes(int index, FileChannel out, long position, int length)
            throws IOException;

    /**
     * Gets a {@link CharSequence} with the given length at the given index.
     *
     * @param length the length to read
     * @param charset that should be used
     * @return the sequence
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.readableBytes}
     */
    public abstract CharSequence getCharSequence(int index, int length, Charset charset);

    /**
     * Sets the specified boolean at the specified absolute {@code index} in this buffer. This
     * method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 1} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setBoolean(int index, boolean value);

    /**
     * Sets the specified byte at the specified absolute {@code index} in this buffer. The 24
     * high-order bits of the specified value are ignored. This method does not modify {@code
     * readerIndex} or {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 1} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setByte(int index, int value);

    /**
     * Sets the specified 16-bit short integer at the specified absolute {@code index} in this
     * buffer. The 16 high-order bits of the specified value are ignored. This method does not
     * modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 2} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setShort(int index, int value);

    /**
     * Sets the specified 16-bit short integer at the specified absolute {@code index} in this
     * buffer with the Little Endian Byte Order. The 16 high-order bits of the specified value are
     * ignored. This method does not modify {@code readerIndex} or {@code writerIndex} of this
     * buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 2} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setShortLE(int index, int value);

    /**
     * Sets the specified 24-bit medium integer at the specified absolute {@code index} in this
     * buffer. Please note that the most significant byte is ignored in the specified value. This
     * method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 3} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setMedium(int index, int value);

    /**
     * Sets the specified 24-bit medium integer at the specified absolute {@code index} in this
     * buffer in the Little Endian Byte Order. Please note that the most significant byte is ignored
     * in the specified value. This method does not modify {@code readerIndex} or {@code
     * writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 3} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setMediumLE(int index, int value);

    /**
     * Sets the specified 32-bit integer at the specified absolute {@code index} in this buffer.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 4} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setInt(int index, int value);

    /**
     * Sets the specified 32-bit integer at the specified absolute {@code index} in this buffer with
     * Little Endian byte order . This method does not modify {@code readerIndex} or {@code
     * writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 4} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setIntLE(int index, int value);

    /**
     * Sets the specified 64-bit long integer at the specified absolute {@code index} in this
     * buffer. This method does not modify {@code readerIndex} or {@code writerIndex} of this
     * buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 8} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setLong(int index, long value);

    /**
     * Sets the specified 64-bit long integer at the specified absolute {@code index} in this buffer
     * in Little Endian Byte Order. This method does not modify {@code readerIndex} or {@code
     * writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 8} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setLongLE(int index, long value);

    /**
     * Sets the specified 2-byte UTF-16 character at the specified absolute {@code index} in this
     * buffer. The 16 high-order bits of the specified value are ignored. This method does not
     * modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 2} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setChar(int index, int value);

    /**
     * Sets the specified 32-bit floating-point number at the specified absolute {@code index} in
     * this buffer. This method does not modify {@code readerIndex} or {@code writerIndex} of this
     * buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 4} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setFloat(int index, float value);

    /**
     * Sets the specified 32-bit floating-point number at the specified absolute {@code index} in
     * this buffer in Little Endian Byte Order. This method does not modify {@code readerIndex} or
     * {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 4} is greater than {@code this.capacity}
     */
    public ByteBuf setFloatLE(int index, float value) {
        return setIntLE(index, Float.floatToRawIntBits(value));
    }

    /**
     * Sets the specified 64-bit floating-point number at the specified absolute {@code index} in
     * this buffer. This method does not modify {@code readerIndex} or {@code writerIndex} of this
     * buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 8} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setDouble(int index, double value);

    /**
     * Sets the specified 64-bit floating-point number at the specified absolute {@code index} in
     * this buffer in Little Endian Byte Order. This method does not modify {@code readerIndex} or
     * {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *     {@code index + 8} is greater than {@code this.capacity}
     */
    public ByteBuf setDoubleLE(int index, double value) {
        return setLongLE(index, Double.doubleToRawLongBits(value));
    }

    /**
     * Transfers the specified source buffer's data to this buffer starting at the specified
     * absolute {@code index} until the source buffer becomes unreadable. This method is basically
     * same with {@link #setBytes(int, ByteBuf, int, int)}, except that this method increases the
     * {@code readerIndex} of the source buffer by the number of the transferred bytes while {@link
     * #setBytes(int, ByteBuf, int, int)} does not. This method does not modify {@code readerIndex}
     * or {@code writerIndex} of the source buffer (i.e. {@code this}).
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + src.readableBytes} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setBytes(int index, ByteBuf src);

    /**
     * Transfers the specified source buffer's data to this buffer starting at the specified
     * absolute {@code index}. This method is basically same with {@link #setBytes(int, ByteBuf,
     * int, int)}, except that this method increases the {@code readerIndex} of the source buffer by
     * the number of the transferred bytes while {@link #setBytes(int, ByteBuf, int, int)} does not.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of the source buffer
     * (i.e. {@code this}).
     *
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0}, if
     *     {@code index + length} is greater than {@code this.capacity}, or if {@code length} is
     *     greater than {@code src.readableBytes}
     */
    public abstract ByteBuf setBytes(int index, ByteBuf src, int length);

    /**
     * Transfers the specified source buffer's data to this buffer starting at the specified
     * absolute {@code index}. This method does not modify {@code readerIndex} or {@code
     * writerIndex} of both the source (i.e. {@code this}) and the destination.
     *
     * @param srcIndex the first index of the source
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0}, if
     *     the specified {@code srcIndex} is less than {@code 0}, if {@code index + length} is
     *     greater than {@code this.capacity}, or if {@code srcIndex + length} is greater than
     *     {@code src.capacity}
     */
    public abstract ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length);

    /**
     * Transfers the specified source array's data to this buffer starting at the specified absolute
     * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of this
     * buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + src.length} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setBytes(int index, byte[] src);

    /**
     * Transfers the specified source array's data to this buffer starting at the specified absolute
     * {@code index}. This method does not modify {@code readerIndex} or {@code writerIndex} of this
     * buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0}, if
     *     the specified {@code srcIndex} is less than {@code 0}, if {@code index + length} is
     *     greater than {@code this.capacity}, or if {@code srcIndex + length} is greater than
     *     {@code src.length}
     */
    public abstract ByteBuf setBytes(int index, byte[] src, int srcIndex, int length);

    /**
     * Transfers the specified source buffer's data to this buffer starting at the specified
     * absolute {@code index} until the source buffer's position reaches its limit. This method does
     * not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + src.remaining()} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setBytes(int index, ByteBuffer src);

    /**
     * Transfers the content of the specified source stream to this buffer starting at the specified
     * absolute {@code index}. This method does not modify {@code readerIndex} or {@code
     * writerIndex} of this buffer.
     *
     * @param length the number of bytes to transfer
     * @return the actual number of bytes read in from the specified channel. {@code -1} if the
     *     specified channel is closed.
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + length} is greater than {@code this.capacity}
     * @throws IOException if the specified stream threw an exception during I/O
     */
    public abstract int setBytes(int index, InputStream in, int length) throws IOException;

    /**
     * Transfers the content of the specified source channel to this buffer starting at the
     * specified absolute {@code index}. This method does not modify {@code readerIndex} or {@code
     * writerIndex} of this buffer.
     *
     * @param length the maximum number of bytes to transfer
     * @return the actual number of bytes read in from the specified channel. {@code -1} if the
     *     specified channel is closed.
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + length} is greater than {@code this.capacity}
     * @throws IOException if the specified channel threw an exception during I/O
     */
    public abstract int setBytes(int index, ScatteringByteChannel in, int length)
            throws IOException;

    /**
     * Transfers the content of the specified source channel starting at the given file position to
     * this buffer starting at the specified absolute {@code index}. This method does not modify
     * {@code readerIndex} or {@code writerIndex} of this buffer. This method does not modify the
     * channel's position.
     *
     * @param position the file position at which the transfer is to begin
     * @param length the maximum number of bytes to transfer
     * @return the actual number of bytes read in from the specified channel. {@code -1} if the
     *     specified channel is closed.
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + length} is greater than {@code this.capacity}
     * @throws IOException if the specified channel threw an exception during I/O
     */
    public abstract int setBytes(int index, FileChannel in, long position, int length)
            throws IOException;

    /**
     * Fills this buffer with <tt>NUL (0x00)</tt> starting at the specified absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param length the number of <tt>NUL</tt>s to write to the buffer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or if
     *     {@code index + length} is greater than {@code this.capacity}
     */
    public abstract ByteBuf setZero(int index, int length);

    /**
     * Writes the specified {@link CharSequence} at the current {@code writerIndex} and increases
     * the {@code writerIndex} by the written bytes.
     *
     * @param index on which the sequence should be written
     * @param sequence to write
     * @param charset that should be used.
     * @return the written number of bytes.
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is not large enough to write
     *     the whole sequence
     */
    public abstract int setCharSequence(int index, CharSequence sequence, Charset charset);

    /**
     * Gets a boolean at the current {@code readerIndex} and increases the {@code readerIndex} by
     * {@code 1} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 1}
     */
    public abstract boolean readBoolean();

    /**
     * Gets a byte at the current {@code readerIndex} and increases the {@code readerIndex} by
     * {@code 1} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 1}
     */
    public abstract byte readByte();

    /**
     * Gets an unsigned byte at the current {@code readerIndex} and increases the {@code
     * readerIndex} by {@code 1} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 1}
     */
    public abstract short readUnsignedByte();

    /**
     * Gets a 16-bit short integer at the current {@code readerIndex} and increases the {@code
     * readerIndex} by {@code 2} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
     */
    public abstract short readShort();

    /**
     * Gets a 16-bit short integer at the current {@code readerIndex} in the Little Endian Byte
     * Order and increases the {@code readerIndex} by {@code 2} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
     */
    public abstract short readShortLE();

    /**
     * Gets an unsigned 16-bit short integer at the current {@code readerIndex} and increases the
     * {@code readerIndex} by {@code 2} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
     */
    public abstract int readUnsignedShort();

    /**
     * Gets an unsigned 16-bit short integer at the current {@code readerIndex} in the Little Endian
     * Byte Order and increases the {@code readerIndex} by {@code 2} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
     */
    public abstract int readUnsignedShortLE();

    /**
     * Gets a 24-bit medium integer at the current {@code readerIndex} and increases the {@code
     * readerIndex} by {@code 3} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 3}
     */
    public abstract int readMedium();

    /**
     * Gets a 24-bit medium integer at the current {@code readerIndex} in the Little Endian Byte
     * Order and increases the {@code readerIndex} by {@code 3} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 3}
     */
    public abstract int readMediumLE();

    /**
     * Gets an unsigned 24-bit medium integer at the current {@code readerIndex} and increases the
     * {@code readerIndex} by {@code 3} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 3}
     */
    public abstract int readUnsignedMedium();

    /**
     * Gets an unsigned 24-bit medium integer at the current {@code readerIndex} in the Little
     * Endian Byte Order and increases the {@code readerIndex} by {@code 3} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 3}
     */
    public abstract int readUnsignedMediumLE();

    /**
     * Gets a 32-bit integer at the current {@code readerIndex} and increases the {@code
     * readerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
     */
    public abstract int readInt();

    /**
     * Gets a 32-bit integer at the current {@code readerIndex} in the Little Endian Byte Order and
     * increases the {@code readerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
     */
    public abstract int readIntLE();

    /**
     * Gets an unsigned 32-bit integer at the current {@code readerIndex} and increases the {@code
     * readerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
     */
    public abstract long readUnsignedInt();

    /**
     * Gets an unsigned 32-bit integer at the current {@code readerIndex} in the Little Endian Byte
     * Order and increases the {@code readerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
     */
    public abstract long readUnsignedIntLE();

    /**
     * Gets a 64-bit integer at the current {@code readerIndex} and increases the {@code
     * readerIndex} by {@code 8} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 8}
     */
    public abstract long readLong();

    /**
     * Gets a 64-bit integer at the current {@code readerIndex} in the Little Endian Byte Order and
     * increases the {@code readerIndex} by {@code 8} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 8}
     */
    public abstract long readLongLE();

    /**
     * Gets a 2-byte UTF-16 character at the current {@code readerIndex} and increases the {@code
     * readerIndex} by {@code 2} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
     */
    public abstract char readChar();

    /**
     * Gets a 32-bit floating point number at the current {@code readerIndex} and increases the
     * {@code readerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
     */
    public abstract float readFloat();

    /**
     * Gets a 32-bit floating point number at the current {@code readerIndex} in Little Endian Byte
     * Order and increases the {@code readerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
     */
    public float readFloatLE() {
        return Float.intBitsToFloat(readIntLE());
    }

    /**
     * Gets a 64-bit floating point number at the current {@code readerIndex} and increases the
     * {@code readerIndex} by {@code 8} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 8}
     */
    public abstract double readDouble();

    /**
     * Gets a 64-bit floating point number at the current {@code readerIndex} in Little Endian Byte
     * Order and increases the {@code readerIndex} by {@code 8} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 8}
     */
    public double readDoubleLE() {
        return Double.longBitsToDouble(readLongLE());
    }

    /**
     * Transfers this buffer's data to a newly created buffer starting at the current {@code
     * readerIndex} and increases the {@code readerIndex} by the number of the transferred bytes (=
     * {@code length}). The returned buffer's {@code readerIndex} and {@code writerIndex} are {@code
     * 0} and {@code length} respectively.
     *
     * @param length the number of bytes to transfer
     * @return the newly created buffer which contains the transferred bytes
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.readableBytes}
     */
    public abstract ByteBuf readBytes(int length);

    /**
     * Returns a new slice of this buffer's sub-region starting at the current {@code readerIndex}
     * and increases the {@code readerIndex} by the size of the new slice (= {@code length}).
     *
     * <p>Also be aware that this method will NOT call {@link #retain()} and so the reference count
     * will NOT be increased.
     *
     * @param length the size of the new slice
     * @return the newly created slice
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.readableBytes}
     */
    public abstract ByteBuf readSlice(int length);

    /**
     * Returns a new retained slice of this buffer's sub-region starting at the current {@code
     * readerIndex} and increases the {@code readerIndex} by the size of the new slice (= {@code
     * length}).
     *
     * <p>Note that this method returns a {@linkplain #retain() retained} buffer unlike {@link
     * #readSlice(int)}. This method behaves similarly to {@code readSlice(...).retain()} except
     * that this method may return a buffer implementation that produces less garbage.
     *
     * @param length the size of the new slice
     * @return the newly created slice
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.readableBytes}
     */
    public abstract ByteBuf readRetainedSlice(int length);

    /**
     * Transfers this buffer's data to the specified destination starting at the current {@code
     * readerIndex} until the destination becomes non-writable, and increases the {@code
     * readerIndex} by the number of the transferred bytes. This method is basically same with
     * {@link #readBytes(ByteBuf, int, int)}, except that this method increases the {@code
     * writerIndex} of the destination by the number of the transferred bytes while {@link
     * #readBytes(ByteBuf, int, int)} does not.
     *
     * @throws IndexOutOfBoundsException if {@code dst.writableBytes} is greater than {@code
     *     this.readableBytes}
     */
    public abstract ByteBuf readBytes(ByteBuf dst);

    /**
     * Transfers this buffer's data to the specified destination starting at the current {@code
     * readerIndex} and increases the {@code readerIndex} by the number of the transferred bytes (=
     * {@code length}). This method is basically same with {@link #readBytes(ByteBuf, int, int)},
     * except that this method increases the {@code writerIndex} of the destination by the number of
     * the transferred bytes (= {@code length}) while {@link #readBytes(ByteBuf, int, int)} does
     * not.
     *
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.readableBytes} or if {@code length} is greater than {@code dst.writableBytes}
     */
    public abstract ByteBuf readBytes(ByteBuf dst, int length);

    /**
     * Transfers this buffer's data to the specified destination starting at the current {@code
     * readerIndex} and increases the {@code readerIndex} by the number of the transferred bytes (=
     * {@code length}).
     *
     * @param dstIndex the first index of the destination
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code dstIndex} is less than {@code 0},
     *     if {@code length} is greater than {@code this.readableBytes}, or if {@code dstIndex +
     *     length} is greater than {@code dst.capacity}
     */
    public abstract ByteBuf readBytes(ByteBuf dst, int dstIndex, int length);

    /**
     * Transfers this buffer's data to the specified destination starting at the current {@code
     * readerIndex} and increases the {@code readerIndex} by the number of the transferred bytes (=
     * {@code dst.length}).
     *
     * @throws IndexOutOfBoundsException if {@code dst.length} is greater than {@code
     *     this.readableBytes}
     */
    public abstract ByteBuf readBytes(byte[] dst);

    /**
     * Transfers this buffer's data to the specified destination starting at the current {@code
     * readerIndex} and increases the {@code readerIndex} by the number of the transferred bytes (=
     * {@code length}).
     *
     * @param dstIndex the first index of the destination
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code dstIndex} is less than {@code 0},
     *     if {@code length} is greater than {@code this.readableBytes}, or if {@code dstIndex +
     *     length} is greater than {@code dst.length}
     */
    public abstract ByteBuf readBytes(byte[] dst, int dstIndex, int length);

    /**
     * Transfers this buffer's data to the specified destination starting at the current {@code
     * readerIndex} until the destination's position reaches its limit, and increases the {@code
     * readerIndex} by the number of the transferred bytes.
     *
     * @throws IndexOutOfBoundsException if {@code dst.remaining()} is greater than {@code
     *     this.readableBytes}
     */
    public abstract ByteBuf readBytes(ByteBuffer dst);

    /**
     * Transfers this buffer's data to the specified stream starting at the current {@code
     * readerIndex}.
     *
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.readableBytes}
     * @throws IOException if the specified stream threw an exception during I/O
     */
    public abstract ByteBuf readBytes(OutputStream out, int length) throws IOException;

    /**
     * Transfers this buffer's data to the specified stream starting at the current {@code
     * readerIndex}.
     *
     * @param length the maximum number of bytes to transfer
     * @return the actual number of bytes written out to the specified channel
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.readableBytes}
     * @throws IOException if the specified channel threw an exception during I/O
     */
    public abstract int readBytes(GatheringByteChannel out, int length) throws IOException;

    /**
     * Gets a {@link CharSequence} with the given length at the current {@code readerIndex} and
     * increases the {@code readerIndex} by the given length.
     *
     * @param length the length to read
     * @param charset that should be used
     * @return the sequence
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.readableBytes}
     */
    public abstract CharSequence readCharSequence(int length, Charset charset);

    /**
     * Transfers this buffer's data starting at the current {@code readerIndex} to the specified
     * channel starting at the given file position. This method does not modify the channel's
     * position.
     *
     * @param position the file position at which the transfer is to begin
     * @param length the maximum number of bytes to transfer
     * @return the actual number of bytes written out to the specified channel
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.readableBytes}
     * @throws IOException if the specified channel threw an exception during I/O
     */
    public abstract int readBytes(FileChannel out, long position, int length) throws IOException;

    /**
     * Increases the current {@code readerIndex} by the specified {@code length} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.readableBytes}
     */
    public abstract ByteBuf skipBytes(int length);

    /**
     * Sets the specified boolean at the current {@code writerIndex} and increases the {@code
     * writerIndex} by {@code 1} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 1}
     */
    public abstract ByteBuf writeBoolean(boolean value);

    /**
     * Sets the specified byte at the current {@code writerIndex} and increases the {@code
     * writerIndex} by {@code 1} in this buffer. The 24 high-order bits of the specified value are
     * ignored.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 1}
     */
    public abstract ByteBuf writeByte(int value);

    /**
     * Sets the specified 16-bit short integer at the current {@code writerIndex} and increases the
     * {@code writerIndex} by {@code 2} in this buffer. The 16 high-order bits of the specified
     * value are ignored.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 2}
     */
    public abstract ByteBuf writeShort(int value);

    /**
     * Sets the specified 16-bit short integer in the Little Endian Byte Order at the current {@code
     * writerIndex} and increases the {@code writerIndex} by {@code 2} in this buffer. The 16
     * high-order bits of the specified value are ignored.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 2}
     */
    public abstract ByteBuf writeShortLE(int value);

    /**
     * Sets the specified 24-bit medium integer at the current {@code writerIndex} and increases the
     * {@code writerIndex} by {@code 3} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 3}
     */
    public abstract ByteBuf writeMedium(int value);

    /**
     * Sets the specified 24-bit medium integer at the current {@code writerIndex} in the Little
     * Endian Byte Order and increases the {@code writerIndex} by {@code 3} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 3}
     */
    public abstract ByteBuf writeMediumLE(int value);

    /**
     * Sets the specified 32-bit integer at the current {@code writerIndex} and increases the {@code
     * writerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 4}
     */
    public abstract ByteBuf writeInt(int value);

    /**
     * Sets the specified 32-bit integer at the current {@code writerIndex} in the Little Endian
     * Byte Order and increases the {@code writerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 4}
     */
    public abstract ByteBuf writeIntLE(int value);

    /**
     * Sets the specified 64-bit long integer at the current {@code writerIndex} and increases the
     * {@code writerIndex} by {@code 8} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 8}
     */
    public abstract ByteBuf writeLong(long value);

    /**
     * Sets the specified 64-bit long integer at the current {@code writerIndex} in the Little
     * Endian Byte Order and increases the {@code writerIndex} by {@code 8} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 8}
     */
    public abstract ByteBuf writeLongLE(long value);

    /**
     * Sets the specified 2-byte UTF-16 character at the current {@code writerIndex} and increases
     * the {@code writerIndex} by {@code 2} in this buffer. The 16 high-order bits of the specified
     * value are ignored.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 2}
     */
    public abstract ByteBuf writeChar(int value);

    /**
     * Sets the specified 32-bit floating point number at the current {@code writerIndex} and
     * increases the {@code writerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 4}
     */
    public abstract ByteBuf writeFloat(float value);

    /**
     * Sets the specified 32-bit floating point number at the current {@code writerIndex} in Little
     * Endian Byte Order and increases the {@code writerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 4}
     */
    public ByteBuf writeFloatLE(float value) {
        return writeIntLE(Float.floatToRawIntBits(value));
    }

    /**
     * Sets the specified 64-bit floating point number at the current {@code writerIndex} and
     * increases the {@code writerIndex} by {@code 8} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 8}
     */
    public abstract ByteBuf writeDouble(double value);

    /**
     * Sets the specified 64-bit floating point number at the current {@code writerIndex} in Little
     * Endian Byte Order and increases the {@code writerIndex} by {@code 8} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is less than {@code 8}
     */
    public ByteBuf writeDoubleLE(double value) {
        return writeLongLE(Double.doubleToRawLongBits(value));
    }

    /**
     * Transfers the specified source buffer's data to this buffer starting at the current {@code
     * writerIndex} until the source buffer becomes unreadable, and increases the {@code
     * writerIndex} by the number of the transferred bytes. This method is basically same with
     * {@link #writeBytes(ByteBuf, int, int)}, except that this method increases the {@code
     * readerIndex} of the source buffer by the number of the transferred bytes while {@link
     * #writeBytes(ByteBuf, int, int)} does not.
     *
     * @throws IndexOutOfBoundsException if {@code src.readableBytes} is greater than {@code
     *     this.writableBytes}
     */
    public abstract ByteBuf writeBytes(ByteBuf src);

    /**
     * Transfers the specified source buffer's data to this buffer starting at the current {@code
     * writerIndex} and increases the {@code writerIndex} by the number of the transferred bytes (=
     * {@code length}). This method is basically same with {@link #writeBytes(ByteBuf, int, int)},
     * except that this method increases the {@code readerIndex} of the source buffer by the number
     * of the transferred bytes (= {@code length}) while {@link #writeBytes(ByteBuf, int, int)} does
     * not.
     *
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.writableBytes} or if {@code length} is greater then {@code src.readableBytes}
     */
    public abstract ByteBuf writeBytes(ByteBuf src, int length);

    /**
     * Transfers the specified source buffer's data to this buffer starting at the current {@code
     * writerIndex} and increases the {@code writerIndex} by the number of the transferred bytes (=
     * {@code length}).
     *
     * @param srcIndex the first index of the source
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code srcIndex} is less than {@code 0},
     *     if {@code srcIndex + length} is greater than {@code src.capacity}, or if {@code length}
     *     is greater than {@code this.writableBytes}
     */
    public abstract ByteBuf writeBytes(ByteBuf src, int srcIndex, int length);

    /**
     * Transfers the specified source array's data to this buffer starting at the current {@code
     * writerIndex} and increases the {@code writerIndex} by the number of the transferred bytes (=
     * {@code src.length}).
     *
     * @throws IndexOutOfBoundsException if {@code src.length} is greater than {@code
     *     this.writableBytes}
     */
    public abstract ByteBuf writeBytes(byte[] src);

    /**
     * Transfers the specified source array's data to this buffer starting at the current {@code
     * writerIndex} and increases the {@code writerIndex} by the number of the transferred bytes (=
     * {@code length}).
     *
     * @param srcIndex the first index of the source
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code srcIndex} is less than {@code 0},
     *     if {@code srcIndex + length} is greater than {@code src.length}, or if {@code length} is
     *     greater than {@code this.writableBytes}
     */
    public abstract ByteBuf writeBytes(byte[] src, int srcIndex, int length);

    /**
     * Transfers the specified source buffer's data to this buffer starting at the current {@code
     * writerIndex} until the source buffer's position reaches its limit, and increases the {@code
     * writerIndex} by the number of the transferred bytes.
     *
     * @throws IndexOutOfBoundsException if {@code src.remaining()} is greater than {@code
     *     this.writableBytes}
     */
    public abstract ByteBuf writeBytes(ByteBuffer src);

    /**
     * Transfers the content of the specified stream to this buffer starting at the current {@code
     * writerIndex} and increases the {@code writerIndex} by the number of the transferred bytes.
     *
     * @param length the number of bytes to transfer
     * @return the actual number of bytes read in from the specified stream
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.writableBytes}
     * @throws IOException if the specified stream threw an exception during I/O
     */
    public abstract int writeBytes(InputStream in, int length) throws IOException;

    /**
     * Transfers the content of the specified channel to this buffer starting at the current {@code
     * writerIndex} and increases the {@code writerIndex} by the number of the transferred bytes.
     *
     * @param length the maximum number of bytes to transfer
     * @return the actual number of bytes read in from the specified channel
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.writableBytes}
     * @throws IOException if the specified channel threw an exception during I/O
     */
    public abstract int writeBytes(ScatteringByteChannel in, int length) throws IOException;

    /**
     * Transfers the content of the specified channel starting at the given file position to this
     * buffer starting at the current {@code writerIndex} and increases the {@code writerIndex} by
     * the number of the transferred bytes. This method does not modify the channel's position.
     *
     * @param position the file position at which the transfer is to begin
     * @param length the maximum number of bytes to transfer
     * @return the actual number of bytes read in from the specified channel
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.writableBytes}
     * @throws IOException if the specified channel threw an exception during I/O
     */
    public abstract int writeBytes(FileChannel in, long position, int length) throws IOException;

    /**
     * Fills this buffer with <tt>NUL (0x00)</tt> starting at the current {@code writerIndex} and
     * increases the {@code writerIndex} by the specified {@code length}.
     *
     * @param length the number of <tt>NUL</tt>s to write to the buffer
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.writableBytes}
     */
    public abstract ByteBuf writeZero(int length);

    /**
     * Writes the specified {@link CharSequence} at the current {@code writerIndex} and increases
     * the {@code writerIndex} by the written bytes. in this buffer.
     *
     * @param sequence to write
     * @param charset that should be used
     * @return the written number of bytes
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is not large enough to write
     *     the whole sequence
     */
    public abstract int writeCharSequence(CharSequence sequence, Charset charset);

    /**
     * Locates the first occurrence of the specified {@code value} in this buffer. The search takes
     * place from the specified {@code fromIndex} (inclusive) to the specified {@code toIndex}
     * (exclusive).
     *
     * <p>If {@code fromIndex} is greater than {@code toIndex}, the search is performed in a
     * reversed order.
     *
     * <p>This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @return the absolute index of the first occurrence if found. {@code -1} otherwise.
     */
    public abstract int indexOf(int fromIndex, int toIndex, byte value);

    /**
     * Locates the first occurrence of the specified {@code value} in this buffer. The search takes
     * place from the current {@code readerIndex} (inclusive) to the current {@code writerIndex}
     * (exclusive).
     *
     * <p>This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @return the number of bytes between the current {@code readerIndex} and the first occurrence
     *     if found. {@code -1} otherwise.
     */
    public abstract int bytesBefore(byte value);

    /**
     * Locates the first occurrence of the specified {@code value} in this buffer. The search starts
     * from the current {@code readerIndex} (inclusive) and lasts for the specified {@code length}.
     *
     * <p>This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @return the number of bytes between the current {@code readerIndex} and the first occurrence
     *     if found. {@code -1} otherwise.
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code
     *     this.readableBytes}
     */
    public abstract int bytesBefore(int length, byte value);

    /**
     * Locates the first occurrence of the specified {@code value} in this buffer. The search starts
     * from the specified {@code index} (inclusive) and lasts for the specified {@code length}.
     *
     * <p>This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @return the number of bytes between the specified {@code index} and the first occurrence if
     *     found. {@code -1} otherwise.
     * @throws IndexOutOfBoundsException if {@code index + length} is greater than {@code
     *     this.capacity}
     */
    public abstract int bytesBefore(int index, int length, byte value);

    /**
     * Iterates over the readable bytes of this buffer with the specified {@code processor} in
     * ascending order.
     *
     * @return {@code -1} if the processor iterated to or beyond the end of the readable bytes. The
     *     last-visited index If the {@link ByteProcessor#process(byte)} returned {@code false}.
     */
    public abstract int forEachByte(ByteProcessor processor);

    /**
     * Iterates over the specified area of this buffer with the specified {@code processor} in
     * ascending order. (i.e. {@code index}, {@code (index + 1)}, .. {@code (index + length - 1)})
     *
     * @return {@code -1} if the processor iterated to or beyond the end of the specified area. The
     *     last-visited index If the {@link ByteProcessor#process(byte)} returned {@code false}.
     */
    public abstract int forEachByte(int index, int length, ByteProcessor processor);

    /**
     * Iterates over the readable bytes of this buffer with the specified {@code processor} in
     * descending order.
     *
     * @return {@code -1} if the processor iterated to or beyond the beginning of the readable
     *     bytes. The last-visited index If the {@link ByteProcessor#process(byte)} returned {@code
     *     false}.
     */
    public abstract int forEachByteDesc(ByteProcessor processor);

    /**
     * Iterates over the specified area of this buffer with the specified {@code processor} in
     * descending order. (i.e. {@code (index + length - 1)}, {@code (index + length - 2)}, ...
     * {@code index})
     *
     * @return {@code -1} if the processor iterated to or beyond the beginning of the specified
     *     area. The last-visited index If the {@link ByteProcessor#process(byte)} returned {@code
     *     false}.
     */
    public abstract int forEachByteDesc(int index, int length, ByteProcessor processor);

    /**
     * Returns a copy of this buffer's readable bytes. Modifying the content of the returned buffer
     * or this buffer does not affect each other at all. This method is identical to {@code
     * buf.copy(buf.readerIndex(), buf.readableBytes())}. This method does not modify {@code
     * readerIndex} or {@code writerIndex} of this buffer.
     */
    public abstract ByteBuf copy();

    /**
     * Returns a copy of this buffer's sub-region. Modifying the content of the returned buffer or
     * this buffer does not affect each other at all. This method does not modify {@code
     * readerIndex} or {@code writerIndex} of this buffer.
     */
    public abstract ByteBuf copy(int index, int length);

    /**
     * Returns a slice of this buffer's readable bytes. Modifying the content of the returned buffer
     * or this buffer affects each other's content while they maintain separate indexes and marks.
     * This method is identical to {@code buf.slice(buf.readerIndex(), buf.readableBytes())}. This
     * method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * <p>Also be aware that this method will NOT call {@link #retain()} and so the reference count
     * will NOT be increased.
     */
    public abstract ByteBuf slice();

    /**
     * Returns a retained slice of this buffer's readable bytes. Modifying the content of the
     * returned buffer or this buffer affects each other's content while they maintain separate
     * indexes and marks. This method is identical to {@code buf.slice(buf.readerIndex(),
     * buf.readableBytes())}. This method does not modify {@code readerIndex} or {@code writerIndex}
     * of this buffer.
     *
     * <p>Note that this method returns a {@linkplain #retain() retained} buffer unlike {@link
     * #slice()}. This method behaves similarly to {@code slice().retain()} except that this method
     * may return a buffer implementation that produces less garbage.
     */
    public abstract ByteBuf retainedSlice();

    /**
     * Returns a slice of this buffer's sub-region. Modifying the content of the returned buffer or
     * this buffer affects each other's content while they maintain separate indexes and marks. This
     * method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * <p>Also be aware that this method will NOT call {@link #retain()} and so the reference count
     * will NOT be increased.
     */
    public abstract ByteBuf slice(int index, int length);

    /**
     * Returns a retained slice of this buffer's sub-region. Modifying the content of the returned
     * buffer or this buffer affects each other's content while they maintain separate indexes and
     * marks. This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * <p>Note that this method returns a {@linkplain #retain() retained} buffer unlike {@link
     * #slice(int, int)}. This method behaves similarly to {@code slice(...).retain()} except that
     * this method may return a buffer implementation that produces less garbage.
     */
    public abstract ByteBuf retainedSlice(int index, int length);

    /**
     * Returns a buffer which shares the whole region of this buffer. Modifying the content of the
     * returned buffer or this buffer affects each other's content while they maintain separate
     * indexes and marks. This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * <p>The reader and writer marks will not be duplicated. Also be aware that this method will
     * NOT call {@link #retain()} and so the reference count will NOT be increased.
     *
     * @return A buffer whose readable content is equivalent to the buffer returned by {@link
     *     #slice()}. However this buffer will share the capacity of the underlying buffer, and
     *     therefore allows access to all of the underlying content if necessary.
     */
    public abstract ByteBuf duplicate();

    /**
     * Returns a retained buffer which shares the whole region of this buffer. Modifying the content
     * of the returned buffer or this buffer affects each other's content while they maintain
     * separate indexes and marks. This method is identical to {@code buf.slice(0, buf.capacity())}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * <p>Note that this method returns a {@linkplain #retain() retained} buffer unlike {@link
     * #slice(int, int)}. This method behaves similarly to {@code duplicate().retain()} except that
     * this method may return a buffer implementation that produces less garbage.
     */
    public abstract ByteBuf retainedDuplicate();

    /**
     * Returns the maximum number of NIO {@link ByteBuffer}s that consist this buffer. Note that
     * {@link #nioBuffers()} or {@link #nioBuffers(int, int)} might return a less number of {@link
     * ByteBuffer}s.
     *
     * @return {@code -1} if this buffer has no underlying {@link ByteBuffer}. the number of the
     *     underlying {@link ByteBuffer}s if this buffer has at least one underlying {@link
     *     ByteBuffer}. Note that this method does not return {@code 0} to avoid confusion.
     * @see #nioBuffer()
     * @see #nioBuffer(int, int)
     * @see #nioBuffers()
     * @see #nioBuffers(int, int)
     */
    public abstract int nioBufferCount();

    /**
     * Exposes this buffer's readable bytes as an NIO {@link ByteBuffer}. The returned buffer either
     * share or contains the copied content of this buffer, while changing the position and limit of
     * the returned NIO buffer does not affect the indexes and marks of this buffer. This method is
     * identical to {@code buf.nioBuffer(buf.readerIndex(), buf.readableBytes())}. This method does
     * not modify {@code readerIndex} or {@code writerIndex} of this buffer. Please note that the
     * returned NIO buffer will not see the changes of this buffer if this buffer is a dynamic
     * buffer and it adjusted its capacity.
     *
     * @throws UnsupportedOperationException if this buffer cannot create a {@link ByteBuffer} that
     *     shares the content with itself
     * @see #nioBufferCount()
     * @see #nioBuffers()
     * @see #nioBuffers(int, int)
     */
    public abstract ByteBuffer nioBuffer();

    /**
     * Exposes this buffer's sub-region as an NIO {@link ByteBuffer}. The returned buffer either
     * share or contains the copied content of this buffer, while changing the position and limit of
     * the returned NIO buffer does not affect the indexes and marks of this buffer. This method
     * does not modify {@code readerIndex} or {@code writerIndex} of this buffer. Please note that
     * the returned NIO buffer will not see the changes of this buffer if this buffer is a dynamic
     * buffer and it adjusted its capacity.
     *
     * @throws UnsupportedOperationException if this buffer cannot create a {@link ByteBuffer} that
     *     shares the content with itself
     * @see #nioBufferCount()
     * @see #nioBuffers()
     * @see #nioBuffers(int, int)
     */
    public abstract ByteBuffer nioBuffer(int index, int length);

    /** Internal use only: Exposes the internal NIO buffer. */
    public abstract ByteBuffer internalNioBuffer(int index, int length);

    /**
     * Exposes this buffer's readable bytes as an NIO {@link ByteBuffer}'s. The returned buffer
     * either share or contains the copied content of this buffer, while changing the position and
     * limit of the returned NIO buffer does not affect the indexes and marks of this buffer. This
     * method does not modify {@code readerIndex} or {@code writerIndex} of this buffer. Please note
     * that the returned NIO buffer will not see the changes of this buffer if this buffer is a
     * dynamic buffer and it adjusted its capacity.
     *
     * @throws UnsupportedOperationException if this buffer cannot create a {@link ByteBuffer} that
     *     shares the content with itself
     * @see #nioBufferCount()
     * @see #nioBuffer()
     * @see #nioBuffer(int, int)
     */
    public abstract ByteBuffer[] nioBuffers();

    /**
     * Exposes this buffer's bytes as an NIO {@link ByteBuffer}'s for the specified index and length
     * The returned buffer either share or contains the copied content of this buffer, while
     * changing the position and limit of the returned NIO buffer does not affect the indexes and
     * marks of this buffer. This method does not modify {@code readerIndex} or {@code writerIndex}
     * of this buffer. Please note that the returned NIO buffer will not see the changes of this
     * buffer if this buffer is a dynamic buffer and it adjusted its capacity.
     *
     * @throws UnsupportedOperationException if this buffer cannot create a {@link ByteBuffer} that
     *     shares the content with itself
     * @see #nioBufferCount()
     * @see #nioBuffer()
     * @see #nioBuffer(int, int)
     */
    public abstract ByteBuffer[] nioBuffers(int index, int length);

    /**
     * Returns {@code true} if and only if this buffer has a backing byte array. If this method
     * returns true, you can safely call {@link #array()} and {@link #arrayOffset()}.
     */
    public abstract boolean hasArray();

    /**
     * Returns the backing byte array of this buffer.
     *
     * @throws UnsupportedOperationException if there no accessible backing byte array
     */
    public abstract byte[] array();

    /**
     * Returns the offset of the first byte within the backing byte array of this buffer.
     *
     * @throws UnsupportedOperationException if there no accessible backing byte array
     */
    public abstract int arrayOffset();

    /**
     * Returns {@code true} if and only if this buffer has a reference to the low-level memory
     * address that points to the backing data.
     */
    public abstract boolean hasMemoryAddress();

    /**
     * Returns the low-level memory address that point to the first byte of ths backing data.
     *
     * @throws UnsupportedOperationException if this buffer does not support accessing the low-level
     *     memory address
     */
    public abstract long memoryAddress();

    /**
     * Decodes this buffer's readable bytes into a string with the specified character set name.
     * This method is identical to {@code buf.toString(buf.readerIndex(), buf.readableBytes(),
     * charsetName)}. This method does not modify {@code readerIndex} or {@code writerIndex} of this
     * buffer.
     *
     * @throws UnsupportedCharsetException if the specified character set name is not supported by
     *     the current VM
     */
    public abstract String toString(Charset charset);

    /**
     * Decodes this buffer's sub-region into a string with the specified character set. This method
     * does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     */
    public abstract String toString(int index, int length, Charset charset);

    /**
     * Returns a hash code which was calculated from the content of this buffer. If there's a byte
     * array which is {@linkplain #equals(Object) equal to} this array, both arrays should return
     * the same value.
     */
    @Override
    public abstract int hashCode();

    /**
     * Determines if the content of the specified buffer is identical to the content of this array.
     * 'Identical' here means:
     *
     * <ul>
     *   <li>the size of the contents of the two buffers are same and
     *   <li>every single byte of the content of the two buffers are same.
     * </ul>
     *
     * Please note that it does not compare {@link #readerIndex()} nor {@link #writerIndex()}. This
     * method also returns {@code false} for {@code null} and an object which is not an instance of
     * {@link ByteBuf} type.
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Compares the content of the specified buffer to the content of this buffer. Comparison is
     * performed in the same manner with the string comparison functions of various languages such
     * as {@code strcmp}, {@code memcmp} and {@link String#compareTo(String)}.
     */
    @Override
    public abstract int compareTo(ByteBuf buffer);

    /**
     * Returns the string representation of this buffer. This method does not necessarily return the
     * whole content of the buffer but returns the values of the key properties such as {@link
     * #readerIndex()}, {@link #writerIndex()} and {@link #capacity()}.
     */
    @Override
    public abstract String toString();

    @Override
    public abstract ByteBuf retain(int increment);

    @Override
    public abstract ByteBuf retain();
}
