package com.ecs.netflix;

public class Kullanici {
    public String ad;
    public String soyad;
    public String email;
    public String telefon;

    public Kullanici() {} // Boş constructor gereklidir!

    public Kullanici(String ad, String soyad, String email, String telefon) {
        this.ad = ad;
        this.soyad = soyad;
        this.email = email;
        this.telefon = telefon;
    }
}
