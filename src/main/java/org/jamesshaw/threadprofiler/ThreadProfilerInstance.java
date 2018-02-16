package org.jamesshaw.threadprofiler;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Non-singleton instance for those who want to use concrete instances.
 */
public class ThreadProfilerInstance {

    private static final ThreadLocal<Stack<DataElement>> threadLocalDataElementStacks = ThreadLocal.withInitial(Stack::new);

    private final Map<String, DataElement> compositeElements = new ConcurrentHashMap<>();

    public DataElement node(String operationKey, String description) {

        Stack<DataElement> dataElements = threadLocalDataElementStacks.get();
        int depth = dataElements.size();

        DataElement item = new DataElement(depth, Thread.currentThread().getName(), operationKey, description, Instrumentation.getWallTime(), Instrumentation.getCPUTime());
//        System.out.printf("%" +  (10 * (depth+1)) + "s %s %s %d %d%n", item.getThreadName(), operationKey, description, item.getWallTime(), item.getCycles());
        if(dataElements.isEmpty()) {
            // Fine, nothing to link
        }else{
            dataElements.peek().addChild(item);
        }

        dataElements.push(item);

        return item;

    }

    public DataElement snapshot(String operationKey, String description) {

        Stack<DataElement> dataElements = threadLocalDataElementStacks.get();
        int depth = dataElements.size();

        DataElement item = new DataElement(depth+1, Thread.currentThread().getName(), operationKey, description, Instrumentation.getWallTime(), Instrumentation.getCPUTime());
//        System.out.printf("%" + (10 * (depth+1)) + "s %s %s %d %d%n", item.getThreadName(), operationKey, description, item.getWallTime(), item.getCycles());
        if(dataElements.isEmpty()) {
                // This has to be added to something that already exists
            // throw new RuntimeException("You have to have an active node before calling snapshot");
        }else{
            dataElements.peek().addChild(item);
        }

        return item;
    }
}








