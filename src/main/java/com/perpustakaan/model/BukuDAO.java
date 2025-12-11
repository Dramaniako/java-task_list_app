package com.perpustakaan.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BukuDAO {

    // Konfigurasi Koneksi (GANTI JIKA PERLU)
    private static final String URL = "jdbc:mysql://localhost:3306/perpustakaan";
    private static final String USER = "root";
    private static final String PASS = "12Bayu12"; // GANTI DENGAN PASSWORD ANDA

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // --- R (READ / Menampilkan Semua Data) ---
    public ObservableList<Buku> getAllBuku() throws SQLException {
        ObservableList<Buku> daftarBuku = FXCollections.observableArrayList();
        String sql = "SELECT id_buku, judul, pengarang FROM buku";

        // Penggunaan Try-with-Resources untuk menutup koneksi otomatis
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Buku buku = new Buku(
                        rs.getInt("id_buku"),
                        rs.getString("judul"),
                        rs.getString("pengarang")
                );
                daftarBuku.add(buku);
            }
        }
        return daftarBuku;
    }

    // --- C (CREATE / Menambah Data) ---
    public void insertBuku(Buku buku) throws SQLException {
        String sql = "INSERT INTO buku (judul, pengarang) VALUES (?, ?)";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, buku.getJudulBuku());
            ps.setString(2, buku.getPengarang());
            ps.executeUpdate();
        }
    }

    // --- U (UPDATE / Mengubah Data) ---
    public void updateBuku(Buku buku) throws SQLException {
        String sql = "UPDATE buku SET judul = ?, pengarang = ? WHERE id_buku = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, buku.getJudulBuku());
            ps.setString(2, buku.getPengarang());
            ps.setInt(3, buku.getIdBuku());
            ps.executeUpdate();
        }
    }

    // --- D (DELETE / Menghapus Data) ---
    public void deleteBuku(int idBuku) throws SQLException {
        String sql = "DELETE FROM buku WHERE id_buku = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idBuku);
            ps.executeUpdate();
        }
    }
}
