package com.freetsinghua.tool.util;

import com.freetsinghua.tool.anotation.Nullable;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 简单任务查看工具
 *
 * @apiNote copy from spring，去掉了没有命名的stopwatch，同时，启动子任务也必须命名，增加自动任务方法
 * @author z.tsinghua
 * @date 2019/2/12
 */
public class StopWatch {
    /** 总的任务id */
    private final String id;
    /** 是否保存子任务信息 */
    private boolean keepTaskList = true;
    /** 子任务列表 */
    private final List<TaskInfo> taskList = new LinkedList<>();
    /** 当前任务的启动时间 */
    private long startTimeMillis;
    /** 当前子任务的名称，通过start方法设置 */
    @Nullable private String currentTaskName;
    /** 最近的子任务 */
    @Nullable private TaskInfo lastTaskInfo;
    /** 子任务数目 */
    private int taskCount;
    /** 所有任务的总运行时间 */
    private long totalTimeMillis;
    /** 自动任务列表，最多1000个任务 */
    private Queue<TaskInfo> taskQueue = new LinkedBlockingDeque<>(1000);

    /**
     * Construct a new stop watch with the given id. Does not start any task.
     *
     * @param id identifier for this stop watch. Handy when we have output from multiple stop
     *     watches and need to distinguish between them.
     */
    public StopWatch(String id) {
        this.id = id;
    }

    /**
     * Return the id of this stop watch, as specified on construction.
     *
     * @return the id (empty String by default)
     * @see #StopWatch(String)
     */
    public String getId() {
        return this.id;
    }

    /** 默认是记录子任务的信息，若是考虑占用内存太大，设置为{@code false} */
    public void setKeepTaskList(boolean keepTaskList) {
        this.keepTaskList = keepTaskList;
    }

    /**
     * 启动有一个子任务
     *
     * @param taskName 子任务名
     */
    public void start(String taskName) throws IllegalStateException {
        if (this.currentTaskName != null) {
            throw new IllegalStateException("Can't start StopWatch: it's already running");
        }
        this.currentTaskName = taskName;
        this.startTimeMillis = System.currentTimeMillis();
    }

    /** 停止当前运行的子任务，先调用{{@link #start(String)}}，然后再调用此方法 */
    public void stop() throws IllegalStateException {
        if (this.currentTaskName == null) {
            throw new IllegalStateException("Can't stop StopWatch: it's not running");
        }
        long lastTime = System.currentTimeMillis() - this.startTimeMillis;
        this.totalTimeMillis += lastTime;
        this.lastTaskInfo = new TaskInfo(this.currentTaskName, lastTime);
        if (this.keepTaskList) {
            this.taskList.add(this.lastTaskInfo);
        }
        ++this.taskCount;
        this.currentTaskName = null;
    }

    /** 返回当前是否有子任务在运行 */
    public boolean isRunning() {
        return (this.currentTaskName != null);
    }

    /**
     * 返回当前子任务的名
     *
     * @return 若是当前没有子任务在运行，则返回{@code null}，否则返回子任务的名
     */
    @Nullable
    public String currentTaskName() {
        return this.currentTaskName;
    }

