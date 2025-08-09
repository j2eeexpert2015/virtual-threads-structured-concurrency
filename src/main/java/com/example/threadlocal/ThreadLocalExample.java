package com.example.threadlocal;

public class ThreadLocalExample {
    private static ThreadLocal<String> threadLocal = ThreadLocal.withInitial(() -> "Initial Value");
    public static void main(String[] args) {
        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();
            threadLocal.set("Value for " + threadName);
            System.out.println("From thread "+threadName + ",threadLocal value: " + threadLocal.get());
        };
        Thread thread1 = new Thread(task, "Thread-1");
        Thread thread2 = new Thread(task, "Thread-2");
        thread1.start();
        thread2.start();
    }
}
