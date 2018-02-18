package org.jamesshaw.threadprofiler;

/**
 * Singleton wrapper for those who like to use singletons for simplicity.
 */
public class ThreadProfiler {

    private static InstrumentationProvider instrumentationProvider = new ManagementBeanInstrumentation();
    private static final ThreadProfilerInstance instance = new ThreadProfilerInstance(instrumentationProvider);


    public static ThreadProfilerNode snapshot(String operationKey, String description) {
        return instance.snapshot(operationKey, description);
    }

    public static ThreadProfilerNode node(String operationKey) {
        return instance.node(operationKey, "");
    }

    public static ThreadProfilerNode snapshot(String operationKey) {
        return instance.snapshot(operationKey, "");
    }

    public static ThreadProfilerNode node(String operationKey, String description) {
        return instance.node(operationKey, description);
    }

    public static String format() {
        return instance.format();
    }

    public static ThreadProfilerInstance getInstance() {
        return instance;
    }
}
