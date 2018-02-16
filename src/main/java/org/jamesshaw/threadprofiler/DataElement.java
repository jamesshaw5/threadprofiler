package org.jamesshaw.threadprofiler;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class DataElement {
    private final String threadName;
    private final int depth;
    private String operationKey;
    private String description;
    private final long wallTime;
    private final long cycles;
    private List<DataElement> children = new ArrayList<>();

    public DataElement(int depth, String threadName, String operationKey, String description, long wallTime, long cycles) {
        this.depth = depth;
        this.threadName = threadName;
        this.operationKey = operationKey;
        this.description = description;
        this.wallTime = wallTime;
        this.cycles = cycles;
    }

    public int getDepth() {
        return depth;
    }

    public String getThreadName() {
        return threadName;
    }

    public void dump() {

        System.out.printf("%" +  (4 * (depth+1)) + "s [%s] %s %10s%n", "", getThreadName(), getOperationKey(), getDescription());

        long wallTimeCursor = this.wallTime;
        long cycleTimeCursor = this.cycles;

        for (DataElement child : children) {

            long relativeWallTime = child.wallTime - wallTimeCursor;
            long relativeCycleTime = child.cycles - cycleTimeCursor;

            String relativeWallInTargetScale = NumberFormat.getInstance().format(relativeWallTime * 1e-6d);
            String relativeCycleInTargetScale = NumberFormat.getInstance().format(relativeCycleTime * 1e-6d);

            System.out.printf("%" +  (4 * (child.getDepth()-getDepth()+1)) + "s [%s] %10s %10s %s ms %s ms%n", "", child.getThreadName(), child.getOperationKey(), child.getDescription(), relativeWallInTargetScale, relativeCycleInTargetScale);

            wallTimeCursor = child.wallTime;
            cycleTimeCursor = child.cycles;
        }

    }

    public long getWallTime() {
        return wallTime;
    }

    public long getCycles() {
        return cycles;
    }

    public DataElement addChild(DataElement item) {
        children.add(item);
        return this;
    }

    public String getDescription() {
        return description;
    }

    public String getOperationKey() {
        return operationKey;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }
}
