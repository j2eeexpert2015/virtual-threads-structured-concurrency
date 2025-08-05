package com.example.continuation;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

/*
 To compile and run this in IntelliJ:

 1. Add to Run/Debug Configurations (VM Options):
      --add-exports java.base/jdk.internal.vm=ALL-UNNAMED

 2. Add to Compiler Settings (Java Compiler → Additional command line parameters):
      --add-exports java.base/jdk.internal.vm=ALL-UNNAMED
*/

public class SimpleContinuationDemo {
    public static void main(String[] args) {
        ContinuationScope scope = new ContinuationScope("DemoScope");

        Continuation cont = new Continuation(scope, () -> {
            System.out.println("Step 1: Inside continuation");
            Continuation.yield(scope);
            System.out.println("Step 2: Resumed continuation");
            Continuation.yield(scope);
            System.out.println("Step 3: Continuation finished");
        });

        System.out.println("Invoking continuation - first time !");
        cont.run();
        System.out.println("Invoking continuation - second time !");
        cont.run();
        System.out.println("Invoking continuation - third  time !");
        cont.run();


        /*
        while (!cont.isDone()) {
            System.out.println(">>> Calling cont.run()");
            cont.run();
        }
         */

        System.out.println("All done!");
    }
}
