package com.scheduler.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {

    // Sesuaikan user dan password dengan database lokal Anda
    private static final String URL = "jdbc:mysql://localhost:3306/db_tugas_mahasiswa";
    private static final String USER = "root";
    private static final String PASSWORD = "12Bayu12";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
