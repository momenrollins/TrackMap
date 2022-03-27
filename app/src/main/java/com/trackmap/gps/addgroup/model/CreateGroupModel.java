package com.trackmap.gps.addgroup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreateGroupModel {

    @SerializedName("item")
    @Expose
    public List<Item> items = null;
    @SerializedName("flags")
    @Expose
    private Integer flags;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Integer getFlags() {
        return flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    public static class Item {

        @SerializedName("nm")
        @Expose
        private String nm;
        @SerializedName("cls")
        @Expose
        private Integer cls;
        @SerializedName("id")
        @Expose
        public Integer id;
        @SerializedName("mu")
        @Expose
        private Integer mu;
        @SerializedName("u")
        @Expose
        private Object u;
        @SerializedName("uacl")
        @Expose
        private Object uacl;

        public String getNm() {
            return nm;
        }

        public void setNm(String nm) {
            this.nm = nm;
        }

        public Integer getCls() {
            return cls;
        }

        public void setCls(Integer cls) {
            this.cls = cls;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getMu() {
            return mu;
        }

        public void setMu(Integer mu) {
            this.mu = mu;
        }

        public Object getU() {
            return u;
        }

        public void setU(Object u) {
            this.u = u;
        }

        public Object getUacl() {
            return uacl;
        }

        public void setUacl(Object uacl) {
            this.uacl = uacl;
        }
    }
}
