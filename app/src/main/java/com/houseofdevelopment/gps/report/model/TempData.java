package com.houseofdevelopment.gps.report.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class TempData implements Serializable {
    public String report_id;
    public String user_id;
    public String name;
    public String type;
    public String ignore_empty_reports;
    public String format;
    public String show_coordinates;
    public String show_addresses;
    public String markers_addresses;
    public String zones_addresses;
    public String stop_duration;
    public String speed_limit;
    public String imei;
    public String marker_ids;
    public String zone_ids;
    public String sensor_names;
    public String data_items;
    public String other;
    public String schedule_period;
    public String schedule_email_address;
    public String dt_schedule_d;
    public String dt_schedule_w;
    public String category;

    public String getReport_id() {
        return report_id;
    }

    public void setReport_id(String report_id) {
        this.report_id = report_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getCategory() {
        return category;
    }
    @NotNull
    @Override
    public String toString() {
        return name;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIgnore_empty_reports() {
        return ignore_empty_reports;
    }

    public void setIgnore_empty_reports(String ignore_empty_reports) {
        this.ignore_empty_reports = ignore_empty_reports;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getShow_coordinates() {
        return show_coordinates;
    }

    public void setShow_coordinates(String show_coordinates) {
        this.show_coordinates = show_coordinates;
    }

    public String getShow_addresses() {
        return show_addresses;
    }

    public void setShow_addresses(String show_addresses) {
        this.show_addresses = show_addresses;
    }

    public String getMarkers_addresses() {
        return markers_addresses;
    }

    public void setMarkers_addresses(String markers_addresses) {
        this.markers_addresses = markers_addresses;
    }

    public String getZones_addresses() {
        return zones_addresses;
    }

    public void setZones_addresses(String zones_addresses) {
        this.zones_addresses = zones_addresses;
    }

    public String getStop_duration() {
        return stop_duration;
    }

    public void setStop_duration(String stop_duration) {
        this.stop_duration = stop_duration;
    }

    public String getSpeed_limit() {
        return speed_limit;
    }

    public void setSpeed_limit(String speed_limit) {
        this.speed_limit = speed_limit;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getMarker_ids() {
        return marker_ids;
    }

    public void setMarker_ids(String marker_ids) {
        this.marker_ids = marker_ids;
    }

    public String getZone_ids() {
        return zone_ids;
    }

    public void setZone_ids(String zone_ids) {
        this.zone_ids = zone_ids;
    }

    public String getSensor_names() {
        return sensor_names;
    }

    public void setSensor_names(String sensor_names) {
        this.sensor_names = sensor_names;
    }

    public String getData_items() {
        return data_items;
    }

    public void setData_items(String data_items) {
        this.data_items = data_items;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getSchedule_period() {
        return schedule_period;
    }

    public void setSchedule_period(String schedule_period) {
        this.schedule_period = schedule_period;
    }

    public String getSchedule_email_address() {
        return schedule_email_address;
    }

    public void setSchedule_email_address(String schedule_email_address) {
        this.schedule_email_address = schedule_email_address;
    }

    public String getDt_schedule_d() {
        return dt_schedule_d;
    }

    public void setDt_schedule_d(String dt_schedule_d) {
        this.dt_schedule_d = dt_schedule_d;
    }

    public String getDt_schedule_w() {
        return dt_schedule_w;
    }

    public void setDt_schedule_w(String dt_schedule_w) {
        this.dt_schedule_w = dt_schedule_w;
    }

}