    /**
     * 返回最近子任务所花费的时间
     *
     * @throws IllegalStateException 若是没有最近的任务记录，则抛出异常
     * @return 最近子任务花费的时间
     */
    public long getLastTaskTimeMillis() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tasks run: can't get last task interval");
        }
        return this.lastTaskInfo.getTimeMillis();
    }

    /**
     * 返回最近子任务的名
     *
     * @throws IllegalStateException 若是最近子任务没有记录
     * @return 最近子任务的名
     */
    public String getLastTaskName() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tasks run: can't get last task name");
        }
        return this.lastTaskInfo.getTaskName();
    }

    /**
     * 返回最近的子任务
     *
     * @throws IllegalStateException 如果最近子任务没有记录
     * @return 最近的子任务
     */
    public TaskInfo getLastTaskInfo() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tasks run: can't get last task info");
        }
        return this.lastTaskInfo;
    }

    /** 获取所有子任务的运行时间之和，以毫秒为单位 */
    public long getTotalTimeMillis() {
        return this.totalTimeMillis;
    }

    /** 获取所有子任务的运行时间之和，以秒为单位 */
    public double getTotalTimeSeconds() {
        return this.totalTimeMillis / 1000.0;
    }

    /** 返回子任务数目 */
    public int getTaskCount() {
        return this.taskCount;
    }

    /** 返回所有子任务 */
    public TaskInfo[] getTaskInfo() {
        if (!this.keepTaskList) {
            throw new UnsupportedOperationException("Task info is not being kept!");
        }
        return this.taskList.toArray(new TaskInfo[0]);
    }

    /** 返回一个简短的描述 */
    public String shortSummary() {
        return "StopWatch '" + getId() + "': running time (millis) = " + getTotalTimeMillis();
    }

    /**
     * Return a string with a table describing all tasks performed. For custom reporting, call
     * getTaskInfo() and use the task info directly.
     */
    public String prettyPrint() {
        StringBuilder sb = new StringBuilder(shortSummary());
        sb.append('\n');
        if (!this.keepTaskList) {
            sb.append("No task info kept");
        } else {
            sb.append("-----------------------------------------\n");
            sb.append("ms     %     Task name\n");
            sb.append("-----------------------------------------\n");
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumIntegerDigits(5);
            nf.setGroupingUsed(false);
            NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setMinimumIntegerDigits(3);
            pf.setGroupingUsed(false);
            for (TaskInfo task : getTaskInfo()) {
                sb.append(nf.format(task.getTimeMillis())).append("  ");
                sb.append(pf.format(task.getTimeSeconds() / getTotalTimeSeconds())).append("  ");
                sb.append(task.getTaskName()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Return an informative string describing all tasks performed For custom reporting, call {@code
     * getTaskInfo()} and use the task info directly.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(shortSummary());
        if (this.keepTaskList) {
            for (TaskInfo task : getTaskInfo()) {
                sb.append("; [")
                        .append(task.getTaskName())
                        .append("] took ")
                        .append(task.getTimeMillis());
                long percent = Math.round((100.0 * task.getTimeSeconds()) / getTotalTimeSeconds());
                sb.append(" = ").append(percent).append("%");
            }
        } else {
            sb.append("; no task info kept");
        }
        return sb.toString();
    }

    /**
     * 创建一个命名的子任务{@code taskName}
     *
     * @param taskName 子任务名
     * @return 返回任务对象
     */
    public TaskInfo createTask(String taskName, Runnable task) {
        return new TaskInfo(taskName, -1L, task);
    }

    /**
     * 添加到自动任务列表中
     *
     * @param taskInfo 任务
     */
    public void addAutoExecuteQueue(TaskInfo taskInfo) {
        this.taskQueue.add(taskInfo);
    }

    /**
     * 重载{{@link #addAutoExecuteQueue(TaskInfo)}}
     *
     * @param taskName 子任务名
     * @param task 具体任务
     */
    public void addAutoExecuteQueue(String taskName, Runnable task) {
        this.taskQueue.add(this.createTask(taskName, task));
    }

    /** 启动自动任务列表 */
    public String startAutoExecuteQueue() {
        if (this.taskQueue.isEmpty()) {
            return "[Empty task queue]";
        }

        while (!this.taskQueue.isEmpty()) {
            TaskInfo taskInfo = this.taskQueue.poll();
            this.start(taskInfo.taskName);
            taskInfo.getTask().run();
            this.stop();
        }

        return this.prettyPrint();
    }

    /** Inner class to hold data about one task executed within the stop watch. */
    public static final class TaskInfo {
        /** 任务名 */
        private final String taskName;
        /** 花费的时间 */
        private final long timeMillis;
        /** 具体任务 */
        @Nullable private final Runnable task;

        TaskInfo(String taskName, long timeMillis) {
            this(taskName, timeMillis, null);
        }

        TaskInfo(String taskName, long timeMillis, Runnable task) {
            this.taskName = taskName;
            this.timeMillis = timeMillis;
            this.task = task;
        }

        /** Return the name of this task. */
        public String getTaskName() {
            return this.taskName;
        }

        /** Return the time in milliseconds this task took. */
        public long getTimeMillis() {
            return this.timeMillis;
        }

        /** Return the time in seconds this task took. */
        public double getTimeSeconds() {
            return (this.timeMillis / 1000.0);
        }

        /** 返回任务 */
        public Runnable getTask() {
            return task;
        }
    }
}
