package com.example.virtualthreads;

public class CreatingVirtualThreads {

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            System.out.println("Running in the thread:" + Thread.currentThread().getName());
            System.out.println("Is it a daemon thread? " + Thread.currentThread().isDaemon());
        };

        Thread platformThread1 = new Thread(task);
        platformThread1.start();
        platformThread1.join();

        Thread platformThread2 = Thread.ofPlatform()
              .daemon()
              .name("Platform thread 2")
              .unstarted(task);
        platformThread2.start();
        platformThread2.join();

        Thread virtualThread1 = Thread.ofVirtual()
              .name("Virtual thread 1")
              .unstarted(task);
        virtualThread1.start();
        virtualThread1.join();

        Thread virtualThread2 = Thread.startVirtualThread(task);
        virtualThread2.join();

    }
}










