package com.trackmap.gps.vehicallist.model;

import java.io.Serializable;
import java.util.List;

public class GroupImeisModelGps3 implements Serializable {
    public boolean status;
    public String name;
    public String group_id;
    public List<String> data;

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
