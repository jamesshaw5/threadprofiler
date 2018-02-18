package org.jamesshaw.threadprofiler

import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ThreadProfilerInstanceTest extends Specification {

    void test_create_node() {

        given:
        Thread.currentThread().setName("main-test-thread");
        FixedInstrumentation fixedInstrumentation = new FixedInstrumentation();
        ThreadProfilerInstance threadProfilerInstance = new ThreadProfilerInstance(fixedInstrumentation).reset();

        when:
        ThreadProfilerNode node = threadProfilerInstance.node("top level");

        then:
        node.getOperationKey() == "top level";
        node.getDepth() == 0;
        node.getThreadName() == "main-test-thread";
        node.getRecordCount() == 0;

        node.getStartingCycleTime() == 0L;
        node.getLastCycleTime() == 0L;
        node.getElapsedCycleTime() == 0L;

        node.getStartingWallTime() == 0L;
        node.getLastWallTime() == 0L;
        node.getElapsedWallTime() == 0L;


    }

    void test_add_child_item() {

        given:
        Thread.currentThread().setName("main-test-thread");
        FixedInstrumentation fixedInstrumentation = new FixedInstrumentation();
        ThreadProfilerInstance threadProfilerInstance = new ThreadProfilerInstance(fixedInstrumentation).reset();

        when:
        ThreadProfilerNode node = threadProfilerInstance.node("top level");

        and:
        fixedInstrumentation.setCurrentCycleTime(1);
        fixedInstrumentation.setCurrentWallTime(2);

        and:
        node.record("some task");

        then:
        node.getOperationKey() == "top level";
        node.getDepth() == 0;
        node.getThreadName() == "main-test-thread";
        node.getRecordCount() == 1;

        node.getStartingCycleTime() == 0L;
        node.getLastCycleTime() == 1L;
        node.getElapsedCycleTime() == 1L;

        node.getStartingWallTime() == 0L;
        node.getLastWallTime() == 2L;
        node.getElapsedWallTime() == 2L;


    }

    void test_add_child_item_with_description_and_large_times() {

        given:
        Thread.currentThread().setName("main-test-thread");
        FixedInstrumentation fixedInstrumentation = new FixedInstrumentation();
        ThreadProfilerInstance threadProfilerInstance = new ThreadProfilerInstance(fixedInstrumentation).reset();

        when:
        ThreadProfilerNode node = threadProfilerInstance.node("top level");

        and:
        fixedInstrumentation.setCurrentCycleTime(
                Formatter._days + Formatter._hours + Formatter._minutes + Formatter._seconds + Formatter._milliseconds + Formatter._microseconds + Formatter._nanoseconds);

        fixedInstrumentation.setCurrentWallTime(
                999 * (Formatter._days + Formatter._hours + Formatter._minutes + Formatter._seconds + Formatter._milliseconds + Formatter._microseconds + Formatter._nanoseconds));

        and:
        node.record("some task", "loaded 10 rows");

        then:
        node.getOperationKey() == "top level";
        node.getDepth() == 0;
        node.getThreadName() == "main-test-thread";
        node.getRecordCount() == 1;

        node.getStartingCycleTime() == 0L;
        node.getLastCycleTime() == 90061001001001L;
        node.getElapsedCycleTime() == 90061001001001L;

        node.getStartingWallTime() == 0L;
        node.getLastWallTime() == 89970939999999999L;
        node.getElapsedWallTime() == 89970939999999999L;

    }

    void test_joining_threads() {

        given:
        Thread.currentThread().setName("main-test-thread");
        FixedInstrumentation fixedInstrumentation = new FixedInstrumentation();
        ThreadProfilerInstance threadProfilerInstance = new ThreadProfilerInstance(fixedInstrumentation);
        ExecutorService pool = Executors.newFixedThreadPool(1);

        when:
        ThreadProfilerNode node = threadProfilerInstance.node("top level");

        and:
        fixedInstrumentation.setCurrentCycleTime(1);
        fixedInstrumentation.setCurrentWallTime(2);

        and:
        node.record("some task", "loaded 10 rows");

        then:
        String joinKey = "123";
        threadProfilerInstance.joinFrom(joinKey, node);

        when:
        pool.execute({ ->
            threadProfilerInstance.joinTo(joinKey);

            threadProfilerInstance.snapshot("thread - step 1", "10 rows");
            threadProfilerInstance.snapshot("thread - step 2", "10 rows");
            ThreadProfilerNode childNode = threadProfilerInstance.node("thread - subnode");
            childNode.record("thread done", "15 objects");

        });

        threadProfilerInstance.snapshot("waiting for pool");
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        threadProfilerInstance.snapshot("done");

        then:
        node.format() == """
                            ---------------------------------------------------------------------------------------------------------------------------------------------------------------------
                            | Thread           | Key                | Details        |                                     Wall time |                                      CPU time |    CPU % |
                            ---------------------------------------------------------------------------------------------------------------------------------------------------------------------
                            | main-test-thread | top level          |                |                                          2 ns |                                          1 ns |     50 % |
                            | main-test-thread |   some task        | loaded 10 rows |                                          0 ns |                                          0 ns |     -- % |
                            | main-test-thread |   waiting for pool |                |                                          0 ns |                                          0 ns |     -- % |
                            | pool-1-thread-1  |   thread - step 1  | 10 rows        |                                          0 ns |                                          0 ns |     -- % |
                            | pool-1-thread-1  |   thread - step 2  | 10 rows        |                                          0 ns |                                          0 ns |     -- % |
                            | pool-1-thread-1  |   thread - subnode |                |                                          0 ns |                                          0 ns |     -- % |
                            | pool-1-thread-1  |     thread done    | 15 objects     |                                          0 ns |                                          0 ns |     -- % |
                            | main-test-thread |   done             |                |                                          0 ns |                                          0 ns |     -- % |
                            ---------------------------------------------------------------------------------------------------------------------------------------------------------------------
                            """.replaceFirst("\n","").stripIndent();
    }

}
