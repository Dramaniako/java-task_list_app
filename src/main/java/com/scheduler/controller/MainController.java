package com.scheduler.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

import com.scheduler.controller.MainController.GroupOption;
import com.scheduler.model.Session;
import com.scheduler.model.Tugas;
import com.scheduler.util.DatabaseHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainController {

    @FXML
    private TextField txtNama;
    @FXML
    private TextArea txtDeskripsi;
    @FXML
    private DatePicker dpTenggat;
    @FXML
    private TextField txtKelompok;
    @FXML
    private TableView<Tugas> tableTugas;
    @FXML
    private ChoiceBox<GroupOption> kelompok;
    @FXML
    private TableColumn<Tugas, String> colNama;
    @FXML
    private TableColumn<Tugas, LocalDate> colTenggat;
    @FXML
    private TableColumn<Tugas, String> colStatus;

    private ObservableList<Tugas> listTugas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        if (Session.getUser() == null) {
            System.out.println("User belum login!");
            return;
        }

        colNama.setCellValueFactory(new PropertyValueFactory<>("namaTugas"));
        colTenggat.setCellValueFactory(new PropertyValueFactory<>("tenggat"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 1. Load the Groups into ChoiceBox first
        loadKelompokOptions();

        // 2. Add listener: When user changes Group in ChoiceBox, reload the table
        kelompok.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadData();
            }
        });

        // 3. Select the first group by default (if any exist) to trigger loadData
        if (!kelompok.getItems().isEmpty()) {
            kelompok.getSelectionModel().selectFirst();
        }
    }

    private void loadKelompokOptions() {
        // Query to find groups where the current user is a member (Anggota)
        // Adjust table names (Kelompok/Anggota) if yours are different
        String query = "SELECT kelompok.id_Kelompok, kelompok.nama_Kelompok FROM kelompok JOIN anggota ON kelompok.id_Kelompok = anggota.id_Kelompok WHERE anggota.id_Pengguna = ?";

        ObservableList<GroupOption> options = FXCollections.observableArrayList();

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, Session.getUser().getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                options.add(new GroupOption(
                        rs.getInt("id_Kelompok"),
                        rs.getString("nama_Kelompok")
                ));
            }
            kelompok.setItems(options);

            if (!options.isEmpty()) {
                kelompok.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        listTugas.clear();
        GroupOption selectedGroup = kelompok.getValue();

        String query;

        if (selectedGroup == null) {
            query = "SELECT * FROM Tugas WHERE id_Pengguna = ?";
        } else {
            query = "SELECT * FROM Tugas WHERE id_Pengguna = ? AND id_Kelompok = ?";
        }

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, Session.getUser().getId());
            pstmt.setInt(2, selectedGroup.getId()); // Use ID from ChoiceBox

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                listTugas.add(new Tugas(
                        rs.getInt("id_Tugas"),
                        rs.getString("nama_Tugas"),
                        rs.getString("deskripsi_Tugas"),
                        rs.getDate("tenggat").toLocalDate(),
                        rs.getString("status"),
                        rs.getString("id_Kelompok")
                ));
            }
            tableTugas.setItems(listTugas);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTambah() {
        GroupOption selectedGroup = kelompok.getValue();
        if (selectedGroup == null) {
            System.out.println("Pilih kelompok terlebih dahulu!");
            return;
        }

        String query = "INSERT INTO Tugas (nama_Tugas, deskripsi_Tugas, tenggat, tanggal_Mulai, status, id_Pengguna, id_Kelompok) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, txtNama.getText());
            pstmt.setString(2, txtDeskripsi.getText());
            pstmt.setDate(3, java.sql.Date.valueOf(dpTenggat.getValue()));
            pstmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.setString(5, "BELUM_DIKERJAKAN");

            // Set User ID
            pstmt.setInt(6, Session.getUser().getId());

            // Set Group ID from ChoiceBox
            pstmt.setString(7, txtKelompok.getText());

            pstmt.executeUpdate();
            loadData();
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fungsi DELETE: Menghapus tugas [cite: 90]
    @FXML
    private void handleHapus() {
        Tugas selected = tableTugas.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "DELETE FROM Tugas WHERE id_Tugas = ?";
            try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleStatus() {
        Tugas selected = tableTugas.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "UPDATE Tugas SET status = 'selesai' WHERE id_Tugas = ?";
            try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void clearForm() {
        txtNama.clear();
        txtDeskripsi.clear();
        dpTenggat.setValue(null);
    }

    public static class GroupOption {

        private final int id;
        private final String name;

        public GroupOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        } // This is what shows in the ChoiceBox
    }

    @FXML
    private void handleBuatKelompok() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buat Kelompok Baru");
        dialog.setHeaderText("Masukkan nama kelompok baru:");
        dialog.setContentText("Nama:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(namaKelompok -> {
            if (namaKelompok.trim().isEmpty()) {
                return;
            }

            String sqlInsertGroup = "INSERT INTO Kelompok (nama_Kelompok) VALUES (?)";
            // NEW QUERY: Insert into Anggota table
            String sqlInsertMember = "INSERT INTO Anggota (id_Pengguna, id_Kelompok) VALUES (?, ?)";

            try (Connection conn = DatabaseHelper.connect()) {
                conn.setAutoCommit(false); // Start Transaction

                // 1. Create the Group
                int newGroupId = -1;
                try (PreparedStatement pstmtGroup = conn.prepareStatement(sqlInsertGroup, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtGroup.setString(1, namaKelompok);
                    int affectedRows = pstmtGroup.executeUpdate();

                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = pstmtGroup.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                newGroupId = generatedKeys.getInt(1);
                            }
                        }
                    }
                }

                // 2. Add User to the new Group (in Anggota table)
                if (newGroupId != -1) {
                    try (PreparedStatement pstmtMember = conn.prepareStatement(sqlInsertMember)) {
                        pstmtMember.setInt(1, Session.getUser().getId()); // User ID
                        pstmtMember.setInt(2, newGroupId);                // New Group ID
                        pstmtMember.executeUpdate();
                    }
                    conn.commit(); // Save everything

                    // Refresh UI
                    loadKelompokOptions();
                    showAlert("Sukses", "Kelompok '" + namaKelompok + "' berhasil dibuat!");
                } else {
                    conn.rollback();
                    showAlert("Error", "Gagal mendapatkan ID kelompok baru.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuat kelompok: " + e.getMessage());
            }
        });
    }

    // Helper method for alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
