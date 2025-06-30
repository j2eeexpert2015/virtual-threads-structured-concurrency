package com.example.pool;

import com.example.util.ThreadUtil;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author hakdogan (hakdogan75@gmail.com)
 * Created on 3.08.2023
 ***/
public class ListPlatformThreads
{
    private static final Pattern WORKER_PATTERN = Pattern.compile("worker-+\\d");
    private static final Pattern POOL_PATTERN = Pattern.compile("@ForkJoinPool-+\\d");
    private static final Set<String> poolNames = ConcurrentHashMap.newKeySet();
    private static final Set<String> pThreadNames = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {

        var threadList = IntStream
                .range(0, 1_000_000)
                .mapToObj(i -> Thread.ofVirtual().unstarted(() -> {

                    var poolName = getPoolName();
                    poolNames.add(poolName);

                    var workerName = getWorkerName();
                    pThreadNames.add(workerName);

                })).toList();


        var start = Instant.now();
        threadList.forEach(Thread::start);
        ThreadUtil.joinAll(threadList);

        System.out.println(String.format("Execution time:  %d ms", ThreadUtil.benchmark(start)));
        System.out.println(String.format("Core             %d", Runtime.getRuntime().availableProcessors()));
        System.out.println(String.format("Pools            %d", poolNames.size()));
        System.out.println(String.format("Platform threads %d", pThreadNames.size()));

    }

    private static String getWorkerName(){
        var name = Thread.currentThread().toString();
        Matcher workerMatcher = WORKER_PATTERN.matcher(name);
        if(workerMatcher.find()){
            return workerMatcher.group();
        }

        return "worker name not found";
    }

    private static String getPoolName(){
        var name = Thread.currentThread().toString();
        Matcher poolMatcher = POOL_PATTERN.matcher(name);
        if(poolMatcher.find()){
            return poolMatcher.group();
        }

        return "pool name not found";
    }
}
