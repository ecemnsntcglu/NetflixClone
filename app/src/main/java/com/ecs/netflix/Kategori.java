package com.ecs.netflix;

import java.util.List;

public class Kategori {
    private String kategoriAdi;
    private List<Dizi> diziListesi;

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
}
