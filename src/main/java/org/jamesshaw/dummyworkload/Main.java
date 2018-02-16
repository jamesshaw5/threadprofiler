package org.jamesshaw.dummyworkload;

import org.jamesshaw.threadprofiler.DataElement;
import org.jamesshaw.threadprofiler.ThreadProfiler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entry point for the dummy workload used to demo the threadprofiler
 */
public class Main {

    private static double blackhole = 0;

    public static void main(String[] args) {

        DataElement mainElement = ThreadProfiler.node("main", "Main method started");
        int numberOfJobsToSimulate = 1;
        int concurrentThreads = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(concurrentThreads, new ThreadFactory() {
            AtomicInteger count = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "worker-" + count.getAndIncrement());
            }
        });

        ThreadProfiler.snapshot("threadpool", "Setting up thread pool");

        for(int i = 0; i < numberOfJobsToSimulate; i++) {

            executorService.execute(() -> {

                DataElement threadElement = ThreadProfiler.node("thread", "Started");

                // Simulate some smaller CPU bound operation [parsing inputs, permissioning etc]
                simulateCPUBound(50, 100);
                ThreadProfiler.snapshot("validation", "Initial validation complete");

                // Simulate some IO bound operation [loading data that we'll be working with]
                simulateIOBound(150, 250);
                ThreadProfiler.snapshot("initial select", "select * from foo where val='234'");

                // Simulate a larger CPU bound operation [processing the data we've loaded]
                simulateCPUBound(150, 250);
                ThreadProfiler.snapshot("main processing", "main processing complete");

                // Simulate a final IO bound operation [committing some state based on the processing]
                simulateIOBound(20, 30);
                ThreadProfiler.snapshot("final update", "insert into results...");

                threadElement.dump();


            });
            ThreadProfiler.snapshot("threads_started", "Passed threads to executor");

        }

        executorService.shutdown();
        try {
            if(executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                System.out.println("Threads completed successfully");
            }else {
                System.out.println("Thread timeout");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            ThreadProfiler.snapshot("done", "Done waiting");
        }


        mainElement.dump();
        System.out.println(blackhole);

    }

    private static void simulateIOBound(long lowerMillis, long upperMillis) {
        try {
            long targetMillis = ThreadLocalRandom.current().nextLong(lowerMillis, upperMillis);
            Thread.sleep(targetMillis);
        } catch (InterruptedException e) {
        }
    }

    private static void simulateCPUBound(int lowerMillis, int upperMillis) {

        long targetMillis = ThreadLocalRandom.current().nextLong(lowerMillis, upperMillis);

        double dumpVariable = targetMillis;

        for (long stop = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(targetMillis); stop > System.nanoTime();) {

            dumpVariable = Math.atan(dumpVariable);

        }

       Main.blackhole += dumpVariable;
    }
}
