package com.houseofdevelopment.gps.notification.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotiDetailResponse {

    @SerializedName("created_at")
    @Expose
    private String created_at;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("long")
    @Expose
    private Double longitude;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("speed")
    @Expose
    private String speed;
    @SerializedName("srNum")
    @Expose
    private Integer srNum;
    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("unit")
    @Expose
    private Integer unit_id;
    @SerializedName("unit_name")
    @Expose
    private String unit_name;
    @SerializedName("username")
    @Expose
    private String username;

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public Integer getSrNum() {
        return srNum;
    }

    public void setSrNum(Integer srNum) {
        this.srNum = srNum;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getUnit() {
        return unit_id;
    }

    public void setUnit(Integer unit) {
        this.unit_id = unit;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
