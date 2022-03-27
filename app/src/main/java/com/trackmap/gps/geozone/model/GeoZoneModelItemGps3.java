package com.trackmap.gps.geozone.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GeoZoneModelItemGps3 implements Serializable, Parcelable {
    public String zone_id;
    public String user_id;
    public String group_id;
    public String zone_name;
    public String zone_color;
    public String zone_visible;
    public String zone_name_visible;
    public String zone_area;
    public List<GeoZoneVerticesGps3>zone_vertices;


    public ArrayList<String> getZone_cars() {
        return zone_cars;
    }

    public void setZone_cars(ArrayList<String> zone_cars) {
        this.zone_cars = zone_cars;
    }

    private ArrayList<String> zone_cars;
    public boolean chkselect;

    protected GeoZoneModelItemGps3(Parcel in) {
        zone_id = in.readString();
        user_id = in.readString();
        group_id = in.readString();
        zone_name = in.readString();
        zone_color = in.readString();
        zone_visible = in.readString();
        zone_name_visible = in.readString();
        zone_area = in.readString();
        chkselect = in.readByte() != 0;
    }

    public static final Creator<GeoZoneModelItemGps3> CREATOR = new Creator<GeoZoneModelItemGps3>() {
        @Override
        public GeoZoneModelItemGps3 createFromParcel(Parcel in) {
            return new GeoZoneModelItemGps3(in);
        }

        @Override
        public GeoZoneModelItemGps3[] newArray(int size) {
            return new GeoZoneModelItemGps3[size];
        }
    };

    public String getZone_id() {
        return zone_id;
    }

    public void setZone_id(String zone_id) {
        this.zone_id = zone_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getZone_name() {
        return zone_name;
    }

    public void setZone_name(String zone_name) {
        this.zone_name = zone_name;
    }

    public String getZone_color() {
        return zone_color;
    }

    public void setZone_color(String zone_color) {
        this.zone_color = zone_color;
    }

    public String getZone_visible() {
        return zone_visible;
    }

    public void setZone_visible(String zone_visible) {
        this.zone_visible = zone_visible;
    }

    public String getZone_name_visible() {
        return zone_name_visible;
    }

    public void setZone_name_visible(String zone_name_visible) {
        this.zone_name_visible = zone_name_visible;
    }

    public String getZone_area() {
        return zone_area;
    }

    public void setZone_area(String zone_area) {
        this.zone_area = zone_area;
    }

    public List<GeoZoneVerticesGps3> getZone_vertices() {
        return zone_vertices;
    }

    public void setZone_vertices(List<GeoZoneVerticesGps3> zone_vertices) {
        this.zone_vertices = zone_vertices;
    }

    public boolean isChkselect() {
        return chkselect;
    }

    public boolean isSelected() {
        return chkselect;
    }

    public void setChkselect(boolean chkselect) {
        this.chkselect = chkselect;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(zone_id);
        parcel.writeString(user_id);
        parcel.writeString(group_id);
        parcel.writeString(zone_name);
        parcel.writeString(zone_color);
        parcel.writeString(zone_visible);
        parcel.writeString(zone_name_visible);
        parcel.writeString(zone_area);
        parcel.writeByte((byte) (chkselect ? 1 : 0));
    }
}
