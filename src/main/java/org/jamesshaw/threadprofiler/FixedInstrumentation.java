package org.jamesshaw.threadprofiler;

/**
 * {@link InstrumentationProvider} that returns fixed values, useful for testing.
 */
public class FixedInstrumentation implements InstrumentationProvider{

    private long currentCycleTime = 0;
    private long currentWallTime = 0;

    public void setCurrentCycleTime(long currentCycleTime) {
        this.currentCycleTime = currentCycleTime;
    }

    public void setCurrentWallTime(long currentWallTime) {
        this.currentWallTime = currentWallTime;
    }

    public long getCycleTime() {
        return currentCycleTime;
    }

    public long getWallTime() {
        return currentWallTime;
    }

}
