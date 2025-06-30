package com.example.platformvsvirtual.workload;

import com.example.util.ThreadType;
import com.example.util.ThreadUtil;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import static com.example.util.CommonUtil.waitForUserInput;


public class Measurements
{
    public static void main(String[] args) {
        ExecutorService platformThreadExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ExecutorService virtualThreadExecutorService = Executors.newVirtualThreadPerTaskExecutor();
        waitForUserInput();
        executeTasksOnGivenExecutor(platformThreadExecutorService, ThreadType.PLATFORM);
        waitForUserInput();
        executeTasksOnGivenExecutor(virtualThreadExecutorService, ThreadType.VIRTUAL);
        waitForUserInput();
    }


    private static void executeTasksOnGivenExecutor(final ExecutorService executor, final ThreadType type){

        var startTime = Instant.now();
        IntStream.range(0, 1000)
                .mapToObj(i -> (Runnable) () -> ThreadUtil.sleepOfMillis(50))
                .forEach(executor::submit);

        ThreadUtil.wait(executor);
        System.out.printf("Completion time of %s %s ms%n", type.getDesc(), ThreadUtil.benchmark(startTime));
    }
}
