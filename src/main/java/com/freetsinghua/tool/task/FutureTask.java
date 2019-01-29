package com.freetsinghua.tool.task;

import sun.misc.Unsafe;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * 可取消的异步计算
 *
 * @author z.tsinghua
 * @date 2019/1/28
 */
public class FutureTask<V> implements RunnableFuture<V> {

    /** 此任务的运行状态， */
    private volatile int state;

    private static final int NEW = 0;
    private static final int COMPLETING = 1;
    private static final int NORMAL = 2;
    private static final int EXCEPTIONAL = 3;
    private static final int CANCELLED = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED = 6;

    /** 底层的{@link Callable},任务完成后，设置为{@code null} */
    private Callable<V> callable;

    /** 运行{@code callable}的线程 */
    private volatile Thread runner;

    /** 任务的运行结果，或者{@link Exception} */
    private Object outCome;

    private WaitNode waiters;

    /**
     * 结果处理
     *
     * @param s 结果状态
     * @return 返回结果，除非任务完成，否则抛出异常
     */
    @SuppressWarnings("unchecked")
    private V report(int s) throws ExecutionException {
        Object x = outCome;

        if (s == NORMAL) {
            return (V) x;
        }

        if (s >= CANCELLED) {
            throw new CancellationException();
        }

        throw new ExecutionException((Throwable) x);
    }

