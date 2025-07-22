package com.example.mountandunmount;

public class VirtualThreadAndForkJoinPool {
    public static void main(String[] args) throws InterruptedException {
        //System.setProperty("jdk.virtualThreadScheduler.parallelism", "8");
        // Print available processors
        //System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        //System.out.println("Virtual Thread Carrier Parallelism: " + System.getProperty("jdk.virtualThreadScheduler.parallelism"));
        Thread thread = new Thread(() -> {
            System.out.println("Thread: " + Thread.currentThread());
        });
        thread.start();

        Thread virtualThread1 = Thread.startVirtualThread(() -> {
            System.out.println("Virtual Thread 1: " + Thread.currentThread());
        });
        virtualThread1.join();

        Thread virtualThread2 = Thread.startVirtualThread(() -> {
            System.out.println("Virtual Thread 2: " + Thread.currentThread());
        });
        virtualThread2.join();

        /*
        for (int i = 0; i < 200; i++) {
            final int threadNumber = i + 1;
            Thread virtualThread = Thread.startVirtualThread(() -> {
                System.out.println("Virtual Thread " + threadNumber + ": " + Thread.currentThread());

                try {
                    Thread.sleep(100); // force blocking
                } catch (InterruptedException ignored) {
                    System.out.println("Exception occurred");
                    ignored.printStackTrace();

                }
            });
            virtualThread.join();
        }
         */

    }
}
