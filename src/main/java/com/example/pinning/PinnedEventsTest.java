package com.example.pinning;


import com.example.util.JFRUtil;

import java.util.concurrent.CountDownLatch;

public class PinnedEventsTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("🚀 Starting JFR recording for virtual thread monitoring...");
        JFRUtil.startVirtualThreadRecording("VirtualThreadJFRDemo");
        int vtCount = 100;
        CountDownLatch latch = new CountDownLatch(vtCount);
        Object lock = new Object(); // Shared lock for synchronized block

        for (int i = 0; i < vtCount; i++) {
            Thread.startVirtualThread(() -> {
                try {
                    synchronized (lock) {
                        // Blocking call inside synchronized block
                        Thread.sleep(10); // This will pin the carrier thread
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Wait for all virtual threads to finish
        System.out.println("\n📊 Stopping JFR recording and analyzing results...");
        var jfrFile = JFRUtil.stopRecording();
        if (jfrFile != null) {
            JFRUtil.analyzeRecording(jfrFile);
        }
    }
}
