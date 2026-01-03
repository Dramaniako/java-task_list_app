package com.scheduler.controller.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.scheduler.controller.scene.SceneController;
import com.scheduler.model.Pengguna;
import com.scheduler.model.Session;
import com.scheduler.util.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtConPassword;
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

        String query = "SELECT * FROM pengguna WHERE nama_Pengguna = ? AND password = ?";

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Pengguna user = new Pengguna(
                        rs.getInt("id_Pengguna"),
                        rs.getString("nama_Pengguna"),
                        rs.getString("email"),
                        rs.getString("password")
                );

                Session.setUser(user);

                SceneController.switchToDashboard(txtUsername);;
            } else {
                lblError.setText("Username atau Password salah!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblError.setText("Database Error!");
        }
    }

    @FXML
    private void switchToRegister() {
        SceneController.switchToRegister(txtUsername);
    }
}
