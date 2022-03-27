package com.trackmap.gps.report.model;


import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class RepModel implements Serializable {

    private Integer id;
    private String n;
    private String ct;
    private Integer c;

    @NotNull
    @Override
    public String toString() {
        return n;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getCt() {
        return ct;
    }

    public void setCt(String ct) {
        this.ct = ct;
    }

    public Integer getC() {
        return c;
    }

    public void setC(Integer c) {
        this.c = c;
    }
}
