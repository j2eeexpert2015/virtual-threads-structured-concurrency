package com.example.continuation;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;
public class ContinuationDemo {

    private static final ContinuationScope scope = new ContinuationScope("Demo");

    public static void main(String[] args) {
        methodA();
    }
    static void methodA() {
        Continuation cont = new Continuation(scope, ContinuationDemo::methodB);
        cont.run();  // First run
        cont.run();  // Resume from yield
    }
    static void methodB() {
        methodC();
    }
    static void methodC() {
        Continuation.yield(scope);
    }
}
