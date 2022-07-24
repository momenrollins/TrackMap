package com.houseofdevelopment.gps.track.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PositionModel {


    @SerializedName("t")
    Long  t;

    @Expose
    @SerializedName("pos")
    PostionPointModel pos;


    public Long getT() {
        return t;
    }

    public PostionPointModel getPos() {
        return pos;
    }
}
