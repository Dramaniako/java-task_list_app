package com.scheduler.model;

public class Kelompok {

    private int idKelompok;
    private String namaKelompok;
    private int idKetua;

    public Kelompok(int idKelompok, int idKetua, String namaKelompok) {
        this.idKelompok = idKelompok;
        this.idKetua = idKetua;
        this.namaKelompok = namaKelompok;
    }

    public int getIdKelompok() {
        return idKelompok;
    }

    public void setIdKelompok(int idKelompok) {
        this.idKelompok = idKelompok;
    }

    public String getNamaKelompok() {
        return namaKelompok;
    }

    public void setNamaKelompok(String namaKelompok) {
        this.namaKelompok = namaKelompok;
    }

    public int getIdKetua() {
        return idKetua;
    }

    public void setIdKetua(int idKetua) {
        this.idKetua = idKetua;
    }

}
