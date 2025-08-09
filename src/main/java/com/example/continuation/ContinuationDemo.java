package com.example.continuation;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

/*
 * Demo of jdk.internal.vm.Continuation (INTERNAL API).
 *
 * IntelliJ setup (add to BOTH):
 *   1) Run/Debug Configuration → VM options
 *   2) Run/Debug Configuration → Modify options → Add additional command line parameters
 *
 * Use these flags:
 *   --add-exports java.base/jdk.internal.vm=ALL-UNNAMED
 *   --add-opens   java.base/jdk.internal.vm=ALL-UNNAMED
 *   --enable-preview   (only needed on JDK 19/20; not required on JDK 21+)
 *
 * Why: Continuation lives in an internal package hidden by the module system.
 * The flags export/open it so the code runs and the debugger can step into it.
 *
 * What this program does:
 *   - Builds a Continuation bound to 'scope' that starts at methodB().
 *   - methodC() calls Continuation.yield(scope) → suspends the continuation.
 *   - First cont.run() runs until yield; second cont.run() resumes after yield and completes.
 *
 * Note: This is unsupported/internal API and can change or be removed. Avoid in production.
 */
public class ContinuationDemo {

    private static final ContinuationScope scope = new ContinuationScope("Demo");

    public static void main(String[] args) {
        methodA();
    }

    static void methodA() {
        Continuation cont = new Continuation(scope, ContinuationDemo::methodB);
        cont.run();  // First run: progresses to yield in methodC()
        cont.run();  // Resume after yield: continuation completes
    }

    static void methodB() {
        methodC();
    }

    static void methodC() {
        Continuation.yield(scope); // Suspend here; caller must run() again to resume
    }
}
