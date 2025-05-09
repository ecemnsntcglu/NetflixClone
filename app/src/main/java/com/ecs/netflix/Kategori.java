package com.ecs.netflix;

import java.util.List;

public class Kategori {
    private String kategoriAdi;
    private List<Content> contentList; // 🔥 Artık `Dizi` yerine `Content` nesnesi kullanılıyor

    public Kategori() {
        // Firebase için boş constructor gerekli
    }

    public Kategori(String kategoriAdi, List<Content> contentList) {
        this.kategoriAdi = kategoriAdi;
        this.contentList = contentList;
    }

    public String getKategoriAdi() {
        return kategoriAdi;
    }

    public List<Content> getContentList() {
        return contentList;
    }

    public void setContentList(List<Content> contentList) {
        this.contentList = contentList;
    }
}