    public FutureTask(Callable<V> callable) {
        if (callable == null) {
            throw new NullPointerException();
        }
        this.callable = callable;
        this.state = NEW;
    }

    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = NEW;
    }

    @Override
    public void run() {

        if (this.state != NEW
                && !UNSAFE.compareAndSwapObject(
                        this, RUNNER_OFF_SET, null, Thread.currentThread())) {
            return;
        }

        try {
            Callable<V> c = this.callable;
            if (c != null && this.state == NEW) {
                V result;
                boolean ran;

                try {
                    result = c.call();
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    setExceptional(ex);
                }

                if (ran) {
                    set(result);
                }
            }
        } finally {
            // runner必须为非null，直到状态已经稳定下来，以防止并发调用run（）
            runner = null;
            // 在使runner归null后,必须重新读取状态以防止泄漏中断
            int s = this.state;
            if (s >= INTERRUPTING) {
                handlePossibleCancellationInterrupt(s);
            }
        }
    }

    /**
     * 执行计算而不设置其结果，然后*将此未来重置为初始状态，如果*计算遇到异常或被取消则无法执行此操作。这*设计用于本质上执行*不止一次的任务。
     *
     * @return 如果操作成功，则返回{@code true}
     */
    protected boolean runAndSet() {
        if (this.state != NEW
                && !UNSAFE.compareAndSwapObject(
                        this, RUNNER_OFF_SET, null, Thread.currentThread())) {
            return false;
        }

        boolean ran = false;
        int s = this.state;

        try {

            Callable<V> c = this.callable;
            if (c != null && s == NEW) {
                try {
                    c.call();
                    ran = true;
                } catch (Throwable thx) {
                    ran = false;
                    setExceptional(thx);
                }
            }
        } finally {
            this.runner = null;
            s = this.state;
            if (s >= INTERRUPTING) {
                handlePossibleCancellationInterrupt(s);
            }
        }

        return ran && s == NEW;
    }

    private void handlePossibleCancellationInterrupt(int s) {
        if (s == INTERRUPTING) {
            while (this.state == INTERRUPTING) {
                Thread.yield();
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {

        if (!(this.state == NEW
                && UNSAFE.compareAndSwapInt(
                        this,
                        STATE_OFF_SET,
                        NEW,
                        mayInterruptIfRunning ? INTERRUPTING : CANCELLED))) {
            return false;
        }

        try {

            if (mayInterruptIfRunning) {
                try {
                    Thread t = runner;
                    if (t != null) {
                        t.interrupt();
                    }
                } finally {
                    UNSAFE.putOrderedInt(this, STATE_OFF_SET, INTERRUPTED);
                }
            }
        } finally {
            finishCompletion();
        }

        return true;
    }

    private void finishCompletion() {
        for (WaitNode q; (q = waiters) != null; ) {
            if (UNSAFE.compareAndSwapObject(this, WAITER_OFF_SET, q, null)) {
                for (; ; ) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t);
                    }

                    WaitNode next = q.next;
                    if (next == null) {
                        break;
                    }

                    q.next = null;
                    q = next;
                }
                break;
            }
        }

        done();

        this.callable = null;
    }

    /**
     * 当此任务转换为状态 {@code isDone}时（无论是正常还是通过取消），都会调用受保护的方法。 默认实现什么都不做。子类可以重写此方法以调用完成回调或执行
     * 请注意，您可以在此方法的实现中查询状态，以确定是否已取消此任务。
     */
    protected void done() {}

    @Override
    public boolean isCancelled() {
        return this.state >= CANCELLED;
    }

    @Override
    public boolean isDone() {
        return this.state != NEW;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        int s = this.state;

        if (s <= COMPLETING) {
            awaitDone(false, 0L);
        }

        return report(s);
    }

    @Override
    public V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null) {
            throw new NullPointerException("unit must not be null");
        }

        int s = this.state;
        if (s <= COMPLETING) {
            s = awaitDone(true, unit.toNanos(timeout));
            if (s <= COMPLETING) {
                throw new TimeoutException();
            }
        }

        return report(s);
    }

    /**
     * 等待完成，或者超时，或者被中断
     *
     * @param timed 如果为true，则等待超时
     * @param nanos 超时纳秒数
     * @return 状态改变
     * @throws InterruptedException 若是被中断
     */
    private int awaitDone(boolean timed, long nanos) throws InterruptedException {

        final long deadLine = timed ? System.nanoTime() + nanos : 0L;
        WaitNode q = null;
        boolean queued = false;

        for (; ; ) {
            if (Thread.interrupted()) {
                removeWaiter(q);
                throw new InterruptedException();
            }

            int s = this.state;
            if (s > COMPLETING) {
                if (q != null) {
                    q.thread = null;

                    return s;
                }
            } else if (s == COMPLETING) {
                Thread.yield();
            } else if (q == null) {
                q = new WaitNode();
            } else if (!queued) {
                queued = UNSAFE.compareAndSwapObject(this, WAITER_OFF_SET, q.next = waiters, q);
            } else if (timed) {
                nanos = deadLine - System.nanoTime();
                if (nanos < 0) {
                    removeWaiter(q);
                    return this.state;
                }
                LockSupport.parkNanos(this, nanos);
            } else {
                LockSupport.park(this);
            }
        }
    }

    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            retry:
            for (; ; ) {
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    s = q.next;
                    if (q.thread != null) {
                        pred = q;
                    } else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) {
                            continue retry;
                        }
                    } else if (!UNSAFE.compareAndSwapObject(this, WAITER_OFF_SET, q, s)) {
                        continue retry;
                    }
                }
                break;
            }
        }
    }

    /**
     * 将此future的结果设置为给定值，除非此未来已设置或已取消。
     *
     * @param v 值
     */
    protected void set(V v) {
        if (UNSAFE.compareAndSwapObject(this, STATE_OFF_SET, NEW, COMPLETING)) {
            outCome = v;
            UNSAFE.putOrderedInt(this, STATE_OFF_SET, NORMAL);
            finishCompletion();
        }
    }

    /**
     * 导致此未来报告带有给定throwable的{@link ExecutionException} 作为其原因，除非此{@link
     * java.util.concurrent.Future}已经{@link #set(Object)}或已被取消{@link #cancel(boolean)}。
     *
     * @param thr 异常
     */
    protected void setExceptional(Throwable thr) {
        if (UNSAFE.compareAndSwapInt(this, STATE_OFF_SET, NEW, COMPLETING)) {
            outCome = thr;
            UNSAFE.putOrderedInt(this, STATE_OFF_SET, EXCEPTIONAL);
            finishCompletion();
        }
    }

    private static final Unsafe UNSAFE;
    private static final long STATE_OFF_SET;
    private static final long RUNNER_OFF_SET;
    private static final long WAITER_OFF_SET;

    static {
        try {
            UNSAFE = Unsafe.getUnsafe();

            Class<?> clazz = FutureTask.class;
            STATE_OFF_SET = UNSAFE.objectFieldOffset(clazz.getDeclaredField("state"));
            RUNNER_OFF_SET = UNSAFE.objectFieldOffset(clazz.getDeclaredField("runner"));
            WAITER_OFF_SET = UNSAFE.objectFieldOffset(clazz.getDeclaredField("waiters"));

        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    /**
     * Simple linked list nodes to record waiting threads in a Treiber stack. See other classes such
     * as Phaser and SynchronousQueue for more detailed explanation.
     */
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;

        WaitNode() {
            thread = Thread.currentThread();
        }
    }
}
