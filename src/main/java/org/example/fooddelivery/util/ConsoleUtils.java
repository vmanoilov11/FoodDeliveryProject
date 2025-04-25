package org.example.fooddelivery.util;

import java.util.Scanner;

public class ConsoleUtils {
    private static final Scanner scanner = new Scanner(System.in);

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(" ".repeat((50 - title.length()) / 2) + title);
        System.out.println("=".repeat(50) + "\n");
    }

    public static void printMenu(String... options) {
        for (int i = 0; i < options.length; i++) {
            System.out.printf("%d. %s%n", i + 1, options[i]);
        }
        System.out.println();
    }

    public static int readChoice(int min, int max) {
        while (true) {
            try {
                System.out.print("Enter your choice (" + min + "-" + max + "): ");
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= min && choice <= max) {
                    return choice;
                }
                System.out.println("Please enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }


    public static void waitForEnter() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public static boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt + " (yes/no): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes")) {
                return true;
            } else if (input.equals("no")) {
                return false;
            }
            System.out.println("Please enter 'yes' or 'no'.");
        }
    }

    public static void printTableHeader(String... columns) {
        StringBuilder separator = new StringBuilder("+");
        StringBuilder header = new StringBuilder("|");

        for (String column : columns) {
            separator.append("-".repeat(30 + 2)).append("+");
            header.append(String.format(" %-" + 30 + "s |", column));
        }

        System.out.println(separator);
        System.out.println(header);
        System.out.println(separator);
    }


    public static void printTableRow(String... values) {
        StringBuilder row = new StringBuilder("|");
        for (String value : values) {
            row.append(String.format(" %-30s |", value));
        }
        System.out.println(row);
    }

    public static void printTableFooter(String... columns) {
        StringBuilder separator = new StringBuilder("+");
        for (int i = 0; i < columns.length; i++) {
            separator.append("-".repeat(32)).append("+");
        }
        System.out.println(separator);
    }




    public static void printSuccess(String message) {
        System.out.println("\n✓ " + message);
    }

    public static void printError(String message) {
        System.out.println("\n✗ " + message);
    }

}
