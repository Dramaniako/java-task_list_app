package com.perpustakaan.model;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Buku {
    // Gunakan Property agar TableView dapat mendeteksi perubahan
    private final IntegerProperty idBuku;
    private final StringProperty judulBuku;
    private final StringProperty pengarang;

    public Buku(int idBuku, String judulBuku, String pengarang) {
        this.idBuku = new SimpleIntegerProperty(idBuku);
        this.judulBuku = new SimpleStringProperty(judulBuku);
        this.pengarang = new SimpleStringProperty(pengarang);
    }
    
    // --- Getters untuk Properties (WAJIB untuk TableView) ---
    public IntegerProperty idBukuProperty() {
        return idBuku;
    }
    public StringProperty judulBukuProperty() {
        return judulBuku;
    }
    public StringProperty pengarangProperty() {
        return pengarang;
    }
    
    // --- Getters Nilai Normal ---
    public int getIdBuku() { return idBuku.get(); }
    public String getJudulBuku() { return judulBuku.get(); }
    public String getPengarang() { return pengarang.get(); }
}
