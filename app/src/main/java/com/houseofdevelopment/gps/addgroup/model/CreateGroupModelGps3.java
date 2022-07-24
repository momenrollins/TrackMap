package com.houseofdevelopment.gps.addgroup.model;

public class CreateGroupModelGps3 {
    public String status;
    public ItemCreateGroupModel data;

    public CreateGroupModelGps3(String status, ItemCreateGroupModel data) {
        this.status = status;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ItemCreateGroupModel getData() {
        return data;
    }

    public void setData(ItemCreateGroupModel data) {
        this.data = data;
    }
}

class ItemCreateGroupModel{
    String group_id;
    int inserted_imeis;

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public int getInserted_imeis() {
        return inserted_imeis;
    }

    public void setInserted_imeis(int inserted_imeis) {
        this.inserted_imeis = inserted_imeis;
    }
}
