package com.trackmap.gps.history.model;

public class TripDetailsGps3 {
    public String type;
    public String lat;
    public String lng;
    public int speed;
    public String dt_start;
    public String dt_end;
    public int duration_time;
    public double route_length;
    public int top_speed;
    public boolean selected;

    public TripDetailsGps3() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDuration_time() {
        return duration_time;
    }

    public void setDuration_time(int duration_time) {
        this.duration_time = duration_time;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public TripDetailsGps3(String type, String lat, String lng, int speed, String dt_start, String dt_end, int duration_time, double route_length, int top_speed) {
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.speed = speed;
        this.dt_start = dt_start;
        this.dt_end = dt_end;
        this.duration_time = duration_time;
        this.route_length = route_length;
        this.top_speed = top_speed;
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

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getDt_start() {
        return dt_start;
    }

    public void setDt_start(String dt_start) {
        this.dt_start = dt_start;
    }

    public String getDt_end() {
        return dt_end;
    }

    public void setDt_end(String dt_end) {
        this.dt_end = dt_end;
    }

    public int getDuration() {
        return duration_time;
    }

    public void setDuration(int duration) {
        this.duration_time = duration;
    }

    public double getRoute_length() {
        return route_length;
    }

    public void setRoute_length(double route_length) {
        this.route_length = route_length;
    }

    public int getTop_speed() {
        return top_speed;
    }

    public void setTop_speed(int top_speed) {
        this.top_speed = top_speed;
    }
}
