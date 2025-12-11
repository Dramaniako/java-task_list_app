package com.scheduler.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.scheduler.model.Session;
import com.scheduler.model.Tugas;
import com.scheduler.util.DatabaseHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainController {

    @FXML
    private TextField txtNama;
    @FXML
    private TextArea txtDeskripsi;
    @FXML
    private DatePicker dpTenggat;
    @FXML
    private TableView<Tugas> tableTugas;
    @FXML
    private TableColumn<Tugas, String> colNama;
    @FXML
    private TableColumn<Tugas, LocalDate> colTenggat;
    @FXML
    private TableColumn<Tugas, String> colStatus;

    private ObservableList<Tugas> listTugas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Cek jika session kosong (akses ilegal tanpa login)
        if (Session.getUser() == null) {
            System.out.println("User belum login!");
            return;
        }

        colNama.setCellValueFactory(new PropertyValueFactory<>("namaTugas"));
        colTenggat.setCellValueFactory(new PropertyValueFactory<>("tenggat"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadData();
    }

    private void loadData() {
        listTugas.clear();
        // PERUBAHAN: Filter WHERE id_Pengguna = ?
        String query = "SELECT * FROM Tugas WHERE id_Pengguna = ?";

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Ambil ID dari Session
            pstmt.setInt(1, Session.getUser().getId());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                listTugas.add(new Tugas(
                        rs.getInt("id_Tugas"),
                        rs.getString("nama_Tugas"),
                        rs.getString("deskripsi_Tugas"),
                        rs.getDate("tenggat").toLocalDate(),
                        rs.getString("status")
                ));
            }
            tableTugas.setItems(listTugas);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTambah() {
        // PERUBAHAN: Masukkan id_Pengguna dari Session
        String query = "INSERT INTO Tugas (nama_Tugas, deskripsi_Tugas, tenggat, tanggal_Mulai, status, id_Pengguna) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, txtNama.getText());
            pstmt.setString(2, txtDeskripsi.getText());
            pstmt.setDate(3, java.sql.Date.valueOf(dpTenggat.getValue()));
            pstmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.setString(5, "BELUM_DIKERJAKAN");

            // Ambil ID dari Session User
            pstmt.setInt(6, Session.getUser().getId());

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

    private void clearForm() {
        txtNama.clear();
        txtDeskripsi.clear();
        dpTenggat.setValue(null);
    }
}
