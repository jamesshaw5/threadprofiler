package org.jamesshaw.threadprofiler;

import java.util.ArrayList;
import java.util.List;

public class ThreadProfilerNode {
    private final static int defaultEntries = 20;
    private final String threadName;
    private final InstrumentationProvider instrumentationProvider;
    private final int depth;
    private  long startingWallTime;
    private  long startingCycleTime;
    private String operationKey;
    private String description;
    private long lastWallTime;
    private long lastCycleTime;
    private List<ThreadProfilerNode> children = new ArrayList<>();
    private ThreadProfilerNode parent = null;

    private final Formatter formatter = new Formatter();

    public ThreadProfilerNode(InstrumentationProvider instrumentationProvider,
                              int depth,
                              String threadName,
                              String operationKey,
                              String description,
                              long wallTime,
                              long cycles) {
        this.instrumentationProvider = instrumentationProvider;
        this.depth = depth;
        this.threadName = threadName;
        this.operationKey = operationKey;
        this.description = description;
        this.startingWallTime = wallTime;
        this.startingCycleTime = cycles;

        this.lastCycleTime = cycles;
        this.lastWallTime = wallTime;
    }

    public void setStartingCycleTime(long startingCycleTime) {
        this.startingCycleTime = startingCycleTime;
    }

    public void setStartingWallTime(long startingWallTime) {
        this.startingWallTime = startingWallTime;
    }

    public void setLastCycleTime(long lastCycleTime) {
        this.lastCycleTime = lastCycleTime;
    }

    public void setLastWallTime(long lastWallTime) {
        this.lastWallTime = lastWallTime;
    }

    public ThreadProfilerNode addChild(ThreadProfilerNode item) {
        children.add(item);
        return this;
    }

    public String format() {
        return formatter.format(this);
    }

    public String getThreadName() {
        return threadName;
    }

    public String getOperationKey() {
        return operationKey;
    }

    public String getDescription() {
        return description;
    }

    public int getDepth() {
        return depth;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }



    public long getElapsedWallTime() {
        return lastWallTime - startingWallTime;
    }

    public long getElapsedCycleTime() {
        return lastCycleTime - startingCycleTime;
    }

    public long getLastCycleTime() {
        return lastCycleTime;
    }

    public long getLastWallTime() {
        return lastWallTime;
    }

    public int getRecordCount() {
        return children.size();
    }

    public long getStartingCycleTime() {
        return startingCycleTime;
    }

    public long getStartingWallTime() {
        return startingWallTime;
    }

    public ThreadProfilerNode record(String label) {
        return record(label, "");
    }

    public List<ThreadProfilerNode> getChildren() {
        return children;
    }

    public ThreadProfilerNode record(String label, String description) {

        long currentWallTime = instrumentationProvider.getWallTime();
        long currentCycleTime = instrumentationProvider.getCycleTime();

        ThreadProfilerNode child = new ThreadProfilerNode(instrumentationProvider, depth + 1, Thread.currentThread().getName(), label, description, currentWallTime, currentCycleTime);
        children.add(child);

        lastWallTime = currentWallTime;
        lastCycleTime = currentCycleTime;

        return this;

    }

    public void update(long startingWallTime, long startingCycleTime) {
        lastCycleTime = startingCycleTime;
        lastWallTime = startingWallTime;
    }
}
