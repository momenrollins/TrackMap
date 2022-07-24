package com.houseofdevelopment.gps.track.model;

import java.util.ArrayList;
import java.util.List;

public class MsgRootGps3 {
    public List<MessageRoot> message_root = new ArrayList<>();

    public boolean status;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<MessageRoot> getMessage_root() {
        return message_root;
    }

    public MsgRootGps3(List<MessageRoot> message_root) {
        this.message_root = message_root;
    }

    public MsgRootGps3() {
    }

    public void setMessage_root(List<MessageRoot> message_root) {
        this.message_root = message_root;
    }


}
