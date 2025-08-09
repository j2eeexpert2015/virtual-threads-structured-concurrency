package com.example.scopedvalue;

import java.util.NoSuchElementException;

public class ScopedValueBinding {

    private static final ScopedValue<String> USER_ID = ScopedValue.newInstance();
    private static final ScopedValue<Integer> SESSION_COUNT = ScopedValue.newInstance();

    public static void main(String[] args) {
        ScopedValueBinding demo = new ScopedValueBinding();
        demo.demonstrateBinding();
    }

    public void demonstrateBinding() {
        System.out.println("=== BEFORE BINDING ===");
        checkBindingStatus();

        // Bind multiple ScopedValues at once
        ScopedValue.where(USER_ID, "User 1")
                .where(SESSION_COUNT, 200)
                .run(() -> {
                    System.out.println("\n=== INSIDE BINDING ===");
                    checkBindingStatus();
                    performOperation();
                });

        System.out.println("\n=== AFTER BINDING ENDS ===");
        checkBindingStatus();
    }

    private void checkBindingStatus() {
        System.out.println("USER_ID bound: " + USER_ID.isBound());
        System.out.println("SESSION_COUNT bound: " + SESSION_COUNT.isBound());

        try {
            System.out.println("USER_ID: " + USER_ID.get());
        } catch (NoSuchElementException e) {
            System.out.println("USER_ID not accessible");
        }

        try {
            System.out.println("SESSION_COUNT: " + SESSION_COUNT.get());
        } catch (NoSuchElementException e) {
            System.out.println("SESSION_COUNT not accessible");
        }
    }

    private void performOperation() {
        System.out.println("Performing operation for " +
                USER_ID.get() + ", session count: " + SESSION_COUNT.get());
    }
}

