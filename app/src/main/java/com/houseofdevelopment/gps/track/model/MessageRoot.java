package com.houseofdevelopment.gps.track.model;

public class MessageRoot {

    public String date;
    public String lat;
    public String lng;
    public String altitude;
    public String angle;
    public String speed;
    public Params params;
    public Boolean isSelected = false;

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public MessageRoot(String date, String lat, String lng, String altitude, String angle, String speed, Params params) {
        this.date = date;
        this.lat = lat;
        this.lng = lng;
        this.altitude = altitude;
        this.angle = angle;
        this.speed = speed;
        this.params = params;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getAngle() {
        return angle;
    }

    public void setAngle(String angle) {
        this.angle = angle;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }
}
