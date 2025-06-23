package com.example.virtualthreads;

/**
 * This class demonstrates the most basic way to create and manage threads in Java.
 * It contrasts traditional platform threads with the new virtual threads introduced in Java 21,
 * highlighting their simplicity of creation and execution.
 */

public class BasicVirtualThreadCreationDemo {
	public static void main(String[] args) throws InterruptedException {
		
		Runnable task = () -> {
            System.out.println("@@@@ Starting Task @@@@@");
            System.out.println("Running in thread: " + Thread.currentThread());
            System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
            System.out.println("Is it a daemon thread? " + Thread.currentThread().isDaemon());
        };
        
        Thread platformThread = new Thread(task);
        platformThread.start();
        platformThread.join();
        
        Thread virtualThread = Thread.startVirtualThread(task);
        virtualThread.join();
		
	}
    
}