package com.houseofdevelopment.gps.homemap.model;

import com.houseofdevelopment.gps.vehicallist.model.ItemGps3;

import java.util.ArrayList;

public class MapListDataModelGps3 {
    ArrayList<ItemGps3> items  = new ArrayList<>();
    public MapListDataModelGps3() {
    }

    public MapListDataModelGps3(ArrayList<ItemGps3> items) {
        this.items = items;
    }

    public ArrayList<ItemGps3> getItems() {
        return items;
    }

    public void setItems(ArrayList<ItemGps3> items) {
        this.items = items;
    }
}
