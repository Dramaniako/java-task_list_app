package com.scheduler;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // PERUBAHAN: Load login.fxml dulu
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/scheduler/view/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 400, 350); // Ukuran lebih kecil untuk login
        stage.setTitle("Login - Sistem Penjadwalan Tugas");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
