package com.scheduler.model;

import java.time.LocalDate;

public class Tugas {

    private int id;
    private String namaTugas;
    private String deskripsi;
    private LocalDate tenggat;
    private String status;
    private String id_Kelompok;

    public Tugas(int id, String namaTugas, String deskripsi, LocalDate tenggat, String status, String id_Kelompok) {
        this.id = id;
        this.namaTugas = namaTugas;
        this.deskripsi = deskripsi;
        this.tenggat = tenggat;
        this.status = status;
        this.id_Kelompok = id_Kelompok;
    }

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

    public String getId_Kelompok() {
        return id_Kelompok;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNamaTugas(String namaTugas) {
        this.namaTugas = namaTugas;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public void setTenggat(LocalDate tenggat) {
        this.tenggat = tenggat;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setId_Kelompok(String id_Kelompok) {
        this.id_Kelompok = id_Kelompok;
    }
}
