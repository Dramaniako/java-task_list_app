package com.scheduler.controller.scene;

import java.io.IOException;

import com.scheduler.model.Session;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class SceneController {

    private static Stage stage;
    private static Parent root;

    private static void switchScene(Node source, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneController.class.getResource(fxmlPath));
            root = loader.load();

            stage = (Stage) source.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML: " + fxmlPath);
        }
    }

    public static void switchToLogin(Node source) {
        switchScene(source, "/com/scheduler/view/login.fxml", "Login - Sistem Penjadwalan Tugas");
    }

    public static void switchToRegister(Node source) {
        switchScene(source, "/com/scheduler/view/register.fxml", "Register - Sistem Penjadwalan Tugas");
    }

    public static void switchToDashboard(Node source) {
        String title = "Dashboard Tugas - "
                + (Session.getUser() != null ? Session.getUser().getNamaPengguna() : "Guest");
        switchScene(source, "/com/scheduler/view/dashboard.fxml", title);
    }
}
