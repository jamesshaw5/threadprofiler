package org.jamesshaw.threadprofiler;

/**
 * Singleton wrapper for those who like to use singletons for simplicity.
 */
public class ThreadProfiler {

    private static final ThreadProfilerInstance instance = new ThreadProfilerInstance();


    public static DataElement snapshot(String operationKey, String description) {
        return instance.snapshot(operationKey, description);
    }

    public static DataElement node(String operationKey, String description) {
        return instance.node(operationKey, description);
    }
}
