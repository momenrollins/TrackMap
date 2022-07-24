package com.houseofdevelopment.gps.notification.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationListData {

    private List<FirstObj> mList = null;

    public List<FirstObj> getmList() {
        return mList;
    }

    public void setmList(List<FirstObj> mList) {
        this.mList = mList;
    }

    public class FirstObj {

        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("n")
        @Expose
        private String n;
        @SerializedName("txt")
        @Expose
        private String txt;
        @SerializedName("ta")
        @Expose
        private Integer ta;
        @SerializedName("td")
        @Expose
        private Integer td;
        @SerializedName("ma")
        @Expose
        private Integer ma;
        @SerializedName("mmtd")
        @Expose
        private Integer mmtd;
        @SerializedName("cdt")
        @Expose
        private Integer cdt;
        @SerializedName("mast")
        @Expose
        private Integer mast;
        @SerializedName("mpst")
        @Expose
        private Integer mpst;
        @SerializedName("cp")
        @Expose
        private Integer cp;
        @SerializedName("fl")
        @Expose
        private Integer fl;
        @SerializedName("tz")
        @Expose
        private Integer tz;
        @SerializedName("la")
        @Expose
        private Integer la;
        @SerializedName("ac")
        @Expose
        private Integer ac;
        @SerializedName("sch")
        @Expose
        private SCH schList;
        @SerializedName("ctrl_sch")
        @Expose
        private CTRL_SCH ctrl_sch;
        @SerializedName("un")
        @Expose
        private Object unList;
        @SerializedName("act")
        @Expose
        private ACT actList;
        @SerializedName("trg")
        @Expose
        private TRG trg;
        @SerializedName("ct")
        @Expose
        private Integer ct;
        @SerializedName("mt")
        @Expose
        private Integer mt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getN() {
            return n;
        }

        public void setN(String n) {
            this.n = n;
        }

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }

        public Integer getTa() {
            return ta;
        }

        public void setTa(Integer ta) {
            this.ta = ta;
        }

        public Integer getTd() {
            return td;
        }

        public void setTd(Integer td) {
            this.td = td;
        }

        public Integer getMa() {
            return ma;
        }

        public void setMa(Integer ma) {
            this.ma = ma;
        }

        public Integer getMmtd() {
            return mmtd;
        }

        public void setMmtd(Integer mmtd) {
            this.mmtd = mmtd;
        }

        public Integer getCdt() {
            return cdt;
        }

        public void setCdt(Integer cdt) {
            this.cdt = cdt;
        }

        public Integer getMast() {
            return mast;
        }

        public void setMast(Integer mast) {
            this.mast = mast;
        }

        public Integer getMpst() {
            return mpst;
        }

        public void setMpst(Integer mpst) {
            this.mpst = mpst;
        }

        public Integer getCp() {
            return cp;
        }

        public void setCp(Integer cp) {
            this.cp = cp;
        }

        public Integer getFl() {
            return fl;
        }

        public void setFl(Integer fl) {
            this.fl = fl;
        }

        public Integer getTz() {
            return tz;
        }

        public void setTz(Integer tz) {
            this.tz = tz;
        }

        public Integer getLa() {
            return la;
        }

        public void setLa(Integer la) {
            this.la = la;
        }

        public Integer getAc() {
            return ac;
        }

        public void setAc(Integer ac) {
            this.ac = ac;
        }

        public SCH getSchList() {
            return schList;
        }

        public void setSchList(SCH schList) {
            this.schList = schList;
        }

        public CTRL_SCH getCtrl_sch() {
            return ctrl_sch;
        }

        public void setCtrl_sch(CTRL_SCH ctrl_sch) {
            this.ctrl_sch = ctrl_sch;
        }

        public Object getUnList() {
            return unList;
        }

        public void setUnList(Object unList) {
            this.unList = unList;
        }

        public ACT getActList() {
            return actList;
        }

        public void setActList(ACT actList) {
            this.actList = actList;
        }

        public TRG getTrg() {
            return trg;
        }

        public void setTrg(TRG trg) {
            this.trg = trg;
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



    }

    private static class SCH {

        @SerializedName("f1")
        @Expose
        private Integer f1;
        @SerializedName("f2")
        @Expose
        private Integer f2;
        @SerializedName("t1")
        @Expose
        private Integer t1;
        @SerializedName("t2")
        @Expose
        private Integer t2;
        @SerializedName("m")
        @Expose
        private Integer m;
        @SerializedName("y")
        @Expose
        private Integer y;
        @SerializedName("w")
        @Expose
        private Integer w;
        @SerializedName("fl")
        @Expose
        private Integer fl;

        public Integer getF1() {
            return f1;
        }

        public void setF1(Integer f1) {
            this.f1 = f1;
        }

        public Integer getF2() {
            return f2;
        }

        public void setF2(Integer f2) {
            this.f2 = f2;
        }

        public Integer getT1() {
            return t1;
        }

        public void setT1(Integer t1) {
            this.t1 = t1;
        }

        public Integer getT2() {
            return t2;
        }

        public void setT2(Integer t2) {
            this.t2 = t2;
        }

        public Integer getM() {
            return m;
        }

        public void setM(Integer m) {
            this.m = m;
        }

        public Integer getY() {
            return y;
        }

        public void setY(Integer y) {
            this.y = y;
        }

        public Integer getW() {
            return w;
        }

        public void setW(Integer w) {
            this.w = w;
        }

        public Integer getFl() {
            return fl;
        }

        public void setFl(Integer fl) {
            this.fl = fl;
        }
    }

    private static class CTRL_SCH {

        @SerializedName("f1")
        @Expose
        private Integer f1;
        @SerializedName("f2")
        @Expose
        private Integer f2;
        @SerializedName("t1")
        @Expose
        private Integer t1;
        @SerializedName("t2")
        @Expose
        private Integer t2;
        @SerializedName("m")
        @Expose
        private Integer m;
        @SerializedName("y")
        @Expose
        private Integer y;
        @SerializedName("w")
        @Expose
        private Integer w;
        @SerializedName("fl")
        @Expose
        private Integer fl;

        public Integer getF1() {
            return f1;
        }

        public void setF1(Integer f1) {
            this.f1 = f1;
        }

        public Integer getF2() {
            return f2;
        }

        public void setF2(Integer f2) {
            this.f2 = f2;
        }

        public Integer getT1() {
            return t1;
        }

        public void setT1(Integer t1) {
            this.t1 = t1;
        }

        public Integer getT2() {
            return t2;
        }

        public void setT2(Integer t2) {
            this.t2 = t2;
        }

        public Integer getM() {
            return m;
        }

        public void setM(Integer m) {
            this.m = m;
        }

        public Integer getY() {
            return y;
        }

        public void setY(Integer y) {
            this.y = y;
        }

        public Integer getW() {
            return w;
        }

        public void setW(Integer w) {
            this.w = w;
        }

        public Integer getFl() {
            return fl;
        }

        public void setFl(Integer fl) {
            this.fl = fl;
        }
    }

    private class ACT {

        @SerializedName("t")
        @Expose
        private String t;
        @SerializedName("p")
        @Expose
        private ActP p;

        public String getT() {
            return t;
        }

        public void setT(String t) {
            this.t = t;
        }

        public ActP getP() {
            return p;
        }

        public void setP(ActP p) {
            this.p = p;
        }
    }

    private static class ActP {
        @SerializedName("email_to")
        @Expose
        private String email_to;
        @SerializedName("html")
        @Expose
        private String html;
        @SerializedName("img_attach")
        @Expose
        private String img_attach;
        @SerializedName("subj")
        @Expose
        private String subj;

        public String getEmail_to() {
            return email_to;
        }

        public void setEmail_to(String email_to) {
            this.email_to = email_to;
        }

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }

        public String getImg_attach() {
            return img_attach;
        }

        public void setImg_attach(String img_attach) {
            this.img_attach = img_attach;
        }

        public String getSubj() {
            return subj;
        }

        public void setSubj(String subj) {
            this.subj = subj;
        }
    }

    private class TRG {

        @SerializedName("t")
        @Expose
        private String t;
        @SerializedName("p")
        @Expose
        private TrgP trgP;

        public String getT() {
            return t;
        }

        public void setT(String t) {
            this.t = t;
        }

        public TrgP getTrgP() {
            return trgP;
        }

        public void setTrgP(TrgP trgP) {
            this.trgP = trgP;
        }
    }

    private static class TrgP {

        @SerializedName("lower_bound")
        @Expose
        private String lower_bound;
        @SerializedName("merge")
        @Expose
        private String merge;
        @SerializedName("prev_msg_diff")
        @Expose
        private String prev_msg_diff;
        @SerializedName("sensor_name_mask")
        @Expose
        private String sensor_name_mask;
        @SerializedName("sensor_type")
        @Expose
        private String sensor_type;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("upper_bound")
        @Expose
        private String upper_bound;

        public String getLower_bound() {
            return lower_bound;
        }

        public void setLower_bound(String lower_bound) {
            this.lower_bound = lower_bound;
        }

        public String getMerge() {
            return merge;
        }

        public void setMerge(String merge) {
            this.merge = merge;
        }

        public String getPrev_msg_diff() {
            return prev_msg_diff;
        }

        public void setPrev_msg_diff(String prev_msg_diff) {
            this.prev_msg_diff = prev_msg_diff;
        }

        public String getSensor_name_mask() {
            return sensor_name_mask;
        }

        public void setSensor_name_mask(String sensor_name_mask) {
            this.sensor_name_mask = sensor_name_mask;
        }

        public String getSensor_type() {
            return sensor_type;
        }

        public void setSensor_type(String sensor_type) {
            this.sensor_type = sensor_type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUpper_bound() {
            return upper_bound;
        }

        public void setUpper_bound(String upper_bound) {
            this.upper_bound = upper_bound;
        }
    }
}


