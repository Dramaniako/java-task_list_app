package com.perpustakaan;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // / PASTIKAN NAMA FXML SESUAI
        scene = new Scene(loadFXML("BukuView"), 700, 600); 
        stage.setTitle("CRUD Buku JavaFX");
        stage.setScene(scene);
        stage.show();
    }
    @Override
    public void stop() throws Exception {
        // Memberikan konfirmasi bahwa siklus hidup aplikasi telah berakhir.
        System.out.println("Aplikasi JavaFX telah dimatikan secara normal.");
        
        // Jika di masa depan Anda menambahkan service/thread background,
        // kode penutupan/shutdown-nya akan diletakkan di sini.
        
        super.stop(); 
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}