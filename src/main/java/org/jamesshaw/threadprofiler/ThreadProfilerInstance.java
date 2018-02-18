package org.jamesshaw.threadprofiler;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Non-singleton instance for those who want to use concrete instances.
 */
public class ThreadProfilerInstance {

    private final ThreadLocal<Stack<ThreadProfilerNode>> threadLocalDataElementStacks = ThreadLocal.withInitial(Stack::new);

    private final Map<String, ThreadProfilerNode> compositeElements = new ConcurrentHashMap<>();
    private final InstrumentationProvider instrumentationProvider;

    private final Map<String, ThreadProfilerNode> joinedNodes = new ConcurrentHashMap<>();

    public ThreadProfilerInstance(InstrumentationProvider instrumentationProvider) {
        this.instrumentationProvider = instrumentationProvider;
    }

    public ThreadProfilerNode clean(String operationKey, String description) {
        threadLocalDataElementStacks.remove();
        return snapshot(operationKey, description);
    }

    public String format() {
        return threadLocalDataElementStacks.get().firstElement().format();
    }

    public void joinFrom(String joinKey, ThreadProfilerNode node) {
        joinedNodes.put(joinKey, node);
    }

    public void joinFrom(String joinKey) {
        joinFrom(joinKey, threadLocalDataElementStacks.get().peek());
    }

    public void joinTo(String joinKey) {

        ThreadProfilerNode threadProfilerNode = joinedNodes.get(joinKey);
        if(threadProfilerNode != null) {
            threadLocalDataElementStacks.get().push(threadProfilerNode);
        }else{
            throw new RuntimeException("No join key registered with key " + joinKey);
        }

    }

    public ThreadProfilerNode node(String operationKey) {
        return node(operationKey, "");
    }


    public ThreadProfilerInstance reset() {
        threadLocalDataElementStacks.remove();
        return this;
    }

    public ThreadProfilerNode node(String operationKey, String description) {

        Stack<ThreadProfilerNode> threadProfilerNodes = threadLocalDataElementStacks.get();
        int newDepth = getNewDepth(threadProfilerNodes);

        ThreadProfilerNode item = new ThreadProfilerNode(instrumentationProvider, newDepth,
                                                         Thread.currentThread().getName(),
                                                         operationKey,
                                                         description,
                                                         instrumentationProvider.getWallTime(),
                                                         instrumentationProvider.getCycleTime());
        //        System.out.printf("%" +  (10 * (depth+1)) + "s %s %s %d %d%n", item.getThreadName(), operationKey, description, item.getWallTime(), item.getCycles());
        if (threadProfilerNodes.isEmpty()) {
            // Fine, nothing to link
        } else {
            threadProfilerNodes.peek().addChild(item);
        }

        threadProfilerNodes.push(item);

        return item;

    }

    private int getNewDepth(Stack<ThreadProfilerNode> threadProfilerNodes) {

        int newDepth;
        if(threadProfilerNodes.isEmpty()) {
            newDepth = 0;
        }else {
            newDepth = threadProfilerNodes.peek().getDepth() + 1;
        }
        return newDepth;
    }

    public ThreadProfilerNode snapshot(String operationKey) {
        return snapshot(operationKey, "");
    }

    public ThreadProfilerNode snapshot(String operationKey, String description) {

        Stack<ThreadProfilerNode> threadProfilerNodes = threadLocalDataElementStacks.get();

        if(threadProfilerNodes.isEmpty()) {
            return node(operationKey, description);
        }else {

            int newDepth = getNewDepth(threadProfilerNodes);

            long wallTime = instrumentationProvider.getWallTime();
            long cycleTime = instrumentationProvider.getCycleTime();

            ThreadProfilerNode item = new ThreadProfilerNode(instrumentationProvider,
                                                             newDepth,
                                                             Thread.currentThread().getName(),
                                                             operationKey,
                                                             description,
                                                             wallTime,
                                                             cycleTime);


            if (threadProfilerNodes.isEmpty()) {
                // This has to be added to something that already exists
                // throw new RuntimeException("You have to have an active node before calling snapshot");
            } else {

                ThreadProfilerNode currentNode = threadProfilerNodes.peek();
                List<ThreadProfilerNode> children = currentNode.getChildren();
                if (children.isEmpty()) {
                    // No sibling to get data from, check out parent

                    item.setStartingCycleTime(currentNode.getLastCycleTime());
                    item.setStartingWallTime(currentNode.getLastWallTime());

                    item.setLastCycleTime(cycleTime);
                    item.setLastWallTime(wallTime);

                } else {
                    ThreadProfilerNode before = children.get(children.size() - 1);

                    item.setStartingCycleTime(before.getLastCycleTime());
                    item.setStartingWallTime(before.getLastWallTime());

                    item.setLastCycleTime(cycleTime);
                    item.setLastWallTime(wallTime);

                }

                children.add(item);
            }

            return item;
        }
    }
}








