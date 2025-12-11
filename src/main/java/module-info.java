module com.perpustakaan {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base; 

    exports com.perpustakaan;

    opens com.perpustakaan to javafx.fxml;
    // 2. IZINKAN FXML LOADER MENGAKSES CONTROLLER 
    // Direktif 'opens' adalah cara yang disarankan untuk Controller.
    opens com.perpustakaan.controller to javafx.fxml; 
    // 3. Izinkan TableView mengakses properti Model (penting untuk CRUD)
    opens com.perpustakaan.model to javafx.base; 
}
