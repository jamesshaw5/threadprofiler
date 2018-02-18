package org.jamesshaw.threadprofiler;

public interface InstrumentationProvider {
    long getCycleTime();
    long getWallTime();
}
