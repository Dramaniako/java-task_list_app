package com.scheduler.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.scheduler.model.Pengguna;
import com.scheduler.model.Session;
import com.scheduler.util.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblError;

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Username dan Password harus diisi!");
            return;
        }

        // Validasi ke Database
        // Menggunakan kolom nama_Pengguna dan password sesuai PDF [cite: 245, 246]
        String query = "SELECT * FROM Pengguna WHERE nama_Pengguna = ? AND password = ?";

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Login Berhasil
                Pengguna user = new Pengguna(
                        rs.getInt("id_Pengguna"),
                        rs.getString("nama_Pengguna"),
                        rs.getString("email"),
                        rs.getString("password")
                );

                // Simpan user ke sesi
                Session.setUser(user);

                // Pindah ke Dashboard
                switchToDashboard();
            } else {
                lblError.setText("Username atau Password salah!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblError.setText("Database Error!");
        }
    }

    private void switchToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/scheduler/view/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 500));
            stage.setTitle("Dashboard Tugas - " + Session.getUser().getNamaPengguna());
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
