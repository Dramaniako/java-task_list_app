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

    public void setId(int id) {
        this.id = id;
    }

    public String getNamaPengguna() {
        return namaPengguna;
    }

    public void setNamaPengguna(String namaPengguna) {
        this.namaPengguna = namaPengguna;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
