package com.example.virtualthreads;

/**
 * Demonstrates creating threads using the Thread.Builder API.
 * Shows:
 * - Platform vs. virtual thread creation
 * - Unstarted thread usage
 */
public class VirtualThreadCreationWithBuilder {

    

    public static void main(String[] args) throws InterruptedException {

        Runnable basicTask = () -> {
            Thread current = Thread.currentThread();
            System.out.println("---- Task Started ----");
            System.out.println("Thread Name: " + current.getName());
            System.out.println("Is Virtual: " + current.isVirtual());
            System.out.println("Is Daemon: " + current.isDaemon());
        };

        System.out.println("\n=== Platform Thread (Builder) ===");
        Thread platformThread = Thread.ofPlatform().name("platform-thread").start(basicTask);
        platformThread.join();

        System.out.println("\n=== Virtual Thread (Started Immediately) ===");
        Thread virtualThread = Thread.ofVirtual().name("virtual-thread").start(basicTask);
        virtualThread.join();

        System.out.println("\n=== Virtual Thread (Unstarted) ===");
        Thread unstartedVirtual = Thread.ofVirtual().name("unstarted-thread").unstarted(basicTask);
        System.out.println("Before start: " + unstartedVirtual.getState());
        unstartedVirtual.start();
        System.out.println("After start: " + unstartedVirtual.getState());
        unstartedVirtual.join();

       
    }
}
