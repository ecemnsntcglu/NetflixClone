package com.ecs.netflix;

import java.util.List;

public class Kategori {
    private String kategoriAdi;
    private List<Dizi> diziListesi; // Firestore'daki dizileri liste olarak sakla

    public Kategori() {
        // Firebase için boş constructor gerekli
    }

    public Kategori(String kategoriAdi, List<Dizi> diziListesi) {
        this.kategoriAdi = kategoriAdi;
        this.diziListesi = diziListesi;
    }

    public String getKategoriAdi() {
        return kategoriAdi;
    }

    public List<Dizi> getDiziListesi() {
        return diziListesi;
    }

    public void setDiziListesi(List<Dizi> diziListesi) {
        this.diziListesi = diziListesi;
    }
}
