package com.ecs.netflix;

public class Bolum {
    private String ad;
    private int bolumNo;
    private int sure;

    public Bolum() {
    }

    public Bolum(String ad, int bolumNo, int sure) {
        this.ad = ad;
        this.bolumNo = bolumNo;
        this.sure = sure;
    }

    public String getAd() {
        return ad;
    }

    public int getBolumNo() {
        return bolumNo;
    }

    public int getSure() {
        return sure;
    }
}
