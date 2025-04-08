package org.example.fooddelivery;

import org.example.fooddelivery.util.DatabaseConnection;

public class FoodDeliveryApplication {
    public static void main(String[] args) {
        try {
            System.out.println("Initializing database...");
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            if (dbConnection.testConnection()) {
                System.out.println("Database connection successful!");
            } else {
                System.err.println("Failed to connect to the database.");
                System.exit(1);
            }
            ConsoleApplication app = new ConsoleApplication();
            app.start();
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
