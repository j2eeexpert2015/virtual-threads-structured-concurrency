package com.example.scopedvalue;

import java.util.NoSuchElementException;

/**
 * Demonstrates dynamic scoping with ScopedValue: bound value
 * flows through the call chain without parameters
 */
public class DynamicScopingDemo {

    // Declare ScopedValue - initially unbound
    private static final ScopedValue<String> USER = ScopedValue.newInstance();

    public static void main(String[] args) {
        DynamicScopingDemo demo = new DynamicScopingDemo();

        System.out.println("=== BEFORE BINDING ===");
        demo.checkValue();

        System.out.println("\n=== INSIDE BINDING (Dynamic Scope Active) ===");
        ScopedValue.runWhere(USER, "User 1", demo::method1);

        System.out.println("\n=== AFTER BINDING ENDS ===");
        demo.checkValue();
    }

    // First method in call chain
    private void method1() {
        System.out.println("Inside method 1");
        checkValue();
        method2();
    }

    // Second method in call chain
    private void method2() {
        System.out.println("Inside method 2");
        checkValue();
        method3();
    }

    // Third method in call chain
    private void method3() {
        System.out.println("Inside method 3");
        checkValue();
    }

    // Checks if USER has a bound value
    private void checkValue() {
        if (USER.isBound()) {
            System.out.println("✅ USER is bound to: " + USER.get());
        } else {
            System.out.println("❌ USER not accessible");
        }
    }
}
