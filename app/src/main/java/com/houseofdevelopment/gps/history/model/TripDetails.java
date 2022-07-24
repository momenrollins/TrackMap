package com.houseofdevelopment.gps.history.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class TripDetails implements Serializable {

    @SerializedName("trips")
    @Expose
    public Trips trips;

    public Trips getTrips() {
        return trips;
    }

    public void setTrips(Trips trips) {
        this.trips = trips;
    }

    public class Trips {
        @SerializedName("0")
        @Expose
        public List<FirstObj> firstObj = null;

        public List<FirstObj> getFirstObj() {
            return firstObj;
        }

        public void setFirstObj(List<FirstObj> firstObj) {
            this.firstObj = firstObj;
        }
    }

    public class FirstObj implements Serializable{
        @SerializedName("from")
        @Expose
        private From fromList;
        @SerializedName("to")
        @Expose
        private To toList;
        @SerializedName("m")
        @Expose
        private Integer history_m;
        @SerializedName("f")
        @Expose
        private Integer history_f;
        @SerializedName("state")
        @Expose
        private Integer history_state;
        @SerializedName("max_speed")
        @Expose
        private Integer history_max_speed;
        @SerializedName("curr_speed")
        @Expose
        private Integer history_curr_speed;
        @SerializedName("avg_speed")
        @Expose
        private Integer history_avg_speed;
        @SerializedName("distance")
        @Expose
        private Double history_distance;
        @SerializedName("odometer")
        @Expose
        private Integer history_odometer;
        @SerializedName("course")
        @Expose
        private Integer history_course;
        @SerializedName("altitude")
        @Expose
        private Integer history_altitude;
        @SerializedName("track")
        @Expose
        private String history_track;

        private String tripEventType = " ";


        public Boolean isSelected = false;

        public From getFromList() {
            return fromList;
        }

        public void setFromList(From fromList) {
            this.fromList = fromList;
        }

        public To getToList() {
            return toList;
        }

        public void setToList(To toList) {
            this.toList = toList;
        }

        public Integer getHistory_m() {
            return history_m;
        }

        public void setHistory_m(Integer history_m) {
            this.history_m = history_m;
        }

        public Integer getHistory_f() {
            return history_f;
        }

        public void setHistory_f(Integer history_f) {
            this.history_f = history_f;
        }

        public Integer getHistory_state() {
            return history_state;
        }

        public void setHistory_state(Integer history_state) {
            this.history_state = history_state;
        }

        public Integer getHistory_max_speed() {
            return history_max_speed;
        }

        public void setHistory_max_speed(Integer history_max_speed) {
            this.history_max_speed = history_max_speed;
        }

        public Integer getHistory_curr_speed() {
            return history_curr_speed;
        }

        public void setHistory_curr_speed(Integer history_curr_speed) {
            this.history_curr_speed = history_curr_speed;
        }

        public Integer getHistory_avg_speed() {
            return history_avg_speed;
        }

        public void setHistory_avg_speed(Integer history_avg_speed) {
            this.history_avg_speed = history_avg_speed;
        }

        public Double getHistory_distance() {
            return history_distance;
        }

        public void setHistory_distance(Double history_distance) {
            this.history_distance = history_distance;
        }

        public Integer getHistory_odometer() {
            return history_odometer;
        }

        public void setHistory_odometer(Integer history_odometer) {
            this.history_odometer = history_odometer;
        }

        public Integer getHistory_course() {
            return history_course;
        }

        public void setHistory_course(Integer history_course) {
            this.history_course = history_course;
        }

        public Integer getHistory_altitude() {
            return history_altitude;
        }

        public void setHistory_altitude(Integer history_altitude) {
            this.history_altitude = history_altitude;
        }

        public String getHistory_track() {
            return history_track;
        }

        public void setHistory_track(String history_track) {
            this.history_track = history_track;
        }

        public String getTripEventType() {
            return tripEventType;
        }

        public void setTripEventType(String tripEventType) {
            this.tripEventType = tripEventType;
        }


    }

    public class From implements Serializable {

        @SerializedName("t")
        @Expose
        private Double from_t;
        @SerializedName("x")
        @Expose
        private Double from_x;
        @SerializedName("y")
        @Expose
        private Double from_y;

        public Double getFrom_t() {
            return from_t;
        }

        public void setFrom_t(Double from_t) {
            this.from_t = from_t;
        }

        public Double getFrom_x() {
            return from_x;
        }

        public void setFrom_x(Double from_x) {
            this.from_x = from_x;
        }

        public Double getFrom_y() {
            return from_y;
        }

        public void setFrom_y(Double from_y) {
            this.from_y = from_y;
        }
    }

    public class To implements Serializable {
        @SerializedName("t")
        @Expose
        private Double to_t;
        @SerializedName("x")
        @Expose
        private Double to_x;
        @SerializedName("y")
        @Expose
        private Double to_y;

        public Double getTo_t() {
            return to_t;
        }

        public void setTo_t(Double to_t) {
            this.to_t = to_t;
        }

        public Double getTo_x() {
            return to_x;
        }

        public void setTo_x(Double to_x) {
            this.to_x = to_x;
        }

        public Double getTo_y() {
            return to_y;
        }

        public void setTo_y(Double to_y) {
            this.to_y = to_y;
        }
    }
}
