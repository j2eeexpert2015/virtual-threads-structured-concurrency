package com.example.scopedvalue;

import java.util.NoSuchElementException;

/**
 * Demonstrates ScopedValue rebinding: an inner scope temporarily overrides an
 * outer binding
 */
public class ScopedValueRebind {

    private static final ScopedValue<String> USER = ScopedValue.newInstance();

    public static void main(String[] args) {
        System.out.println("=== BEFORE BINDING ===");
        printUser();

        // Outer binding: USER = "Alice"
        System.out.println("\n=== OUTER BINDING (USER = \"Alice\") ===");
        ScopedValue.runWhere(USER, "Alice", () -> {
            printUser(); // Expect: Alice

            // Inner rebinding: USER = "Bob" (shadows outer)
            System.out.println("\n--- ENTER INNER REBIND (USER = \"Bob\") ---");
            ScopedValue.runWhere(USER, "Bob", () -> {
                printUser(); // Expect: Bob
                System.out.println("Inner work done with USER = " + USER.get());
            });
            System.out.println("--- EXIT INNER REBIND (restores USER = \"Alice\") ---\n");

            printUser(); // Expect: Alice (restored automatically)
            System.out.println("Outer work continues with USER = " + USER.get());
        });

        System.out.println("\n=== AFTER ALL BINDINGS END ===");
        printUser(); // Unbound again
    }

    private static void printUser() {
        System.out.println("USER bound? " + USER.isBound());
        try {
            System.out.println("USER value  : " + USER.get());
        } catch (NoSuchElementException e) {
            System.out.println("USER value  : <not accessible>");
        }
    }
}

