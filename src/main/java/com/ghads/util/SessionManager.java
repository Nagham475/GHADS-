package com.ghads.util;

import com.ghads.model.User;

/**
 * Session utility — holds the currently logged-in user for the application session.
 * Uses a simple static reference (single-user desktop app).
 */
public class SessionManager {

    private static User currentUser;

    private SessionManager() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    public static boolean isCoordinator() {
        return currentUser != null && "COORDINATOR".equals(currentUser.getRole());
    }

    public static void clearSession() {
        currentUser = null;
    }
}
