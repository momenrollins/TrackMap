package com.houseofdevelopment.gps.track.model;

import com.google.gson.annotations.SerializedName;

public class PostionPointModel {

    @SerializedName("x")
    double x;
    @SerializedName("y")
    double y;

    @SerializedName("s")
    int s;
    @SerializedName("c")
    int c;


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getS() {
        return s;
    }

    public int getC() {
        return c;
    }
}
