package com.example.virtualthreads;

public class VirtualThreadCreationWithBuilder {
	public static void main(String[] args) throws InterruptedException {
		// Create a reusable task that prints thread information
		// This same task will run on both platform and virtual threads for comparison
		Runnable task = () -> {
			System.out.println("@@@@ Starting Task @@@@@");
			System.out.println("Running in thread: " + Thread.currentThread());
			System.out.println("Thread name: " + Thread.currentThread().getName());
			System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
			System.out.println("Is it a daemon thread? " + Thread.currentThread().isDaemon());
		};

		System.out.println("\n=== Platform Thread Builder ===");
		// Create a platform thread with custom name and start it immediately
		Thread platformThread = Thread.ofPlatform().name("custom-platform-thread").start(task);
		platformThread.join(); // Wait for platform thread to complete

		System.out.println("\n=== Virtual Thread Builder ===");
		// Create a virtual thread with custom name, start it, and wait for completion
		Thread virtualThreadStarted = Thread.ofVirtual().name("custom-virtual-thread").start(task);
		virtualThreadStarted.join();
		
		System.out.println("\n=== Virtual Thread Builder (Unstarted) ===");
        // Create virtual thread without starting - same pattern works for both thread types
        Thread virtualThreadUnStarted = Thread.ofVirtual()
            .name("unstarted-virtual-thread")
            .unstarted(task);
        System.out.println("state(before start): " + virtualThreadUnStarted.getState());
        virtualThreadUnStarted.start(); // Start when ready
        System.out.println("state(after start): " + virtualThreadUnStarted.getState());
        virtualThreadUnStarted.join();   // Wait for completion
	}
}