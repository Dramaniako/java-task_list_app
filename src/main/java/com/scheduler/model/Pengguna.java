package com.scheduler.model;

public class Pengguna {

    private int id;
    private String namaPengguna;
    private String email;
    private String password;

    public Pengguna(int id, String namaPengguna, String email, String password) {
        this.id = id;
        this.namaPengguna = namaPengguna;
        this.email = email;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getNamaPengguna() {
        return namaPengguna;
    }
    // Getter lain bisa ditambahkan jika perlu
}
