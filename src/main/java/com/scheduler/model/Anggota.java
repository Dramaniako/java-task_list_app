package com.scheduler.model;

public class Anggota {

    private int idAnggota;
    private int idPengguna;
    private int idKelompok;
    private String namaPengguna;
    private String statusAnggota;

    public Anggota(int idAnggota, int idKelompok, int idPengguna, String namaPengguna, String statusAnggota) {
        this.idAnggota = idAnggota;
        this.idKelompok = idKelompok;
        this.idPengguna = idPengguna;
        this.namaPengguna = namaPengguna;
        this.statusAnggota = statusAnggota;
    }

    public int getIdAnggota() {
        return idAnggota;
    }

    public void setIdAnggota(int idAnggota) {
        this.idAnggota = idAnggota;
    }

    public int getIdPengguna() {
        return idPengguna;
    }

    public void setIdPengguna(int idPengguna) {
        this.idPengguna = idPengguna;
    }

    public int getIdKelompok() {
        return idKelompok;
    }

    public void setIdKelompok(int idKelompok) {
        this.idKelompok = idKelompok;
    }

    public String getNamaPengguna() {
        return namaPengguna;
    }

    public void setNamaPengguna(String namaPengguna) {
        this.namaPengguna = namaPengguna;
    }

    public String getStatusAnggota() {
        return statusAnggota;
    }

    public void setStatusAnggota(String statusAnggota) {
        this.statusAnggota = statusAnggota;
    }

}
