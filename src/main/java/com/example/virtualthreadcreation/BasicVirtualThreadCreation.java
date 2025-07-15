package com.example.virtualthreadcreation;

/**
 * This class demonstrates the most basic way to create and manage threads in Java.
 * It contrasts traditional platform threads with the new virtual threads introduced in Java 21,
 * highlighting their simplicity of creation and execution.
 *
 * It also shows:
 * - Full thread object (Thread.toString()) output
 * - Thread name using getName()
 * - Virtual vs platform characteristics
 */
public class BasicVirtualThreadCreation {
    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            Thread thread = Thread.currentThread();
            System.out.println("@@@@ Starting Task @@@@@");
            System.out.println("-> Full Thread Object     : " + thread); // calls toString()
            System.out.println("-> Thread Name            : " + thread.getName());
            System.out.println("-> Is Virtual Thread?     : " + thread.isVirtual());
            System.out.println("-> Is Daemon Thread?      : " + thread.isDaemon());
        };

        // 1. Platform Thread (default name assigned by JVM)
        Thread platformThread = new Thread(task);
        platformThread.start();
        platformThread.join();

        // 2. Virtual Thread (default unnamed)
        Thread virtualThreadUnnamed = Thread.startVirtualThread(task);
        virtualThreadUnnamed.join();


    }
}
