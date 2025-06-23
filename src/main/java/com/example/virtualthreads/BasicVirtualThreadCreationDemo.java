package com.example.virtualthreads;

public class BasicVirtualThreadCreationDemo {
	public static void main(String[] args) throws InterruptedException {
		
		Runnable task = () -> {
            System.out.println("@@@@ Starting Task @@@@@");
            System.out.println("Running in thread: " + Thread.currentThread());
            System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
            System.out.println("Is it a daemon thread? " + Thread.currentThread().isDaemon());
        };
        
        Thread platformThread1 = new Thread(task);
        platformThread1.start();
        platformThread1.join();
        
        Thread.startVirtualThread(task).join();
		
	}
    public void runDemo() {
        System.out.println("\n--- Basic Virtual Thread Creation (Thread.startVirtualThread()) ---");
        
        Runnable task = () -> {
            System.out.println("Hello, World!");
            System.out.println("Running in thread: " + Thread.currentThread());
            System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
        };
        
        Thread.startVirtualThread(task);
    }
}