package com.kelompok3.YOURNEWS_IDN.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Berita {

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("totalResult")
    @Expose
    private int totalResult;

    @SerializedName("articles")
    @Expose
    private List<Artikel> artikel;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalResult() {
        return totalResult;
    }

    public void setTotalResult(int totalResult) {
        this.totalResult = totalResult;
    }

    public List<Artikel> getArtikel() {
        return artikel;
    }

    public void setArtikel(List<Artikel> artikel) {
        this.artikel = artikel;
    }
}
