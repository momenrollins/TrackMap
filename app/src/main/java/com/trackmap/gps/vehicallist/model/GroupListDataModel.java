package com.trackmap.gps.vehicallist.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class GroupListDataModel {

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

    public SearchSpec getSearchSpec() {
        return searchSpec;
    }

    public void setSearchSpec(SearchSpec searchSpec) {
        this.searchSpec = searchSpec;
    }

    public Object getDataFlags() {
        return dataFlags;
    }

    public void setDataFlags(Object dataFlags) {
        this.dataFlags = dataFlags;
    }

    public Integer getTotalItemsCount() {
        return totalItemsCount;
    }

    public void setTotalItemsCount(Integer totalItemsCount) {
        this.totalItemsCount = totalItemsCount;
    }

    public Integer getIndexFrom() {
        return indexFrom;
    }

    public void setIndexFrom(Integer indexFrom) {
        this.indexFrom = indexFrom;
    }

    public Integer getIndexTo() {
        return indexTo;
    }

    public void setIndexTo(Integer indexTo) {
        this.indexTo = indexTo;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class SearchSpec {

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


    public static class Item implements Serializable {

        @SerializedName("nm")
        @Expose
        private String nm;
        @SerializedName("cls")
        @Expose
        private Integer cls;
        @SerializedName("id")
        @Expose
        public Integer id;
        @SerializedName("prp")
        @Expose
        private Object prp;
        @SerializedName("crt")
        @Expose
        private Integer crt;
        @SerializedName("bact")
        @Expose
        private Integer bact;
        @SerializedName("gd")
        @Expose
        private String gd;
        @SerializedName("mu")
        @Expose
        private Integer mu;
        @SerializedName("ct")
        @Expose
        private Integer ct;
        @SerializedName("ftp")
        @Expose
        private Object ftp;
        @SerializedName("flds")
        @Expose
        private Object flds;
        @SerializedName("fldsmax")
        @Expose
        private Integer fldsmax;
        @SerializedName("aflds")
        @Expose
        private Object aflds;
        @SerializedName("afldsmax")
        @Expose
        private Integer afldsmax;
        @SerializedName("uri")
        @Expose
        private String uri;
        @SerializedName("ugi")
        @Expose
        private Integer ugi;
        @SerializedName("u")
        @Expose
        private List<String> u;
        @SerializedName("uacl")
        @Expose
        private Object uacl;
        public boolean isSelected = false;
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

        public /*Prp*/Object getPrp() {
            return prp;
        }

        public void setPrp(Object prp) {
            this.prp = prp;
        }

        public Integer getCrt() {
            return crt;
        }

        public void setCrt(Integer crt) {
            this.crt = crt;
        }

        public Integer getBact() {
            return bact;
        }

        public void setBact(Integer bact) {
            this.bact = bact;
        }

        public String getGd() {
            return gd;
        }

        public void setGd(String gd) {
            this.gd = gd;
        }

        public Integer getMu() {
            return mu;
        }

        public void setMu(Integer mu) {
            this.mu = mu;
        }

        public Integer getCt() {
            return ct;
        }

        public void setCt(Integer ct) {
            this.ct = ct;
        }

        public Object getFtp() {
            return ftp;
        }

        public void setFtp(Object ftp) {
            this.ftp = ftp;
        }

        public Integer getAfldsmax() {
            return afldsmax;
        }

        public void setAfldsmax(Integer afldsmax) {
            this.afldsmax = afldsmax;
        }

        public Object getFlds() {
            return flds;
        }

        public void setFlds(Object flds) {
            this.flds = flds;
        }

        public Integer getFldsmax() {
            return fldsmax;
        }

        public void setFldsmax(Integer fldsmax) {
            this.fldsmax = fldsmax;
        }

        public Object getAflds() {
            return aflds;
        }

        public void setAflds(Object aflds) {
            this.aflds = aflds;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public Integer getUgi() {
            return ugi;
        }

        public void setUgi(Integer ugi) {
            this.ugi = ugi;
        }

        public List<String> getU() {
            return u;
        }

        public void setU(List<String> u) {
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

