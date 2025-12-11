package com.scheduler.model;

public class Session {

    private static Pengguna currentUser;

    public static void setUser(Pengguna user) {
        currentUser = user;
    }

    public static Pengguna getUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
