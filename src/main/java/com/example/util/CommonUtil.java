package com.example.util;

import java.util.Scanner;

public class CommonUtil {
    // Single Scanner reused across pauses
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Pauses execution until the user presses Enter.
     */
    public static void waitForUserInput() {
        System.out.print("Press Enter to continue...");
        scanner.nextLine(); // Wait for Enter
        System.out.println("Proceeding...\n");
    }


}
