package org.jamesshaw.threadprofiler;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class ManagementBeanInstrumentation implements InstrumentationProvider{

    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    public long getCycleTime() {
        long value = threadMXBean.getCurrentThreadCpuTime();
        return value;
    }

    public long getWallTime() {
        return System.nanoTime();
    }

}
