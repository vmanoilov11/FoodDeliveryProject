package org.example.fooddelivery.util;

public class PasswordUtils {

    public static boolean isPasswordValid(String password) {
        if (password == null) return false;
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    }


    public static String getPasswordRequirements() {
        return "Password must:\n" +
                "- Be at least 8 characters long\n" +
                "- Contain at least one letter\n" +
                "- Contain at least one number";
    }
}
