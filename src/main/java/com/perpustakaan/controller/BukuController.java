package com.perpustakaan.controller;

import java.sql.SQLException;

import com.perpustakaan.model.Buku;
import com.perpustakaan.model.BukuDAO;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class BukuController {
    
    // Injeksi komponen dari FXML
    @FXML private TextField txtId;
    @FXML private TextField txtJudul;
    @FXML private TextField txtPengarang;
    @FXML private TableView<Buku> tvBuku;
    @FXML private TableColumn<Buku, Number> colID; // Number karena idBuku adalah IntegerProperty
    @FXML private TableColumn<Buku, String> colJudul;
    @FXML private TableColumn<Buku, String> colPengarang;
    @FXML private Label lblStatus;

    private BukuDAO bukuDAO;
    @FXML
    public void initialize() {
        bukuDAO = new BukuDAO();
        
        // 1. Menghubungkan Kolom TableView ke Model Data (Buku.java)
        colID.setCellValueFactory(data -> data.getValue().idBukuProperty());
        colJudul.setCellValueFactory(data -> data.getValue().judulBukuProperty());
        colPengarang.setCellValueFactory(data -> data.getValue().pengarangProperty());
        
        // 2. Memuat data awal
        loadDataBuku();
        
        // 3. Menambahkan Listener: Mengisi Form saat baris di tabel diklik
        tvBuku.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtId.setText(String.valueOf(newSelection.getIdBuku()));
                txtJudul.setText(newSelection.getJudulBuku());
                txtPengarang.setText(newSelection.getPengarang());
            }
        });
    }
    
    // --- Metode Utilitas ---
    private void loadDataBuku() {
        try {
            tvBuku.setItems(bukuDAO.getAllBuku());
            lblStatus.setText("Data berhasil dimuat.");
        } catch (SQLException e) {
            lblStatus.setText("Gagal memuat data: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        txtId.clear();
        txtJudul.clear();
        txtPengarang.clear();
        tvBuku.getSelectionModel().clearSelection();
    }
    
    // --- Metode CRUD ---

    @FXML
    private void handleSimpan() {
        // Cek jika ID kosong (memastikan ini adalah data baru)
        if (!txtId.getText().isEmpty()) {
            lblStatus.setText("Gunakan tombol 'Ubah' untuk mengupdate data.");
            return;
        }
        if (txtJudul.getText().isEmpty()) {
            lblStatus.setText("Judul tidak boleh kosong!");
            return;
        }

        try {
            Buku bukuBaru = new Buku(0, txtJudul.getText(), txtPengarang.getText());
            bukuDAO.insertBuku(bukuBaru);
            clearForm();
            loadDataBuku();
            lblStatus.setText("✅ Data baru berhasil ditambahkan.");
        } catch (SQLException e) {
            lblStatus.setText("Gagal simpan: " + e.getMessage());
        }
    }

    @FXML
    private void handleUbah() {
        if (txtId.getText().isEmpty()) {
            lblStatus.setText("Pilih data di tabel yang ingin diubah terlebih dahulu.");
            return;
        }

        try {
            int id = Integer.parseInt(txtId.getText());
            Buku bukuDiubah = new Buku(id, txtJudul.getText(), txtPengarang.getText());
            bukuDAO.updateBuku(bukuDiubah);
            clearForm();
            loadDataBuku();
            lblStatus.setText("✅ Data berhasil diubah.");
        } catch (SQLException | NumberFormatException e) {
            lblStatus.setText("Gagal ubah: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleHapus() {
        if (txtId.getText().isEmpty()) {
            lblStatus.setText("Pilih data di tabel yang ingin dihapus terlebih dahulu.");
            return;
        }

        // Tampilkan konfirmasi Alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Yakin hapus buku ini?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            try {
                bukuDAO.deleteBuku(Integer.parseInt(txtId.getText()));
                clearForm();
                loadDataBuku();
                lblStatus.setText("✅ Data berhasil dihapus.");
            } catch (SQLException | NumberFormatException e) {
                lblStatus.setText("Gagal hapus: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleReset() {
        clearForm();
        lblStatus.setText("Status: Siap.");
    }
}