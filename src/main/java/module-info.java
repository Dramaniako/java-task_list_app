module com.scheduler {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.scheduler to javafx.fxml;
    opens com.scheduler.controller to javafx.fxml;
    // Membuka akses model agar TableView bisa membaca properti
    opens com.scheduler.model to javafx.base; 
    
    exports com.scheduler;
}
