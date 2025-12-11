package com.scheduler.model;

import java.time.LocalDate;

public class Tugas {

    private int id;
    private String namaTugas;
    private String deskripsi;
    private LocalDate tenggat;
    private String status;

    public Tugas(int id, String namaTugas, String deskripsi, LocalDate tenggat, String status) {
        this.id = id;
        this.namaTugas = namaTugas;
        this.deskripsi = deskripsi;
        this.tenggat = tenggat;
        this.status = status;
    }

    // Getters and Setters sesuai konsep Enkapsulasi [cite: 166]
    public int getId() {
        return id;
    }

    public String getNamaTugas() {
        return namaTugas;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public LocalDate getTenggat() {
        return tenggat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
