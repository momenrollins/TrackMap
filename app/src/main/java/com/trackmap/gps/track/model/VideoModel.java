package com.trackmap.gps.track.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VideoModel {
    @SerializedName("count")
     int count;
    
    @SerializedName("messages")
     List<PositionModel> messages;

    public int getCount() {
        return count;
    }

    public List<PositionModel> getMessages() {
        return messages;
    }
}
