package com.houseofdevelopment.gps.geozone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GeoZoneListModel {

    @SerializedName("searchSpec")
    @Expose
    private SearchSpec searchSpec;
    @SerializedName("dataFlags")
    @Expose
    private Object dataFlags;
    @SerializedName("totalItemsCount")
    @Expose
    private Integer totalItemsCount;
    @SerializedName("indexFrom")
    @Expose
    private Integer indexFrom;
    @SerializedName("indexTo")
    @Expose
    private Integer indexTo;
    @SerializedName("items")
    @Expose
    public List<Item> items = null;

    private class SearchSpec {
        @SerializedName("itemsType")
        @Expose
        private String itemsType;
        @SerializedName("propName")
        @Expose
        private String propName;
        @SerializedName("propValueMask")
        @Expose
        private String propValueMask;
        @SerializedName("sortType")
        @Expose
        private String sortType;
        @SerializedName("propType")
        @Expose
        private String propType;
        @SerializedName("or_logic")
        @Expose
        private String orLogic;

        public String getItemsType() {
            return itemsType;
        }

        public void setItemsType(String itemsType) {
            this.itemsType = itemsType;
        }

        public String getPropName() {
            return propName;
        }

        public void setPropName(String propName) {
            this.propName = propName;
        }

        public String getPropValueMask() {
            return propValueMask;
        }

        public void setPropValueMask(String propValueMask) {
            this.propValueMask = propValueMask;
        }

        public String getSortType() {
            return sortType;
        }

        public void setSortType(String sortType) {
            this.sortType = sortType;
        }

        public String getPropType() {
            return propType;
        }

        public void setPropType(String propType) {
            this.propType = propType;
        }

        public String getOrLogic() {
            return orLogic;
        }

        public void setOrLogic(String orLogic) {
            this.orLogic = orLogic;
        }
    }

    public class Item {

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
        private String mu;
        @SerializedName("zl")
        @Expose
        public Object zl;
        @SerializedName("zlmax")
        @Expose
        public Integer zlmax;
        @SerializedName("uacl")
        @Expose
        public Object uacl;

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

        public String getMu() {
            return mu;
        }

        public void setMu(String mu) {
            this.mu = mu;
        }

        public Object getZl() {
            return zl;
        }

        public void setZl(Object zl) {
            this.zl = zl;
        }

        public Integer getZlmax() {
            return zlmax;
        }

        public void setZlmax(Integer zlmax) {
            this.zlmax = zlmax;
        }

        public Object getUacl() {
            return uacl;
        }

        public void setUacl(Object uacl) {
            this.uacl = uacl;
        }
    }

    public class ZL {

        private List<ZLObj> mList = null;

        public List<ZLObj> getmList() {
            return mList;
        }

        public void setmList(List<ZLObj> mList) {
            this.mList = mList;
        }
    }

    public class ZLObj {

        @SerializedName("n")
        @Expose
        private String n;
        @SerializedName("d")
        @Expose
        private String d;
        @SerializedName("id")
        @Expose
        public Integer id;
        @SerializedName("f")
        @Expose
        private Integer f;
        @SerializedName("t")
        @Expose
        private Integer t;
        @SerializedName("w")
        @Expose
        public Double w;
        @SerializedName("e")
        @Expose
        private Integer e;
        @SerializedName("c")
        @Expose
        private Long c;
        @SerializedName("i")
        @Expose
        public Integer i;
        @SerializedName("libId")
        @Expose
        public Integer libId;
        @SerializedName("path")
        @Expose
        private String path;
        @SerializedName("b")
        @Expose
        public BModel b;
        @SerializedName("ct")
        @Expose
        private Integer ct;
        @SerializedName("mt")
        @Expose
        public Integer mt;
        public ArrayList<String> unitIds;

        public boolean chkselect;

        public ArrayList<String> getUnitIds() {
            return unitIds;
        }

        public void setUnitIds(ArrayList<String> unitIds) {
            this.unitIds = unitIds;
        }

        public boolean isChkselect() {
            return chkselect;
        }

        public String getN() {
            return n;
        }

        public void setN(String n) {
            this.n = n;
        }

        public String getD() {
            return d;
        }

        public void setD(String d) {
            this.d = d;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getF() {
            return f;
        }

        public void setF(Integer f) {
            this.f = f;
        }

        public Integer getT() {
            return t;
        }

        public void setT(Integer t) {
            this.t = t;
        }

        public Double getW() {
            return w;
        }

        public void setW(Double w) {
            this.w = w;
        }

        public Integer getE() {
            return e;
        }

        public void setE(Integer e) {
            this.e = e;
        }

        public Long getC() {
            return c;
        }

        public void setC(Long c) {
            this.c = c;
        }

        public Integer getI() {
            return i;
        }

        public void setI(Integer i) {
            this.i = i;
        }

        public Integer getLibId() {
            return libId;
        }

        public void setLibId(Integer libId) {
            this.libId = libId;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public BModel getB() {
            return b;
        }

        public void setB(BModel b) {
            this.b = b;
        }

        public Integer getCt() {
            return ct;
        }

        public void setCt(Integer ct) {
            this.ct = ct;
        }

        public Integer getMt() {
            return mt;
        }

        public void setMt(Integer mt) {
            this.mt = mt;
        }

        public boolean isSelected() {
            return chkselect;
        }

        public void setChkselect(boolean chkselect) {
            this.chkselect = chkselect;
        }
    }

    public static class BModel {
        @SerializedName("min_x")
        @Expose
        private Double min_x;
        @SerializedName("min_y")
        @Expose
        private Double min_y;
        @SerializedName("max_x")
        @Expose
        private Double max_x;
        @SerializedName("max_y")
        @Expose
        private Double max_y;
        @SerializedName("cen_x")
        @Expose
        public Double cen_x;
        @SerializedName("cen_y")
        @Expose
        public Double cen_y;

        public Double getMin_x() {
            return min_x;
        }

        public void setMin_x(Double min_x) {
            this.min_x = min_x;
        }

        public Double getMin_y() {
            return min_y;
        }

        public void setMin_y(Double min_y) {
            this.min_y = min_y;
        }

        public Double getMax_x() {
            return max_x;
        }

        public void setMax_x(Double max_x) {
            this.max_x = max_x;
        }

        public Double getMax_y() {
            return max_y;
        }

        public void setMax_y(Double max_y) {
            this.max_y = max_y;
        }

        public Double getCen_x() {
            return cen_x;
        }

        public void setCen_x(Double cen_x) {
            this.cen_x = cen_x;
        }

        public Double getCen_y() {
            return cen_y;
        }

        public void setCen_y(Double cen_y) {
            this.cen_y = cen_y;
        }
    }
}
