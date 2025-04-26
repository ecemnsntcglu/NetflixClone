package com.ecs.netflix;

public class Dizi {
    private String diziAdi;
    private String diziResimUrl;

    public Dizi(String diziAdi, String diziResimUrl) {
        this.diziAdi = diziAdi;
        this.diziResimUrl = diziResimUrl;
    }

    public String getDiziAdi() {
        return diziAdi;
    }

    public String getDiziResimUrl() {
        return diziResimUrl;
    }
}
