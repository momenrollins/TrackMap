package com.houseofdevelopment.gps.geozone.model;

import java.util.List;

public class GeoZonesModelRootGps3 {
    public Boolean status;
    public List<GeoZoneModelItemGps3> data;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public List<GeoZoneModelItemGps3> getData() {
        return data;
    }

    public void setData(List<GeoZoneModelItemGps3> data) {
        this.data = data;
    }
}
