package org.jamesshaw.threadprofiler;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class Instrumentation {

    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    public static long getCPUTime() {
        return threadMXBean.getCurrentThreadCpuTime();
    }

    public static long getWallTime() {
        return System.nanoTime();
    }

}
