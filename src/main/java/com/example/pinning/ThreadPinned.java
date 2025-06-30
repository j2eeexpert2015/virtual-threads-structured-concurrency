package com.example.pinning;

import com.example.util.ThreadUtil;

import java.util.stream.IntStream;


public class ThreadPinned
{
    private static final Object lock = new Object();

    public static void main(String[] args) {
        // Enable detailed pinning diagnostics
        System.setProperty("jdk.tracePinnedThreads", "full");

        var threadList = IntStream.range(0, 10)
                .mapToObj(i -> Thread.ofVirtual().unstarted(() -> {

                    if (i == 0) {
                        System.out.println(Thread.currentThread());
                    }

                    synchronized (lock) {
                        ThreadUtil.sleepOfMillis(25);
                    }

                    if (i == 0) {
                        System.out.println(Thread.currentThread());
                    }

                })).toList();

        threadList.forEach(Thread::start);
        ThreadUtil.joinAll(threadList);
    }
}
