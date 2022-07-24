package com.houseofdevelopment.gps.report.model;


import java.util.List;

public class TempRoot {
    public boolean status;
    public List<TempData> data;
    public String msg;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<TempData> getData() {
        return data;
    }

    public void setData(List<TempData> data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public TempRoot() {
    }

    public TempRoot(boolean status, List<TempData> data, String msg) {
        this.status = status;
        this.data = data;
        this.msg = msg;
    }
}
