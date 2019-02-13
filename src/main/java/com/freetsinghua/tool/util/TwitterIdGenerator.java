package com.freetsinghua.tool.util;

/**
 * Twitter公开的一个算法，实现ID唯一，且有序自增
 *
 * <p>用来产生id 该类是线程安全的
 *
 * @author z.tsinghua
 * @date 2018/12/26
 */
public final class TwitterIdGenerator {
    private long workerId;
    private long dataCenterId;
    private long sequence = 0L;
    private long workerIdBits = 5L;
    private long dataCenterIdBits = 5L;
    private long sequenceBits = 12L;
    private long workerIdShift = sequenceBits;
    private long dataCenterIdShift = sequenceBits + workerIdBits;
    private long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
    private long sequenceMask = ~(-1L << sequenceBits);
    private long lastTimestamp = -1L;

    private static class IdGenHolder {
        private static final TwitterIdGenerator INSTANCE = new TwitterIdGenerator();
    }

    public static TwitterIdGenerator getInstance() {
        return IdGenHolder.INSTANCE;
    }

    private TwitterIdGenerator() {
        this(0L, 0L);
    }

    private TwitterIdGenerator(long workerId, long dataCenterId) {
        long maxWorkerId = ~(-1L << workerIdBits);
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        long maxDataCenterId = ~(-1L << dataCenterIdBits);
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Data center Id can't be greater than %d or less than 0",
                            maxDataCenterId));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format(
                            "Clock moved backwards.  Refusing to generate id for %d milliseconds",
                            lastTimestamp - timestamp));
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        long twepoch = 1288834974657L;
        return ((timestamp - twepoch) << timestampLeftShift)
                | (dataCenterId << dataCenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
