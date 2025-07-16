package com.example.virtualthreadcreation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadDemo {
    public static void main(String[] args) {
        Callable<HttpResponse<String>> task = VirtualThreadDemo::slowRestCall;
        int noOfRestCalls = 100;
        //platform threads (useVirtualThreads false)
        executeTask(task, noOfRestCalls, false);

        //virtual threads (useVirtualThreads true)
        executeTask(task, noOfRestCalls, true);
    }

    private static void executeTask(Callable<HttpResponse<String>> task, int noOfRestCalls, boolean useVirtualThreads) {
        String threadType = useVirtualThreads ? "virtual" : "platform";

        ExecutorService executorService = useVirtualThreads
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(noOfRestCalls);

        System.out.println("Starting " + threadType + " thread execution for " + noOfRestCalls + " API calls...");

        long startTime = System.currentTimeMillis();

        try (executorService) {
            List<CompletableFuture<HttpResponse<String>>> restCallFutures = submitRestCalls(task, noOfRestCalls, executorService);
            CompletableFuture.allOf(restCallFutures.toArray(new CompletableFuture[0])).join();
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("Total time taken by %s threads for %d API calls: %d ms%n",
                threadType, noOfRestCalls, (endTime - startTime));
    }

    private static List<CompletableFuture<HttpResponse<String>>> submitRestCalls(
            Callable<HttpResponse<String>> task,
            int noOfRestCalls,
            ExecutorService executorService) {

        List<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>(noOfRestCalls);

        for (int i = 0; i < noOfRestCalls; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return task.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executorService));
        }

        return futures;
    }


    private static HttpResponse<String> slowRestCall() {
        HttpResponse<String> response;
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            response = httpClient
                    .send(HttpRequest.newBuilder(new URI("https://randomuser.me/api"))
                            .GET()
                            .build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
