package com.example.scopedvalue;

/**
 * Demonstrates using ScopedValue in Java 21+.
 *
 * Unlike ThreadLocal:
 *  - ScopedValue is immutable within its bound scope.
 *  - Value is tied to the scope, not the thread, and disappears after the scope ends.
 *  - Safer for structured concurrency and virtual threads.
 */
public class ScopedValueExample {

    // Create a ScopedValue holder for String data
    private static final ScopedValue<String> VALUE = ScopedValue.newInstance();

    public static void main(String[] args) {

        // Task for Thread-1: bind a value to the scope, then access it
        Runnable task1 = () ->
                ScopedValue.where(VALUE, "Value for Thread-1").run(() -> {
                    System.out.println(Thread.currentThread().getName() +
                            ": " + VALUE.get());
                });

        // Task for Thread-2: bind a different value to the scope
        Runnable task2 = () ->
                ScopedValue.where(VALUE, "Value for Thread-2").run(() -> {
                    System.out.println(Thread.currentThread().getName() +
                            ": " + VALUE.get());
                });

        // Create and start platform threads
        Thread thread1 = new Thread(task1, "Thread-1");
        Thread thread2 = new Thread(task2, "Thread-2");
        thread1.start();
        thread2.start();

        // ===== Virtual Thread examples =====
        // Same logic as above, but using virtual threads

        Thread vThread1 = Thread.ofVirtual()
                .name("VThread-1")
                .unstarted(() ->
                        ScopedValue.where(VALUE, "Value for VThread-1").run(() -> {
                            System.out.println(Thread.currentThread().getName() +
                                    ": " + VALUE.get());
                        })
                );

        Thread vThread2 = Thread.ofVirtual()
                .name("VThread-2")
                .unstarted(() ->
                        ScopedValue.where(VALUE, "Value for VThread-2").run(() -> {
                            System.out.println(Thread.currentThread().getName() +
                                    ": " + VALUE.get());
                        })
                );

        // Start virtual threads
        vThread1.start();
        vThread2.start();

        // ✅ wait for both
        try {
            vThread1.join();
            vThread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
