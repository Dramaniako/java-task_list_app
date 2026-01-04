package com.scheduler.controller.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.scheduler.controller.scene.SceneController;
import com.scheduler.util.DatabaseHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

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
    private void handleRegister() {
        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConPassword.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            lblError.setText("Semua field harus diisi!");
            return;
        } else if (!password.equals(confirmPassword)) {
            lblError.setText("Confirm password harus sama dengan password");
            return;
        }

        String query = "SELECT * FROM pengguna WHERE email = ? OR nama_Pengguna = ?";
        String regist = "INSERT INTO pengguna (nama_Pengguna, email, password) VALUES (?,?,?)";

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query); PreparedStatement reg = conn.prepareStatement(regist)) {

            pstmt.setString(1, email);
            pstmt.setString(2, username);

            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                reg.setString(1, username);
                reg.setString(2, email);
                reg.setString(3, password);

                reg.executeUpdate();

                switchToLogin();
            } else {
                lblError.setText("Email atau Username sudah digunakan!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblError.setText("Database Error!");
        }
    }

    @FXML
    private void switchToLogin() {
        SceneController.switchToLogin(txtUsername);
    }
}
